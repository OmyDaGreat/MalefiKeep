package xyz.malefic.malefikeep.api.workspaces

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
import xyz.malefic.malefikeep.api.requireAuth
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.db.Users
import xyz.malefic.malefikeep.db.Workspaces
import xyz.malefic.malefikeep.models.CreateWorkspaceRequest
import xyz.malefic.malefikeep.models.Workspace
import java.util.UUID

@Api
suspend fun createWorkspace(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.POST) {
        ctx.res.status = 405
        return
    }
    val userId =
        ctx.requireAuth() ?: run {
            ctx.respondError(401, "Unauthorized")
            return
        }

    val bodyText =
        ctx.req.body?.text() ?: run {
            ctx.respondError(400, "Missing request body")
            return
        }
    val request =
        runCatching { apiJson.decodeFromString<CreateWorkspaceRequest>(bodyText) }
            .getOrElse {
                ctx.respondError(400, "Invalid request body")
                return
            }

    if (request.name.isBlank()) {
        ctx.respondError(400, "Workspace name is required")
        return
    }

    val workspace =
        transaction {
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            Workspaces.insert {
                it[Workspaces.id] = id
                it[name] = request.name
                it[ownerId] = userId
                it[createdAt] = now
            }
            val ownerUsername = Users.selectAll().where { Users.id eq userId }.single()[Users.username]
            Workspace(id = id, name = request.name, ownerId = userId, ownerUsername = ownerUsername, createdAt = now)
        }

    ctx.respondJson(201, apiJson.encodeToString(workspace))
}
