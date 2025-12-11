@file:OptIn(ExperimentalTime::class)

package com.programmersbox.common.db

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "favorite_table")
data class FavoriteRoom(
    @PrimaryKey
    val id: Long,
    val name: String,
    val description: String? = null,
    val type: String = "Other",
    val nsfw: Boolean = false,
    val imageUrl: String? = null,
    val favoriteType: FavoriteType = FavoriteType.Model,
    val imageMeta: String? = null,
    val hash: String? = null,
    val modelId: Long = id,
    val dateAdded: Long = Clock.System.now().toEpochMilliseconds(),
)

@Serializable
@Entity(tableName = "blacklisted_table")
data class BlacklistedItemRoom(
    @PrimaryKey
    val id: Long,
    val name: String = "",
    val nsfw: Boolean = false,
    val imageUrl: String? = null,
)

@Serializable
data class ImageMetaDb(
    val size: String? = null,
    val seed: Long? = null,
    val model: String? = null,
    val steps: Long? = null,
    val prompt: String? = null,
    val sampler: String? = null,
    val cfgScale: Double? = null,
    val clipSkip: String? = null,
    val negativePrompt: String? = null,
)

enum class FavoriteType {
    Model,
    Image,
    Creator
}

@Serializable
sealed interface FavoriteModel {
    val id: Long
    val name: String
    val imageUrl: String?
    val modelType: String?
    val dateAdded: Long

    @Stable
    @Serializable
    data class Model(
        override val id: Long,
        override val name: String,
        override val imageUrl: String?,
        val description: String?,
        @SerialName("favorite_type")
        val type: String,
        val nsfw: Boolean,
        val hash: String?,
        override val modelType: String = "Model",
        override val dateAdded: Long = Clock.System.now().toEpochMilliseconds(),
    ) : FavoriteModel

    @Stable
    @Serializable
    data class Image(
        override val id: Long,
        override val name: String,
        override val imageUrl: String?,
        val imageMetaDb: ImageMetaDb?,
        val nsfw: Boolean,
        val modelId: Long,
        val hash: String?,
        override val modelType: String = "Image",
        override val dateAdded: Long = Clock.System.now().toEpochMilliseconds(),
    ) : FavoriteModel

    @Stable
    @Serializable
    data class Creator(
        override val id: Long,
        override val name: String,
        override val imageUrl: String?,
        override val modelType: String = "Creator",
        override val dateAdded: Long = Clock.System.now().toEpochMilliseconds(),
    ) : FavoriteModel
}

@Serializable
data class CustomList(
    @Embedded
    val item: CustomListItem,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "uuid"
    )
    val list: List<CustomListInfo>,
)

@Serializable
@Entity(tableName = "CustomListItem")
data class CustomListItem(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "time")
    val time: Long = Clock.System.now().toEpochMilliseconds(),
    val coverImage: String?,
    val hash: String? = null,
)

@Serializable
@Entity(tableName = "CustomListInfo")
data class CustomListInfo @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey
    @ColumnInfo(defaultValue = "0c65586e-f3dc-4878-be63-b134fb46466c")
    val uniqueId: String = Uuid.random().toString(),
    @ColumnInfo("uuid")
    val uuid: String,
    val id: Long,
    val name: String,
    val description: String? = null,
    val type: String = "Other",
    val nsfw: Boolean = false,
    val imageUrl: String? = null,
    val favoriteType: FavoriteType = FavoriteType.Model,
    val imageMeta: String? = null,
    val hash: String? = null,
    val modelId: Long = id,
    val dateAdded: Long = Clock.System.now().toEpochMilliseconds(),
)

fun CustomListItem.toImageHash() = if (coverImage != null)
    ImageHash(coverImage, hash)
else
    null

fun CustomListInfo.toImageHash() = if (imageUrl != null)
    ImageHash(imageUrl, hash)
else
    null

fun CustomList.toImageHash(): ImageHash? {
    return if (item.coverImage != null && item.coverImage.endsWith("mp4")) {
        list.firstOrNull()?.toImageHash()
    } else if (item.coverImage != null && item.hash != null) {
        item.toImageHash()
    } else {
        list.firstOrNull()?.toImageHash()
    }
}

data class ImageHash(
    val url: String? = null,
    val hash: String? = null,
)