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
import kotlin.time.Clock

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

@Stable
@Immutable
@Serializable
@Entity(tableName = "search_history_item")
data class SearchHistoryItem(
    @PrimaryKey
    val searchQuery: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)