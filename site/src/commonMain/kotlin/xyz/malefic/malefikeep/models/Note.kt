package xyz.malefic.malefikeep.models

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String = "",
    val workspaceId: String = "",
    val title: String = "",
    val content: String = "",
    val color: String = "#ffffff",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
