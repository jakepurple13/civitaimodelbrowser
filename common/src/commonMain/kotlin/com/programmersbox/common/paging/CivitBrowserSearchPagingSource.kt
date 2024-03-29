package com.programmersbox.common.paging

import com.programmersbox.common.CivitAi
import com.programmersbox.common.Network
import com.programmersbox.common.PAGE_LIMIT

class CivitBrowserSearchPagingSource(
    network: Network,
    private val searchQuery: String,
    includeNsfw: Boolean = true,
) : CivitAiPagingSource(network, includeNsfw) {
    override suspend fun networkLoad(
        params: LoadParams<String>,
        page: Int,
        includeNsfw: Boolean,
    ): Result<CivitAi> = network.searchModels(
        page = page.coerceAtLeast(1),
        perPage = PAGE_LIMIT,
        searchQuery = searchQuery,
        includeNsfw = includeNsfw
    )
}