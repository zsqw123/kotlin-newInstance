package host.bytedance

import zsu.ni.newInstance

class Container<T>(val content: T)

inline fun <reified T> myContainer(): Container<T> {
    val content = newInstance<T>() // <---- magic here!
    return Container(content)
}

inline fun <reified T, reified P> myContainer(arg: P): Container<T> {
    val content: T = newInstance(arg) // <---- magic here!
    return Container(content)
}

class Foo
class Bar(val arg: Int)

fun main() {
    val container = myContainer<Foo>()
    val foo = container.content // <---- It's [Foo] instance here!
    val barContainer: Container<Bar> = myContainer(114514)
    val bar = container.content // <---- It's [Bar] instance with arg=114514 here!
}