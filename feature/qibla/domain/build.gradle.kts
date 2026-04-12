plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.qibla.domain" }
    configureIosFramework("FeatureQiblaDomain")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.domain)
        }
    }
}