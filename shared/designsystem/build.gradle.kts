plugins {
    id("mudawama.kmp.compose")
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
