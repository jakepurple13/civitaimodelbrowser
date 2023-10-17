package com.programmersbox.common.paging

import com.programmersbox.common.CivitAi
import com.programmersbox.common.Network

class CivitBrowserSearchPagingSource(
    network: Network,
    private val searchQuery: String,
    includeNsfw: Boolean = true,
) : CivitAiPagingSource(network, includeNsfw) {
    override suspend fun networkLoad(
        params: LoadParams<Int>,
        page: Int,
        includeNsfw: Boolean,
    ): Result<CivitAi> = network.searchModels(
        page = page.coerceAtLeast(1),
        perPage = 20,
        searchQuery = searchQuery,
        includeNsfw = includeNsfw
    )
}