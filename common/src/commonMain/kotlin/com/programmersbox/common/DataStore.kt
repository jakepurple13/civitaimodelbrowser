package com.programmersbox.common

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import okio.Path.Companion.toPath


class DataStore(
    producePath: () -> String = { "androidx.preferences_pb" },
) {
    private val dataStore = PreferenceDataStoreFactory.createWithPath { producePath().toPath() }

    val showNsfw = DataStoreTypeNonNull(
        booleanPreferencesKey("show_nsfw"),
        dataStore
    )
    val hideNsfwStrength = DataStoreTypeNonNull(
        floatPreferencesKey("hide_nsfw_strength"),
        dataStore
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
    ) : DataStoreType<T>(key, dataStore) {
        override val flow: Flow<T> = dataStore.data
            .mapNotNull { it[key] }
            .distinctUntilChanged()
    }
}