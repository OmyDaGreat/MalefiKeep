package xyz.malefic.malefikeep.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.lineHeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.onMouseEnter
import com.varabyte.kobweb.compose.ui.modifiers.onMouseLeave
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.right
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.modifiers.whiteSpace
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.fa.FaTrash
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.malefikeep.NotesStyles
import xyz.malefic.malefikeep.models.Note

@Composable
fun NoteItem(
    note: Note,
    onDelete: (Note) -> Unit,
) {
    var isHovered by remember { mutableStateOf(false) }

    Box(
        Modifier
            .then(NotesStyles.NoteCard.toModifier())
            .fillMaxWidth()
            .backgroundColor(Color(note.color))
            .onMouseEnter { isHovered = true }
            .onMouseLeave { isHovered = false },
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (note.title.isNotEmpty()) {
                H3(
                    attrs =
                        Modifier
                            .fillMaxWidth()
                            .margin(bottom = 8.px)
                            .fontSize(18.px)
                            .fontWeight(FontWeight.Bold)
                            .toAttrs(),
                ) {
                    Text(note.title)
                }
            }

            P(
                attrs =
                    Modifier
                        .fillMaxWidth()
                        .lineHeight(1.5)
                        .whiteSpace(WhiteSpace.PreWrap)
                        .toAttrs(),
            ) {
                Text(note.content)
            }
        }

        if (isHovered) {
            Box(
                Modifier
                    .position(Position.Absolute)
                    .top(8.px)
                    .right(8.px),
            ) {
                FaTrash(
                    Modifier
                        .color(Color.darkgray)
                        .fontSize(16.px)
                        .cursor(Cursor.Pointer)
                        .onClick { onDelete(note) },
                )
            }
        }
    }
}
