package com.programmersbox.common.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history_item WHERE searchQuery LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 5")
    fun getSearchHistory(query: String): Flow<List<SearchHistoryItem>>

    @Query("SELECT * FROM search_history_item ORDER BY timestamp DESC")
    fun getAllSearchHistoryFlow(): Flow<List<SearchHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSearchHistory(query: SearchHistoryItem)

    @Delete
    suspend fun removeSearchHistory(item: SearchHistoryItem)

    @Query("SELECT COUNT(searchQuery) FROM search_history_item")
    fun getSearchCount(): Flow<Int>

    @Query("SELECT * FROM search_history_item ORDER BY timestamp DESC")
    suspend fun getAllSearchHistory(): List<SearchHistoryItem>
}

@Serializable
@Entity(tableName = "search_history_item")
data class SearchHistoryItem(
    @PrimaryKey
    val searchQuery: String,
    val timestamp: Long = System.currentTimeMillis(),
)