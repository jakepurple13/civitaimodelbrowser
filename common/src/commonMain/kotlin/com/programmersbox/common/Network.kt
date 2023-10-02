package com.programmersbox.common

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

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

    suspend fun getModels(page: Int, perPage: Int = 20) = runCatching {
        client.get("models?page=$page&sort=Newest&limit=$perPage&nsfw=true") {
            contentType(ContentType.Application.Json)
        }.body<CivitAi>()
    }
}