plugins {
    id("mudawama.kmp.compose")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.home.presentation" }
    configureIosFramework("FeatureHomePresentation", isStatic = true)

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.designsystem)
            implementation(projects.feature.habits.domain)
            implementation(projects.feature.prayer.domain)
            implementation(projects.feature.athkar.domain)
            implementation(projects.feature.quran.domain)
            implementation(projects.feature.settings.domain)
        }
    }
}
