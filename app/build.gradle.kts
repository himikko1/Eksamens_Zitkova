plugins {

    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.google.gms.google.services)

}



android {

    namespace = "com.example.myapplication"

    compileSdk = 35



    defaultConfig {

        applicationId = "com.example.myapplication"

        minSdk = 34

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

    implementation(libs.androidx.browser)

//Images

    implementation(libs.ui)

    implementation(libs.androidx.material)

    implementation(libs.androidx.runtime)

    implementation(libs.androidx.foundation)

    implementation(libs.androidx.activity.compose.v182) // For rememberLauncherForActivityResult

    implementation(libs.coil.compose) // For image loading (optional, but recommended)



// Firebase - исправлено для избежания конфликтов

    implementation(libs.firebase.storage.ktx) // Use the latest version

    implementation(platform(libs.firebase.bom))

    implementation(libs.google.firebase.auth.ktx)

    implementation(libs.google.firebase.firestore.ktx)

// Удалена дублирующая зависимость: implementation(libs.firebase.auth)

    implementation(libs.firebase.crashlytics.buildtools)







// Compose

    implementation(libs.androidx.compose.bom.v20250101)

    implementation(libs.material3)

    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.ui)

    implementation(libs.androidx.ui.graphics)

    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.androidx.material3)



// Core Android

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.runtime.livedata)

    implementation(libs.androidx.runtime.livedata.v178)



// Navigation

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.lifecycle.service)

//implementation(libs.androidbrowserhelper)

    val nav_version = "2.8.8"

    implementation("androidx.navigation:navigation-compose:$nav_version")

    implementation("androidx.navigation:navigation-fragment:$nav_version")

    implementation("androidx.navigation:navigation-ui:$nav_version")

    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")



// Serialization

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")



// Coroutines

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")



// Dialog sheets

    implementation("com.maxkeppeler.sheets-compose-dialogs:core:1.3.0")

    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.3.0")



//theme

    implementation ("androidx.compose.material3:material3:1.1.0")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    implementation(libs.androidx.datastore.core)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.datastore.preferences.core)

    implementation ("androidx.compose.material3:material3:1.3.2")



//water

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")



//video

    implementation("io.coil-kt:coil-compose:2.7.0") // Or Glide if you prefer



//stepCounter

    implementation("androidx.compose.ui:ui:1.5.2")

    implementation("androidx.activity:activity-compose:1.8.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.x.y")





// Testing

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(platform(libs.androidx.compose.bom))

    androidTestImplementation(libs.androidx.ui.test.junit4)

    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")



// Debug

    debugImplementation(libs.androidx.ui.tooling)

    debugImplementation(libs.androidx.ui.test.manifest)

}