plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.prayer.domain" }
    configureIosFramework("FeaturePrayerDomain")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.time)
            implementation(projects.shared.core.domain)
            implementation(projects.feature.habits.domain)
            implementation(projects.feature.settings.domain)
        }
    }
}
