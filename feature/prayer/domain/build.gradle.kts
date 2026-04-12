plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.prayer.domain" }
    configureIosFramework("FeaturePrayerDomain")
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(projects.shared.core.time)
            api(projects.shared.core.domain)
            // EXCEPTION: shared LogStatus enum — depends on habits:domain for that enum only
            implementation(projects.feature.habits.domain)
            implementation(projects.feature.settings.domain)
        }
    }
}
