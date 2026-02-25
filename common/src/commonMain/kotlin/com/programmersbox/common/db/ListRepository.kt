package com.programmersbox.common.db

import com.programmersbox.common.presentation.components.ToastType
import com.programmersbox.common.presentation.components.ToasterState
import com.programmersbox.resources.Res
import com.programmersbox.resources.added_to
import com.programmersbox.resources.list_created
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ListRepository(
    private val listDao: ListDao,
    private val toasterState: ToasterState,
) {
    fun getAllLists(): Flow<List<CustomList>> = listDao.getAllLists()

    fun getAllLists(search: String): Flow<List<CustomList>> = listDao.getAllLists(search)

    fun getCustomListItemFlow(uuid: String): Flow<CustomList> = listDao.getCustomListItemFlow(uuid)

    suspend fun getCustomListItem(uuid: String): CustomList = listDao.getCustomListItem(uuid)

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    suspend fun createList(
        name: String,
        description: String? = null,
        coverImage: String? = null,
        showToast: Boolean = true,
    ): String {
        val uuid = Uuid.random().toString()
        val listItem = CustomListItem(
            uuid = uuid,
            name = name,
            description = description,
            coverImage = coverImage,
        )
        listDao.createList(listItem)
        if (showToast) {
            toasterState.show(
                getString(Res.string.list_created),
                type = ToastType.Success
            )
        }
        return uuid
    }

    @OptIn(ExperimentalTime::class)
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
        val item = listDao.getCustomListItem(uuid)
        if (item.list.any { it.id == id }) return false
        listDao.addItem(
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
        listDao.updateList(item.item.copy(time = Clock.System.now().toEpochMilliseconds()))
        return true
    }

    suspend fun addToMultipleLists(
        selectedLists: List<CustomList>,
        id: Long,
        name: String,
        description: String? = null,
        type: String = "Other",
        nsfw: Boolean = false,
        imageUrl: String? = null,
        favoriteType: FavoriteType = FavoriteType.Model,
        hash: String? = null,
        creatorName: String? = null,
        creatorImage: String? = null,
    ) {
        selectedLists.forEach { item ->
            addToList(
                uuid = item.item.uuid,
                id = id,
                name = name,
                description = description,
                type = type,
                nsfw = nsfw,
                imageUrl = imageUrl,
                favoriteType = favoriteType,
                hash = hash,
                creatorName = creatorName,
                creatorImage = creatorImage,
            )
        }
        val message = if (selectedLists.size == 1) {
            getString(Res.string.added_to, selectedLists.first().item.name)
        } else {
            "Added to lists"
        }
        toasterState.show(message, type = ToastType.Success)
    }

    suspend fun removeItem(listItem: CustomListInfo) = listDao.removeItem(listItem)

    suspend fun removeList(item: CustomList) {
        item.list.forEach { listDao.removeItem(it) }
        listDao.removeList(item.item)
    }

    suspend fun updateList(listItem: CustomListItem) = listDao.updateList(listItem)

    suspend fun updateFullList(item: CustomListItem) {
        listDao.updateList(item.copy(time = Clock.System.now().toEpochMilliseconds()))
    }

    suspend fun updateCoverImage(uuid: String, coverImage: String?, hash: String? = null) {
        listDao.updateCoverImage(uuid, coverImage, hash)
    }

    suspend fun updateBiometric(uuid: String, useBiometric: Boolean) {
        listDao.updateBiometric(uuid, useBiometric)
    }

    fun search(query: String): Flow<List<CustomList>> = listDao.search(query)

    fun searchListsWithFts(userInput: String): Flow<List<CustomList>> =
        listDao.searchListsWithFts(userInput)

    fun getAllListsCount(): Flow<Int> = listDao.getAllListsCount()

    fun getTypeCounts(): Flow<DataCounts> = listDao.getTypeCounts()
}