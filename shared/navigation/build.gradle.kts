// 4 source files in commonMain: Routes.kt, Placeholders.kt, MudawamaBottomBar.kt, MudawamaAppShell.kt
plugins {
    id("mudawama.kmp.presentation")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.navigation"
    }

    configureIosFramework("MudawamaNavigation", isStatic = true)

    sourceSets {
        commonMain {
            dependencies {
                api(projects.shared.designsystem)
                implementation(libs.navigation3.ui)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.material.icons.extended)
                implementation(libs.ui.tooling.preview)
            }
        }
    }
}

