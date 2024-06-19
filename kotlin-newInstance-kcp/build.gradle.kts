plugins {
    kotlin("jvm")
    id("jvmPublish")
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(project(":kotlin-newInstance-runtime"))

    ksp(D.autoServiceKsp)
    implementation(D.autoService)
    compileOnly(D.compilerEmbeddable)

    testImplementation(D.jUnitJupiterApi)
    testRuntimeOnly(D.jUnitJupiterEngine)
    testImplementation(D.compileTesting)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}
