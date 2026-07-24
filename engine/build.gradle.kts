plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

// Pure-Kotlin/JVM module: the whole game engine, no Android dependencies, so
// its tests run fast on any JVM (mirrors the iOS `HokmKit` package).
dependencies {
    api(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnit()
}
