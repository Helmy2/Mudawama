plugins {
    id("mudawama.kmp")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.core.common" }
    configureIosFramework("SharedCoreCommon")
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(projects.shared.core.domain)
        }
        androidMain.dependencies {
            implementation(libs.play.services.location)
        }
    }
}
