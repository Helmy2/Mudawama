plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.prayer.data" }
    configureIosFramework("FeaturePrayerData")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.prayer.domain)
            implementation(projects.shared.core.database)
            implementation(projects.shared.core.time)
            implementation(projects.shared.core)        // LocationProvider
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.ktor)
        }
        androidMain.dependencies { implementation(libs.ktor.client.okhttp) }
        iosMain.dependencies    { implementation(libs.ktor.client.darwin) }
    }
}
