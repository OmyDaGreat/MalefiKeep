package xyz.malefic.malefikeep.pages

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
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.malefikeep.api.apiPost
import xyz.malefic.malefikeep.api.clientJson
import xyz.malefic.malefikeep.components.FormInput
import xyz.malefic.malefikeep.models.RegisterRequest

@Page
@Composable
fun RegisterPage() {
    val ctx = rememberPageContext()
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().backgroundColor(Color("#f5f5f5")), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .width(400.px)
                .backgroundColor(Color.white)
                .borderRadius(8.px)
                .boxShadow(0.px, 4.px, 16.px, color = rgba(0, 0, 0, 0.12))
                .padding(40.px),
        ) {
            H1(attrs = Modifier.margin(bottom = 24.px).fontSize(24.px).toAttrs()) { Text("Create account") }

            FormInput(label = "Username", value = username, placeholder = "yourname") { username = it }
            FormInput(label = "Email", value = email, type = "email", placeholder = "you@example.com", marginTop = 16) { email = it }
            FormInput(
                label = "Password",
                value = password,
                type = "password",
                placeholder = "Min 8 characters",
                marginTop = 16,
            ) { password = it }

            if (errorMsg.isNotEmpty()) {
                P(attrs = Modifier.color(Color("#d32f2f")).margin(top = 8.px, bottom = 0.px).toAttrs()) {
                    Text(errorMsg)
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        errorMsg = ""
                        val result = apiPost(
                            "/api/auth/register",
                            clientJson.encodeToString(RegisterRequest(username, email, password)),
                        )
                        if (result.isSuccess) {
                            ctx.router.navigateTo("/login")
                        } else {
                            errorMsg = result.exceptionOrNull()?.message?.substringAfter(": ") ?: "Registration failed."
                        }
                        loading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .margin(top = 24.px)
                    .padding(12.px)
                    .backgroundColor(Color("#4285f4"))
                    .color(Color.white)
                    .borderRadius(4.px),
            ) {
                Text(if (loading) "Creating account…" else "Create account")
            }

            Row(Modifier.fillMaxWidth().margin(top = 16.px)) {
                P(attrs = Modifier.margin(0.px).toAttrs()) { Text("Already have an account? ") }
                Button(
                    onClick = { ctx.router.navigateTo("/login") },
                    modifier = Modifier.backgroundColor(Color.transparent).color(Color("#4285f4")).padding(0.px).margin(left = 4.px),
                ) {
                    Text("Sign in")
                }
            }
        }
    }
}
