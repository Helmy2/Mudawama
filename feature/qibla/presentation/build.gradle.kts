plugins {
    id("mudawama.kmp.compose")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.qibla.presentation" }
    configureIosFramework("FeatureQiblaPresentation")
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(projects.feature.qibla.domain)
            implementation(projects.feature.qibla.data)
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.core.time)
            implementation(projects.shared.core.domain)
            implementation(projects.shared.designsystem)
            implementation(projects.feature.settings.domain)
            implementation(libs.bundles.compose)
            implementation(libs.bundles.lifecycle)
            implementation(libs.compose.resources)
            implementation(libs.koin.compose.viewmodel)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin)
            implementation(libs.kotlinx.datetime)
            implementation(libs.material.icons.extended)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ui.tooling)
            implementation(libs.ui.tooling.preview)
            implementation(libs.koin.android)
        }
    }
}