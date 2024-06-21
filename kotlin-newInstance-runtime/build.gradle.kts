plugins {
    kotlin("jvm")
    id("jvmPublish")
//    kotlin("multiplatform")
//    id("kmpPublish")
}

// due to kcp has no such extension point
// I don't want to support multiplatform currently.

//kotlin {
//    jvm()
//    js {
//        browser()
//        nodejs()
//    }
//}
