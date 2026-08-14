import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.roborazzi)
}

group = System.getenv("GROUP") ?: project.findProperty("GROUP")?.toString() ?: "com.himanshoe"

// charty-3d carries its own version, because it has its own history. It arrived at Charty 3.0 with
// no releases behind it, and numbering it 3.0.0 would claim two major versions it never had. Its
// dependency on charty is a project dependency, so whatever version charty is built at here is the
// version the published metadata requires — the two can move independently without ever resolving
// against a pairing that was not built and tested together.
version =
    System.getenv("VERSION_NAME_3D")
        ?: project.findProperty("VERSION_NAME_3D")?.toString()
        ?: "1.0.0-SNAPSHOT"

kotlin {
    sourceSets.all {
        languageSettings.optIn("com.himanshoe.charty.common.annotation.ChartyExperimental")
    }

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
            baseName = "charty3d"
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
        commonMain.dependencies {
            api(projects.charty)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        getByName("androidUnitTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.junit4)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
        }
    }
}

android {
    namespace = "com.himanshoe.charty3d"
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

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

// The publishing plugin stamps every publication with the VERSION_NAME property, which is charty's
// version. This puts charty-3d's own version back on them. Without it the artifact would publish at
// charty's version whatever VERSION_NAME_3D said, and the mismatch would surface only once it was on
// Maven Central and could not be taken back.
publishing {
    publications.withType<MavenPublication>().configureEach {
        version = project.version.toString()
    }
}

dokka {
    moduleName.set("charty-3d")
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    pom {
        name.set("Charty 3D")
        description.set("Projected three-dimensional charts for Charty, the Compose Multiplatform chart library")
        inceptionYear.set("2026")
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
