package xyz.malefic.malefikeep.api.workspaces.notes

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
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
import xyz.malefic.malefikeep.models.Note

@Api
suspend fun listNotes(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.GET) {
        ctx.res.status = 405
        return
    }
    val userId = ctx.requireAuth() ?: run { ctx.respondError(401, "Unauthorized"); return }
    val workspaceId = ctx.req.params["workspaceId"] ?: run { ctx.respondError(400, "Missing workspaceId"); return }

    val notes =
        transaction {
            val workspace = Workspaces.selectAll().where { Workspaces.id eq workspaceId }.singleOrNull()
                ?: return@transaction null

            val isOwner = workspace[Workspaces.ownerId] == userId
            val isMember = WorkspaceMembers.selectAll()
                .where { (WorkspaceMembers.workspaceId eq workspaceId) and (WorkspaceMembers.userId eq userId) }
                .count() > 0

            if (!isOwner && !isMember) return@transaction null

            Notes.selectAll()
                .where { Notes.workspaceId eq workspaceId }
                .orderBy(Notes.createdAt to SortOrder.DESC)
                .map { row ->
                    Note(
                        id = row[Notes.id],
                        workspaceId = row[Notes.workspaceId],
                        title = row[Notes.title],
                        content = row[Notes.content],
                        color = row[Notes.color],
                        createdAt = row[Notes.createdAt],
                        updatedAt = row[Notes.updatedAt],
                    )
                }
        } ?: run { ctx.respondError(403, "Access denied"); return }

    ctx.respondJson(200, apiJson.encodeToString(notes))
}
