package xyz.malefic.malefikeep.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.jetbrains.compose.web.css.px
import org.w3c.dom.get
import xyz.malefic.malefikeep.api.apiDelete
import xyz.malefic.malefikeep.api.apiGet
import xyz.malefic.malefikeep.api.apiPost
import xyz.malefic.malefikeep.api.clientJson
import xyz.malefic.malefikeep.api.decodeOrNull
import xyz.malefic.malefikeep.components.CreateNote
import xyz.malefic.malefikeep.components.Header
import xyz.malefic.malefikeep.components.MemberManager
import xyz.malefic.malefikeep.components.NotesGrid
import xyz.malefic.malefikeep.models.CreateNoteRequest
import xyz.malefic.malefikeep.models.Note
import xyz.malefic.malefikeep.models.WorkspaceRole

@Page
@Composable
fun WorkspacePage() {
    val ctx = rememberPageContext()
    val scope = rememberCoroutineScope()

    val workspaceId = ctx.route.params["id"] ?: ctx.route.queryParams["id"] ?: ""
    val userId = localStorage["auth-user-id"] ?: ""
    val token = localStorage["auth-token"]

    // Redirect to login if not authenticated
    if (token == null) {
        LaunchedEffect(Unit) { ctx.router.navigateTo("/login") }
        return
    }

    var notes by remember { mutableStateOf(listOf<Note>()) }
    var workspaceName by remember { mutableStateOf("") }
    var isOwner by remember { mutableStateOf(false) }
    var canWrite by remember { mutableStateOf(false) }

    LaunchedEffect(workspaceId) {
        if (workspaceId.isEmpty()) {
            ctx.router.navigateTo("/")
            return@LaunchedEffect
        }

        // Load workspace info from the list endpoint
        val wsResult = apiGet("/api/workspaces/list")
        val workspaces = wsResult.decodeOrNull<List<xyz.malefic.malefikeep.models.Workspace>>() ?: emptyList()
        val workspace = workspaces.find { it.id == workspaceId }
        if (workspace == null) {
            ctx.router.navigateTo("/")
            return@LaunchedEffect
        }

        workspaceName = workspace.name
        isOwner = workspace.ownerId == userId
        canWrite = isOwner // Members' write access resolved below

        // Load members to determine write access for non-owners
        if (!isOwner) {
            val membersResult = apiGet("/api/workspaces/members/list?workspaceId=$workspaceId")
            val members = membersResult.decodeOrNull<List<xyz.malefic.malefikeep.models.WorkspaceMember>>() ?: emptyList()
            canWrite = members.find { it.userId == userId }?.role == WorkspaceRole.READ_WRITE
        }

        // Load notes
        val notesResult = apiGet("/api/workspaces/notes/list?workspaceId=$workspaceId")
        notes = notesResult.decodeOrNull<List<Note>>() ?: emptyList()
    }

    Column(Modifier.fillMaxSize()) {
        Header(onLogout = { ctx.router.navigateTo("/") })

        Box(
            Modifier.fillMaxWidth().fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(Modifier.fillMaxWidth().maxWidth(1200.px)) {
                if (canWrite) {
                    CreateNote { title, content, color ->
                        scope.launch {
                            val result =
                                apiPost(
                                    "/api/workspaces/notes/create",
                                    clientJson.encodeToString(
                                        CreateNoteRequest(
                                            workspaceId = workspaceId,
                                            title = title,
                                            content = content,
                                            color = color,
                                        ),
                                    ),
                                )
                            val newNote = result.decodeOrNull<Note>()
                            if (newNote != null) notes = listOf(newNote) + notes
                        }
                    }
                }

                NotesGrid(
                    notes = notes,
                    canDelete = canWrite,
                    onDeleteNote = { noteToDelete ->
                        scope.launch {
                            apiDelete("/api/workspaces/notes/delete?id=${noteToDelete.id}&workspaceId=$workspaceId")
                            notes = notes.filter { it.id != noteToDelete.id }
                        }
                    },
                )

                MemberManager(
                    workspaceId = workspaceId,
                    currentUserId = userId,
                    isOwner = isOwner,
                )
            }
        }
    }
}
