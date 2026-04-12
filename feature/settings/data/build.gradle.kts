plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.settings.data" }
    configureIosFramework("FeatureSettingsData")
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(projects.shared.core.data)
            implementation(projects.feature.settings.domain)
            implementation(libs.androidx.datastore.preferences)
        }
    }
}