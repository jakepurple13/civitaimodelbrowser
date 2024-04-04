package com.programmersbox.common.db

import com.programmersbox.common.Creator
import com.programmersbox.common.ImageMeta
import com.programmersbox.common.ModelType
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json

class FavoritesDatabase(
    name: String = Realm.DEFAULT_FILE_NAME,
) {
    private val json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
                    Favorite::class,
                    ImageMetaDb::class,
                    BlacklistedItem::class
                )
            )
                .schemaVersion(8)
                .name(name)
                .migration({ })
                //.deleteRealmIfMigrationNeeded()
                .build()
        )
    }

    fun getBlacklistedItems() = realm.query<BlacklistedItem>()
        .asFlow()
        .mapNotNull { it.list }

    suspend fun blacklistItem(
        id: Long,
        name: String,
        nsfw: Boolean,
        imageUrl: String? = null,
    ) {
        realm.write {
            copyToRealm(
                BlacklistedItem().apply {
                    this.id = id
                    this.name = name
                    this.nsfw = nsfw
                    this.imageUrl = imageUrl
                }
            )
        }
    }

    suspend fun removeBlacklistItem(item: BlacklistedItem) {
        realm.write {
            findLatest(item)?.let { delete(it) }
        }
    }

    fun getFavorites(
        sortedBy: Sort = Sort.DESCENDING,
    ) = realm.query(Favorite::class)
        .sort("dateAdded", sortedBy)
        .asFlow()
        .mapNotNull { it.list }
        .mapToFavoriteModels()

    suspend fun addFavorite(
        id: Long,
        modelId: Long,
        name: String = "",
        description: String? = null,
        type: ModelType = ModelType.Other,
        nsfw: Boolean = false,
        imageUrl: String? = null,
        favoriteType: FavoriteType = FavoriteType.Model,
        imageMetaDb: ImageMetaDb? = null,
    ) {
        realm.write {
            copyToRealm(
                Favorite().apply {
                    this.id = id
                    this.name = name
                    this.description = description
                    this.type = type.name
                    this.nsfw = nsfw
                    this.imageUrl = imageUrl
                    this.favoriteType = favoriteType.name
                    this.imageMeta = imageMetaDb
                    this.modelId = modelId
                }
            )
        }
    }

    suspend fun removeModel(id: Long) {
        realm.write {
            query<Favorite>("id == $0 AND favoriteType == $1", id, FavoriteType.Model.name)
                .find()
                .forEach { delete(it) }
        }
    }

    suspend fun removeImage(imageUrl: String) {
        realm.write {
            query<Favorite>("imageUrl == $0 AND favoriteType == $1", imageUrl, FavoriteType.Image.name)
                .find()
                .forEach { delete(it) }
        }
    }

    suspend fun removeCreator(creator: Creator) {
        realm.write {
            query<Favorite>("name == $0 AND favoriteType == $1", creator.username, FavoriteType.Creator.name)
                .find()
                .forEach { delete(it) }
        }
    }

    suspend fun export() = realm.query(Favorite::class)
        .find()
        .map { it.toModel() }

    suspend fun import(jsonString: String) {
        val list = json.decodeFromString<List<FavoriteModel>>(jsonString).toMutableList()
        realm.write {
            val favoriteList = query<Favorite>().find()
            list.removeIf { m -> favoriteList.any { it.id == m.id } }
            list.map { fm ->
                Favorite().apply {
                    this.id = fm.id
                    this.name = fm.name
                    this.imageUrl = fm.imageUrl
                    this.dateAdded = fm.dateAdded

                    when (fm) {
                        is FavoriteModel.Model -> {
                            description = fm.description
                            type = fm.type
                            this.nsfw = fm.nsfw
                            this.favoriteType = fm.modelType
                        }

                        is FavoriteModel.Image -> {
                            nsfw = fm.nsfw
                            imageMeta = fm.imageMetaDb
                            this.favoriteType = fm.modelType
                        }

                        is FavoriteModel.Creator -> {

                        }
                    }
                }
            }.forEach { copyToRealm(it) }
        }
    }
}

