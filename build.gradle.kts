// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.0")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:11.3.2")
    }
}

plugins {
    id("com.android.application").version("8.2.0") apply false
    id("com.android.library").version("8.2.0") apply false
    id("org.jetbrains.kotlin.android").version("1.9.21") apply false
    kotlin("multiplatform").version("1.9.21") apply false
    id("org.jetbrains.compose").version("1.6.11") apply false
    id("org.jetbrains.dokka").version("1.9.0")
}
