plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.athkar.data" }
    configureIosFramework("FeatureAthkarData")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.athkar.domain)
            implementation(projects.shared.core.database)
            implementation(projects.shared.core.data)
            implementation(projects.shared.core.domain)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.datastore.preferences)
        }
    }
}
