package com.programmersbox.common

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import com.programmersbox.common.db.FavoriteModel

public actual fun getPlatformName(): String {
    return "civitaimodelbrowser"
}

@Composable
public fun UIShow(
    onShareClick: (String) -> Unit,
    producePath: () -> String,
    onExport: (List<FavoriteModel>) -> Unit = {},
    onImport: () -> String = { "" },
    export: @Composable () -> Unit = {},
    import: @Composable () -> Unit = {},
) {
    App(
        onShareClick = onShareClick,
        producePath = producePath,
        onExport = onExport,
        onImport = onImport,
        export = export,
        import = import
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
        windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
    ) {
        content()
    }
}

internal actual val showRefreshButton: Boolean = false