plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.qibla.data" }
    configureIosFramework("FeatureQiblaData")
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(projects.shared.core.domain)
            implementation(projects.feature.qibla.domain)
        }
    }
}