import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import java.util.*

plugins {
    `maven-publish`
    signing
}

val properties = Properties()

val publishPropertiesFile = File(System.getProperty("user.home"), ".gradle/publish.properties")

val canPublish = publishPropertiesFile.exists()

publishPropertiesFile.takeIf { canPublish }?.reader()?.use {
    properties.load(it)
}

val isJvmPublish = extensions.findByType<KotlinJvmProjectExtension>() != null

require(isJvmPublish) {
    "not a jvm module to publish"
}

if (canPublish) {
    println("need publish! found publish property file")
    ext["signing.keyId"] = properties["signing.keyId"]
    ext["signing.password"] = properties["signing.password"]
    ext["signing.secretKeyRingFile"] = properties["signing.secretKeyRingFile"]
    val mavenCentralUsername: String by properties
    val mavenCentralPassword: String by properties
    val githubEmail: String by properties
    val projectGitUrl: String by project
    val exVersion: String by project
    val mavenArtifactId: String = property("MAVEN_ARTIFACT") as? String ?: project.name

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
    }

    publishing {
        // Configure maven central repository
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = mavenCentralUsername
                    password = mavenCentralPassword
                }
            }
        }

        val sourcesJar by tasks.registering(Jar::class) {
            archiveClassifier.set("sources")
            from(kotlinExtension.sourceSets.getByName("main").kotlin.srcDirs)
        }
        publications {
            create<MavenPublication>("maven") {
                from(components.getByName("java"))
                artifact(sourcesJar)
            }
        }

        // Configure all publications
        afterEvaluate {
            publications.withType<MavenPublication> {
                groupId = "host.bytedance"
                version = exVersion
                artifactId =  mavenArtifactId
                // Stub javadoc.jar artifact
                if (artifacts.none { it.classifier == "javadoc" }) artifact(javadocJar) {
                    classifier = "javadoc"
                }
            }
        }


        publications.withType<MavenPublication> {
            // Provide artifacts information requited by Maven Central
            pom {
                name.set("kotlin-newInstance")
                description.set("inline newInstance without reflection")
                url.set(projectGitUrl)

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("zsqw123")
                        name.set("zsub")
                        email.set(githubEmail)
                    }
                }
                scm {
                    url.set(projectGitUrl)
                }
            }
        }
    }

    // Signing artifacts. `signing.*` extra properties values will be used
    signing {
//        val signTasks =
        sign(publishing.publications)
//        afterEvaluate {
//            tasks.withType(AbstractPublishToMaven::class.java)
//                .forEach {
//                    it.mustRunAfter(signTasks)
//                }
//        }
    }
}
