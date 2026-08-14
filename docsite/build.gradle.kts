plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(libs.jetbrains.markdown)
}

kotlin {
    jvmToolchain(17)
}

/**
 * Renders `docs/` into a static site under `build/site`.
 *
 * Kept as a JVM module rather than a script so the generator is ordinary Kotlin: it compiles, it is
 * linted with everything else, and a mistake in it is a compile error rather than a broken page
 * discovered after deployment.
 */
val generateDocsSite by tasks.registering(JavaExec::class) {
    group = "documentation"
    description = "Renders docs/**/*.md into a static site under build/site."

    mainClass.set("com.himanshoe.docsite.MainKt")
    classpath = sourceSets.getByName("main").runtimeClasspath

    val docsDir = rootProject.layout.projectDirectory.dir("docs/charty")
    val brandDir = rootProject.layout.projectDirectory.dir("img")
    val outputDir = layout.buildDirectory.dir("site")

    inputs.dir(docsDir).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.dir(brandDir).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.dir(layout.projectDirectory.dir("src/main/resources")).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(outputDir)

    argumentProviders.add(
        CommandLineArgumentProvider {
            listOf(
                docsDir.asFile.absolutePath,
                outputDir.get().asFile.absolutePath,
                brandDir.asFile.absolutePath,
            )
        },
    )
}
