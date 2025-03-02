plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.kointest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kointest"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures {
        compose = true
    }
}

// Define versions here instead of in libs.versions.toml
val composeVersion = "1.6.1"
val composeMaterial3Version = "1.2.0"
val navVersion = "2.7.6"
val koinVersion = "3.5.0"
val coilVersion = "2.5.0"
val lifecycleVersion = "2.7.0"

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose dependencies
    implementation(platform("androidx.compose:compose-bom:2023.10.00"))
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-graphics:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material3:material3:$composeMaterial3Version")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:$navVersion")

    // Koin for dependency injection
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:$coilVersion")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")
}
