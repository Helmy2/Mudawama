plugins {
    id("mudawama.kmp.database")
}

kotlin {
    android {
        namespace = "io.github.helmy2.mudawama.core.database"
    }

    configureIosFramework("shared:core:databaseKit")
}
