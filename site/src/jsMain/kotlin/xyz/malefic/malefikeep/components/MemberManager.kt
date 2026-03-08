package xyz.malefic.malefikeep.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.varabyte.kobweb.compose.ui.modifiers.border
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
import kotlinx.serialization.encodeToString
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.malefikeep.api.apiDelete
import xyz.malefic.malefikeep.api.apiGet
import xyz.malefic.malefikeep.api.apiPost
import xyz.malefic.malefikeep.api.apiPut
import xyz.malefic.malefikeep.api.clientJson
import xyz.malefic.malefikeep.api.decodeOrNull
import xyz.malefic.malefikeep.models.AddMemberRequest
import xyz.malefic.malefikeep.models.UpdateMemberRoleRequest
import xyz.malefic.malefikeep.models.WorkspaceMember
import xyz.malefic.malefikeep.models.WorkspaceRole

@Composable
fun MemberManager(
    workspaceId: String,
    currentUserId: String,
    isOwner: Boolean,
) {
    val scope = rememberCoroutineScope()
    var members by remember { mutableStateOf(listOf<WorkspaceMember>()) }
    var addUsername by remember { mutableStateOf("") }
    var addRole by remember { mutableStateOf(WorkspaceRole.READ_ONLY) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(workspaceId) {
        val result = apiGet("/api/workspaces/members/list?workspaceId=$workspaceId")
        members = result.decodeOrNull<List<WorkspaceMember>>() ?: emptyList()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .margin(top = 24.px)
            .border(1.px, LineStyle.Solid, Color("#e0e0e0"))
            .borderRadius(8.px)
            .padding(16.px),
    ) {
        H3(attrs = Modifier.margin(bottom = 12.px).fontSize(16.px).toAttrs()) { Text("Members") }

        members.forEach { member ->
            Row(
                Modifier.fillMaxWidth().margin(bottom = 8.px).gap(8.px),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                P(attrs = Modifier.margin(0.px).weight(1f).toAttrs()) {
                    Text("${member.username} — ${member.role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}")
                }
                if (isOwner && member.userId != currentUserId) {
                    Button(
                        onClick = {
                            scope.launch {
                                val newRole =
                                    if (member.role ==
                                        WorkspaceRole.READ_ONLY
                                    ) {
                                        WorkspaceRole.READ_WRITE
                                    } else {
                                        WorkspaceRole.READ_ONLY
                                    }
                                apiPut(
                                    "/api/workspaces/members/update",
                                    clientJson.encodeToString(UpdateMemberRoleRequest(workspaceId, member.userId, newRole)),
                                )
                                members = members.map { if (it.userId == member.userId) it.copy(role = newRole) else it }
                            }
                        },
                        modifier = Modifier.padding(4.px, 8.px).fontSize(12.px).backgroundColor(Color("#f5f5f5")),
                    ) {
                        Text("Toggle role")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                apiDelete("/api/workspaces/members/remove?workspaceId=$workspaceId&userId=${member.userId}")
                                members = members.filter { it.userId != member.userId }
                            }
                        },
                        modifier =
                            Modifier
                                .padding(4.px, 8.px)
                                .fontSize(12.px)
                                .color(Color("#d32f2f"))
                                .backgroundColor(Color("#fff0f0")),
                    ) {
                        Text("Remove")
                    }
                }
            }
        }

        if (isOwner) {
            Row(Modifier.fillMaxWidth().margin(top = 12.px).gap(8.px), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    FormInput(label = "", value = addUsername, placeholder = "Username to invite") { addUsername = it }
                }
                Select(attrs = {
                    onChange { addRole = WorkspaceRole.valueOf(it.value ?: "READ_ONLY") }
                    style { property("padding", "10px") }
                }) {
                    Option("READ_ONLY", { if (addRole == WorkspaceRole.READ_ONLY) attr("selected", "true") }) { Text("Read only") }
                    Option("READ_WRITE", { if (addRole == WorkspaceRole.READ_WRITE) attr("selected", "true") }) { Text("Read & write") }
                }
                Button(
                    onClick = {
                        if (addUsername.isBlank()) return@Button
                        scope.launch {
                            val result =
                                apiPost(
                                    "/api/workspaces/members/add",
                                    clientJson.encodeToString(AddMemberRequest(workspaceId, addUsername, addRole)),
                                )
                            val newMember = result.decodeOrNull<WorkspaceMember>()
                            if (newMember != null) {
                                members = members + newMember
                                addUsername = ""
                                errorMsg = ""
                            } else {
                                errorMsg = result.exceptionOrNull()?.message?.substringAfter(": ") ?: "Failed to add member."
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .padding(10.px, 16.px)
                            .backgroundColor(Color("#4285f4"))
                            .color(Color.white)
                            .borderRadius(4.px),
                ) {
                    Text("Invite")
                }
            }
            if (errorMsg.isNotEmpty()) {
                P(attrs = Modifier.color(Color("#d32f2f")).margin(top = 4.px).toAttrs()) { Text(errorMsg) }
            }
        }
    }
}
