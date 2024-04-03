package com.programmersbox.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.programmersbox.common.CivitAi
import com.programmersbox.common.Models
import com.programmersbox.common.Network

abstract class CivitAiPagingSource(
    protected val network: Network,
    private val includeNsfw: Boolean = true,
) : PagingSource<String, Models>() {
    override val keyReuseSupported: Boolean get() = true

    override fun getRefreshKey(state: PagingState<String, Models>): String? {
        return null
        return state.anchorPosition
            ?.let { state.closestPageToPosition(it) }
            ?.let { it.prevKey ?: it.nextKey }
    }

    abstract suspend fun networkLoad(
        params: LoadParams<String>,
        page: Int,
        includeNsfw: Boolean,
    ): Result<CivitAi>

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Models> {
        val request = if (params.key == null) {
            networkLoad(
                params = params,
                page = 1,
                includeNsfw = includeNsfw
            )
        } else {
            network.fetchRequest<CivitAi>(params.key.orEmpty())
        }
        return request.fold(
            onSuccess = { response ->
                LoadResult.Page(
                    data = response.items,
                    prevKey = response.metadata.prevPage,
                    nextKey = response.metadata.nextPage
                )
            },
            onFailure = {
                it.printStackTrace()
                LoadResult.Error(it)
            }
        )
    }
}