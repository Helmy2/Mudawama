plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.quran.domain" }
    configureIosFramework("FeatureQuranDomain")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.time)
            implementation(projects.shared.core.domain)
        }
    }
}
