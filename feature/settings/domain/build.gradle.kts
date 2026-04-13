plugins {
    id("mudawama.kmp")
    id("mudawama.kmp.koin")
}

kotlin {
    android { namespace = "io.github.helmy2.mudawama.settings.domain" }
    configureIosFramework("FeatureSettingsDomain")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.core.domain)
        }
    }
}