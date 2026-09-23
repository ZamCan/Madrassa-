plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zamcan.madrassa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zamcan.madrassa"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.7.0"
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
