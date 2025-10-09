plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)  // Add this line
}

android {
    namespace = "hk.hku.cs.swifttrip"
    compileSdk = 36

    defaultConfig {
        applicationId = "hk.hku.cs.swifttrip"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.ktor.client.core)
    // Engine: Choose one (Android or CIO)
    implementation(libs.ktor.client.android)  // For Android native engine
    // OR: implementation("io.ktor:ktor-client-cio:3.3.0")  // For CIO engine (multiplatform)
    // JSON serialization (for Amadeus API responses)
    implementation(libs.ktor.client.logging)  // Add this
    implementation(libs.gson)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.androidx.lifecycle.runtime.ktx)  // For lifecycleScope
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
}