plugins {
    id("mudawama.kmp.compose")
    id("mudawama.kmp.koin")
    alias(libs.plugins.skie)
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.umbrella"
    }

    configureIosFramework("MudawamaUI", isStatic = true) {
        export(projects.shared.core.domain)
        export(projects.feature.qibla.data)
        export(projects.feature.qibla.domain)
        export(projects.feature.qibla.presentation)
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.shared.core.domain)
                implementation(projects.shared.core.data)
                implementation(projects.shared.core.database)
                implementation(projects.shared.core.time)
                implementation(projects.shared.designsystem)
                implementation(projects.shared.navigation)

                implementation(projects.feature.habits.domain)
                implementation(projects.feature.habits.data)
                implementation(projects.feature.habits.presentation)

                implementation(projects.feature.prayer.domain)
                implementation(projects.feature.prayer.data)
                implementation(projects.feature.prayer.presentation)

                implementation(projects.feature.quran.domain)
                implementation(projects.feature.quran.data)
                implementation(projects.feature.quran.presentation)

                implementation(projects.feature.athkar.domain)
                implementation(projects.feature.athkar.data)
                implementation(projects.feature.athkar.presentation)

                implementation(projects.feature.home.presentation)

                implementation(projects.feature.settings.domain)
                implementation(projects.feature.settings.data)
                implementation(projects.feature.settings.presentation)

                api(projects.feature.qibla.domain)
                api(projects.feature.qibla.data)
                api(projects.feature.qibla.presentation)
            }
        }
    }
}
