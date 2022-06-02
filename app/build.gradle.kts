import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    kotlin("android")
    kotlin("kapt")
    id("com.android.application")
    // If there will be problems with that plugin delete it + sync + rebuild
    id("dagger.hilt.android.plugin")
}

android {

    namespace = "com.picrunner"
    compileSdk = 32

    defaultConfig {
        applicationId = "com.picrunner"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("keyStore.jks")
            storePassword = gradleLocalProperties(rootDir).getValue("storePassword").toString()
            keyAlias = gradleLocalProperties(rootDir).getValue("keyAlias").toString()
            keyPassword = gradleLocalProperties(rootDir).getValue("keyPassword").toString()
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false

        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    buildToolsVersion = "32.1.0-rc1"
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    //Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.2")

    //UI
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.13.0")

    //Lifecycle
    implementation("androidx.lifecycle:lifecycle-service:2.4.1")

    //ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")

    //Hilt
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.41")
    implementation("com.google.dagger:hilt-android:2.38.1")
    kapt("com.google.dagger:hilt-android-compiler:2.38.1")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    //OkHttp
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("androidx.room:room-ktx:2.4.2")
    implementation("androidx.room:room-runtime:2.4.2")

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

    //Serialization
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    //GMS
    implementation("com.google.android.gms:play-services-location:19.0.1")

    //Room
    kapt("androidx.room:room-compiler:2.4.2")
    implementation("androidx.room:room-runtime:2.4.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}