package org.firas.datetime

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 * @author Wu Yuping
 * @version 1.0.0
 * @since 1.0.0
 */
class DateTests {

    @Test
    fun testDate() {
        val date = Date()
        assertTrue(date.getTime() > (2019 - 1970) * 365L * 24L * 3600L)

        val random = Random.Default
        for (i in 1..1000) {
            val l = random.nextLong(0L, (2099 - 1970) * 365L * 24L * 3600L)
            date.setTime(l)
            assertEquals(l, date.getTime())
        }
    }
}