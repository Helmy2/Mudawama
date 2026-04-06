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
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.ktor)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        // Note: offline-first module — Ktor kept for future API layer. See plan.md 1.
    }
}
