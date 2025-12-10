package com.programmersbox.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.programmersbox.common.CivitAiCustomImages
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.Network

class CivitImagePagingSource(
    private val network: Network,
    private val includeNsfw: Boolean = true,
) : PagingSource<String, Pair<Long, List<CustomModelImage>>>() {
    override val keyReuseSupported: Boolean get() = true

    override fun getRefreshKey(state: PagingState<String, Pair<Long, List<CustomModelImage>>>): String? {
        return state.anchorPosition
            ?.let { state.closestPageToPosition(it) }
            ?.let { it.prevKey ?: it.nextKey }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Pair<Long, List<CustomModelImage>>> {
        val request = if (params.key == null) {
            network.fetchAllImages(includeNsfw = includeNsfw)
        } else {
            network.fetchRequest<CivitAiCustomImages>(params.key.orEmpty())
        }
        return request
            .fold(
                onSuccess = { response ->
                    LoadResult.Page(
                        data = response
                            .items
                            .groupBy { it.postId ?: 0L }
                            .toList(),
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