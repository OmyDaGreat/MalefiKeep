package xyz.malefic.malefikeep.api

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.fetch.RequestInit
import xyz.malefic.malefikeep.models.AuthResponse
import xyz.malefic.malefikeep.models.RefreshRequest

val clientJson = Json { ignoreUnknownKeys = true }

private fun authHeaders(includeBody: Boolean = false): dynamic {
    val headers: dynamic = js("{}")
    if (includeBody) headers["Content-Type"] = "application/json"
    val token = localStorage["auth-token"]
    if (token != null) headers["Authorization"] = "Bearer $token"
    return headers
}

private fun buildInit(
    method: String,
    body: String? = null,
): RequestInit {
    val init: dynamic = js("{}")
    init.method = method
    init.headers = authHeaders(includeBody = body != null)
    if (body != null) init.body = body
    return init.unsafeCast<RequestInit>()
}

private suspend fun tryRefresh(): Boolean {
    val refreshToken = localStorage["auth-refresh-token"] ?: return false
    return runCatching {
        val body = clientJson.encodeToString(RefreshRequest(refreshToken))
        val init: dynamic = js("{}")
        init.method = "POST"
        val headers: dynamic = js("{}")
        headers["Content-Type"] = "application/json"
        init.headers = headers
        init.body = body
        val response = window.fetch("/api/auth/refresh", init.unsafeCast<RequestInit>()).await()
        if (!response.ok) return false
        val auth = clientJson.decodeFromString<AuthResponse>(response.text().await())
        localStorage["auth-token"] = auth.token
        if (auth.accessTokenExpiresAt != null) localStorage["auth-token-expires"] = auth.accessTokenExpiresAt.toString()
        true
    }.getOrDefault(false)
}

fun clearAuth() {
    localStorage.removeItem("auth-token")
    localStorage.removeItem("auth-user-id")
    localStorage.removeItem("auth-username")
    localStorage.removeItem("auth-refresh-token")
    localStorage.removeItem("auth-token-expires")
}

private suspend fun fetchWithRefresh(
    path: String,
    method: String,
    body: String? = null,
): Result<String> =
    runCatching {
        val response = window.fetch(path, buildInit(method, body)).await()
        if (response.status.toInt() == 401 && tryRefresh()) {
            val retried = window.fetch(path, buildInit(method, body)).await()
            if (!retried.ok) {
                if (retried.status.toInt() == 401) {
                    clearAuth()
                    window.location.href = "/login"
                }
                error("HTTP ${retried.status}: ${retried.text().await()}")
            }
            retried.text().await()
        } else {
            if (!response.ok) error("HTTP ${response.status}: ${response.text().await()}")
            response.text().await()
        }
    }

suspend fun apiGet(path: String): Result<String> = fetchWithRefresh(path, "GET")

suspend fun apiPost(
    path: String,
    body: String,
): Result<String> = fetchWithRefresh(path, "POST", body)

suspend fun apiPut(
    path: String,
    body: String,
): Result<String> = fetchWithRefresh(path, "PUT", body)

suspend fun apiDelete(
    path: String,
    body: String? = null,
): Result<String> = fetchWithRefresh(path, "DELETE", body)

inline fun <reified T> Result<String>.decodeOrNull(): T? =
    getOrNull()?.let { runCatching { clientJson.decodeFromString<T>(it) }.getOrNull() }
