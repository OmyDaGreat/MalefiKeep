package xyz.malefic.malefikeep.api.workspaces

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.requireAuth
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.db.Users
import xyz.malefic.malefikeep.db.WorkspaceMembers
import xyz.malefic.malefikeep.db.Workspaces
import xyz.malefic.malefikeep.models.Workspace

@Api
suspend fun listWorkspaces(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.GET) {
        ctx.res.status = 405
        return
    }
    val userId = ctx.requireAuth() ?: run { ctx.respondError(401, "Unauthorized"); return }

    val workspaces =
        transaction {
            val ownedIds = Workspaces.selectAll().where { Workspaces.ownerId eq userId }.map { it[Workspaces.id] }
            val memberIds =
                WorkspaceMembers
                    .selectAll()
                    .where { WorkspaceMembers.userId eq userId }
                    .map { it[WorkspaceMembers.workspaceId] }
            val allIds = (ownedIds + memberIds).distinct()

            Workspaces
                .selectAll()
                .where { Workspaces.id inList allIds }
                .map { row ->
                    val ownerUsername =
                        Users
                            .selectAll()
                            .where { Users.id eq row[Workspaces.ownerId] }
                            .single()[Users.username]
                    Workspace(
                        id = row[Workspaces.id],
                        name = row[Workspaces.name],
                        ownerId = row[Workspaces.ownerId],
                        ownerUsername = ownerUsername,
                        createdAt = row[Workspaces.createdAt],
                    )
                }
        }

    ctx.respondJson(200, apiJson.encodeToString(workspaces))
}
