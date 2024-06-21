plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "1.2.1"
    id("jvmPublish")
    id("com.github.gmazzo.buildconfig")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
}

tasks.test {
    useJUnitPlatform()
}

val exGroup: String by project
val exVersion: String by project
val projectGitUrl: String by project

version = exVersion
group = exGroup

gradlePlugin {
    website = projectGitUrl
    vcsUrl = projectGitUrl
    plugins {
        create("kotlinNewInstance") {
            id = "host.bytedance.kotlin-newInstance"
            displayName = "inline newInstance without reflection."
            description = "inline newInstance without reflection."
            tags = listOf("kcp", "kotlin")
            implementationClass = "zsu.ni.kcp.NewInstanceGradlePlugin"
        }
    }
}

buildConfig {
    val kcpProject = project(":kotlin-newInstance-kcp")
    val runtimeProject = project(":kotlin-newInstance-runtime")
    packageName("$exGroup.ni")
    buildConfigField("String", "GROUP", "\"$exGroup\"")
    buildConfigField("String", "KCP_NAME", "\"${kcpProject.name}\"")
    buildConfigField("String", "RUNTIME_NAME", "\"${runtimeProject.name}\"")
    buildConfigField("String", "VERSION", "\"$exVersion\"")
}

