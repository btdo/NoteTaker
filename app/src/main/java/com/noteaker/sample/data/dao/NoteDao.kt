package com.noteaker.sample.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.noteaker.sample.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY lastUpdated DESC")
    fun getAllFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes where status = :status ORDER BY lastUpdated DESC")
    fun getByStatusFlow(status: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes where syncStatus = :syncStatus")
    fun getBySyncStatus(syncStatus: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE serverId = :id")
    suspend fun getByServerId(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoteEntity): Long

    @Update
    suspend fun update(entity: NoteEntity)

    @Delete
    suspend fun delete(entity: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteByNoteId(noteId: Long)
}