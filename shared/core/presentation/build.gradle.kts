plugins {
    id("mudawama.kmp.presentation")
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
            }
        }
    }
}