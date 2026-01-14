package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

    @Composable
    fun showNsfw() = rememberPreference(
        key = booleanPreferencesKey("show_nsfw"),
        defaultValue = false
    )

    @Composable
    fun hideNsfwStrength() = rememberPreference(
        key = floatPreferencesKey("hide_nsfw_strength"),
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
    fun rememberUseProgressive() = rememberPreference(
        booleanPreferencesKey("use_progressive"),
        true
    )

    @Composable
    fun rememberUseToolbar() = rememberPreference(
        booleanPreferencesKey("use_toolbar"),
        false
    )

    @Composable
    fun rememberThemeMode(): MutableState<ThemeMode> = rememberPreferenceType(
        key = stringPreferencesKey("theme_mode"),
        defaultValue = ThemeMode.System,
        mapToValue = { ThemeMode.valueOf(it) },
        mapToString = { it.name }
    )

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Composable
    fun rememberBlurType(): MutableState<BlurType> = rememberPreferenceType(
        key = stringPreferencesKey("blur_type"),
        defaultValue = BlurType(HazeBlur.Material, HazeLevel.Regular),
        mapToValue = { json.decodeFromString<BlurType>(it) },
        mapToString = { json.encodeToString(it) }
    )

    @Composable
    fun rememberIsAmoled(): MutableState<Boolean> = rememberPreference(
        key = booleanPreferencesKey("is_amoled"),
        defaultValue = false
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

    @Composable
    private fun <T> rememberPreferenceType(
        key: Preferences.Key<String>,
        defaultValue: T,
        mapToValue: (String) -> T,
        mapToString: (T) -> String,
    ): MutableState<T> {
        val coroutineScope = rememberCoroutineScope()
        val state by remember {
            dataStore
                .data
                .map {
                    it[key]
                        ?.let { p1 -> runCatching { mapToValue(p1) }.getOrNull() }
                        ?: defaultValue
                }
        }.collectAsStateWithLifecycle(initialValue = defaultValue)

        return remember(state) {
            object : MutableState<T> {
                override var value: T
                    get() = state
                    set(value) {
                        coroutineScope.launch {
                            dataStore.edit { it[key] = mapToString(value) }
                        }
                    }

                override fun component1() = value
                override fun component2(): (T) -> Unit = { value = it }
            }
        }
    }
}

enum class ThemeMode {
    System,
    Light,
    Dark
}

@Serializable
enum class HazeBlur(
    val levels: Array<HazeLevel>
) {
    Material(
        arrayOf(
            HazeLevel.UltraThin,
            HazeLevel.Thin,
            HazeLevel.Regular,
            HazeLevel.Thick,
            HazeLevel.UltraThick
        )
    ) {
        @Composable
        override fun toHazeStyle(level: HazeLevel) = when (level) {
            HazeLevel.UltraThin -> HazeMaterials.ultraThin()
            HazeLevel.Thin -> HazeMaterials.thin()
            HazeLevel.Regular -> HazeMaterials.regular()
            HazeLevel.Thick -> HazeMaterials.thick()
            HazeLevel.UltraThick -> HazeMaterials.ultraThick()
        }
    },
    Cupertino(
        arrayOf(
            HazeLevel.UltraThin,
            HazeLevel.Thin,
            HazeLevel.Regular,
            HazeLevel.Thick,
        )
    ) {
        @Composable
        override fun toHazeStyle(level: HazeLevel) = when (level) {
            HazeLevel.UltraThin -> CupertinoMaterials.ultraThin()
            HazeLevel.Thin -> CupertinoMaterials.thin()
            HazeLevel.Regular -> CupertinoMaterials.regular()
            HazeLevel.Thick -> CupertinoMaterials.thick()
            HazeLevel.UltraThick -> CupertinoMaterials.regular()
        }
    };

    @Composable
    abstract fun toHazeStyle(level: HazeLevel): HazeStyle
}

@Serializable
enum class HazeLevel {
    UltraThin,
    Thin,
    Regular,
    Thick,
    UltraThick
}

@Serializable
@Stable
data class BlurType(
    val type: HazeBlur,
    val level: HazeLevel
) {
    @Composable
    fun toHazeStyle() = type.toHazeStyle(level)
}