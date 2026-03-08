package xyz.malefic.malefikeep.models

import kotlinx.serialization.Serializable

@Serializable
enum class WorkspaceRole { READ_ONLY, READ_WRITE }

@Serializable
data class Workspace(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val ownerUsername: String = "",
    val createdAt: Long = 0L,
)

@Serializable
data class WorkspaceMember(
    val workspaceId: String = "",
    val userId: String = "",
    val username: String = "",
    val role: WorkspaceRole = WorkspaceRole.READ_ONLY,
)
