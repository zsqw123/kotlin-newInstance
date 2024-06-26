# kotlin-newInstance

## Usage

non-reflection `newInstance` by Kotlin inline reified type parameter.

```kotlin
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
```

It also supports passing argument into `newInstance` function.

```kotlin
inline fun <reified T, reified P> myContainer(arg: P): Container<T> {
    val content: T = newInstance(arg) // <---- magic here!
    return Container(content)
}

class Bar(val arg: Int)

fun main() {
    val barContainer: Container<Bar> = myContainer(114514)
    val bar = container.content // <---- It's [Bar] instance with arg=114514 here!
}
```

## Apply plugin

Recommended version alignments:

| Kotlin | kotlin-newInstance-gradle |
|--------|---------------------------|
| 2.0.0  | \>= 0.0.3-beta            |

```kotlin
// Using the plugins DSL
plugins {
    id("host.bytedance.kotlin-newInstance") version "<latest>"
}

// or using legacy plugin application
buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("host.bytedance:kotlin-newInstance-gradle:<latest>")
    }
}

apply(plugin = "host.bytedance.kotlin-newInstance")
```

The plugin will be published both **gradle plugin portal**(at first) and **maven central**.

- maven central: [![Maven Central](https://img.shields.io/maven-central/v/host.bytedance/kotlin-newInstance-gradle)](https://central.sonatype.com/artifact/host.bytedance/kotlin-newInstance-gradle)
- gradle plugin portal: [![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/host.bytedance.kotlin-newInstance)](https://plugins.gradle.org/plugin/host.bytedance.kotlin-newInstance)

## License

```
Copyright 2024 zsqw123

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
