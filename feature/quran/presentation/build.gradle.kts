plugins {
    id("mudawama.kmp.compose")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.quran.presentation" }
    configureIosFramework("FeatureQuranPresentation", isStatic = true)
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.designsystem)
            implementation(projects.feature.quran.domain)
        }
    }
}