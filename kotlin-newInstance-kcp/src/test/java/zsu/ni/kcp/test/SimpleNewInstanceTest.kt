package zsu.ni.kcp.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.JvmTarget
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import zsu.ni.kcp.NewInstanceKCP

@OptIn(ExperimentalCompilerApi::class)
class SimpleNewInstanceTest {
    @Test
    fun `simple new instance`() {
        val kotlinSource = SourceFile.kotlin(
            "Foo.kt", """
        package zsu.test

        import zsu.ni.newInstance

        inline fun <reified T> from(): T {
            return newInstance()
        }

        class Foo {
            val c = 114514
        }

        class Bar {
            val foo = from<Foo>()
            fun call() = foo.c
        }
    """
        )

        val compilation = KotlinCompilation().apply {
            jvmTarget = JvmTarget.JVM_17.description
            sources = listOf(kotlinSource)
            compilerPluginRegistrars = listOf(NewInstanceKCP())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()
        println(compilation.generatedFiles)
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)

        val classLoader = compilation.classLoader
        val barClass = classLoader.loadClass("zsu.test.Bar")
        val callMethod = barClass.getMethod("call")
        val barInstance = barClass.getConstructor().newInstance()
        val result = callMethod.invoke(barInstance)
        Assertions.assertEquals(114514, result)
    }
}