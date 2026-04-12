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
                implementation(projects.shared.designsystem)
                implementation(libs.navigation3.ui)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
