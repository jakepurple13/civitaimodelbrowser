package com.programmersbox.common

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLQueryComponent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent

const val PAGE_LIMIT = 20

interface KtorPluginProvider {
    fun install(config: HttpClientConfig<*>)
}

class Network : KoinComponent {
    companion object {
        private const val URL = "https://civitai.com/api/v1/"
        const val CIVITAI_MODELS_URL = "https://civitai.com/models/"
    }

    private val json: Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    val client: HttpClient by lazy {
        HttpClient {
            getKoin().getAll<KtorPluginProvider>().forEach { it.install(this) }
            install(ContentNegotiation) { json(json) }
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
            }
            defaultRequest {
                url(URL)
                bearerAuth(BuildKonfig.API_KEY) //Token goes here!
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
        client.get("models?page=$page&sort=${sort.value}&limit=$perPage&nsfw=$includeNsfw")
            .body<CivitAi>()
    }

    suspend fun getModels(
        page: Int,
        creatorUsername: String,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("models?page=$page&sort=Newest&limit=$perPage&nsfw=$includeNsfw&username=$creatorUsername")
            .body<CivitAi>()
    }

    suspend fun searchModels(
        page: Int,
        searchQuery: String,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("models?&sort=Newest&limit=$perPage&nsfw=$includeNsfw&query=$searchQuery".encodeURLQueryComponent())
            .body<CivitAi>()
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
        client.get("images?limit=$perPage&page=$page&modelId=$modelId${if (!includeNsfw) "&nsfw=None" else ""}")
            .body<CivitAiCustomImages>()
    }

    suspend fun fetchAllImages(
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("images?nsfw=$includeNsfw&sort=Newest")
            .body<CivitAiCustomImages>()
    }
}