import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
}

group = System.getenv("GROUP") ?: project.findProperty("GROUP")?.toString() ?: "com.himanshoe"
version = System.getenv("VERSION_NAME") ?: project.findProperty("VERSION_NAME")?.toString()

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

// Configure Maven publishing using Vanniktech plugin
mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()


    pom {
        name.set("Charty")
        description.set("An Elementary Compose Multiplatform Chart library")
        inceptionYear.set("2025")
        url.set("https://github.com/hi-manshu/charty")

        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("hi-manshu")
                name.set("Himanshu Singh")
                url.set("https://github.com/hi-manshu")
            }
        }

        scm {
            url.set("https://github.com/hi-manshu/charty")
            connection.set("scm:git:git://github.com/hi-manshu/charty.git")
            developerConnection.set("scm:git:ssh://git@github.com/hi-manshu/charty.git")
        }
    }
}


