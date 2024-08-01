plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize") // Added for parcelize plugin
}

val kotlin_version = "1.5.31" // Move this to the top for visibility

android {
    namespace = "com.example.guru2_cleanspirit"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.guru2_cleanspirit"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Updated for modern Java
        targetCompatibility = JavaVersion.VERSION_17 // Updated for modern Java
    }

    kotlinOptions {
        jvmTarget = "17" // Updated to match Java version
    }

    buildFeatures {
        viewBinding = true // Enable view binding
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1") // Updated to latest version
    implementation("com.google.android.material:material:1.5.0") // Updated to latest version
    implementation("androidx.constraintlayout:constraintlayout:2.1.3") // Updated to latest version
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
