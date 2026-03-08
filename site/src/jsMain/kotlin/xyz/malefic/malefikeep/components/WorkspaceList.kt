package xyz.malefic.malefikeep.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.Button
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.malefikeep.api.apiDelete
import xyz.malefic.malefikeep.api.apiPost
import xyz.malefic.malefikeep.api.clientJson
import xyz.malefic.malefikeep.api.decodeOrNull
import xyz.malefic.malefikeep.models.CreateWorkspaceRequest
import xyz.malefic.malefikeep.models.Workspace

@Composable
fun WorkspaceList(
    workspaces: List<Workspace>,
    currentUserId: String,
    onWorkspaceClick: (Workspace) -> Unit,
    onWorkspaceCreated: (Workspace) -> Unit,
    onWorkspaceDeleted: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var newName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth().padding(24.px)) {
        H2(attrs = Modifier.margin(bottom = 16.px).fontSize(20.px).toAttrs()) { Text("My Workspaces") }

        // Create workspace form
        Row(
            Modifier.fillMaxWidth().margin(bottom = 24.px).gap(8.px),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                FormInput(label = "", value = newName, placeholder = "New workspace name…") { newName = it }
            }
            Button(
                onClick = {
                    if (newName.isBlank()) return@Button
                    scope.launch {
                        val result =
                            apiPost(
                                "/api/workspaces/create",
                                clientJson.encodeToString(CreateWorkspaceRequest(newName)),
                            )
                        val workspace = result.decodeOrNull<Workspace>()
                        if (workspace != null) {
                            onWorkspaceCreated(workspace)
                            newName = ""
                            errorMsg = ""
                        } else {
                            errorMsg = "Failed to create workspace."
                        }
                    }
                },
                modifier =
                    Modifier
                        .padding(10.px, 20.px)
                        .backgroundColor(Color("#4285f4"))
                        .color(Color.white)
                        .borderRadius(4.px),
            ) {
                Text("Create")
            }
        }

        if (errorMsg.isNotEmpty()) {
            P(attrs = Modifier.color(Color("#d32f2f")).margin(bottom = 8.px).toAttrs()) { Text(errorMsg) }
        }

        if (workspaces.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().padding(40.px),
                contentAlignment = Alignment.Center,
            ) {
                P(attrs = Modifier.color(Color.darkgray).fontSize(16.px).toAttrs()) {
                    Text("No workspaces yet. Create one above!")
                }
            }
        } else {
            workspaces.forEach { workspace ->
                WorkspaceItem(
                    workspace = workspace,
                    isOwner = workspace.ownerId == currentUserId,
                    onClick = { onWorkspaceClick(workspace) },
                    onDelete = {
                        scope.launch {
                            apiDelete("/api/workspaces/delete?id=${workspace.id}")
                            onWorkspaceDeleted(workspace.id)
                        }
                    },
                )
            }
        }
    }
}
