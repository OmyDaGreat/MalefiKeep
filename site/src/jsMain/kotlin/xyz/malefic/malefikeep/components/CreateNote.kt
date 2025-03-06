package xyz.malefic.malefikeep.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.alignItems
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.display
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.justifyContent
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.silk.components.forms.Button
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.outline
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import xyz.malefic.malefikeep.models.Note
import kotlin.random.Random

@Composable
fun CreateNote(onNoteCreated: (Note) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    val colorOptions = listOf("#ffffff", "#f28b82", "#fbbc04", "#fff475", "#ccff90", "#a7ffeb", "#cbf0f8", "#d7aefb")
    var selectedColor by remember { mutableStateOf(colorOptions[0]) }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(16.px)
            .display(DisplayStyle.Flex)
            .justifyContent(JustifyContent.Center),
    ) {
        Column(
            Modifier
                .width(600.px)
                .maxWidth(100.percent)
                .backgroundColor(Color(selectedColor))
                .borderRadius(8.px)
                .boxShadow(0.px, 2.px, 4.px, color = rgba(0, 0, 0, 0.2))
                .padding(16.px)
                .onClick { if (!isExpanded) isExpanded = true },
        ) {
            if (isExpanded) {
                Input(
                    type = InputType.Text,
                    attrs = {
                        placeholder("Title")
                        id("title")
                        style {
                            width(100.percent)
                            border(0.px)
                            backgroundColor(Color.transparent)
                            fontSize(18.px)
                            fontWeight(FontWeight.Medium.toString())
                            padding(8.px)
                            outline("none")
                        }
                        value(title)
                        onInput { event -> title = event.value }
                    },
                )
            }

            TextArea(
                attrs = {
                    placeholder(if (isExpanded) "Take a note..." else "Click to add a note...")
                    id("content")
                    style {
                        width(100.percent)
                        border(0.px)
                        backgroundColor(Color.transparent)
                        fontSize(16.px)
                        height(if (isExpanded) 150.px else 50.px)
                        padding(8.px)
                        outline("none")
                        property("resize", "none")
                    }
                    value(content)
                    onInput { event -> content = event.target.value }
                },
            )

            if (isExpanded) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .margin(top = 16.px)
                        .justifyContent(JustifyContent.SpaceBetween)
                        .alignItems(AlignItems.Center),
                ) {
                    Row {
                        colorOptions.forEach { color ->
                            Box(
                                Modifier
                                    .size(24.px)
                                    .margin(right = 8.px)
                                    .backgroundColor(Color(color))
                                    .borderRadius(50.percent)
                                    .border(
                                        width = if (selectedColor == color) 2.px else 1.px,
                                        style = LineStyle.Solid,
                                        color = if (selectedColor == color) Color("#000000") else Color("#e0e0e0"),
                                    ).cursor(Cursor.Pointer)
                                    .onClick { selectedColor = color },
                            )
                        }
                    }

                    Row {
                        Button(
                            onClick = {
                                isExpanded = false
                                title = ""
                                content = ""
                                selectedColor = colorOptions[0]
                            },
                            modifier =
                                Modifier
                                    .margin(right = 8.px)
                                    .padding(8.px, 16.px)
                                    .backgroundColor(Colors.Transparent),
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (content.isNotEmpty()) {
                                    val newNote =
                                        Note(
                                            id = generateId(),
                                            title = title,
                                            content = content,
                                            color = selectedColor,
                                        )
                                    onNoteCreated(newNote)
                                    title = ""
                                    content = ""
                                    isExpanded = false
                                    selectedColor = colorOptions[0]
                                }
                            },
                            modifier =
                                Modifier
                                    .padding(8.px, 16.px)
                                    .backgroundColor(Color("#4285f4"))
                                    .color(Colors.White),
                        ) {
                            Text("Add Note")
                        }
                    }
                }
            }
        }
    }
}

private fun generateId(): String = Random.nextInt(100000, 999999).toString()
