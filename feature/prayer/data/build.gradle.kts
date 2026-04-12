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
            implementation(projects.feature.habits.domain)     // HabitWithStatus, HabitLog
            implementation(projects.feature.habits.data)       // HabitMapper, HabitLogMapper
            implementation(projects.feature.settings.domain)    // CalculationMethod for prayer
            implementation(projects.shared.core.database)
            implementation(projects.shared.core.time)
            api(projects.shared.core.domain)  // HttpClient from core domain
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.ktor)
        }
        androidMain.dependencies { implementation(libs.ktor.client.okhttp) }
        iosMain.dependencies    { implementation(libs.ktor.client.darwin) }
    }
}
