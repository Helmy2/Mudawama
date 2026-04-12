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
                api(projects.shared.core.time)
                api(libs.bundles.compose)
                api(libs.bundles.lifecycle)
                api(libs.compose.resources)
                api(libs.ui.tooling.preview)
                api(libs.material.icons.extended)
            }
        }
    }
}
