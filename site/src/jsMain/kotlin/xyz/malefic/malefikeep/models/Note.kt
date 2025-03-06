package xyz.malefic.malefikeep.models

import kotlinx.serialization.Serializable
import kotlin.js.Date

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val color: String = "#ffffff",
    val createdAt: Long = Date.now().toLong(),
)
