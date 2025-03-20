import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication

plugins {
    kotlin("plugin.serialization") version "2.1.20"
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
}

group = "xyz.malefic.malefikeep"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("Powered by Kobweb")
        }
    }
}

kotlin {
    configAsKobwebApplication("malefikeep")

    sourceSets {
        jsMain.dependencies {
            implementation(libs.kotlinx.serialization)
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.silk)
            implementation(libs.silk.icons.fa)
        }
    }
}
