package com.programmersbox.common

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import okio.Path.Companion.toPath


class DataStore private constructor(
    producePath: () -> String = { "androidx.preferences_pb" },
) {
    private val dataStore = PreferenceDataStoreFactory.createWithPath { producePath().toPath() }

    companion object {
        lateinit var dataStore: com.programmersbox.common.DataStore

        fun getStore(producePath: () -> String = { "androidx.preferences_pb" }) =
            if (::dataStore.isInitialized)
                dataStore
            else {
                dataStore = DataStore(producePath)
                dataStore
            }
    }

    val showNsfw = DataStoreTypeNonNull(
        key = booleanPreferencesKey("show_nsfw"),
        dataStore = dataStore,
        defaultValue = false
    )
    val hideNsfwStrength = DataStoreTypeNonNull(
        key = floatPreferencesKey("hide_nsfw_strength"),
        dataStore = dataStore,
        defaultValue = 6f
    )

    val includeNsfw = DataStoreTypeNonNull(
        key = booleanPreferencesKey("include_nsfw"),
        dataStore = dataStore,
        defaultValue = true
    )

    val reverseFavorites = DataStoreTypeNonNull(
        key = booleanPreferencesKey("reverse_favorites"),
        dataStore = dataStore,
        defaultValue = false
    )

    open class DataStoreType<T>(
        protected val key: Preferences.Key<T>,
        protected val dataStore: DataStore<Preferences>,
    ) {
        open val flow: Flow<T?> = dataStore.data
            .map { it[key] }
            .distinctUntilChanged()

        open suspend fun update(value: T) {
            dataStore.edit { it[key] = value }
        }
    }

    open class DataStoreTypeNonNull<T>(
        key: Preferences.Key<T>,
        dataStore: DataStore<Preferences>,
        defaultValue: T,
    ) : DataStoreType<T>(key, dataStore) {
        override val flow: Flow<T> = dataStore.data
            .mapNotNull { it[key] ?: defaultValue }
            .distinctUntilChanged()
    }
}