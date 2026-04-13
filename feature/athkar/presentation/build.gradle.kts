plugins {
    id("mudawama.kmp.compose")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.athkar.presentation" }
    configureIosFramework("FeatureAthkarPresentation", isStatic = true)
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.athkar.domain)
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.designsystem)
            implementation(projects.shared.core.domain)
        }
    }
}
