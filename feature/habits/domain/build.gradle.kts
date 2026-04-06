plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.habits.domain"
    }

    configureIosFramework("FeatureHabitsDomain")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(projects.shared.core.time)
            api(projects.shared.core.domain)
        }
    }
}
