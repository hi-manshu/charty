plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.detekt) apply false
}

// Configure Detekt for all subprojects
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))

        // Explicitly set source directories
        source.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
            "src/jsMain/kotlin",
            "src/wasmJsMain/kotlin",
            "src/jvmMain/kotlin"
        )

        // Make sure detekt fails on issues
        ignoreFailures = false
    }

    plugins.withId("io.gitlab.arturbosch.detekt") {
        dependencies {
            add("detektPlugins", project(":detekt-rules"))
        }
    }
}