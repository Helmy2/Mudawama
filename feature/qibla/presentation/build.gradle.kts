plugins {
    id("mudawama.kmp.compose")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.qibla.presentation" }
    configureIosFramework("FeatureQiblaPresentation")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.core.domain)
            implementation(projects.shared.designsystem)
            implementation(projects.feature.qibla.domain)
            implementation(projects.feature.qibla.data)
            implementation(projects.feature.settings.domain)
        }
    }
}