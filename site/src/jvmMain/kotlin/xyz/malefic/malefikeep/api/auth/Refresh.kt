package xyz.malefic.malefikeep.api.auth

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.text
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.auth.JwtUtils
import xyz.malefic.malefikeep.db.RefreshTokens
import xyz.malefic.malefikeep.db.Users
import xyz.malefic.malefikeep.models.AuthResponse
import xyz.malefic.malefikeep.models.RefreshRequest

@Api
suspend fun refresh(ctx: ApiContext) {
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
        runCatching { apiJson.decodeFromString<RefreshRequest>(bodyText) }
            .getOrElse {
                ctx.respondError(400, "Invalid request body")
                return
            }

    val now = System.currentTimeMillis()

    val (userId, username) =
        transaction {
            val row =
                RefreshTokens
                    .selectAll()
                    .where { RefreshTokens.id eq request.refreshToken }
                    .singleOrNull()
                    ?: return@transaction null

            if (row[RefreshTokens.expiresAt] < now) return@transaction null

            val user =
                Users.selectAll().where { Users.id eq row[RefreshTokens.userId] }.singleOrNull()
                    ?: return@transaction null

            user[Users.id] to user[Users.username]
        } ?: run {
            ctx.respondError(401, "Invalid or expired refresh token")
            return
        }

    val newToken = JwtUtils.generateToken(userId, username)
    val expiresAt = now + 3 * 24 * 60 * 60 * 1000L

    ctx.respondJson(200, apiJson.encodeToString(AuthResponse(newToken, userId, username, request.refreshToken, expiresAt)))
}
