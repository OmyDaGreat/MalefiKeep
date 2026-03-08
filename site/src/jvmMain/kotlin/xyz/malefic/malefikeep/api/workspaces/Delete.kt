package xyz.malefic.malefikeep.api.workspaces

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.requireAuth
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.db.Notes
import xyz.malefic.malefikeep.db.WorkspaceMembers
import xyz.malefic.malefikeep.db.Workspaces
import xyz.malefic.malefikeep.models.SuccessResponse

@Api
suspend fun deleteWorkspace(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.DELETE) {
        ctx.res.status = 405
        return
    }
    val userId = ctx.requireAuth() ?: run { ctx.respondError(401, "Unauthorized"); return }
    val workspaceId = ctx.req.params["id"] ?: run { ctx.respondError(400, "Missing id parameter"); return }

    transaction {
        val workspace =
            Workspaces
                .selectAll()
                .where { Workspaces.id eq workspaceId }
                .singleOrNull()

        if (workspace == null) {
            ctx.respondError(404, "Workspace not found")
            return@transaction
        }
        if (workspace[Workspaces.ownerId] != userId) {
            ctx.respondError(403, "Only the owner can delete a workspace")
            return@transaction
        }

        Notes.deleteWhere { Notes.workspaceId eq workspaceId }
        WorkspaceMembers.deleteWhere { WorkspaceMembers.workspaceId eq workspaceId }
        Workspaces.deleteWhere { Workspaces.id eq workspaceId }
        ctx.respondJson(200, apiJson.encodeToString(SuccessResponse("Workspace deleted")))
    }
}
