plugins {
    id("mudawama.kmp.data")
}

mudawama {
    data {
        useDataStore = true
        useTink = true
        useSerialization = true
    }
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.core.data"
    }

    configureIosFramework("shared:core:dataKit")

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.shared.core.domain)
            }
        }
    }
}