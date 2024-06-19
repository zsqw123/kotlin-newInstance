plugins {
    kotlin("multiplatform")
    id("kmpPublish")
}

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }
}

dependencies {

}
