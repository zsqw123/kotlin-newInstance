package zsu.ni.kcp.test

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

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

        val compilation = compileKotlin(kotlinSource)
        println(compilation.generatedFiles)
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)

        val classLoader = compilation.classLoader
        val barClass = classLoader.loadClass("zsu.test.Bar")
        val callMethod = barClass.getMethod("call")
        val barInstance = barClass.getConstructor().newInstance()
        val result = callMethod.invoke(barInstance)
        Assertions.assertEquals(114514, result)
    }

    @Test
    fun `new instance with args`() {
        val kotlinSource = SourceFile.kotlin(
            "Foo.kt", """
        package zsu.test

        import zsu.ni.newInstance

        inline fun <reified T, reified P> from(payload: P): T {
            val test = 2
            val str = test.toString()
            return newInstance(payload)
        }

        class Foo(val i: Int)

        class Bar {
            val test = 4
            val foo: Foo = from(1918)
            fun call() = foo.i
        }
    """
        )

        val compilation = compileKotlin(kotlinSource)
        println(compilation.generatedFiles)
        Assertions.assertEquals(KotlinCompilation.ExitCode.OK, compilation.exitCode)

        val classLoader = compilation.classLoader
        val barClass = classLoader.loadClass("zsu.test.Bar")
        val callMethod = barClass.getMethod("call")
        val barInstance = barClass.getConstructor().newInstance()
        val result = callMethod.invoke(barInstance)
        Assertions.assertEquals(1918, result)
    }
}