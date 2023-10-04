package com.programmersbox.common.db

import com.programmersbox.common.*
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Clock

class FavoritesDatabase(
    name: String = Realm.DEFAULT_FILE_NAME,
) {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
                    FavoriteList::class,
                    Favorite::class
                )
            )
                .schemaVersion(2)
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
                Models(
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
                )
            }
        }

    suspend fun addFavorite(
        id: Long,
        name: String = "",
        description: String? = null,
        type: ModelType = ModelType.Other,
        nsfw: Boolean = false,
        imageUrl: String? = null,
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
                }
            )
        }
    }

    suspend fun removeFavorite(id: Long) {
        realm.updateInfo<FavoriteList> { it?.favorites?.removeIf { it.id == id } }
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