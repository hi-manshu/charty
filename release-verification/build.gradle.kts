import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// The versions under test. Defaults match the current release so the check can be run by hand with
// no arguments; the release workflow passes whatever it is about to publish.
val chartyVersion: String =
    System.getenv("CHARTY_VERSION")
        ?: project.findProperty("chartyVersion")?.toString()
        ?: "3.0.0"

val charty3dVersion: String =
    System.getenv("CHARTY_3D_VERSION")
        ?: project.findProperty("charty3dVersion")?.toString()
        ?: "1.0.0"

kotlin {
    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    // Present because iOS is where a multiplatform publication most often turns out to be
    // incomplete: a missing .klib or a stale metadata variant resolves fine on the JVM and fails
    // only here. Configured on any host, but only buildable on macOS.
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("com.himanshoe:charty:$chartyVersion")
            implementation("com.himanshoe:charty-3d:$charty3dVersion")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
        }
    }
}

tasks.register("verifyRelease") {
    group = "verification"
    description = "Compiles a consumer against the published charty and charty-3d artifacts."

    // Only the targets buildable on this host. The iOS compilation is added on macOS, where it is
    // the whole reason this target is declared; asking for it on Linux would fail for a reason that
    // has nothing to do with the artifacts.
    dependsOn("compileKotlinJvm", "compileKotlinJs", "compileKotlinWasmJs")
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        dependsOn("compileKotlinIosSimulatorArm64")
    }

    // Reports what the versions actually resolved to, which is not always what was asked for. If a
    // consumer names an older charty than the one charty-3d was built against, Gradle upgrades it —
    // that is the metadata doing its job, and it should be visible in the log rather than inferred.
    // A charty-3d that resolves to something other than the requested version is a real problem: it
    // means the artifact under test is not the one being compiled.
    val resolvedVersions =
        configurations.named("jvmCompileClasspath").map { classpath ->
            classpath.incoming.resolutionResult.allComponents
                .map { component -> component.moduleVersion }
                .filter { module -> module?.group == "com.himanshoe" }
                .associate { module -> module!!.name to module.version }
        }

    doLast {
        val resolved = resolvedVersions.get()
        resolved.toSortedMap().forEach { (name, version) ->
            logger.lifecycle("   resolved $name:$version")
        }
        val actual3d = resolved["charty-3d"]
        if (actual3d != null && actual3d != charty3dVersion) {
            throw GradleException(
                "charty-3d resolved to $actual3d, not the requested $charty3dVersion — " +
                    "the artifact under test is not the one being compiled.",
            )
        }
        // Reports what resolved, never what was asked for. A check that announces success using the
        // requested version would claim to have compiled against something it did not touch.
        val actualCharty = resolved["charty"] ?: chartyVersion
        logger.lifecycle("✅ charty:$actualCharty and charty-3d:${actual3d ?: charty3dVersion} resolve and compile.")
    }
}
