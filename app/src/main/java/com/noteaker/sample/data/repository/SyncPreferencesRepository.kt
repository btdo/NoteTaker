package com.noteaker.sample.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create DataStore instance
// This is a modern replacement for SharedPreferences
private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_preferences")

/**
 * Repository interface for sync-related preferences.
 *
 * Stores metadata like:
 * - lastSyncedAt: When was the last successful sync (for delta sync)
 * - deviceId: Unique identifier for this device
 */
interface SyncPreferencesRepository {
    /**
     * Get the last sync timestamp in milliseconds.
     * Used for delta sync: "Give me notes changed since this time"
     *
     * @return Last sync timestamp, or null if never synced
     */
    suspend fun getLastSyncTime(): Long?

    /**
     * Update the last sync timestamp.
     * Call this after successful sync.
     *
     * @param timestamp Current time in milliseconds (System.currentTimeMillis())
     */
    suspend fun setLastSyncTime(timestamp: Long)

    /**
     * Observe last sync time as Flow (for UI).
     * Useful for showing "Last synced: 5 minutes ago" in UI.
     */
    fun observeLastSyncTime(): Flow<Long?>

    /**
     * Clear all sync preferences (for logout).
     */
    suspend fun clearAll()
}

/**
 * DataStore implementation of SyncPreferencesRepository.
 *
 * DataStore is the modern replacement for SharedPreferences:
 * - Type-safe
 * - Asynchronous (suspend functions)
 * - Transactional (atomic updates)
 * - Kotlin-first with Flow support
 */
@Singleton
class DataStoreSyncPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncPreferencesRepository {

    private val dataStore = context.syncDataStore

    companion object {
        // Keys for DataStore preferences
        // Using longPreferencesKey for type safety
        private val LAST_SYNC_TIME_KEY = longPreferencesKey("last_sync_timestamp")
    }

    override suspend fun getLastSyncTime(): Long? {
        // Read from DataStore
        // first() gets the current value (not a Flow)
        return dataStore.data.first()[LAST_SYNC_TIME_KEY]
    }

    override suspend fun setLastSyncTime(timestamp: Long) {
        // Write to DataStore
        // edit {} provides transactional updates
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME_KEY] = timestamp
        }
    }

    override fun observeLastSyncTime(): Flow<Long?> {
        // Return a Flow that emits whenever the value changes
        // Useful for UI updates
        return dataStore.data.map { preferences ->
            preferences[LAST_SYNC_TIME_KEY]
        }
    }

    override suspend fun clearAll() {
        // Clear all preferences (for logout)
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}