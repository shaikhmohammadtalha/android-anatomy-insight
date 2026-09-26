// Apply necessary plugins for the Android project
plugins {
    alias(libs.plugins.android.application) // Android application plugin (built-in Kotlin in AGP 9)
    alias(libs.plugins.kotlin.compose) // Jetpack Compose support
    alias(libs.plugins.ksp) // Kotlin Symbol Processing (for Room, etc.)
}

android {
    namespace = "com.shaikhmohammadtalha.anatomyinsight" // Application package name
    compileSdk = 37 // Compile SDK version

    defaultConfig {
        applicationId = "com.shaikhmohammadtalha.anatomyinsight" // Unique app identifier
        minSdk = 27 // Minimum supported Android version
        targetSdk = 37 // Target Android version
        versionCode = 1 // Internal app version
        versionName = "1.2" // User-visible app version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // Test runner
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Disable code minification
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // Default ProGuard rules
                "proguard-rules.pro" // Custom ProGuard rules
            )
        }
    }

    // Set Java compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true // Enable Jetpack Compose
    }

    // Packaging options
    packaging {
        jniLibs {
            useLegacyPackaging = false // Use new packaging system
        }
    }

    // APK splitting options (disabled for now)
    splits {
        abi {
            isEnable = false // Disable ABI-based APK splitting
            isUniversalApk = true // Generate a universal APK
        }
    }
}

dependencies {
    // --- Architecture Components ---

    // Room (Local Database)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // DataStore (Preferences)
    implementation(libs.androidx.datastore.preferences)

    // --- Core Libraries ---

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // --- Jetpack Compose ---

    // Compose BOM (manages Compose library versions)
    implementation(platform(libs.androidx.compose.bom))

    // Core Compose Libraries
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)

    // Material Design & Icons
    implementation(libs.androidx.material3)
    implementation(libs.material3)
    implementation(libs.material)
    implementation(libs.androidx.materialIconsCore)
    implementation(libs.androidx.materialIconsExtended)

    // --- Image Loading ---

    implementation(libs.coil.compose) // Coil for Compose

    // --- 3D Rendering with Filament ---

    implementation(libs.filament.android)
    implementation(libs.filament.utils.android)
    implementation(libs.gltfio.android)

    // --- Testing ---

    testImplementation(libs.junit) // Unit tests
    androidTestImplementation(libs.androidx.junit) // Android JUnit tests
    androidTestImplementation(libs.androidx.espresso.core) // Espresso UI testing
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Compose BOM for tests
    androidTestImplementation(libs.androidx.ui.test.junit4) // Compose UI test framework

    // --- Debugging Tools ---

    debugImplementation(libs.androidx.ui.tooling) // UI preview tools
    debugImplementation(libs.androidx.ui.test.manifest) // Compose test manifest
}
