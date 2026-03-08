package xyz.malefic.malefikeep.api.auth

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.text
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.db.RefreshTokens
import xyz.malefic.malefikeep.models.LogoutRequest
import xyz.malefic.malefikeep.models.SuccessResponse

@Api
suspend fun logout(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.DELETE) {
        ctx.res.status = 405
        return
    }

    val bodyText = ctx.req.body?.text() ?: run { ctx.respondError(400, "Missing request body"); return }
    val request =
        runCatching { apiJson.decodeFromString<LogoutRequest>(bodyText) }
            .getOrElse { ctx.respondError(400, "Invalid request body"); return }

    transaction {
        RefreshTokens.deleteWhere { RefreshTokens.id eq request.refreshToken }
    }

    ctx.respondJson(200, apiJson.encodeToString(SuccessResponse("Logged out")))
}
