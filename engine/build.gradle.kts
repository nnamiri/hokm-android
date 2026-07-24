plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Pure-Kotlin/JVM module: the whole game engine, no Android dependencies, so
// its tests run fast on any JVM (mirrors the iOS `HokmKit` package).
dependencies {
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnit()
}