private fun Flow<RealmResults<Favorite>>.mapToFavoriteModels() = mapNotNull {
    it.map { favorite ->
        val type = runCatching { FavoriteType.valueOf(favorite.favoriteType) }
            .getOrDefault(FavoriteType.Model)
        when (type) {
            FavoriteType.Model -> {
                FavoriteModel.Model(
                    id = favorite.id,
                    name = favorite.name,
                    imageUrl = favorite.imageUrl,
                    description = favorite.description,
                    type = favorite.type,
                    nsfw = favorite.nsfw,
                    modelType = type.name,
                )
            }

            FavoriteType.Image -> {
                FavoriteModel.Image(
                    id = favorite.id,
                    name = favorite.name,
                    imageUrl = favorite.imageUrl,
                    nsfw = favorite.nsfw,
                    imageMetaDb = favorite.imageMeta,
                    modelId = favorite.modelId,
                    modelType = type.name,
                )
            }

            FavoriteType.Creator -> {
                FavoriteModel.Creator(
                    id = favorite.id,
                    name = favorite.name,
                    imageUrl = favorite.imageUrl,
                    modelType = type.name,
                )
            }
        }
        /*Models(
            id = favorite.id,
            name = favorite.name,
            description = favorite.description,
            type = ModelType.valueOf(favorite.type),
            nsfw = favorite.nsfw,
            allowNoCredit = false,
            allowCommercialUse = "",
            allowDerivatives = false,
            allowDifferentLicense = false,
            tags = emptyList(),
            modelVersions = listOf(
                ModelVersion(
                    id = 0,
                    modelId = 0,
                    name = "",
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                    trainedWords = emptyList(),
                    baseModel = "",
                    baseModelType = null,
                    earlyAccessTimeFrame = 0,
                    description = null,
                    images = listOf(
                        ModelImage(
                            id = null,
                            url = favorite.imageUrl.orEmpty(),
                            nsfw = NsfwLevel.None,
                            width = 0,
                            height = 0,
                            meta = null
                        )
                    ),
                    downloadUrl = ""
                )
            )
        )*/
    }
}

fun ImageMeta.toDb() = ImageMetaDb().apply {
    cfgScale = this@toDb.cfgScale
    clipSkip = this@toDb.clipSkip
    model = this@toDb.model
    seed = this@toDb.seed
    prompt = this@toDb.prompt
    negativePrompt = this@toDb.negativePrompt
    size = this@toDb.size
    steps = this@toDb.steps
    sampler = this@toDb.sampler
}

fun ImageMetaDb.toMeta() = ImageMeta(
    size = size,
    seed = seed,
    model = model,
    steps = steps,
    prompt = prompt,
    sampler = sampler,
    cfgScale = cfgScale,
    clipSkip = clipSkip,
    negativePrompt = negativePrompt
)

fun Favorite.toModel(): FavoriteModel {
    val type = runCatching { FavoriteType.valueOf(favoriteType) }
        .getOrDefault(FavoriteType.Model)
    return when (type) {
        FavoriteType.Model -> {
            FavoriteModel.Model(
                id = id,
                name = name,
                imageUrl = imageUrl,
                description = description,
                type = this.type,
                nsfw = nsfw,
                modelType = type.name,
                dateAdded = dateAdded
            )
        }

        FavoriteType.Image -> {
            FavoriteModel.Image(
                id = id,
                name = name,
                imageUrl = imageUrl,
                nsfw = nsfw,
                imageMetaDb = imageMeta,
                modelId = modelId,
                modelType = type.name,
                dateAdded = dateAdded
            )
        }

        FavoriteType.Creator -> {
            FavoriteModel.Creator(
                id = id,
                name = name,
                imageUrl = imageUrl,
                modelType = type.name,
                dateAdded = dateAdded
            )
        }
    }
}