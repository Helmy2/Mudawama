plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.quran.data" }
    configureIosFramework("FeatureQuranData")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.quran.domain)
            implementation(projects.shared.core.database)
            implementation(projects.shared.core.time)
            implementation(projects.shared.core.domain)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.ktor)
        }
        androidMain.dependencies { implementation(libs.ktor.client.okhttp) }
        iosMain.dependencies    { implementation(libs.ktor.client.darwin) }
    }
}
