plugins {
    id("mudawama.kmp.compose")
}

compose.resources {
    // Res must be public so shared:navigation and all feature :presentation
    // modules can import mudawama.shared.designsystem.Res directly.
    // By default the convention plugin leaves publicResClass = false (internal).
    publicResClass = true
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.designsystem"
    }

    configureIosFramework("MudawamaDesignSystem", isStatic = true)

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.bundles.compose)
                implementation(libs.bundles.lifecycle)
                implementation(libs.compose.resources)
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
