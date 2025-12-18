package com.programmersbox.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.programmersbox.common.CivitAiCustomImages
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.Network
import com.programmersbox.common.PAGE_LIMIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class CivitDetailsImagePagingSource(
    private val network: Network,
    private val includeNsfw: Boolean = true,
    private val modelId: String?,
) : PagingSource<String, CustomModelImage>() {
    override val keyReuseSupported: Boolean get() = true

    override fun getRefreshKey(state: PagingState<String, CustomModelImage>): String? {
        return state.anchorPosition
            ?.let { state.closestPageToPosition(it) }
            ?.let { it.prevKey ?: it.nextKey }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, CustomModelImage> {
        val request = withContext(Dispatchers.IO) {
            if (params.key == null) {
                network.fetchAllImagesByModel(
                    modelId = modelId.orEmpty(),
                    page = 1,
                    perPage = PAGE_LIMIT,
                    includeNsfw = includeNsfw
                )
            } else {
                network.fetchRequest<CivitAiCustomImages>(params.key.orEmpty())
            }
        }
        return request
            .fold(
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