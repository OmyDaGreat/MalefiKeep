package xyz.malefic.malefikeep.api.workspaces.notes

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.text
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.malefic.malefikeep.api.apiJson
import xyz.malefic.malefikeep.api.requireAuth
import xyz.malefic.malefikeep.api.respondError
import xyz.malefic.malefikeep.api.respondJson
import xyz.malefic.malefikeep.db.Notes
import xyz.malefic.malefikeep.db.WorkspaceMembers
import xyz.malefic.malefikeep.db.Workspaces
import xyz.malefic.malefikeep.models.CreateNoteRequest
import xyz.malefic.malefikeep.models.Note
import java.util.UUID

@Api
suspend fun createNote(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.POST) {
        ctx.res.status = 405
        return
    }
    val userId = ctx.requireAuth() ?: run { ctx.respondError(401, "Unauthorized"); return }

    val bodyText = ctx.req.body?.text() ?: run { ctx.respondError(400, "Missing request body"); return }
    val request =
        runCatching { apiJson.decodeFromString<CreateNoteRequest>(bodyText) }
            .getOrElse { ctx.respondError(400, "Invalid request body"); return }

    if (request.content.isBlank()) {
        ctx.respondError(400, "Note content is required")
        return
    }

    val note =
        transaction {
            val workspace = Workspaces.selectAll().where { Workspaces.id eq request.workspaceId }.singleOrNull()
                ?: return@transaction null to "Workspace not found"

            val isOwner = workspace[Workspaces.ownerId] == userId
            val member = WorkspaceMembers.selectAll()
                .where { (WorkspaceMembers.workspaceId eq request.workspaceId) and (WorkspaceMembers.userId eq userId) }
                .singleOrNull()
            val canWrite = isOwner || member?.get(WorkspaceMembers.role) == "READ_WRITE"
            if (!canWrite) return@transaction null to "Write permission required"

            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            Notes.insert {
                it[Notes.id] = id
                it[Notes.workspaceId] = request.workspaceId
                it[title] = request.title
                it[content] = request.content
                it[color] = request.color
                it[createdAt] = now
                it[updatedAt] = now
            }
            Note(
                id = id,
                workspaceId = request.workspaceId,
                title = request.title,
                content = request.content,
                color = request.color,
                createdAt = now,
                updatedAt = now,
            ) to null
        }

    val (result, error) = note
    if (error != null) {
        ctx.respondError(if (error.contains("not found", ignoreCase = true)) 404 else 403, error)
        return
    }
    ctx.respondJson(201, apiJson.encodeToString(result!!))
}
