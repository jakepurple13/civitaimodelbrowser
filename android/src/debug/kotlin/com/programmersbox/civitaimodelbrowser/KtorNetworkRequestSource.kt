package com.programmersbox.civitaimodelbrowser

import com.ms.square.debugoverlay.DebugOverlay
import com.ms.square.debugoverlay.NetworkRequestSource
import com.ms.square.debugoverlay.internal.InternalDebugOverlayApi
import com.ms.square.debugoverlay.model.NetworkError
import com.ms.square.debugoverlay.model.NetworkRequest
import com.programmersbox.common.KtorPluginProvider
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.dsl.module
import java.net.HttpURLConnection.HTTP_BAD_GATEWAY
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.HttpURLConnection.HTTP_UNAVAILABLE

/**
 * Default maximum body size (2MB) before truncation.
 */
public const val DEFAULT_MAX_BODY_SIZE: Long = 2 * 1024 * 1024 // 2MB

public val DEFAULT_HEADERS_REDACT: Set<String> = setOf(
    "authorization",
    "api-key",
    "x-api-key",
    "cookie",
    "set-cookie",
    "x-auth-token",
    "x-csrf-token",
    "x-session-id",
    "proxy-authorization",
    "x-access-token"
)

public val DEFAULT_QUERY_PARAMS_REDACT: Set<String> = setOf(
    "token",
    "key",
    "password"
)

private const val HTTP_CLIENT_ERROR_START = 400
private const val HTTP_SERVER_ERROR_START = 500

val debugModule = module {
    single<KtorPluginProvider> { KtorDebugOverlayPlugin() }
}

@OptIn(InternalDebugOverlayApi::class)
public class KtorDebugOverlayPlugin(
    private val maxStoredRequests: Int = 100,
    private val headersNameToRedact: Set<String> = DEFAULT_HEADERS_REDACT,
    private val queryParamsNameToRedact: Set<String> = DEFAULT_QUERY_PARAMS_REDACT,
    private val maxBodySize: Long = DEFAULT_MAX_BODY_SIZE,
) : NetworkRequestSource, KtorPluginProvider {

    private val recentRequests = mutableListOf<NetworkRequest>()
    private val _requests = MutableStateFlow<List<NetworkRequest>>(emptyList())

    init {
        DebugOverlay.configure { networkRequestSource = this@KtorDebugOverlayPlugin }
    }

    override val requests: Flow<List<NetworkRequest>> = _requests.asStateFlow()

    private val startNsKey = AttributeKey<Long>("startNs")

    private val plugin = createClientPlugin("DebugOverlayPlugin") {
        onRequest { request, content ->
            request.attributes.put(startNsKey, System.nanoTime())
        }

        onResponse { response ->
            val startNs = response.call.attributes[startNsKey]
            val tookMs = (System.nanoTime() - startNs) / 1_000_000

            val requestData = captureRequestData(response)
            val responseData = captureResponseData(response)

            addRequest(
                protocol = response.version.toString(),
                method = response.request.method.value,
                url = response.request.url.toString(),
                durationMs = tookMs,
                statusCode = response.status.value,
                requestData = requestData,
                responseData = responseData,
                error = if (response.status.value >= HTTP_CLIENT_ERROR_START) {
                    createErrorFromResponse(response.status.value, responseData.content)
                } else {
                    null
                }
            )
        }
    }

    override fun install(config: HttpClientConfig<*>) {
        config.install(plugin)
    }

    private fun captureHeaders(headers: Headers): Map<String, String> =
        headers.entries().associate { (name, values) ->
            name to if (headersNameToRedact.any { it.equals(name, ignoreCase = true) }) {
                "[REDACTED]"
            } else {
                values.joinToString(", ")
            }
        }

    private suspend fun captureRequestData(response: HttpResponse): NetworkData {
        val request = response.request
        val requestHeaders = captureHeaders(request.headers)
        val requestContentType = request.contentType()?.toString()
        val requestContentLength = request.contentLength()

        return NetworkData(
            headers = requestHeaders,
            contentType = requestContentType,
            contentSize = requestContentLength,
            content = null // Request body capturing requires more work in Ktor
        )
    }

    private suspend fun captureResponseData(response: HttpResponse): NetworkData {
        val responseHeaders = captureHeaders(response.headers)
        val responseContentType = response.contentType()?.toString()
        val responseContentLength = response.contentLength()

        val content = if (responseContentLength != null && responseContentLength > maxBodySize) {
            "N/A - [body too large]"
        } else {
            runCatching { response.bodyAsText() }.getOrNull()
        }

        return NetworkData(
            headers = responseHeaders,
            contentType = responseContentType,
            contentSize = responseContentLength,
            content = content
        )
    }

    private fun addRequest(
        protocol: String?,
        method: String,
        url: String,
        statusCode: Int? = null,
        durationMs: Long,
        requestData: NetworkData? = null,
        responseData: NetworkData? = null,
        error: NetworkError? = null,
    ) {
        val redactUrl = redactUrl(url)
        val newRequest = NetworkRequest(
            protocol = protocol,
            method = method,
            url = redactUrl,
            statusCode = statusCode,
            durationMs = durationMs,
            responseSize = responseData?.contentSize,
            requestSize = requestData?.contentSize,
            requestHeaders = requestData?.headers ?: emptyMap(),
            responseHeaders = responseData?.headers ?: emptyMap(),
            requestBody = requestData?.content,
            responseBody = responseData?.content,
            timestampMs = System.currentTimeMillis(),
            error = error
        )

        recentRequests.add(newRequest)
        if (recentRequests.size > maxStoredRequests) {
            recentRequests.removeAt(0)
        }
        _requests.update {
            recentRequests.toList()
        }
    }

    private fun redactUrl(urlStr: String): String {
        // Simple redaction logic for URL string
        var redactedUrl = urlStr
        queryParamsNameToRedact.forEach { param ->
            val regex = Regex("([?&]$param=)[^&]*", RegexOption.IGNORE_CASE)
            redactedUrl = redactedUrl.replace(regex, "$1[REDACTED]")
        }
        return redactedUrl
    }
}

private fun createErrorFromResponse(statusCode: Int, body: String?): NetworkError {
    val statusMessage = when (statusCode) {
        HTTP_BAD_REQUEST -> "Bad Request"
        HTTP_UNAUTHORIZED -> "Unauthorized"
        HTTP_FORBIDDEN -> "Forbidden"
        HTTP_NOT_FOUND -> "Not Found"
        HTTP_INTERNAL_ERROR -> "Internal Server Error"
        HTTP_BAD_GATEWAY -> "Bad Gateway"
        HTTP_UNAVAILABLE -> "Service Unavailable"
        else -> "Error $statusCode"
    }

    return NetworkError(
        title = "$statusCode $statusMessage",
        message = when (statusCode) {
            in 400..499 -> "Client error: The request was invalid or cannot be served."
            in 500..599 -> "Server error: The server failed to fulfill a valid request."
            else -> "Request failed with status $statusCode"
        },
        stackTrace = body
    )
}

private data class NetworkData(
    val headers: Map<String, String>,
    val contentType: String?,
    val contentSize: Long?,
    val content: String?,
)