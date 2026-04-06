// 4 source files in commonMain: Routes.kt, Placeholders.kt, MudawamaBottomBar.kt, MudawamaAppShell.kt
plugins {
    id("mudawama.kmp.compose")
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
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.compose)
                implementation(libs.bundles.lifecycle)
                implementation(libs.compose.resources)
                implementation(libs.navigation3.ui)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.material.icons.extended)
                implementation(libs.ui.tooling.preview)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.ui.tooling)
            }
        }
    }
}
