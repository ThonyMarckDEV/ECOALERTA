plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.ecoalerta"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ecoalerta"
        minSdk = 26
        targetSdk = 34
        versionCode = 8  //CAMBIAR POR VERSION
        versionName = "1.8" //CAMBIAR POR VERSION

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //BIBLIOTECAS PARA GIF
    implementation ("com.github.bumptech.glide:glide:4.15.1");
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1");
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"));
    implementation ("com.google.firebase:firebase-auth");
    implementation ("com.google.firebase:firebase-firestore");
    //MAPA
    implementation ("com.google.android.gms:play-services-maps:18.1.0");
    //VOLLEY
    implementation ("com.android.volley:volley:1.2.1");
    //PLAY SERVICES
    implementation ("com.google.android.gms:play-services-location:19.1.0");
    //
    // Dependencia de OkHttp
    implementation ("com.squareup.okhttp3:okhttp:4.9.3");
    //
    implementation ("com.squareup.picasso:picasso:2.71828");
    //
    implementation ("androidx.core:core:1.6.0");

}