package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath


class DataStore private constructor(
    producePath: () -> String = { "androidx.preferences_pb" },
) {
    val dataStore = PreferenceDataStoreFactory.createWithPath { producePath().toPath() }

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

    @Composable
    fun rememberReverseFavorites() = rememberPreference(
        booleanPreferencesKey("reverse_favorites"),
        false
    )

    @Composable
    fun rememberShowBlur() = rememberPreference(
        booleanPreferencesKey("show_blur"),
        true
    )

    @Composable
    fun rememberUseToolbar() = rememberPreference(
        booleanPreferencesKey("use_toolbar"),
        true
    )

    open class DataStoreType<T>(
        val key: Preferences.Key<T>,
        protected val dataStore: DataStore<Preferences>,
    ) {
        open val flow: Flow<T?> = dataStore.data
            .map { it[key] }
            .distinctUntilChanged()

        open suspend fun update(value: T) {
            dataStore.edit { it[key] = value }
        }

        open suspend fun get(): T? = flow.firstOrNull()
    }

    open class DataStoreTypeNonNull<T>(
        key: Preferences.Key<T>,
        dataStore: DataStore<Preferences>,
        val defaultValue: T,
    ) : DataStoreType<T>(key, dataStore) {
        override val flow: Flow<T> = dataStore.data
            .mapNotNull { it[key] ?: defaultValue }
            .distinctUntilChanged()

        override suspend fun get(): T = flow.firstOrNull() ?: defaultValue
    }

    @Composable
    private fun <T> rememberPreference(
        key: Preferences.Key<T>,
        defaultValue: T,
    ): MutableState<T> {
        val coroutineScope = rememberCoroutineScope()
        val state by remember {
            dataStore.data.map { it[key] ?: defaultValue }
        }.collectAsStateWithLifecycle(initialValue = defaultValue)

        return remember(state) {
            object : MutableState<T> {
                override var value: T
                    get() = state
                    set(value) {
                        coroutineScope.launch {
                            dataStore.edit { it[key] = value }
                        }
                    }

                override fun component1() = value
                override fun component2(): (T) -> Unit = { value = it }
            }
        }
    }
}

