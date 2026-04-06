plugins {
    id("mudawama.kmp")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.umbrella.core"
    }

    configureIosFramework("MudawamaCore", isStatic = true) {
        export(projects.shared.core.domain)
        export(projects.shared.core.time)
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.shared.core.domain)
                api(projects.shared.core.data)
                api(projects.shared.core.database)
                api(projects.shared.core.time)
            }
        }
    }
}
