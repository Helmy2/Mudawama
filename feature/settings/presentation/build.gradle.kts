plugins {
    id("mudawama.kmp.compose")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.settings.presentation" }
    configureIosFramework("FeatureSettingsPresentation")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.core.domain)
            implementation(projects.shared.designsystem)
            implementation(projects.feature.settings.domain)
            implementation(projects.feature.settings.data)
            implementation(projects.feature.quran.domain)
        }
    }
}