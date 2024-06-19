# kotlin-newInstance

## Usage

non-reflection used `newInstance` for multiplatform

## Apply plugin

Recommended version alignments:

| Kotlin | kotlin-newInstance-gradle |
|--------|---------------------------|
| 2.0.0  | \>= 0.0.1-beta            |

```kotlin
// Using the plugins DSL
plugins {
    id("host.bytedance.kotlin-newInstance") version "<latest>"
}

// or using legacy plugin application
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("host.bytedance:kotlin-newInstance-gradle:<latest>")
    }
}

apply(plugin = "host.bytedance.kotlin-newInstance")
```

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
