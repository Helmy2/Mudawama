plugins {
    id("mudawama.kmp.presentation")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.umbrella"
    }

    configureIosFramework("MudawamaUI", isStatic = true) {
        export(projects.shared.core.domain)
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.shared.core.domain)
                implementation(projects.shared.core.data)
                implementation(projects.shared.designsystem)
            }
        }
    }
}