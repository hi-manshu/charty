import java.util.Properties

apply(plugin = "maven-publish")
apply(plugin = "signing")

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        artifact(javadocJar)

        pom {
            name.set(project.findProperty("POM_NAME")?.toString() ?: project.name)
            description.set(project.findProperty("POM_DESCRIPTION")?.toString() ?: "Charty - Compose Multiplatform Charts")
            url.set(project.findProperty("POM_URL")?.toString() ?: "https://github.com/hi-manshu/charty")

            licenses {
                license {
                    name.set(project.findProperty("POM_LICENCE_NAME")?.toString() ?: "The Apache Software License, Version 2.0")
                    url.set(project.findProperty("POM_LICENCE_URL")?.toString() ?: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set(project.findProperty("POM_LICENCE_DIST")?.toString() ?: "repo")
                }
            }

            developers {
                developer {
                    id.set(project.findProperty("POM_DEVELOPER_ID")?.toString() ?: "hi-manshu")
                    name.set(project.findProperty("POM_DEVELOPER_NAME")?.toString() ?: "Himanshu Singh")
                    url.set(project.findProperty("POM_DEVELOPER_URL")?.toString() ?: "https://github.com/hi-manshu")
                }
            }

            scm {
                url.set(project.findProperty("POM_SCM_URL")?.toString() ?: "https://github.com/hi-manshu/charty")
                connection.set("scm:git:git://github.com/hi-manshu/charty.git")
                developerConnection.set("scm:git:ssh://git@github.com/hi-manshu/charty.git")
            }
        }
    }

    repositories {
        maven {
            name = "central"
            url = uri("https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC")
            credentials {
                username = System.getenv("MAVEN_CENTRAL_USERNAME") ?: project.findProperty("mavenCentralUsername")?.toString()
                password = System.getenv("MAVEN_CENTRAL_PASSWORD") ?: project.findProperty("mavenCentralPassword")?.toString()
            }
        }

        // Fallback to OSSRH for compatibility
        maven {
            name = "ossrh"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_CENTRAL_USERNAME") ?: project.findProperty("mavenCentralUsername")?.toString()
                password = System.getenv("MAVEN_CENTRAL_PASSWORD") ?: project.findProperty("mavenCentralPassword")?.toString()
            }
        }
    }
}

configure<SigningExtension> {
    val signingKeyId = System.getenv("SIGNING_KEY_ID") ?: project.findProperty("signing.keyId")?.toString()
    val signingKey = System.getenv("SIGNING_KEY") ?: project.findProperty("signing.key")?.toString()
    val signingPassword = System.getenv("SIGNING_PASSWORD") ?: project.findProperty("signing.password")?.toString()

    if (signingKeyId != null && signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(extensions.getByType<PublishingExtension>().publications)
    }
}

// Make sure signing happens before publishing
tasks.withType<PublishToMavenRepository> {
    dependsOn(tasks.withType<Sign>())
}

