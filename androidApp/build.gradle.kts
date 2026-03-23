plugins {
    id("mudawama.android.application")
}

android {
    namespace = "io.github.helmy2.mudawama"
}

dependencies {
    implementation(project(":shared:umbrella-ui"))
}