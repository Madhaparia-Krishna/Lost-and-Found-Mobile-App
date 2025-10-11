plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.loginandregistration"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.loginandregistration"
        minSdk = 31
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

    // --- FIX START ---
    // Add this block to enable View Binding
    buildFeatures {
        viewBinding = true
    }
    // --- FIX END ---
}

dependencies {
    implementation(platform(libs.firebase.bom)) // Added Firebase BOM platform
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx) // Changed to firebase.auth.ktx
    implementation("com.google.firebase:firebase-firestore-ktx") // For Firestore
    implementation("com.google.firebase:firebase-storage-ktx") // For image storage
    implementation("com.google.firebase:firebase-messaging-ktx") // For FCM push notifications
    implementation("com.github.bumptech.glide:glide:4.16.0") // For image loading
    
    // Admin Dashboard Dependencies
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") // For charts
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0") // For background jobs
    
    // PDF Generation Dependencies
    implementation("com.itextpdf:itext7-core:7.2.5") // For PDF generation
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.gms:play-services-auth:21.2.0") // For Google Sign-In
}
