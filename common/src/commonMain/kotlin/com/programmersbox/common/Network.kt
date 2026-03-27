package com.programmersbox.common

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json

const val PAGE_LIMIT = 20

interface KtorPluginProvider {
    fun install(config: HttpClientConfig<*>)
}

class Network(
    dataStore: DataStore,
    vararg plugins: KtorPluginProvider,
) {
    private val json: Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private var apiToken: String? = null

    init {
        dataStore
            .apiToken
            .flow
            .onEach { apiToken = it }
            .launchIn(CoroutineScope(Dispatchers.IO))
    }

    val client: HttpClient by lazy {
        HttpClient {
            plugins.forEach {
                println("Loading in plugin: ${it::class.simpleName}")
                it.install(this)
            }
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 2)
                retryOnException(maxRetries = 2, retryOnTimeout = true)
                exponentialDelay(baseDelayMs = 1000L)
            }
            defaultRequest {
                url(Consts.URL)
                apiToken
                    ?.takeIf { it.isNotBlank() }
                    ?.let { bearerAuth(it) }
                contentType(ContentType.Application.Json)
            }
        }
    }

    suspend inline fun <reified T> fetchRequest(url: String) = runCatching {
        client.get(url).body<T>()
    }

    suspend fun getModels(
        page: Int,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
        sort: CivitSort = CivitSort.Newest,
    ) = runCatching {
        client.get("models") {
            parameter("page", page)
            parameter("sort", sort.value)
            parameter("limit", perPage)
            parameter("nsfw", includeNsfw)
        }.body<CivitAi>()
    }

    suspend fun getModels(
        page: Int,
        creatorUsername: String,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("models") {
            parameter("page", page)
            parameter("username", creatorUsername)
            parameter("limit", perPage)
            parameter("nsfw", includeNsfw)
        }.body<CivitAi>()
    }

    suspend fun searchModels(
        page: Int,
        searchQuery: String,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("models") {
            parameter("query", searchQuery)
            parameter("limit", perPage)
            parameter("nsfw", includeNsfw)
        }.body<CivitAi>()
    }

    suspend fun fetchModel(id: String) = runCatching {
        client.get("models/$id")
            .body<Models>()
    }

    suspend fun fetchAllImagesByModel(
        modelId: String,
        page: Int,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("images") {
            parameter("limit", perPage)
            parameter("page", page)
            parameter("modelId", modelId)
            parameter("sort", "Newest")
            if (!includeNsfw) parameter("nsfw", "None")
        }.body<CivitAiCustomImages>()
    }

    suspend fun fetchAllImagesByModelVersion(
        modelId: String,
        page: Int,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("images") {
            parameter("limit", perPage)
            parameter("page", page)
            parameter("modelVersionId", modelId)
            parameter("sort", "Newest")
            if (!includeNsfw) parameter("nsfw", "None")
        }.body<CivitAiCustomImages>()
    }

    suspend fun fetchAllImages(
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("images") {
            parameter("nsfw", if (includeNsfw) includeNsfw else "None")
            parameter("sort", "Newest")
        }.body<CivitAiCustomImages>()
    }
}