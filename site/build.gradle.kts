import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication

plugins {
    kotlin("plugin.serialization") version "2.3.10"
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
    configAsKobwebApplication("malefikeep", true)

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)
        }

        jsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.silk)
            implementation(libs.silk.icons.fa)
        }

        jvmMain.dependencies {
            implementation(libs.kobweb.api)
            implementation(libs.exposed.core)
            implementation(libs.exposed.jdbc)
            implementation(libs.hikari)
            implementation(libs.postgresql)
            implementation(libs.jwt)
            implementation(libs.jbcrypt)
        }
    }
}
