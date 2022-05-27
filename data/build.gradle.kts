import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    kotlin("android")
    id("com.android.library")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version ("1.6.21")
    id("com.stepango.aar2jar") version "0.6"
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    }
}

android {
    namespace = "com.picrunner.data"
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}

dependencies {
    implementation(project(":domain"))

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    //OkHttp
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    //Room
    kapt("androidx.room:room-compiler:2.4.2")
    implementation("androidx.room:room-common:2.4.2")
    implementation("androidx.room:room-ktx:2.4.2")
    implementation("androidx.room:room-runtime:2.4.2")

    //Serialization
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
}