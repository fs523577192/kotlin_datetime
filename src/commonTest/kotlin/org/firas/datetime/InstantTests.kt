package org.firas.datetime

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 *
 * @author Wu Yuping
 * @version 1.0.0
 * @since 1.0.0
 */
class InstantTests {

    @Test
    fun testNow() {
        assertTrue(Instant.now().epochSecond > (2019 - 1970) * 365L * 24L * 3600L)
    }

    @Test
    fun testOfEpochSecond() {
        val random = Random.Default
        for (i in 1..1000) {
            val a = random.nextLong(0L, (2099 - 1970) * 365L * 24L * 3600L)
            val b = random.nextLong(0L, (2099 - 1970) * 365L * 24L * 3600L)
            val aa = Instant.ofEpochSecond(a)
            val bb = Instant.ofEpochSecond(b)
            assertTrue(if (a > b) aa > bb else if (a < b) aa < bb else aa == bb)
        }
    }
}