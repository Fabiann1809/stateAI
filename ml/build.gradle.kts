plugins {
    alias(libs.plugins.stateai.android.library)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    androidResources {
        noCompress += "tflite"
    }
    sourceSets {
        // Shared parity cases generated in ml-python, read by the instrumented parity test.
        getByName("androidTest").assets.srcDir("../shared/parity")
    }
}

dependencies {
    api(project(":core-domain"))
    implementation(libs.litert)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlinx.serialization.json)
}
