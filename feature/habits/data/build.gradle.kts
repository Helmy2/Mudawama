plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.habits.data"
    }

    configureIosFramework("FeatureHabitsData")

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.habits.domain)
            implementation(projects.shared.core.database)
            implementation(projects.shared.core.time)
            implementation(libs.kotlinx.coroutines.core)
            // Note: offline-first module — Ktor kept for future API layer
        }
    }
}
