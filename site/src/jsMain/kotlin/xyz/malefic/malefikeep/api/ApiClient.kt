package xyz.malefic.malefikeep.api

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import org.w3c.dom.get

val clientJson = Json { ignoreUnknownKeys = true }

private fun authHeaders(includeBody: Boolean = false): dynamic {
    val headers: dynamic = js("{}")
    if (includeBody) headers["Content-Type"] = "application/json"
    val token = localStorage["auth-token"]
    if (token != null) headers["Authorization"] = "Bearer $token"
    return headers
}

private fun buildInit(method: String, body: String? = null): RequestInit {
    val init: dynamic = js("{}")
    init.method = method
    init.headers = authHeaders(includeBody = body != null)
    if (body != null) init.body = body
    return init.unsafeCast<RequestInit>()
}

suspend fun apiGet(path: String): Result<String> =
    runCatching {
        val response = window.fetch(path, buildInit("GET")).await()
        if (!response.ok) error("HTTP ${response.status}")
        response.text().await()
    }

suspend fun apiPost(path: String, body: String): Result<String> =
    runCatching {
        val response = window.fetch(path, buildInit("POST", body)).await()
        if (!response.ok) {
            val errBody = response.text().await()
            error("HTTP ${response.status}: $errBody")
        }
        response.text().await()
    }

suspend fun apiPut(path: String, body: String): Result<String> =
    runCatching {
        val response = window.fetch(path, buildInit("PUT", body)).await()
        if (!response.ok) {
            val errBody = response.text().await()
            error("HTTP ${response.status}: $errBody")
        }
        response.text().await()
    }

suspend fun apiDelete(path: String): Result<String> =
    runCatching {
        val response = window.fetch(path, buildInit("DELETE")).await()
        if (!response.ok) error("HTTP ${response.status}")
        response.text().await()
    }

inline fun <reified T> Result<String>.decodeOrNull(): T? =
    getOrNull()?.let { runCatching { clientJson.decodeFromString<T>(it) }.getOrNull() }
