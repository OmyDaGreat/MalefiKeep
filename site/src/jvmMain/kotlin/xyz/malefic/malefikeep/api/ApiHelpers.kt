package xyz.malefic.malefikeep.api

import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.Body
import com.varabyte.kobweb.api.http.json
import kotlinx.serialization.json.Json
import xyz.malefic.malefikeep.auth.JwtUtils

val apiJson = Json { ignoreUnknownKeys = true }

fun ApiContext.requireAuth(): String? {
    val authHeader = req.headers["Authorization"]?.firstOrNull() ?: return null
    val token = authHeader.removePrefix("Bearer ").trim()
    return JwtUtils.extractUserId(token)
}

fun ApiContext.respondJson(
    status: Int = 200,
    body: String,
) {
    res.status = status
    res.body = Body.json(body)
}

fun ApiContext.respondError(
    status: Int,
    message: String,
) {
    val escaped = message.replace("\\", "\\\\").replace("\"", "\\\"")
    respondJson(status, """{"message":"$escaped"}""")
}
