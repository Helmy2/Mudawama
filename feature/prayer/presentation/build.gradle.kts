plugins {
    id("mudawama.kmp.compose")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.prayer.presentation" }
    configureIosFramework("FeaturePrayerPresentation", isStatic = true)
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.domain)
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.designsystem)
            implementation(projects.feature.prayer.domain)
            implementation(projects.feature.settings.domain)
        }
    }
}
