package xyz.malefic.malefikeep.api.workspaces.members

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
import xyz.malefic.malefikeep.db.Users
import xyz.malefic.malefikeep.db.WorkspaceMembers
import xyz.malefic.malefikeep.db.Workspaces
import xyz.malefic.malefikeep.models.AddMemberRequest
import xyz.malefic.malefikeep.models.WorkspaceMember
import xyz.malefic.malefikeep.models.WorkspaceRole

@Api
suspend fun addMember(ctx: ApiContext) {
    if (ctx.req.method != HttpMethod.POST) {
        ctx.res.status = 405
        return
    }
    val userId =
        ctx.requireAuth() ?: run {
            ctx.respondError(401, "Unauthorized")
            return
        }

    val bodyText = ctx.req.body?.text() ?: run { ctx.respondError(400, "Missing request body"); return }
    val request =
        runCatching { apiJson.decodeFromString<AddMemberRequest>(bodyText) }.getOrElse {
            ctx.respondError(400, "Invalid request body")
            return
        }

    val member =
        transaction {
            val workspace =
                Workspaces.selectAll().where { Workspaces.id eq request.workspaceId }.singleOrNull()
                    ?: return@transaction null to "Workspace not found"
            if (workspace[Workspaces.ownerId] != userId) return@transaction null to "Only the owner can add members"

            val targetUser =
                Users.selectAll().where { Users.username eq request.username }.singleOrNull()
                    ?: return@transaction null to "User '${request.username}' not found"

            val targetId = targetUser[Users.id]
            if (targetId == userId) return@transaction null to "Cannot add yourself as a member"

            val alreadyMember =
                WorkspaceMembers
                    .selectAll()
                    .where { (WorkspaceMembers.workspaceId eq request.workspaceId) and (WorkspaceMembers.userId eq targetId) }
                    .count() > 0
            if (alreadyMember) return@transaction null to "User is already a member"

            WorkspaceMembers.insert {
                it[workspaceId] = request.workspaceId
                it[this.userId] = targetId
                it[role] = request.role.name
            }
            WorkspaceMember(
                workspaceId = request.workspaceId,
                userId = targetId,
                username = request.username,
                role = request.role,
            ) to null
        }

    val (result, error) = member
    if (error != null) {
        ctx.respondError(if (error.contains("not found", ignoreCase = true)) 404 else 403, error)
        return
    }
    ctx.respondJson(201, apiJson.encodeToString(result!!))
}
