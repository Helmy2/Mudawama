plugins {
    id("mudawama.kmp.compose")
    id("mudawama.kmp.koin")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.habits.presentation"
    }

    configureIosFramework("FeatureHabitsPresentation", isStatic = true)

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.habits.domain)
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.designsystem)
            implementation(projects.shared.core.domain)
        }
    }
}
