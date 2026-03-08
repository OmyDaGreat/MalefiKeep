package xyz.malefic.malefikeep.db

import org.jetbrains.exposed.v1.core.Table

object Users : Table("users") {
    val id = varchar("id", 36)
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = text("password_hash")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object Workspaces : Table("workspaces") {
    val id = varchar("id", 36)
    val name = varchar("name", 100)
    val ownerId = varchar("owner_id", 36).references(Users.id)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object WorkspaceMembers : Table("workspace_members") {
    val workspaceId = varchar("workspace_id", 36).references(Workspaces.id)
    val userId = varchar("user_id", 36).references(Users.id)
    val role = varchar("role", 20) // "READ_ONLY" | "READ_WRITE"
    override val primaryKey = PrimaryKey(workspaceId, userId)
}

object Notes : Table("notes") {
    val id = varchar("id", 36)
    val workspaceId = varchar("workspace_id", 36).references(Workspaces.id)
    val title = varchar("title", 255).default("")
    val content = text("content")
    val color = varchar("color", 20).default("#ffffff")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}
