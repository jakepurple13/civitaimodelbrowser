package com.programmersbox.common.db

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.programmersbox.common.Creator
import com.programmersbox.common.ImageMeta
import com.programmersbox.common.ModelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Database(
    entities = [
        FavoriteRoom::class,
        BlacklistedItemRoom::class,
        CustomListInfo::class,
        CustomListItem::class,
        SearchHistoryItem::class,
    ],
    version = 8,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
    ]
)

@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): FavoritesDao
    abstract fun getListDao(): ListDao
    abstract fun getSearchHistoryDao(): SearchHistoryDao
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
        .setDriver(BundledSQLiteDriver())
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
        hash: String? = null,
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
            hash = hash,
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

    @Query("SELECT EXISTS(SELECT * FROM favorite_table WHERE favoriteType = :type AND name = :username)")
    fun getFavoritesByType(type: FavoriteType, username: String?): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM favorite_table WHERE favoriteType = :type AND id = :id)")
    fun getFavoritesByType(type: FavoriteType, id: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM favorite_table WHERE favoriteType = :type AND imageUrl = :imageUrl)")
    fun getFavoritesImages(type: FavoriteType, imageUrl: String?): Flow<Boolean>

    @Query("SELECT COUNT(id) FROM favorite_table")
    fun getFavoritesCount(): Flow<Int>

    @Query(
        """
        SELECT 
            SUM(CASE WHEN favoriteType = 'Model' THEN 1 ELSE 0 END) as modelCount,
            SUM(CASE WHEN favoriteType = 'Image' THEN 1 ELSE 0 END) as imageCount,
            SUM(CASE WHEN favoriteType = 'Creator' THEN 1 ELSE 0 END) as creatorCount
        FROM favorite_table
    """
    )
    fun getTypeCounts(): Flow<DataCounts>

    @Query("SELECT COUNT(id) FROM blacklisted_table")
    fun getBlacklistCount(): Flow<Int>

    @Query("SELECT * FROM favorite_table ORDER BY dateAdded DESC")
    suspend fun getFavoritesSync(): List<FavoriteRoom>

    @Query("SELECT * FROM favorite_table WHERE nsfw = :includeNsfw ORDER BY dateAdded DESC")
    fun getFavoritesWithNSFW(includeNsfw: Boolean): Flow<List<FavoriteRoom>>

    @Query(
        """
        SELECT * FROM favorite_table
        WHERE (nsfw = :includeNsfw OR nsfw = 0) 
        AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') 
        ORDER BY dateAdded DESC
    """
    )
    fun searchFavorites(
        query: String,
        includeNsfw: Boolean,
    ): Flow<List<FavoriteRoom>>

    @Query(
        """
        SELECT * FROM favorite_table
        WHERE (nsfw = :includeNsfw OR nsfw = 0) 
        AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') 
        AND (favoriteType IN (:types) OR type IN (:types))
        ORDER BY dateAdded DESC
    """
    )
    fun searchFavorites(
        query: String,
        includeNsfw: Boolean,
        types: List<String>?
    ): Flow<List<FavoriteRoom>>

    @Ignore
    fun searchForFavorites(
        query: String,
        json: Json = Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
        includeNsfw: Boolean,
        type: List<String>,
    ) = if (type.isEmpty()) {
        searchFavorites(
            query = query,
            includeNsfw = includeNsfw,
        )
    } else {
        searchFavorites(
            query = query,
            includeNsfw = includeNsfw,
            types = type
        )
    }.map { value -> value.map { favorite -> favorite.toModel(json) } }

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
    fun getFavoriteModels(
        json: Json = Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
        includeNsfw: Boolean,
    ) = if (includeNsfw) {
        getFavorites()
    } else {
        getFavoritesWithNSFW(false)
    }.map { value ->
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
                hash = hash,
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
                hash = hash,
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

    @Query("SELECT EXISTS(SELECT * FROM blacklisted_table WHERE imageUrl = :imageUrl)")
    fun getBlacklistedByImageUrl(imageUrl: String?): Flow<Boolean>

    @Query("SELECT * FROM blacklisted_table")
    suspend fun getBlacklistedSync(): List<BlacklistedItemRoom>

    @Delete
    suspend fun delete(item: FavoriteRoom)

    @Delete
    suspend fun delete(item: BlacklistedItemRoom)

    @Ignore
    suspend fun removeCreator(creator: Creator) = removeCreator(creator.username)

    @Query("DELETE FROM favorite_table WHERE name = :username AND favoriteType = :favoriteType")
    suspend fun removeCreator(username: String?, favoriteType: FavoriteType = FavoriteType.Creator)

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
    suspend fun exportFavorites(
        json: Json = Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    ) = getFavoritesSync().map { room -> room.toModel(json) }

    @Ignore
    suspend fun exportBlacklisted() = getBlacklistedSync()

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

    @Ignore
    suspend fun importOnlyFavorites(
        jsonString: String,
        json: Json = Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    ) {
        val list = json.decodeFromString<List<FavoriteModel>>(jsonString)
        list.forEach { model ->
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
    }

    @Ignore
    suspend fun importBlacklisted(
        jsonString: String,
        json: Json = Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
    ) {
        val list = json.decodeFromString<List<BlacklistedItemRoom>>(jsonString)
        list.forEach { item -> insert(item) }
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

data class DataCounts(
    val modelCount: Int,
    val imageCount: Int,
    val creatorCount: Int
)
