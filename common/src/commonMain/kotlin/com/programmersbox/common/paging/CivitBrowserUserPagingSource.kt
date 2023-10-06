package com.programmersbox.common.paging

import com.programmersbox.common.CivitAi
import com.programmersbox.common.Network

class CivitBrowserUserPagingSource(
    network: Network,
    includeNsfw: Boolean = true,
    private val username: String,
) : CivitAiPagingSource(network, includeNsfw) {
    override suspend fun networkLoad(
        params: LoadParams<Int>,
        page: Int,
        includeNsfw: Boolean,
    ): Result<CivitAi> = network.getModels(
        page = page.coerceAtLeast(1),
        perPage = params.loadSize,
        includeNsfw = includeNsfw,
        creatorUsername = username
    )
}