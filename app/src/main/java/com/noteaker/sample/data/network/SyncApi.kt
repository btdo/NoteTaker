package com.noteaker.sample.data.network

import com.noteaker.sample.data.model.NoteEntity
import com.noteaker.sample.domain.model.NoteStatus
import kotlinx.coroutines.delay

interface SyncApi {
    suspend fun getNotesSince(timestamp: Long): List<NoteEntity>
    suspend fun pushNotes(notes: List<NoteEntity>): List<NoteEntity>
}

class FakeSyncApi : SyncApi {
    override suspend fun getNotesSince(timestamp: Long): List<NoteEntity> {
        return listOf(
            NoteEntity(
                id = 1,
                title = "Test",
                note = "Test Note",
                status = NoteStatus.ACTIVE.name,
                lastUpdated = 1704124800000L,  // January 1, 2024, 12:00 PM
                version = 1,
                syncStatus = "SYNCED",
                serverId = 134324
            ),
            NoteEntity(
                id = 2,
                title = "Test2",
                note = "Test Note2 Sync",
                status = NoteStatus.ACTIVE.name,
                lastUpdated = 1709308800000L,  // March 1, 2024, 12:00 PM
                version = 1,
                syncStatus = "SYNCED",
                serverId = 12323
            ),
            NoteEntity(
                id = 4,
                title = "Test2",
                note = "Test Note2 Sync",
                status = NoteStatus.ARCHIVED.name,
                lastUpdated = 1711900800000L,  // March 31, 2024, 12:00 PM
                version = 1,
                syncStatus = "SYNCED",
                serverId = 12323
            )
        )
    }

    override suspend fun pushNotes(notes: List<NoteEntity>): List<NoteEntity> {
        delay(3000)
        return notes
    }
}