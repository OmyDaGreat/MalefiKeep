package xyz.malefic.malefikeep.api.auth

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.text
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.auth.JwtUtils
import xyz.malefic.malefikeep.auth.PasswordUtils
import xyz.malefic.malefikeep.db.Users
import xyz.malefic.malefikeep.models.AuthResponse
import xyz.malefic.malefikeep.models.RegisterRequest
import java.util.UUID

@Api
suspend fun register(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.POST) {
        ctx.res.status = 405
        return
    }

    val bodyText = ctx.req.body?.text() ?: run { ctx.respondError(400, "Missing request body"); return }
    val request =
        runCatching { apiJson.decodeFromString<RegisterRequest>(bodyText) }
            .getOrElse { ctx.respondError(400, "Invalid request body"); return }

    if (request.username.isBlank() || request.email.isBlank() || request.password.length < 8) {
        ctx.respondError(400, "Username and email are required; password must be at least 8 characters")
        return
    }

    val (userId, token) =
        transaction {
            val existing = Users.selectAll().where { Users.email eq request.email }.singleOrNull()
            if (existing != null) return@transaction null
            val id = UUID.randomUUID().toString()
            Users.insert {
                it[Users.id] = id
                it[username] = request.username
                it[email] = request.email
                it[passwordHash] = PasswordUtils.hash(request.password)
                it[createdAt] = System.currentTimeMillis()
            }
            id to JwtUtils.generateToken(id, request.username)
        } ?: run { ctx.respondError(409, "Email already registered"); return }

    ctx.respondJson(201, apiJson.encodeToString(AuthResponse(token, userId, request.username)))
}
