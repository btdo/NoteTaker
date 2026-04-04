package com.noteaker.sample.data.repository

import com.noteaker.sample.data.dao.NoteDao
import com.noteaker.sample.data.model.NoteEntity
import com.noteaker.sample.data.model.SyncResult
import com.noteaker.sample.data.network.SyncApi
import com.noteaker.sample.di.DefaultDispatcher
import com.noteaker.sample.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sync Strategy: Version-based with Pending Changes Queue
 */
@Singleton
class NoteSyncRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val syncPreferences: SyncPreferencesRepository,  // ✅ DataStore for lastSyncedAt
    private val syncApi: SyncApi,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    /**
     * DELTA SYNC: Efficient Sync Strategy
     * ====================================
     *
     * Your idea is EXACTLY RIGHT! Store lastSyncedAt and use it to fetch only changed notes.
     *
     * Flow:
     * 1. Get lastSyncedAt from DataStore/Room (e.g., March 1, 10:00 AM)
     * 2. Call API: syncApi.getNotesSince(lastSyncedAt)
     * 3. Server returns: SELECT * FROM notes WHERE lastUpdated > lastSyncedAt
     * 4. Merge those notes locally
     * 5. Update lastSyncedAt to System.currentTimeMillis()
     *
     * Benefits:
     * - If you have 10,000 notes but only 3 changed → sync only 3 (not 10,000!)
     * - Faster sync (less network data)
     * - Less battery usage
     * - Scalable (works with millions of notes)
     *
     * This is how Evernote, Notion, Dropbox, Google Drive all work!
     */
    suspend fun syncNotes(): Result<SyncResult> = withContext(dispatcher) {
        return@withContext runCatching {
            var conflictsCount = 0
            // STEP 1: Get last sync timestamp from storage
            val lastSyncedAt = syncPreferences.getLastSyncTime() ?: 0L
            // STEP 2: Pull remote changes SINCE last sync (YOUR IDEA!)
            val remoteChanges = syncApi.getNotesSince(lastSyncedAt)
            val pushNotes = mutableListOf<NoteEntity>()
            // STEP 3: Detect conflicts and merge
            val errorNotes = mutableListOf<NoteEntity>()
            remoteChanges.forEach { remote ->
                val local = noteDao.getByServerId(remote.serverId!!)
                if (local == null) {
                    // New note from another device → just insert
                    noteDao.insert(remote)
                } else {
                    // Check for conflict using version numbers
                    val action = detectSyncAction(
                        localVersion = local.version,
                        localSyncStatus = local.syncStatus,
                        remoteVersion = remote.version
                    )

                    when (action) {
                        SyncAction.ACCEPT_REMOTE -> {
                            noteDao.update(remote.copy(id = local.id))
                        }

                        SyncAction.PUSH_LOCAL -> {
                            pushNotes.add(local)
                        }

                        SyncAction.CONFLICT -> {
                            conflictsCount++
                            // Mark as conflict, show dialog to user
                            noteDao.update(local.copy(syncStatus = "CONFLICT"))
                        }

                        SyncAction.ALREADY_SYNCED -> {
                            // Do nothing, already up to date
                        }

                        SyncAction.UNKNOWN -> {
                            errorNotes.add(local)
                        }
                    }
                }
            }

            // STEP 5: Push local pending changes to server
            val pendingNotes = noteDao.getBySyncStatus("PENDING")
            pushNotes.addAll(pendingNotes)
            val pushResponse = syncApi.pushNotes(notes = pushNotes)
            pushResponse.forEach {
                noteDao.update(it.copy(syncStatus = "SYNCED"))
            }

            // STEP 6: Update last sync timestamp to NOW
            val now = System.currentTimeMillis()
            syncPreferences.setLastSyncTime(now)

            // Return result
            SyncResult(
                success = true,
                pushedCount = pushNotes.size,  // TODO: Track actual count
                pulledCount = remoteChanges.size,
                conflictsCount = conflictsCount,  // TODO: Track conflicts
                errors = errorNotes.map { it.serverId.toString() }
            )
        }
    }

}

// ============================================================================
// HELPER: Sync Action Detection
// ============================================================================

/**
 * Determines what action to take when syncing a note.
 *
 * NOTE: The term "CONFLICT" here means "USER MUST DECIDE"
 *       The term "ACCEPT_REMOTE" means "NO USER DECISION NEEDED, SAFE TO MERGE"
 */
enum class SyncAction {
    /**
     * Server has changes, local hasn't been edited.
     * Safe to accept server's version without asking user.
     *
     * Example: Local v5 (SYNCED), Remote v6 → Another device edited, take v6
     */
    ACCEPT_REMOTE,

    /**
     * Local has changes, server hasn't changed.
     * Safe to push local version to server.
     *
     * Example: Local v6 (PENDING), Remote v5 → Just upload local changes
     */
    PUSH_LOCAL,

    /**
     * TRUE CONFLICT: Both local and remote have changes.
     * User must decide which version to keep.
     *
     * Example: Local v6 "Buy eggs" (PENDING), Remote v6 "Buy milk"
     *          → Both edited offline, user picks winner
     */
    CONFLICT,

    /**
     * Already up to date, nothing to do.
     *
     * Example: Local v6 (SYNCED), Remote v6 → No changes
     */
    ALREADY_SYNCED,

    /**
     * Edge case that shouldn't happen.
     * Log for investigation.
     */
    UNKNOWN
}

/**
 * Detects what sync action to take based on version numbers and sync status.
 *
 * @param localVersion Current local note version
 * @param localSyncStatus "SYNCED", "PENDING", or "CONFLICT"
 * @param remoteVersion Server's note version
 * @return Action to take (ACCEPT_REMOTE, PUSH_LOCAL, CONFLICT, etc.)
 */
fun detectSyncAction(
    localVersion: Long,
    localSyncStatus: String,
    remoteVersion: Long
): SyncAction {

    // CASE 1: Server is one or multiple versions ahead, local not edited
    // Example: I was offline for a while, server went v5 → v6 → v7 → v8
    if (remoteVersion >= localVersion + 1 && localSyncStatus == "SYNCED") {
        return SyncAction.ACCEPT_REMOTE
    }

    // CASE 3: TRUE CONFLICT - Both local and remote have changes
    // Example:
    //   - Both start at v5
    //   - I edit offline → local v6 (PENDING)
    //   - Another device edits → server v6
    //   - When I try to sync: local v6 (PENDING) vs remote v6
    //   - CONFLICT! User must choose.
    if (remoteVersion >= localVersion && localSyncStatus == "PENDING") {
        return SyncAction.CONFLICT
    }

    // CASE 4: Local has changes, server hasn't changed
    // Example: I edit offline, server still on old version
    if (remoteVersion == localVersion - 1 && localSyncStatus == "PENDING" ) {
        return SyncAction.PUSH_LOCAL
    }

    // CASE 5: Already synced, same version
    if (remoteVersion == localVersion && localSyncStatus == "SYNCED") {
        return SyncAction.ALREADY_SYNCED
    }

    // CASE 6: Edge case - shouldn't happen in normal flow
    // Example: Local v10, Remote v5, Local PENDING
    //          → How did local get to v10 without syncing v6-v9?
    return SyncAction.UNKNOWN
}