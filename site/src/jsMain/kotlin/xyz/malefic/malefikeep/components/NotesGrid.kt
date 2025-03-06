package xyz.malefic.malefikeep.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.GridEntry
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.display
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.gridTemplateColumns
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.fr
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.malefikeep.models.Note

@Composable
fun NotesGrid(
    notes: List<Note>,
    onDeleteNote: (Note) -> Unit,
) {
    val breakpoint = rememberBreakpoint()

    if (notes.isEmpty()) {
        Box(
            Modifier.fillMaxWidth().height(200.px),
            contentAlignment = Alignment.Center,
        ) {
            P(
                attrs =
                    Modifier
                        .fontSize(18.px)
                        .color(Color.darkgray)
                        .toAttrs(),
            ) {
                Text("No notes yet. Create one!")
            }
        }
    } else {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(16.px),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .display(DisplayStyle.Grid)
                    .gridTemplateColumns {
                        when (breakpoint) {
                            Breakpoint.SM -> size(1.fr)
                            Breakpoint.MD -> size(1.fr)
                            else ->
                                repeat(GridEntry.Repeat.Auto.Type.AutoFill) {
                                    minmax(300.px, 1.fr)
                                }
                        }
                    }.gap(16.px),
            ) {
                notes.forEach { note ->
                    NoteItem(note = note, onDelete = onDeleteNote)
                }
            }
        }
    }
}
