package com.programmersbox.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.programmersbox.common.CivitAi
import com.programmersbox.common.Models
import com.programmersbox.common.Network

abstract class CivitAiPagingSource(
    protected val network: Network,
    private val includeNsfw: Boolean = true,
) : PagingSource<Int, Models>() {
    override val keyReuseSupported: Boolean get() = true

    override fun getRefreshKey(state: PagingState<Int, Models>): Int? {
        return state.anchorPosition
            ?.let { state.closestPageToPosition(it) }
            ?.let { it.prevKey?.plus(1) ?: it.nextKey?.minus(1) }
    }

    abstract suspend fun networkLoad(
        params: LoadParams<Int>,
        page: Int,
        includeNsfw: Boolean,
    ): Result<CivitAi>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Models> {
        val page = params.key ?: 1

        return networkLoad(
            params = params,
            page = page,
            includeNsfw = includeNsfw
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