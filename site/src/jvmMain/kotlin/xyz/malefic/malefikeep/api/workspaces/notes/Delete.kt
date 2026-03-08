package xyz.malefic.malefikeep.api.workspaces.notes

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
import xyz.malefic.malefikeep.db.Notes
import xyz.malefic.malefikeep.db.WorkspaceMembers
import xyz.malefic.malefikeep.db.Workspaces
import xyz.malefic.malefikeep.models.SuccessResponse

@Api
suspend fun deleteNote(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.DELETE) {
        ctx.res.status = 405
        return
    }
    val userId = ctx.requireAuth() ?: run { ctx.respondError(401, "Unauthorized"); return }
    val noteId = ctx.req.params["id"] ?: run { ctx.respondError(400, "Missing id parameter"); return }
    val workspaceId = ctx.req.params["workspaceId"] ?: run { ctx.respondError(400, "Missing workspaceId parameter"); return }

    transaction {
        val workspace = Workspaces.selectAll().where { Workspaces.id eq workspaceId }.singleOrNull()
            ?: return@transaction ctx.respondError(404, "Workspace not found")

        val isOwner = workspace[Workspaces.ownerId] == userId
        val member = WorkspaceMembers.selectAll()
            .where { (WorkspaceMembers.workspaceId eq workspaceId) and (WorkspaceMembers.userId eq userId) }
            .singleOrNull()
        val canWrite = isOwner || member?.get(WorkspaceMembers.role) == "READ_WRITE"
        if (!canWrite) return@transaction ctx.respondError(403, "Write permission required")

        Notes.deleteWhere { Notes.id eq noteId }
        ctx.respondJson(200, apiJson.encodeToString(SuccessResponse("Note deleted")))
    }
}
