package xyz.malefic.malefikeep.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

@Composable
fun FormInput(
    label: String,
    value: String,
    type: String = "text",
    placeholder: String = "",
    marginTop: Int = 0,
    onChange: (String) -> Unit,
) {
    val inputType =
        when (type) {
            "password" -> InputType.Password
            "email" -> InputType.Email
            else -> InputType.Text
        }

    Column(Modifier.fillMaxWidth().margin(top = marginTop.px)) {
        Label(
            attrs = Modifier.fontSize(14.px).margin(bottom = 4.px).toAttrs(),
        ) {
            Text(label)
        }
        Input(
            type = inputType,
            attrs = {
                placeholder(placeholder)
                value(value)
                onInput { onChange(it.value) }
                style {
                    property("width", "100%")
                    property("padding", "10px 12px")
                    property("border", "1px solid #e0e0e0")
                    property("border-radius", "4px")
                    property("font-size", "15px")
                    property("outline", "none")
                    property("box-sizing", "border-box")
                }
            },
        )
    }
}
