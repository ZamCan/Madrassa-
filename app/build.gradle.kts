plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    androidResources { noCompress += listOf("mp3") }
    namespace = "com.zamcan.madrassa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zamcan.madrassa"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.8.0"
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
