plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.adoptapaws"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.adoptapaws"
        minSdk = 24
        targetSdk = 34
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

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.cast.framework)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.3.0")

    // Firebase Auth (si deseas usar Firebase para manejar la autenticación)
    implementation("com.google.firebase:firebase-auth:21.0.5")

    // Si estás usando Jetpack Compose
    implementation("androidx.compose.ui:ui:1.3.1")
    implementation("androidx.compose.material:material:1.3.1")
    implementation("androidx.navigation:navigation-compose:2.5.3")

    // Firebase SDK (si deseas usarlo para autenticación)
    implementation("com.google.firebase:firebase-auth-ktx:21.0.5")

    // Image Picker (si quieres permitir seleccionar imagen)
    implementation ("com.github.dhaval2404:imagepicker:2.1")

    // Necesario para la compatibilidad con Compose
    implementation ("androidx.activity:activity-compose:1.3.1")
    implementation("io.coil-kt:coil-compose:2.2.2")

    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.maps.android:maps-compose:2.0.0")

    implementation ("androidx.fragment:fragment-ktx:1.3.6")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.code.gson:gson:2.10.1")

}