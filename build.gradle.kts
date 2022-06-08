plugins {
    id("org.jetbrains.kotlin.android") version "1.6.21" apply false
    id("org.jetbrains.kotlin.jvm") version "1.6.21" apply false
    id("com.android.application") version "7.2.0" apply false
    id("com.android.library") version "7.2.0" apply false
}

buildscript {
    repositories {
        mavenCentral {
            metadataSources {
                mavenPom()
                artifact()
                ignoreGradleMetadataRedirection()
            }
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.41")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
