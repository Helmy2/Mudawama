plugins {
    id("mudawama.kmp.compose")
}

compose.resources {
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
