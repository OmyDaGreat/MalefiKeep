package xyz.malefic.malefikeep.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.onMouseEnter
import com.varabyte.kobweb.compose.ui.modifiers.onMouseLeave
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.fa.FaFolder
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.malefikeep.models.Workspace

@Composable
fun WorkspaceItem(
    workspace: Workspace,
    isOwner: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var hovered by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxWidth()
            .margin(bottom = 8.px)
            .backgroundColor(if (hovered) Color("#f0f4ff") else Color.white)
            .borderRadius(8.px)
            .boxShadow(0.px, 1.px, 4.px, color = rgba(0, 0, 0, 0.1))
            .padding(16.px)
            .cursor(Cursor.Pointer)
            .onMouseEnter { hovered = true }
            .onMouseLeave { hovered = false }
            .onClick { onClick() },
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FaFolder(Modifier.color(Color("#fbbc04")).fontSize(20.px).margin(right = 12.px))
            Column(Modifier.weight(1f)) {
                Span(attrs = Modifier.fontSize(16.px).toAttrs()) { Text(workspace.name) }
                P(
                    attrs =
                        Modifier
                            .fontSize(12.px)
                            .color(Color.darkgray)
                            .margin(top = 2.px, bottom = 0.px)
                            .toAttrs(),
                ) {
                    Text("Owner: ${workspace.ownerUsername}")
                }
            }
            if (isOwner) {
                FaTrash(
                    Modifier
                        .color(Color.darkgray)
                        .fontSize(14.px)
                        .cursor(Cursor.Pointer)
                        .onClick { event ->
                            event.stopPropagation()
                            onDelete()
                        },
                )
            }
        }
    }
}
