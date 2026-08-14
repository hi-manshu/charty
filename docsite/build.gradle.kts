plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(libs.jetbrains.markdown)
}

kotlin {
    jvmToolchain(17)
}

// The shared detekt configuration lists the multiplatform source sets, which a plain JVM module does
// not have. Added here rather than to the root, so the generator is analysed without also dragging in
// the JMH fixtures under :benchmark — code whose whole job is hard-coded numbers, and which a rule set
// written for library code has nothing useful to say about.
detekt {
    source.setFrom("src/main/kotlin")
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

/**
 * The whole published site: documentation at the root, playground under `/playground`.
 *
 * Assembled by the build rather than by the deploy workflow, so what runs locally is byte for byte
 * what ships. A site whose parts are only ever joined together on CI is a site whose links can only
 * be tested on CI.
 */
val assembleSite by tasks.registering(Sync::class) {
    group = "documentation"
    description = "Assembles the documentation site with the playground under /playground."

    into(layout.buildDirectory.dir("published-site"))

    from(generateDocsSite)
    from(project(":composeApp").tasks.named("wasmJsBrowserDistribution")) {
        into("playground")
    }

    /*
     * Stamps the playground's script tag with the hash of the script itself.
     *
     * The WebAssembly bundle is content-hashed but composeApp.js is not, and the two ship together.
     * A browser holding a cached composeApp.js from an earlier deploy asks for the bundle that build
     * referenced — a file the current deploy no longer contains — and the playground dies on a 404
     * with nothing on screen. Naming the script by its own content makes every deploy a new URL, so
     * a stale copy can never be paired with a bundle that has been deleted.
     */
    // Resolved here rather than inside doLast: a task action that reaches back into the project at
    // execution time cannot be stored in the configuration cache.
    val scriptReference = "composeApp.js"
    val playgroundDir = layout.buildDirectory.dir("published-site/playground")

    doLast {
        val playground = playgroundDir.get().asFile
        val script = playground.resolve(scriptReference)
        val page = playground.resolve("index.html")
        if (script.isFile && page.isFile) {
            val fingerprint =
                script
                    .readBytes()
                    .contentHashCode()
                    .toUInt()
                    .toString(radix = 16)
            page.writeText(page.readText().replace(scriptReference, "$scriptReference?v=$fingerprint"))
            println("Playground script stamped: $scriptReference?v=$fingerprint")
        }
    }
}
