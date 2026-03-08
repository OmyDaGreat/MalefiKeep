package xyz.malefic.malefikeep.api.workspaces.members

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.requireAuth
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.db.Users
import xyz.malefic.malefikeep.db.WorkspaceMembers
import xyz.malefic.malefikeep.db.Workspaces
import xyz.malefic.malefikeep.models.WorkspaceMember
import xyz.malefic.malefikeep.models.WorkspaceRole

@Api
suspend fun listMembers(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.GET) {
        ctx.res.status = 405
        return
    }
    val userId = ctx.requireAuth() ?: run { ctx.respondError(401, "Unauthorized"); return }
    val workspaceId = ctx.req.params["workspaceId"] ?: run { ctx.respondError(400, "Missing workspaceId"); return }

    val members =
        transaction {
            val workspace = Workspaces.selectAll().where { Workspaces.id eq workspaceId }.singleOrNull()
                ?: return@transaction null

            val isOwner = workspace[Workspaces.ownerId] == userId
            val isMember = WorkspaceMembers.selectAll()
                .where { (WorkspaceMembers.workspaceId eq workspaceId) and (WorkspaceMembers.userId eq userId) }
                .count() > 0

            if (!isOwner && !isMember) return@transaction null

            WorkspaceMembers.selectAll()
                .where { WorkspaceMembers.workspaceId eq workspaceId }
                .map { row ->
                    val username = Users.selectAll()
                        .where { Users.id eq row[WorkspaceMembers.userId] }
                        .single()[Users.username]
                    WorkspaceMember(
                        workspaceId = workspaceId,
                        userId = row[WorkspaceMembers.userId],
                        username = username,
                        role = WorkspaceRole.valueOf(row[WorkspaceMembers.role]),
                    )
                }
        } ?: run { ctx.respondError(403, "Access denied"); return }

    ctx.respondJson(200, apiJson.encodeToString(members))
}
