package com.programmersbox.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.programmersbox.common.Models
import com.programmersbox.common.Network

class CivitBrowserUserPagingSource(
    private val network: Network,
    private val includeNsfw: Boolean = true,
    private val username: String,
) : PagingSource<Int, Models>() {

    override val keyReuseSupported: Boolean get() = true

    override fun getRefreshKey(state: PagingState<Int, Models>): Int? {
        return state.anchorPosition
            ?.let { state.closestPageToPosition(it) }
            ?.let { it.prevKey?.plus(1) ?: it.nextKey?.minus(1) }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Models> {
        val page = params.key ?: 1

        return network.getModels(
            page = page.coerceAtLeast(1),
            perPage = params.loadSize,
            includeNsfw = includeNsfw,
            creatorUsername = username
        ).fold(
            onSuccess = { response ->
                val prevKey = page.takeIf { it > 1 }?.minus(1)

                // This API defines that it's out of data when a page returns empty. When out of
                // data, we return `null` to signify no more pages should be loaded
                val nextKey = if (response.items.isNotEmpty()) page + 1 else null

                LoadResult.Page(
                    data = response.items,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            },
            onFailure = {
                it.printStackTrace()
                LoadResult.Error(it)
            }
        )
    }
}