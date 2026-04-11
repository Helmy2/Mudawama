plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.athkar.domain" }
    configureIosFramework("FeatureAthkarDomain")
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(projects.shared.core.domain)
        }
    }
}
