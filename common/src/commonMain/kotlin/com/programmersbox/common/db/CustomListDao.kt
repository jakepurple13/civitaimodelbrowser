package com.programmersbox.common.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Dao
interface ListDao {
    @Transaction
    @Query("SELECT * FROM CustomListItem ORDER BY useBiometric ASC, time DESC")
    fun getAllLists(): Flow<List<CustomList>>

    @Transaction
    @Query("SELECT * FROM CustomListItem WHERE name LIKE '%' || :search || '%' ORDER BY useBiometric ASC, time DESC")
    fun getAllLists(search: String): Flow<List<CustomList>>

    @Transaction
    @Query("SELECT * FROM CustomListItem WHERE uuid in (:uuid)")
    suspend fun getAllListItems(vararg uuid: String): List<CustomList>

    @Query("SELECT COUNT(uuid) FROM CustomListItem")
    fun getAllListsCount(): Flow<Int>

    @Query(
        """
        SELECT 
            SUM(CASE WHEN favoriteType = 'Model' THEN 1 ELSE 0 END) as modelCount,
            SUM(CASE WHEN favoriteType = 'Image' THEN 1 ELSE 0 END) as imageCount,
            SUM(CASE WHEN favoriteType = 'Creator' THEN 1 ELSE 0 END) as creatorCount
        FROM CustomListInfo
    """
    )
    fun getTypeCounts(): Flow<DataCounts>

    @Transaction
    @Query(
        """
        SELECT * FROM CustomListItem
        WHERE name LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        OR uuid IN (SELECT uuid FROM CustomListInfo WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY useBiometric ASC, time DESC
    """
    )
    fun search(query: String): Flow<List<CustomList>>

    @Query("SELECT COUNT(uuid) FROM CustomListInfo")
    fun getAllListItemsCount(): Flow<Int>

    @Transaction
    @Query("SELECT * FROM CustomListItem ORDER BY time DESC")
    suspend fun getAllListsSync(): List<CustomList>

    @Transaction
    @Query("SELECT * FROM CustomListItem WHERE :uuid = uuid")
    suspend fun getCustomListItem(uuid: String): CustomList

    @Transaction
    @Query("SELECT * FROM CustomListItem WHERE :uuid = uuid")
    fun getCustomListItemFlow(uuid: String): Flow<CustomList>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createList(listItem: CustomListItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addItem(listItem: CustomListInfo)

    @Delete
    suspend fun removeItem(listItem: CustomListInfo)

    @Query("DELETE FROM CustomListInfo WHERE :uuid = uuid")
    suspend fun removeItem(uuid: String)

    @Update
    suspend fun updateList(listItem: CustomListItem)

    @Delete
    suspend fun removeList(item: CustomListItem)

    @OptIn(ExperimentalUuidApi::class)
    @Ignore
    suspend fun create(name: String, coverImage: String? = null) {
        createList(
            CustomListItem(
                uuid = Uuid.random().toString(),
                name = name,
                coverImage = coverImage
            )
        )
    }

    @Ignore
    suspend fun removeList(item: CustomList) {
        item.list.forEach { removeItem(it) }
        removeList(item.item)
    }

    @Query("UPDATE CustomListItem SET coverImage = :coverImage, hash = :hash WHERE uuid = :uuid")
    suspend fun updateCoverImage(uuid: String, coverImage: String?, hash: String? = null)

    @Query("UPDATE CustomListItem SET useBiometric = :useBiometric WHERE uuid = :uuid")
    suspend fun updateBiometric(uuid: String, useBiometric: Boolean)

    @OptIn(ExperimentalTime::class)
    @Ignore
    suspend fun updateFullList(item: CustomListItem) {
        updateList(item.copy(time = Clock.System.now().toEpochMilliseconds()))
    }

    @OptIn(ExperimentalTime::class)
    @Ignore
    suspend fun addToList(
        uuid: String,
        id: Long,
        name: String,
        description: String? = null,
        type: String = "Other",
        nsfw: Boolean = false,
        imageUrl: String? = null,
        favoriteType: FavoriteType = FavoriteType.Model,
        imageMeta: String? = null,
        hash: String? = null,
        creatorName: String? = null,
        creatorImage: String? = null,
        modelId: Long = id,
        dateAdded: Long = Clock.System.now().toEpochMilliseconds(),
    ): Boolean {
        val item = getCustomListItem(uuid)
        if (item.list.any { it.id == id }) return false
        addItem(
            CustomListInfo(
                uuid = uuid,
                id = id,
                name = name,
                description = description,
                type = type,
                nsfw = nsfw,
                imageUrl = imageUrl,
                favoriteType = favoriteType,
                imageMeta = imageMeta,
                creatorName = creatorName,
                creatorImage = creatorImage,
                hash = hash,
                modelId = modelId,
                dateAdded = dateAdded,
            )
        )
        updateFullList(item.item)
        return true
    }

    @Transaction
    @Query(
        """
    SELECT * FROM CustomListItem
    WHERE uuid IN (
        SELECT uuid FROM CustomListItemFts 
        WHERE CustomListItemFts MATCH :ftsQuery
    )
    OR uuid IN (
        SELECT parentUuid FROM CustomListInfoFts 
        WHERE CustomListInfoFts MATCH :ftsQuery
    )
    ORDER BY useBiometric ASC, time DESC
    """
    )
    fun searchCustomListsQuickly(ftsQuery: String): Flow<List<CustomList>>

    @Ignore
    fun searchListsWithFts(userInput: String): Flow<List<CustomList>> {
        // If input is "vacation", this becomes "*vacation*"
        // This tells FTS to look for the word anywhere in the string.
        val ftsQuery = "*$userInput*"

        return searchCustomListsQuickly(ftsQuery)
    }
}