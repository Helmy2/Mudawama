plugins {
    id("mudawama.kmp.compose")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.habits.presentation"
    }

    configureIosFramework("FeatureHabitsPresentation", isStatic = true)

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.habits.domain)
            implementation(projects.shared.core.presentation)
            implementation(projects.shared.designsystem)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bundles.compose)
            implementation(libs.bundles.lifecycle)
            implementation(libs.compose.resources)
            implementation(libs.koin.compose.viewmodel)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin)
            implementation(libs.kotlinx.datetime)
            implementation(libs.material.icons.extended)
            // MUST NOT depend on feature:habits:data or shared:core:database (FR-019)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ui.tooling)
            implementation(libs.ui.tooling.preview)
            implementation(libs.koin.android)
        }
    }
}
