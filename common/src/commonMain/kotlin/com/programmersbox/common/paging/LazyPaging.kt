package com.programmersbox.common.paging

import androidx.paging.compose.LazyPagingItems

@Suppress("PrimitiveInLambda")
public fun <T : Any> LazyPagingItems<T>.itemKeyIndexed(
    key: ((item: @JvmSuppressWildcards T, index: Int) -> Any)? = null,
): (index: Int) -> Any {
    return { index ->
        if (key == null) {
            index
        } else {
            val item = peek(index)
            if (item == null) index else key(item, index)
        }
    }
}