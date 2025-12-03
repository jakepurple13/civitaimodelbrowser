@file:OptIn(ExperimentalTime::class)

package com.programmersbox.common.db

import androidx.compose.runtime.Stable
import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Entity(tableName = "favorite_table")
data class FavoriteRoom(
    @androidx.room.PrimaryKey
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
    @androidx.room.PrimaryKey
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
