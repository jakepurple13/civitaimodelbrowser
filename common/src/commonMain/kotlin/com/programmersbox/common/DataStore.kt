package com.programmersbox.common

import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import okio.Path.Companion.toPath


object DataStore {
    private val dataStore = PreferenceDataStoreFactory.createWithPath { "androidx.preferences_pb".toPath() }

    val showNsfw = DataStoreTypeNonNull(booleanPreferencesKey("show_nsfw"))
    val hideNsfwStrength = DataStoreTypeNonNull(floatPreferencesKey("hide_nsfw_strength"))

    open class DataStoreType<T>(
        protected val key: Preferences.Key<T>,
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
    ) : DataStoreType<T>(key) {
        override val flow: Flow<T> = dataStore.data
            .mapNotNull { it[key] }
            .distinctUntilChanged()
    }
}