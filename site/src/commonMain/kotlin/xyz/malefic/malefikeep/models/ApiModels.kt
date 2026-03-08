package xyz.malefic.malefikeep.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
)

@Serializable
data class CreateWorkspaceRequest(
    val name: String,
)

@Serializable
data class AddMemberRequest(
    val workspaceId: String,
    val username: String,
    val role: WorkspaceRole,
)

@Serializable
data class UpdateMemberRoleRequest(
    val workspaceId: String,
    val userId: String,
    val role: WorkspaceRole,
)

@Serializable
data class CreateNoteRequest(
    val workspaceId: String,
    val title: String = "",
    val content: String,
    val color: String = "#ffffff",
)

@Serializable
data class UpdateNoteRequest(
    val id: String,
    val workspaceId: String,
    val title: String = "",
    val content: String,
    val color: String = "#ffffff",
)

@Serializable
data class ErrorResponse(val message: String)

@Serializable
data class SuccessResponse(val message: String)
