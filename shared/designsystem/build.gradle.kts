plugins {
    id("mudawama.kmp.presentation")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.designsystem"
    }

    configureIosFramework("MudawamaDesignSystem", isStatic = true)

    sourceSets {
        commonMain {
            dependencies {
            }
        }
    }
}
