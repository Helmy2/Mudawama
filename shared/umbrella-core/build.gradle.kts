plugins {
    id("mudawama.kmp.library")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.umbrella.core"
    }

    configureIosFramework("MudawamaCore", isStatic = true) {
        export(projects.shared.core.domain)
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.shared.core.domain)
                api(projects.shared.core.data)
                api(projects.shared.core.database)
            }
        }
    }
}