package com.programmersbox.common.db

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class FavoriteList : RealmObject {
    var favorites = realmListOf<Favorite>()
}

class Favorite : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var name: String = ""
    var description: String? = null
    var type: String = "Other"
    var nsfw: Boolean = false
    var imageUrl: String? = null
    var favoriteType: String = FavoriteType.Model.name
    var imageMeta: ImageMetaDb? = null
    var modelId: Long = id
}

@Serializable
class ImageMetaDb : RealmObject {
    var size: String? = null
    var seed: Long? = null
    var model: String? = null
    var steps: Long? = null
    var prompt: String? = null
    var sampler: String? = null
    var cfgScale: Double? = null
    var clipSkip: String? = null
    var negativePrompt: String? = null
}

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

    @Serializable
    data class Model(
        override val id: Long,
        override val name: String,
        override val imageUrl: String?,
        val description: String?,
        @SerialName("favorite_type")
        val type: String,
        val nsfw: Boolean,
        override val modelType: String = "Model",
    ) : FavoriteModel

    @Serializable
    data class Image(
        override val id: Long,
        override val name: String,
        override val imageUrl: String?,
        val imageMetaDb: ImageMetaDb?,
        val nsfw: Boolean,
        val modelId: Long,
        override val modelType: String = "Image",
    ) : FavoriteModel

    @Serializable
    data class Creator(
        override val id: Long,
        override val name: String,
        override val imageUrl: String?,
        override val modelType: String = "Creator",
    ) : FavoriteModel
}