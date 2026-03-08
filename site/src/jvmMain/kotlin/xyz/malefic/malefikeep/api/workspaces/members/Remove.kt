package xyz.malefic.malefikeep.api.workspaces.members

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.requireAuth
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.db.WorkspaceMembers
import xyz.malefic.malefikeep.db.Workspaces
import xyz.malefic.malefikeep.models.SuccessResponse

@Api
suspend fun removeMember(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.DELETE) {
        ctx.res.status = 405
        return
    }
    val userId = ctx.requireAuth() ?: run { ctx.respondError(401, "Unauthorized"); return }
    val workspaceId = ctx.req.params["workspaceId"] ?: run { ctx.respondError(400, "Missing workspaceId"); return }
    val targetUserId = ctx.req.params["userId"] ?: run { ctx.respondError(400, "Missing userId"); return }

    transaction {
        val workspace = Workspaces.selectAll().where { Workspaces.id eq workspaceId }.singleOrNull()
            ?: return@transaction ctx.respondError(404, "Workspace not found")

        val isSelfRemoval = targetUserId == userId
        val isOwner = workspace[Workspaces.ownerId] == userId
        if (!isOwner && !isSelfRemoval) return@transaction ctx.respondError(403, "Only the owner can remove members")

        WorkspaceMembers.deleteWhere {
            (WorkspaceMembers.workspaceId eq workspaceId) and (WorkspaceMembers.userId eq targetUserId)
        }
        ctx.respondJson(200, apiJson.encodeToString(SuccessResponse("Member removed")))
    }
}
