import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
}

group = System.getenv("GROUP") ?: project.findProperty("GROUP")?.toString() ?: "com.himanshoe"
version = System.getenv("VERSION_NAME") ?: project.findProperty("VERSION_NAME")?.toString() ?: "1.0.0"

composeCompiler {
    metricsDestination.set(project.layout.buildDirectory.dir("compose_metrics"))
    reportsDestination.set(project.layout.buildDirectory.dir("compose_reports"))
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "charty"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.himanshoe.charty"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    moduleName.set("charty")

    dokkaSourceSets.configureEach {
        when (name) {
            "commonMain" -> {
                sourceRoots.from(file("src/commonMain/kotlin"))
            }
        }
    }
}

// Apply publishing configuration
apply(from = "${rootProject.projectDir}/gradle/publish.gradle.kts")

