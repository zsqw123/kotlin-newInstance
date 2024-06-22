package zsu.ni.kcp.test

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.JvmTarget
import zsu.ni.kcp.NewInstanceKCP

@OptIn(ExperimentalCompilerApi::class)
fun compileKotlin(vararg kotlinSource: SourceFile): JvmCompilationResult {
    return KotlinCompilation().apply {
        jvmTarget = JvmTarget.JVM_17.description
        sources = kotlinSource.toList()
        compilerPluginRegistrars = listOf(NewInstanceKCP())
        inheritClassPath = true
        messageOutputStream = System.out
    }.compile()
}

