plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
}

kotlin {
    jvmToolchain(17)
}

android {
    compileSdk = 36
    namespace = "com.thejiltedalchemist.lunchroulette"
    defaultConfig {
        applicationId = "com.thejiltedalchemist.lunchroulette"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
