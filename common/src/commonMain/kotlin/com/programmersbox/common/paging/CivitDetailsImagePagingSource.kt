package com.programmersbox.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.Network

class CivitDetailsImagePagingSource(
    private val network: Network,
    private val includeNsfw: Boolean = true,
    private val modelId: String?,
) : PagingSource<Int, CustomModelImage>() {
    override val keyReuseSupported: Boolean get() = true

    override fun getRefreshKey(state: PagingState<Int, CustomModelImage>): Int? {
        return state.anchorPosition
            ?.let { state.closestPageToPosition(it) }
            ?.let { it.prevKey?.plus(1) ?: it.nextKey?.minus(1) }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CustomModelImage> {
        val page = params.key ?: 1

        return network.fetchAllImagesByModel(
            modelId = modelId.orEmpty(),
            page = page,
            perPage = 20,
            includeNsfw = includeNsfw
        )
            .fold(
                onSuccess = { response ->
                    val prevKey = page.takeIf { it > 1 }?.minus(1)

                    // This API defines that it's out of data when a page returns empty. When out of
                    // data, we return `null` to signify no more pages should be loaded
                    val nextKey = if (response.isNotEmpty()) page + 1 else null

                    LoadResult.Page(
                        data = response,
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