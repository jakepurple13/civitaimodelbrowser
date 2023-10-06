package com.programmersbox.common.paging

import com.programmersbox.common.CivitAi
import com.programmersbox.common.Network

class CivitBrowserPagingSource(
    network: Network,
    includeNsfw: Boolean = true,
) : CivitAiPagingSource(network, includeNsfw) {
    override suspend fun networkLoad(
        params: LoadParams<Int>,
        page: Int,
        includeNsfw: Boolean,
    ): Result<CivitAi> = network.getModels(
        page = page.coerceAtLeast(1),
        perPage = params.loadSize,
        includeNsfw = includeNsfw
    )
}

/*
@OptIn(ExperimentalPagingApi::class)
class AvatarRemoteMediator(
    private val pageCount: Int,
    private val networkService: AvatarApiService,
) : RemoteMediator<Int, Beer>() {

    private var lastUpdated = 0L
    private var page = 1

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Beer>,
    ): MediatorResult {
        return try {
            // The network load method takes an optional after=<user.id>
            // parameter. For every page after the first, pass the last user
            // ID to let it continue from where it left off. For REFRESH,
            // pass null to load the first page.
            val loadKey = when (loadType) {
                LoadType.REFRESH -> {
                    page = 1
                    null
                }
                // In this example, you never need to prepend, since REFRESH
                // will always load the first page in the list. Immediately
                // return, reporting end of pagination.
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem =
                        state.lastItemOrNull() ?: return MediatorResult.Success(endOfPaginationReached = true)

                    // You must explicitly check if the last item is null when
                    // appending, since passing null to networkService is only
                    // valid for initial load. If lastItem is null it means no
                    // items were loaded after the initial REFRESH and there are
                    // no more items to load.

                    lastItem.id
                }
            }

            // Suspending network load via Retrofit. This doesn't need to be
            // wrapped in a withContext(Dispatcher.IO) { ... } block since
            // Retrofit's Coroutine CallAdapter dispatches on a worker
            // thread.
            val response = networkService.getCharacters(pageCount, page)
            page++
            */
/*lastUpdated = System.currentTimeMillis()
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    userDao.clearAll()
                }

                // Insert new users into database, which invalidates the
                // current PagingData, allowing Paging to present the updates
                // in the DB.
                userDao.insertCharacters(response)
            }*//*


            MediatorResult.Success(endOfPaginationReached = response.isEmpty())
        } catch (e: IOException) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = 1.hours.inWholeMilliseconds
        return if (Clock.System.now().toEpochMilliseconds() - lastUpdated >= cacheTimeout) {
            // Cached data is up-to-date, so there is no need to re-fetch
            // from the network.
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            // Need to refresh cached data from network; returning
            // LAUNCH_INITIAL_REFRESH here will also block RemoteMediator's
            // APPEND and PREPEND from running until REFRESH succeeds.
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}*/
