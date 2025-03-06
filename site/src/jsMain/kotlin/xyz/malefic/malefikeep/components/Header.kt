package xyz.malefic.malefikeep.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight.Companion.Medium
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment.Center
import com.varabyte.kobweb.compose.ui.Alignment.CenterVertically
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.alignItems
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.display
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.fa.FaLightbulb
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle.Companion.Flex
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

@Composable
fun Header() {
    Box(
        Modifier
            .fillMaxWidth()
            .backgroundColor(Color("#ffffff"))
            .boxShadow(0.px, 2.px, 4.px, color = rgba(0, 0, 0, 0.1))
            .padding(16.px),
        contentAlignment = Center,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
        ) {
            Box(Modifier.display(Flex).alignItems(AlignItems.Center)) {
                FaLightbulb(
                    Modifier
                        .margin(right = 10.px)
                        .color(Color("#fbbc04"))
                        .fontSize(24.px),
                )
                H1(
                    attrs =
                        Modifier
                            .margin(0.px)
                            .fontSize(22.px)
                            .fontWeight(Medium)
                            .toAttrs(),
                ) {
                    Text("Kobweb Notes")
                }
            }
        }
    }
}
