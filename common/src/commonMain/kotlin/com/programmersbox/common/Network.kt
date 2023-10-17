package com.programmersbox.common

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

const val PAGE_LIMIT = 20

class Network {
    companion object {
        private const val URL = "https://civitai.com/api/v1/"
    }

    private val json: Json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
        defaultRequest {
            url(URL)
        }
    }

    suspend fun getModels(
        page: Int,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("models?page=$page&sort=Newest&limit=$perPage&nsfw=$includeNsfw") {
            contentType(ContentType.Application.Json)
        }.body<CivitAi>()
    }

    suspend fun getModels(
        page: Int,
        creatorUsername: String,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("models?page=$page&sort=Newest&limit=$perPage&nsfw=$includeNsfw&username=$creatorUsername") {
            contentType(ContentType.Application.Json)
        }.body<CivitAi>()
    }

    suspend fun searchModels(
        page: Int,
        searchQuery: String,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,
    ) = runCatching {
        client.get("models?page=$page&sort=Newest&limit=$perPage&nsfw=$includeNsfw&query=$searchQuery".encodeURLQueryComponent()) {
            contentType(ContentType.Application.Json)
        }.body<CivitAi>()
    }

    suspend fun fetchModel(id: String) = runCatching {
        client.get("models/$id") {
            contentType(ContentType.Application.Json)
        }.body<Models>()
    }

    suspend fun fetchAllImagesByModel(
        modelId: String,
        page: Int,
        perPage: Int = PAGE_LIMIT,
        includeNsfw: Boolean = true,

        ) = runCatching {
        client.get("images?limit=$perPage&page=$page&modelId=$modelId${if (!includeNsfw) "&nsfw=None" else ""}") {
            contentType(ContentType.Application.Json)
        }.body<CivitAiCustomImages>().items
    }
}