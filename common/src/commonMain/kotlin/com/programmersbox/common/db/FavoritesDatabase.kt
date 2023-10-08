package com.programmersbox.common.db

import com.programmersbox.common.ImageMeta
import com.programmersbox.common.ModelType
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.mapNotNull

class FavoritesDatabase(
    name: String = Realm.DEFAULT_FILE_NAME,
) {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
                    FavoriteList::class,
                    Favorite::class,
                    ImageMetaDb::class
                )
            )
                .schemaVersion(3)
                .name(name)
                .migration(AutomaticSchemaMigration { })
                //.deleteRealmIfMigrationNeeded()
                .build()
        )
    }

    private val list = realm.initDbBlocking { FavoriteList() }

    fun getFavorites() = list
        .asFlow()
        .mapNotNull { it.obj?.favorites }
        .mapNotNull {
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
                            nsfw = favorite.nsfw
                        )
                    }

                    FavoriteType.Image -> {
                        FavoriteModel.Image(
                            id = favorite.id,
                            name = favorite.name,
                            imageUrl = favorite.imageUrl,
                            nsfw = favorite.nsfw,
                            imageMetaDb = favorite.imageMeta
                        )
                    }

                    FavoriteType.Creator -> {
                        FavoriteModel.Creator(
                            id = favorite.id,
                            name = favorite.name,
                            imageUrl = favorite.imageUrl
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

    suspend fun addFavorite(
        id: Long,
        name: String = "",
        description: String? = null,
        type: ModelType = ModelType.Other,
        nsfw: Boolean = false,
        imageUrl: String? = null,
        favoriteType: FavoriteType = FavoriteType.Model,
        imageMetaDb: ImageMetaDb? = null,
    ) {
        realm.updateInfo<FavoriteList> {
            it?.favorites?.add(
                Favorite().apply {
                    this.id = id
                    this.name = name
                    this.description = description
                    this.type = type.name
                    this.nsfw = nsfw
                    this.imageUrl = imageUrl
                    this.favoriteType = favoriteType.name
                    this.imageMeta = imageMetaDb
                }
            )
        }
    }

    suspend fun removeFrom(block: RealmList<Favorite>.() -> Unit) {
        realm.updateInfo<FavoriteList> { it?.favorites?.block() }
    }

    suspend fun removeFavorite(id: Long) {
        realm.updateInfo<FavoriteList> { it?.favorites?.removeIf { f -> f.id == id } }
    }

    suspend fun removeFavoriteByName(name: String) {
        realm.updateInfo<FavoriteList> { it?.favorites?.removeIf { f -> f.name == name } }
    }

    suspend fun removeFavorite(url: String) {
        realm.updateInfo<FavoriteList> { it?.favorites?.removeIf { f -> f.imageUrl == url } }
    }
}

private suspend inline fun <reified T : RealmObject> Realm.updateInfo(crossinline block: MutableRealm.(T?) -> Unit) {
    query(T::class).first().find()?.also { info ->
        write { block(findLatest(info)) }
    }
}

private inline fun <reified T : RealmObject> Realm.initDbBlocking(crossinline default: () -> T): T {
    val f = query(T::class).first().find()
    return f ?: writeBlocking { copyToRealm(default()) }
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