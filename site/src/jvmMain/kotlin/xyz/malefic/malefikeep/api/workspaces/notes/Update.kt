package xyz.malefic.malefikeep.api.workspaces.notes

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.text
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.requireAuth
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.db.Notes
import xyz.malefic.malefikeep.db.WorkspaceMembers
import xyz.malefic.malefikeep.db.Workspaces
import xyz.malefic.malefikeep.models.SuccessResponse
import xyz.malefic.malefikeep.models.UpdateNoteRequest

@Api
suspend fun updateNote(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.PUT) {
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
        runCatching { apiJson.decodeFromString<UpdateNoteRequest>(bodyText) }
            .getOrElse {
                ctx.respondError(400, "Invalid request body")
                return
            }

    transaction {
        val workspace =
            Workspaces.selectAll().where { Workspaces.id eq request.workspaceId }.singleOrNull()
                ?: return@transaction ctx.respondError(404, "Workspace not found")

        val isOwner = workspace[Workspaces.ownerId] == userId
        val member =
            WorkspaceMembers
                .selectAll()
                .where { (WorkspaceMembers.workspaceId eq request.workspaceId) and (WorkspaceMembers.userId eq userId) }
                .singleOrNull()
        val canWrite = isOwner || member?.get(WorkspaceMembers.role) == "READ_WRITE"
        if (!canWrite) return@transaction ctx.respondError(403, "Write permission required")

        val updated =
            Notes.update({ Notes.id eq request.id }) {
                it[title] = request.title
                it[content] = request.content
                it[color] = request.color
                it[updatedAt] = System.currentTimeMillis()
            }
        if (updated == 0) {
            ctx.respondError(404, "Note not found")
        } else {
            ctx.respondJson(200, apiJson.encodeToString(SuccessResponse("Note updated")))
        }
    }
}
