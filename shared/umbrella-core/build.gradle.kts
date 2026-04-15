plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
    alias(libs.plugins.skie)
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.umbrella.core"
    }

    configureIosFramework("MudawamaCore", isStatic = true) {
        // Core infrastructure
        export(projects.shared.core.domain)
        export(projects.shared.core.data)
        export(projects.shared.core.time)
        // Feature domain layers
        export(projects.feature.habits.domain)
        export(projects.feature.prayer.domain)
        export(projects.feature.quran.domain)
        export(projects.feature.athkar.domain)
        export(projects.feature.settings.domain)
        export(projects.feature.qibla.domain)
        // Feature data layers (Koin modules + data types live here)
        export(projects.feature.habits.data)
        export(projects.feature.prayer.data)
        export(projects.feature.quran.data)
        export(projects.feature.athkar.data)
        export(projects.feature.settings.data)
        export(projects.feature.qibla.data)
    }

    sourceSets {
        commonMain {
            dependencies {
                // Must mirror every export() above as api()
                api(projects.shared.core.domain)
                api(projects.shared.core.data)
                api(projects.shared.core.time)
                api(projects.feature.habits.domain)
                api(projects.feature.prayer.domain)
                api(projects.feature.quran.domain)
                api(projects.feature.athkar.domain)
                api(projects.feature.settings.domain)
                api(projects.feature.qibla.domain)
                api(projects.feature.habits.data)
                api(projects.feature.prayer.data)
                api(projects.feature.quran.data)
                api(projects.feature.athkar.data)
                api(projects.feature.settings.data)
                api(projects.feature.qibla.data)
                // Database: implementation only — Swift does not reference Room entities directly
                implementation(projects.shared.core.database)
            }
        }
    }
}
