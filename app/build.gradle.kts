plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")

    id ("kotlin-kapt")
    id ("androidx.navigation.safeargs.kotlin")
    id("androidx.room")

}

android {
    namespace = "com.example.sharingphoto"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sharingphoto"
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

    room {
        schemaDirectory("$projectDir/schemas")
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.swiperefreshlayout)


    val nav_version = "2.9.0"
    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.squareup.picasso:picasso:2.8")

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")

    implementation ("androidx.room:room-rxjava3:$room_version")

    implementation ("androidx.preference:preference-ktx:1.2.1")

    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("androidx.media3:media3-ui-compose:1.8.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.6.1")
}