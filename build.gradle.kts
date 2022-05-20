plugins {
    id("org.jetbrains.kotlin.android") version "1.6.21" apply false
    id("org.jetbrains.kotlin.jvm") version "1.6.21" apply false
    id("com.android.application") version "7.1.3" apply false
    id("com.android.library") version "7.1.3" apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
