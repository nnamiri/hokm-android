package eu.amiri.hokm.engine

/**
 * Deterministic SplitMix64 generator so deals can be reproduced in tests.
 * Same algorithm as the iOS engine's `SeededGenerator`.
 *
 * Note: the Kotlin [shuffled] below is a standard Fisher–Yates using this
 * generator. It is deterministic within the Android app but **not** byte-
 * identical to Swift's `Array.shuffled(using:)`, so it is not suitable for
 * cross-play with the iOS build without aligning the shuffle algorithm.
 */
class SeededGenerator(seed: Long) {
    private var state: Long = if (seed == 0L) -0x61c8864680b583 else seed // 0x9E3779B97F4A7C15

    fun next(): Long {
        state += -0x61c8864680b583L // 0x9E3779B97F4A7C15
        var z = state
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L // 0xBF58476D1CE4E5B9
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L // 0x94D049BB133111EB
        return z xor (z ushr 31)
    }

    /** A non-negative Int in 0 until [bound] (bound > 0). */
    fun nextInt(bound: Int): Int {
        val r = next() and Long.MAX_VALUE // drop sign
        return (r % bound).toInt()
    }
}

/** Deterministic Fisher–Yates shuffle driven by [rng]. */
fun <T> List<T>.shuffled(rng: SeededGenerator): List<T> {
    val out = this.toMutableList()
    for (i in out.indices.reversed()) {
        val j = rng.nextInt(i + 1)
        val tmp = out[i]; out[i] = out[j]; out[j] = tmp
    }
    return out
}
