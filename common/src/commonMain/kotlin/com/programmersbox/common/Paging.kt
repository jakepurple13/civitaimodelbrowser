package com.programmersbox.common

import androidx.paging.PagingSource
import androidx.paging.PagingState

class CivitBrowserPagingSource(
    private val network: Network,
    private val perPage: Int = 20,
    private val includeNsfw: Boolean = true,
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
            perPage = perPage,
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
