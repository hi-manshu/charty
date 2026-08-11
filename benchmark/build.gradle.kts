plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxBenchmark)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.charty)
    implementation(libs.kotlinx.benchmark.runtime)
}

benchmark {
    configurations {
        named("main") {
            // Kept modest so a full run finishes quickly in CI/dev; raise for publishable numbers.
            warmups = 3
            iterations = 5
            iterationTime = 300
            iterationTimeUnit = "ms"
        }
    }
    targets {
        register("main")
    }
}
