package com.programmersbox.common.backup

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.programmersbox.common.DataStore
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListDao
import com.programmersbox.common.db.SearchHistoryDao
import com.programmersbox.common.db.SearchHistoryItem
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class BackupRepository(
    private val favoritesDao: FavoritesDao,
    private val listDao: ListDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val dataStore: DataStore,
    private val zipper: Zipper,
) {
    private val json by lazy {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    suspend fun packageItems(
        platformFile: PlatformFile,
        includeFavorites: Boolean,
        includeBlacklisted: Boolean,
        includeSettings: Boolean,
        includeSearchHistory: Boolean,
        listItemsByUuid: List<String>,
    ) {
        val itemsToZip = buildMap<String, String> {
            if (includeFavorites) {
                put(
                    "favorites.json",
                    json.encodeToString(favoritesDao.exportFavorites(json))
                )
            }

            if (includeBlacklisted) {
                put(
                    "blacklisted.json",
                    json.encodeToString(favoritesDao.exportBlacklisted())
                )
            }

            if (listItemsByUuid.isNotEmpty()) {
                put(
                    "lists.json",
                    json.encodeToString(listDao.getAllListItems(*listItemsByUuid.toTypedArray()))
                )
            }

            if (includeSearchHistory) {
                put(
                    "search_history.json",
                    json.encodeToString(searchHistoryDao.getAllSearchHistory())
                )
            }

            if (includeSettings) {
                runCatching {
                    val map = dataStore.dataStore.data.firstOrNull()?.asMap()!!
                    BackupSettings(
                        map
                            .filter { it.value is String }
                            .mapKeys { it.key.name }
                            .mapValues { it.value.toString() },
                        map
                            .filter { it.value is Int }
                            .mapKeys { it.key.name }
                            .mapValues { it.value as Int },
                        map
                            .filter { it.value is Long }
                            .mapKeys { it.key.name }
                            .mapValues { it.value as Long },
                        map
                            .filter { it.value is Boolean }
                            .mapKeys { it.key.name }
                            .mapValues { it.value as Boolean },
                        map
                            .filter { it.value is Double }
                            .mapKeys { it.key.name }
                            .mapValues { it.value as Double },
                        map
                            .filter { it.value is ByteArray }
                            .mapKeys { it.key.name }
                            .mapValues { it.value as ByteArray },
                    )
                }
                    .getOrNull()
                    ?.let { put("settings.json", json.encodeToString(it)) }
            }
        }

        zipper.zip(platformFile, itemsToZip)
    }

    suspend fun restoreItems(platformFile: PlatformFile) {
        zipper.unzip(
            platformFile,
            onInfo = { fileName, jsonString ->
                when (fileName) {
                    "favorites.json" -> favoritesDao.importOnlyFavorites(jsonString, json)
                    "blacklisted.json" -> favoritesDao.importBlacklisted(jsonString, json)
                    "lists.json" -> {
                        val lists = json.decodeFromString<List<CustomList>>(jsonString)
                        lists.forEach { listDao.createList(it.item) }
                        lists.forEach { it.list.forEach { item -> listDao.addItem(item) } }
                    }

                    "settings.json" -> {
                        runCatching {
                            val backupSettings = json.decodeFromString<BackupSettings>(jsonString)
                            with(backupSettings) {
                                dataStore.dataStore.edit { p ->
                                    stringSettings.forEach {
                                        p[stringPreferencesKey(it.key)] = it.value
                                    }
                                    intSettings.forEach {
                                        p[intPreferencesKey(it.key)] = it.value
                                    }
                                    longSettings.forEach {
                                        p[longPreferencesKey(it.key)] = it.value
                                    }
                                    booleanSettings.forEach {
                                        p[booleanPreferencesKey(it.key)] = it.value
                                    }
                                    doubleSettings.forEach {
                                        p[doublePreferencesKey(it.key)] = it.value
                                    }
                                    byteArraySettings.forEach {
                                        p[byteArrayPreferencesKey(it.key)] = it.value
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    suspend fun restoreItems(
        backupItems: BackupItems,
        includeSettings: Boolean,
        includeFavorites: Boolean,
        includeBlacklisted: Boolean,
        includeSearchHistory: Boolean,
    ) {
        if (includeFavorites) {
            backupItems.favorites?.let {
                favoritesDao.importOnlyFavorites(json.encodeToString(it), json)
            }
        }
        if (includeBlacklisted) {
            backupItems.blacklisted?.let {
                favoritesDao.importBlacklisted(json.encodeToString(it), json)
            }
        }
        backupItems.lists?.let { lists ->
            lists.forEach { listDao.createList(it.item) }
            lists.forEach { it.list.forEach { item -> listDao.addItem(item) } }
        }
        if (includeSearchHistory) {
            backupItems.searchHistory?.forEach {
                searchHistoryDao.addSearchHistory(it)
            }
        }
        if (includeSettings) {
            backupItems.settings?.let { backupSettings ->
                with(backupSettings) {
                    dataStore.dataStore.edit { p ->
                        stringSettings.forEach {
                            p[stringPreferencesKey(it.key)] = it.value
                        }
                        intSettings.forEach {
                            p[intPreferencesKey(it.key)] = it.value
                        }
                        longSettings.forEach {
                            p[longPreferencesKey(it.key)] = it.value
                        }
                        booleanSettings.forEach {
                            p[booleanPreferencesKey(it.key)] = it.value
                        }
                        doubleSettings.forEach {
                            p[doublePreferencesKey(it.key)] = it.value
                        }
                        byteArraySettings.forEach {
                            p[byteArrayPreferencesKey(it.key)] = it.value
                        }
                    }
                }
            }
        }
    }

    suspend fun readItems(platformFile: PlatformFile): BackupItems {
        var favorites: List<FavoriteModel>? = null
        var blacklisted: List<BlacklistedItemRoom>? = null
        var lists: List<CustomList>? = null
        var settings: BackupSettings? = null
        var searchHistory: List<SearchHistoryItem>? = null
        zipper.unzip(
            platformFile,
            onInfo = { fileName, jsonString ->
                when (fileName) {
                    "favorites.json" -> favorites = json.decodeFromString(jsonString)
                    "blacklisted.json" -> blacklisted = json.decodeFromString(jsonString)
                    "lists.json" -> lists = json.decodeFromString<List<CustomList>>(jsonString)
                    "settings.json" -> settings = json.decodeFromString<BackupSettings>(jsonString)
                    "search_history.json" -> searchHistory =
                        json.decodeFromString<List<SearchHistoryItem>>(jsonString)
                }
            }
        )
        return BackupItems(favorites, blacklisted, lists, settings, searchHistory)
    }
}

data class BackupItems(
    val favorites: List<FavoriteModel>?,
    val blacklisted: List<BlacklistedItemRoom>?,
    val lists: List<CustomList>?,
    val settings: BackupSettings?,
    val searchHistory: List<SearchHistoryItem>?,
)

expect class Zipper {
    suspend fun zip(
        platformFile: PlatformFile,
        itemsToZip: Map<String, String>
    )

    suspend fun unzip(
        platformFile: PlatformFile,
        onInfo: suspend (fileName: String, jsonString: String) -> Unit
    )
}

@Serializable
data class BackupSettings(
    val stringSettings: Map<String, String>,
    val intSettings: Map<String, Int>,
    val longSettings: Map<String, Long>,
    val booleanSettings: Map<String, Boolean>,
    val doubleSettings: Map<String, Double>,
    val byteArraySettings: Map<String, ByteArray>,
)