package com.programmersbox.common.db

import androidx.room.*
import com.programmersbox.common.Creator
import com.programmersbox.common.ImageMeta
import com.programmersbox.common.ModelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Database(
    entities = [FavoriteRoom::class, BlacklistedItemRoom::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)

@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): FavoritesDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        //.addMigrations(MIGRATIONS)
        //.setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

@Dao
interface FavoritesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavoriteRoom)

    @Ignore
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
        json: Json = Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    ) = insert(
        FavoriteRoom(
            id = id,
            name = name,
            description = description,
            type = type.name,
            nsfw = nsfw,
            imageUrl = imageUrl,
            favoriteType = favoriteType,
            imageMeta = imageMetaDb?.let { json.encodeToString(it) },
            modelId = modelId
        )
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BlacklistedItemRoom)

    @Ignore
    suspend fun blacklistItem(
        id: Long,
        name: String,
        nsfw: Boolean,
        imageUrl: String? = null,
    ) = insert(BlacklistedItemRoom(id, name, nsfw, imageUrl))

    @Query("SELECT * FROM favorite_table ORDER BY dateAdded DESC")
    fun getFavorites(): Flow<List<FavoriteRoom>>

    @Query("SELECT * FROM favorite_table ORDER BY dateAdded DESC")
    suspend fun getFavoritesSync(): List<FavoriteRoom>

    @Ignore
    fun getFavoriteModels(
        json: Json = Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    ) = getFavorites().map { value ->
        value.map { favorite -> favorite.toModel(json) }
    }

    @Ignore
    private fun FavoriteRoom.toModel(json: Json) = when (favoriteType) {
        FavoriteType.Model -> {
            FavoriteModel.Model(
                id = id,
                name = name,
                imageUrl = imageUrl,
                description = description,
                type = type,
                nsfw = nsfw,
                modelType = favoriteType.name,
            )
        }

        FavoriteType.Image -> {
            FavoriteModel.Image(
                id = id,
                name = name,
                imageUrl = imageUrl,
                nsfw = nsfw,
                imageMetaDb = imageMeta?.let { json.decodeFromString(it) },
                modelId = modelId,
                modelType = favoriteType.name,
            )
        }

        FavoriteType.Creator -> {
            FavoriteModel.Creator(
                id = id,
                name = name,
                imageUrl = imageUrl,
                modelType = favoriteType.name,
            )
        }
    }

    @Query("SELECT * FROM blacklisted_table")
    fun getBlacklisted(): Flow<List<BlacklistedItemRoom>>

    @Query("SELECT * FROM blacklisted_table")
    suspend fun getBlacklistedSync(): List<BlacklistedItemRoom>

    @Delete
    suspend fun delete(item: FavoriteRoom)

    @Delete
    suspend fun delete(item: BlacklistedItemRoom)

    @Ignore
    suspend fun removeCreator(creator: Creator) = removeCreator(creator.username)

    @Query("DELETE FROM favorite_table WHERE name = :username AND favoriteType = 'Creator'")
    suspend fun removeCreator(username: String?)

    @Query("DELETE FROM favorite_table WHERE imageUrl = :imageUrl AND favoriteType = 'Image'")
    suspend fun removeImage(imageUrl: String?)

    @Query("DELETE FROM favorite_table WHERE id = :id AND favoriteType = 'Model'")
    suspend fun removeModel(id: Long)

    @Ignore
    suspend fun export(
        json: Json = Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    ) = CivitDb(
        favorites = getFavoritesSync().map { room -> room.toModel(json) },
        blacklistedItemRoom = getBlacklistedSync()
    )

    @Ignore
    suspend fun importFavorites(
        jsonString: String,
        json: Json = Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    ) {
        val list = json.decodeFromString<CivitDb>(jsonString)
        list.favorites.forEach { model ->
            insert(
                when (model) {
                    is FavoriteModel.Model -> FavoriteRoom(
                        id = model.id,
                        name = model.name,
                        description = model.description,
                        type = model.type,
                        nsfw = model.nsfw,
                        imageUrl = model.imageUrl,
                        favoriteType = FavoriteType.Model,
                    )

                    is FavoriteModel.Creator -> FavoriteRoom(
                        id = model.id,
                        name = model.name,
                        type = model.modelType,
                        imageUrl = model.imageUrl,
                        favoriteType = FavoriteType.Creator,
                    )

                    is FavoriteModel.Image -> FavoriteRoom(
                        id = model.id,
                        name = model.name,
                        type = model.modelType,
                        imageUrl = model.imageUrl,
                        nsfw = model.nsfw,
                        favoriteType = FavoriteType.Image,
                        imageMeta = model.imageMetaDb?.let { json.encodeToString(it) }
                    )
                }
            )
        }

        list.blacklistedItemRoom.forEach { item -> insert(item) }
    }
}

fun ImageMeta.toDb() = ImageMetaDb(
    cfgScale = this@toDb.cfgScale,
    clipSkip = this@toDb.clipSkip,
    model = this@toDb.model,
    seed = this@toDb.seed,
    prompt = this@toDb.prompt,
    negativePrompt = this@toDb.negativePrompt,
    size = this@toDb.size,
    steps = this@toDb.steps,
    sampler = this@toDb.sampler,
)

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

@Serializable
data class CivitDb(
    val favorites: List<FavoriteModel>,
    val blacklistedItemRoom: List<BlacklistedItemRoom>,
)