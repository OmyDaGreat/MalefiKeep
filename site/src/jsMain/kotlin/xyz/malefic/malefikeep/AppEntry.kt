package xyz.malefic.malefikeep

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.margin
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import org.jetbrains.compose.web.css.*

object NotesStyles {
    val NoteCard =
        CssStyle {
            base {
                Modifier
                    .backgroundColor(Color.white)
                    .borderRadius(8.px)
                    .boxShadow(0.px, 2.px, 4.px, color = rgba(0, 0, 0, 0.2))
                    .padding(16.px)
                    .margin(8.px)
                    .cursor(Cursor.Pointer)
                    .transition(Transition.all(200.ms))
            }
            hover {
                Modifier
                    .boxShadow(0.px, 4.px, 8.px, color = rgba(0, 0, 0, 0.3))
                    .transform { scale(1.02f) }
            }
        }
}

object Style : StyleSheet() {
    init {
        universal style {
            margin(0.px)
            padding(0.px)
            boxSizing("border-box")
        }

        "body" style {
            backgroundColor(Color("#f5f5f5"))
            fontFamily("Roboto", "sans-serif")
        }
    }
}

@App
@Composable
fun AppEntry(content: @Composable () -> Unit) {
    SilkApp {
        Style(Style)
        content()
    }
}
