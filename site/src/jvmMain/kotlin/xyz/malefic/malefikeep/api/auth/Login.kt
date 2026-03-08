package xyz.malefic.malefikeep.api.auth

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.text
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.auth.JwtUtils
import xyz.malefic.malefikeep.auth.PasswordUtils
import xyz.malefic.malefikeep.db.RefreshTokens
import xyz.malefic.malefikeep.db.Users
import xyz.malefic.malefikeep.models.AuthResponse
import xyz.malefic.malefikeep.models.LoginRequest

@Api
suspend fun login(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.POST) {
        ctx.res.status = 405
        return
    }

    val bodyText =
        ctx.req.body?.text() ?: run {
            ctx.respondError(400, "Missing request body")
            return
        }
    val request =
        runCatching { apiJson.decodeFromString<LoginRequest>(bodyText) }
            .getOrElse {
                ctx.respondError(400, "Invalid request body")
                return
            }

    val user =
        transaction {
            Users.selectAll().where { Users.email eq request.email }.singleOrNull()
        } ?: run {
            ctx.respondError(401, "Invalid credentials")
            return
        }

    if (!PasswordUtils.verify(request.password, user[Users.passwordHash])) {
        ctx.respondError(401, "Invalid credentials")
        return
    }

    val userId = user[Users.id]
    val username = user[Users.username]
    val now = System.currentTimeMillis()
    val token = JwtUtils.generateToken(userId, username)
    val expiresAt = now + 3 * 24 * 60 * 60 * 1000L

    val refreshToken =
        if (request.rememberMe) {
            val rt = JwtUtils.generateRefreshToken()
            transaction {
                RefreshTokens.insert {
                    it[id] = rt
                    it[RefreshTokens.userId] = userId
                    it[RefreshTokens.expiresAt] = now + JwtUtils.REFRESH_TOKEN_EXPIRY_MS
                    it[createdAt] = now
                }
            }
            rt
        } else {
            null
        }

    ctx.respondJson(200, apiJson.encodeToString(AuthResponse(token, userId, username, refreshToken, expiresAt)))
}
