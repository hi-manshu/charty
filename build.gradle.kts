plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.mavenPublish) apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
        source.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
            "src/jsMain/kotlin",
            "src/wasmJsMain/kotlin",
            "src/jvmMain/kotlin"
        )
        ignoreFailures = false
    }

    plugins.withId("io.gitlab.arturbosch.detekt") {
        dependencies {
            add("detektPlugins", "io.nlopez.compose.rules:detekt:0.4.28")
        }
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
        android.set(true)
        verbose.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)

        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }
}

// Task to generate Compose compiler reports
tasks.register("generateComposeReports") {
    group = "compose"
    description = "Generate Compose compiler stability reports for all modules"

    dependsOn(
        ":charty:compileDebugKotlinAndroid",
        ":composeApp:compileDebugKotlinAndroid"
    )

    doLast {
        println("Compose reports generated in:")
        println("  - charty/build/compose_reports/")
        println("  - composeApp/build/compose_reports/")
    }
}

// Task to check Compose stability
tasks.register<Exec>("checkComposeStability") {
    group = "compose"
    description = "Analyze Compose stability and generate report"

    dependsOn("generateComposeReports")

    commandLine("python3", "scripts/analyze_compose_stability.py", ".", "stability_report.md")

    isIgnoreExitValue = true

    doLast {
        val reportFile = file("stability_report.md")
        if (reportFile.exists()) {
            println("\n✓ Stability report generated: stability_report.md")
            println("\nPreview:")
            println("=".repeat(60))
            reportFile.readLines().take(20).forEach { println(it) }
            if (reportFile.readLines().size > 20) {
                println("\n... (see stability_report.md for full report)")
            }
            println("=".repeat(60))
        }
    }
}

// Task to open compose reports directory
tasks.register("openComposeReports") {
    group = "compose"
    description = "Open the Compose reports directory"

    doLast {
        val chartyReports = file("charty/build/compose_reports")
        val composeAppReports = file("composeApp/build/compose_reports")

        if (chartyReports.exists()) {
            println("Opening: ${chartyReports.absolutePath}")
            project.exec {
                commandLine("open", chartyReports.absolutePath)
                isIgnoreExitValue = true
            }
        }

        if (composeAppReports.exists()) {
            println("Opening: ${composeAppReports.absolutePath}")
            project.exec {
                commandLine("open", composeAppReports.absolutePath)
                isIgnoreExitValue = true
            }
        }

        if (!chartyReports.exists() && !composeAppReports.exists()) {
            println("No Compose reports found. Run 'generateComposeReports' first.")
        }
    }
}

// Aggregate Dokka task to generate HTML docs for the charty module using Dokka v2
tasks.register("dokkaHtmlAll") {
    group = "documentation"
    description = "Generate Dokka v2 HTML documentation for all library modules"
    dependsOn(":charty:dokkaGeneratePublicationHtml")
}
