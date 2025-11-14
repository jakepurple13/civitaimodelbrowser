package com.programmersbox.common

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gigamole.composescrollbars.Scrollbars
import com.gigamole.composescrollbars.config.ScrollbarsConfig
import com.gigamole.composescrollbars.config.ScrollbarsOrientation
import com.gigamole.composescrollbars.rememberScrollbarsState
import com.gigamole.composescrollbars.scrolltype.ScrollbarsScrollType
import com.gigamole.composescrollbars.scrolltype.knobtype.ScrollbarsDynamicKnobType
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.db.CivitDb

public actual fun getPlatformName(): String {
    return "Android ${android.os.Build.VERSION.SDK_INT}"
}

@Composable
public fun UIShow(
    onShareClick: (String) -> Unit,
    producePath: () -> String,
    onExport: (CivitDb) -> Unit = {},
    onImport: () -> String = { "" },
    export: @Composable () -> Unit = {},
    import: (@Composable () -> Unit)? = null,
) {
    App(
        onShareClick = onShareClick,
        producePath = producePath,
        onExport = onExport,
        onImport = onImport,
        export = export,
        import = import,
        builder = getDatabaseBuilder(LocalContext.current)
    )
}

internal actual fun getPagingPlaceholderKey(index: Int): Any = PagingPlaceholderKey(index)

private data class PagingPlaceholderKey(private val index: Int) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingPlaceholderKey> =
            object : Parcelable.Creator<PagingPlaceholderKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingPlaceholderKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingPlaceholderKey?>(size)
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun SheetDetails(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets.systemBars.only(WindowInsetsSides.Top) },
    ) {
        content()
    }
}

internal actual val showRefreshButton: Boolean = false

@Composable
internal actual fun CustomScrollBar(lazyGridState: LazyGridState, modifier: Modifier) {
    Scrollbars(
        state = rememberScrollbarsState(
            config = ScrollbarsConfig(
                orientation = ScrollbarsOrientation.Vertical
            ),
            scrollType = ScrollbarsScrollType.Lazy.Grid.Dynamic(
                knobType = ScrollbarsDynamicKnobType.Auto(),
                state = lazyGridState,
                spanCount = 3
            )
        ),
        modifier = modifier
    )
}

@Composable
internal actual fun ContextMenu(
    isBlacklisted: Boolean,
    blacklistItems: List<BlacklistedItemRoom>,
    modelId: Long,
    name: String,
    nsfw: Boolean,
    imageUrl: String?,
    content: @Composable () -> Unit,
) {
    content()
}

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

/*
actual fun createRoomDatabase(): AppDatabase {
    val dbFile = getDatabasePath(dbFileName)
    return Room.databaseBuilder<AppDatabase>(
        context = app,
        name = dbFile.absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}*/
