plugins {
    id("kotlin")
    id("org.jetbrains.kotlin.plugin.serialization") version("1.6.21")
    `java-library`
}

dependencies {
    implementation(project(":domain"))

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    //OkHttp
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

    //Serialization
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
}