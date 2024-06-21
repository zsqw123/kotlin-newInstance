package zsu.cacheable.kcp.test

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

        class Foo        

        fun main() {
            val foo = from<Foo>()
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
    }
}