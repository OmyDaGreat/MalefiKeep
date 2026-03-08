package xyz.malefic.malefikeep.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.px
import org.w3c.dom.get
import xyz.malefic.malefikeep.api.apiGet
import xyz.malefic.malefikeep.api.decodeOrNull
import xyz.malefic.malefikeep.components.Header
import xyz.malefic.malefikeep.components.WorkspaceList
import xyz.malefic.malefikeep.models.Workspace

@Page
@Composable
fun HomePage() {
    val ctx = rememberPageContext()
    val token = localStorage["auth-token"]
    val userId = localStorage["auth-user-id"] ?: ""

    // Redirect to login if not authenticated
    if (token == null) {
        LaunchedEffect(Unit) { ctx.router.navigateTo("/login") }
        return
    }

    var workspaces by remember { mutableStateOf(listOf<Workspace>()) }

    LaunchedEffect(Unit) {
        val result = apiGet("/api/workspaces/list")
        workspaces = result.decodeOrNull<List<Workspace>>() ?: emptyList()
    }

    Column(Modifier.fillMaxSize()) {
        Header(onLogout = {
            localStorage.removeItem("auth-token")
            localStorage.removeItem("auth-user-id")
            localStorage.removeItem("auth-username")
            ctx.router.navigateTo("/login")
        })

        Box(
            Modifier.fillMaxWidth().fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(Modifier.fillMaxWidth().maxWidth(900.px)) {
                WorkspaceList(
                    workspaces = workspaces,
                    currentUserId = userId,
                    onWorkspaceClick = { ctx.router.navigateTo("/workspace?id=${it.id}") },
                    onWorkspaceCreated = { workspaces = workspaces + it },
                    onWorkspaceDeleted = { id -> workspaces = workspaces.filter { it.id != id } },
                )
            }
        }
    }
}
