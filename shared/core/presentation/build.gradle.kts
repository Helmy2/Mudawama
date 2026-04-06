plugins {
    id("mudawama.kmp.compose")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.core.presentation"
    }

    configureIosFramework("shared:core:presentationKit")

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.shared.core.domain)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.compose)
                implementation(libs.bundles.lifecycle)
                implementation(libs.compose.resources)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.ui.tooling)
                implementation(libs.ui.tooling.preview)
            }
        }
    }
}
