package com.programmersbox.common.db

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Stable
@Immutable
@Serializable
@Entity(tableName = "Notes")
data class Notes(
    @PrimaryKey
    val uuid: String,
    val modelUrl: String,
    val modelId: String,
    val note: String,
)

@Dao
interface NotesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Notes)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(note: List<Notes>)

    @Delete
    suspend fun delete(note: Notes)

    @Query("SELECT * FROM Notes WHERE modelUrl = :modelUrl")
    fun getNotes(modelUrl: String): Flow<List<Notes>>

    @Query("SELECT * FROM Notes")
    suspend fun getAllNotes(): List<Notes>

    @Query("SELECT COUNT(uuid) FROM Notes")
    fun getNotesCount(): Flow<Int>
}