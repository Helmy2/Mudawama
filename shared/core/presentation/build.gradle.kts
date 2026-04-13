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
                api(libs.bundles.compose)
                api(libs.bundles.lifecycle)
                api(libs.compose.resources)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.appcompat)
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}
