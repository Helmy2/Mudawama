plugins {
    id("mudawama.kmp.compose")
    id("mudawama.kmp.koin")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.umbrella"
    }

    configureIosFramework("MudawamaUI", isStatic = true) {
        export(projects.shared.core.domain)
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

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.bundles.koin)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.compose)
                implementation(libs.bundles.lifecycle)
                implementation(libs.compose.resources)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.ui.tooling)
                implementation(libs.ui.tooling.preview)
            }
        }
    }
}
