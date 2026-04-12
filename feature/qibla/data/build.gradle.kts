plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.qibla.data" }
    configureIosFramework("FeatureQiblaData")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.domain)
            implementation(projects.feature.qibla.domain)
        }
    }
}