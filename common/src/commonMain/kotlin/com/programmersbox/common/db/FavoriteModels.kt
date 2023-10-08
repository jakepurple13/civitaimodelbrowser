package com.programmersbox.common.db

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

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
}

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

sealed class FavoriteModel(
    val id: Long,
    val name: String,
    val imageUrl: String?,
) {
    class Model(
        id: Long,
        name: String,
        imageUrl: String?,
        val description: String?,
        val type: String,
        val nsfw: Boolean,
    ) : FavoriteModel(id, name, imageUrl)

    class Image(
        id: Long,
        name: String,
        imageUrl: String?,
        val imageMetaDb: ImageMetaDb?,
        val nsfw: Boolean,
    ) : FavoriteModel(id, name, imageUrl)

    class Creator(
        id: Long,
        name: String,
        imageUrl: String?,
    ) : FavoriteModel(id, name, imageUrl)
}