package host.bytedance

import zsu.ni.newInstance

class Container<T>(val content: T)

inline fun <reified T> myContainer(): Container<T> {
    val content = newInstance<T>() // <---- magic here!
    return Container(content)
}

class Foo

fun main() {
    val container = myContainer<Foo>()
    val foo = container.content // <---- It's [Foo] instance here!
}