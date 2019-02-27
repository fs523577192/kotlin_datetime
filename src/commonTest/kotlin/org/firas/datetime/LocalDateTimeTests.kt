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
class LocalDateTimeTests {

    @Test
    fun testNow() {
        assertTrue(LocalDateTime.now() > LocalDateTime.of(2019, 2, 27, 20, 0))
    }

    @Test
    fun testPlusAndMinus() {
        val random = Random.Default
        for (i in 1..1000) {
            val a = LocalDateTime.of(random.nextInt(1970, 2099),
                random.nextInt(1, 12), random.nextInt(1, 28),
                random.nextInt(0, 23), random.nextInt(0, 59),
                random.nextInt(0, 59))
            assertTrue(a.plusSeconds(random.nextLong(1L, 36500L * 24L * 3600L)) > a)
            assertTrue(a.plusMinutes(random.nextLong(1L, 36500L * 24L * 60L)) > a)
            assertTrue(a.plusHours(random.nextLong(1L, 36500L * 24L)) > a)
            assertTrue(a.plusDays(random.nextLong(1L, 36500L)) > a)
            assertTrue(a.plusMonths(random.nextLong(1L, 1200L)) > a)
            assertTrue(a.plusYears(random.nextLong(1L, 100L)) > a)

            assertTrue(a.minusSeconds(random.nextLong(1L, 36500L * 24L * 3600L)) < a)
            assertTrue(a.minusMinutes(random.nextLong(1L, 36500L * 24L * 60L)) < a)
            assertTrue(a.minusHours(random.nextLong(1L, 36500L * 24L)) < a)
            assertTrue(a.minusDays(random.nextLong(1L, 36500L)) < a)
            assertTrue(a.minusMonths(random.nextLong(1L, 1200L)) < a)
            assertTrue(a.minusYears(random.nextLong(1L, 100L)) < a)
        }
    }
}