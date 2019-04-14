/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firas.datetime

import org.firas.datetime.LocalTime.Companion.MINUTES_PER_HOUR
import org.firas.datetime.LocalTime.Companion.NANOS_PER_MILLI
import org.firas.datetime.LocalTime.Companion.NANOS_PER_SECOND
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_DAY
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_HOUR
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_MINUTE
import org.firas.datetime.temporal.*
import org.firas.math.BigDecimal
import org.firas.math.BigInteger
import org.firas.datetime.util.MathUtils

/**
 * A time-based amount of time, such as '34.5 seconds'.
 *
 *
 * This class models a quantity or amount of time in terms of seconds and nanoseconds.
 * It can be accessed using other duration-based units, such as minutes and hours.
 * In addition, the {@link ChronoUnit#DAYS DAYS} unit can be used and is treated as
 * exactly equal to 24 hours, thus ignoring daylight savings effects.
 * See [Period] for the date-based equivalent to this class.
 *
 *
 * A physical duration could be of infinite length.
 * For practicality, the duration is stored with constraints similar to {@link Instant}.
 * The duration uses nanosecond resolution with a maximum value of the seconds that can
 * be held in a {@code long}. This is greater than the current estimated age of the universe.
 *
 *
 * The range of a duration requires the storage of a number larger than a {@code long}.
 * To achieve this, the class stores a {@code long} representing seconds and an {@code int}
 * representing nanosecond-of-second, which will always be between 0 and 999,999,999.
 * The model is of a directed duration, meaning that the duration may be negative.
 *
 *
 * The duration is measured in "seconds", but these are not necessarily identical to
 * the scientific "SI second" definition based on atomic clocks.
 * This difference only impacts durations measured near a leap-second and should not affect
 * most applications.
 * See [Instant] for a discussion as to the meaning of the second and time-scales.
 *
 *
 *
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`===`), identity hash code, or synchronization) on instances of
 * `Duration` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class Duration private constructor(
        val seconds: Long,
        private val nanos: Int): TemporalAmount, Comparable<Duration> {

    companion object {
        /**
         * Constant for a duration of zero.
         */
        val ZERO = Duration(0, 0)

        private val UNITS = listOf<TemporalUnit>(ChronoUnit.SECONDS, ChronoUnit.NANOS)

        /**
         * Serialization version.
         */
        private val serialVersionUID = 3078945930695997490L

        /**
         * Constant for nanos per second.
         */
        private val BI_NANOS_PER_SECOND = BigInteger.valueOf(LocalTime.NANOS_PER_SECOND)

        /**
         * Obtains a `Duration` representing a number of seconds.
         *
         *
         * The nanosecond in second field is set to zero.
         *
         * @param seconds  the number of seconds, positive or negative
         * @return a `Duration`, not null
         */
        fun ofSeconds(seconds: Long): Duration {
            return create(seconds, 0)
        }

        /**
         * Obtains a `Duration` representing a number of seconds and an
         * adjustment in nanoseconds.
         *
         *
         * This method allows an arbitrary number of nanoseconds to be passed in.
         * The factory will alter the values of the second and nanosecond in order
         * to ensure that the stored nanosecond is in the range 0 to 999,999,999.
         * For example, the following will result in exactly the same duration:
         * <pre>
         * Duration.ofSeconds(3, 1);
         * Duration.ofSeconds(4, -999_999_999);
         * Duration.ofSeconds(2, 1000_000_001);
         * </pre>
         *
         * @param seconds  the number of seconds, positive or negative
         * @param nanoAdjustment  the nanosecond adjustment to the number of seconds, positive or negative
         * @return a `Duration`, not null
         * @throws ArithmeticException if the adjustment causes the seconds to exceed the capacity of `Duration`
         */
        fun ofSeconds(seconds: Long, nanoAdjustment: Long): Duration {
            val secs = MathUtils.addExact(seconds, MathUtils.floorDiv(nanoAdjustment,
                    LocalTime.NANOS_PER_SECOND))
            val nos = MathUtils.floorMod(nanoAdjustment,
                    LocalTime.NANOS_PER_SECOND).toInt()
            return create(secs, nos)
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains a `Duration` representing a number of milliseconds.
         *
         *
         * The seconds and nanoseconds are extracted from the specified milliseconds.
         *
         * @param millis  the number of milliseconds, positive or negative
         * @return a `Duration`, not null
         */
        fun ofMillis(millis: Long): Duration {
            var secs = millis / 1000
            var mos = (millis % 1000).toInt()
            if (mos < 0) {
                mos += 1000
                secs -= 1
            }
            return create(secs, mos * 1000000)
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains a `Duration` representing a number of nanoseconds.
         *
         *
         * The seconds and nanoseconds are extracted from the specified nanoseconds.
         *
         * @param nanos  the number of nanoseconds, positive or negative
         * @return a `Duration`, not null
         */
        fun ofNanos(nanos: Long): Duration {
            var secs = nanos / LocalTime.NANOS_PER_SECOND
            var nos = (nanos % LocalTime.NANOS_PER_SECOND).toInt()
            if (nos < 0) {
                nos += LocalTime.NANOS_PER_SECOND.toInt()
                secs -= 1
            }
            return create(secs, nos)
        }

        private fun create(negate: Boolean,
                           daysAsSecs: Long,
                           hoursAsSecs: Long,
                           minsAsSecs: Long,
                           secs: Long,
                           nanos: Int): Duration {
            val seconds = MathUtils.addExact(daysAsSecs, MathUtils.addExact(hoursAsSecs,
                    MathUtils.addExact(minsAsSecs, secs)))
            return if (negate) {
                ofSeconds(seconds, nanos.toLong()).negated()
            } else {
                ofSeconds(seconds, nanos.toLong())
            }
        }

        /**
         * Obtains an instance of `Duration` using seconds and nanoseconds.
         *
         * @param seconds  the length of the duration in seconds, positive or negative
         * @param nanoAdjustment  the nanosecond adjustment within the second, from 0 to 999,999,999
         */
        private fun create(seconds: Long, nanoAdjustment: Int): Duration {
            return if (seconds or nanoAdjustment.toLong() == 0L) {
                ZERO
            } else {
                Duration(seconds, nanoAdjustment)
            }
        }

        /**
         * Creates an instance of `Duration` from a number of seconds.
         *
         * @param seconds  the number of seconds, up to scale 9, positive or negative
         * @return a `Duration`, not null
         * @throws ArithmeticException if numeric overflow occurs
         */
        private fun create(seconds: BigDecimal): Duration {
            val nanos = seconds.movePointRight(9).toBigIntegerExact()
            val divRem = nanos.divideAndRemainder(BI_NANOS_PER_SECOND)
            if (divRem[0].bitLength() > 63) {
                throw ArithmeticException("Exceeds capacity of Duration: $nanos")
            }
            return ofSeconds(divRem[0].toLong(), divRem[1].toLong())
        }
    } // companion object

    /**
     * Gets the value of the requested unit.
     *
     *
     * This returns a value for each of the two supported units,
     * [SECONDS][ChronoUnit.SECONDS] and [NANOS][ChronoUnit.NANOS].
     * All other units throw an exception.
     *
     * @param unit the `TemporalUnit` for which to return the value
     * @return the long value of the unit
     * @throws DateTimeException if the unit is not supported
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     */
    override fun get(unit: TemporalUnit): Long {
        return when (unit) {
            ChronoUnit.SECONDS -> seconds
            ChronoUnit.NANOS -> nanos.toLong()
            else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
        }
    }

    /**
     * Gets the set of units supported by this duration.
     *
     *
     * The supported units are [SECONDS][ChronoUnit.SECONDS],
     * and [NANOS][ChronoUnit.NANOS].
     * They are returned in the order seconds, nanos.
     *
     *
     * This set can be used in conjunction with [.get]
     * to access the entire state of the duration.
     *
     * @return a list containing the seconds and nanos units, not null
     */
    override fun getUnits(): List<TemporalUnit> {
        return UNITS
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the number of days in this duration.
     *
     *
     * This returns the total number of days in the duration by dividing the
     * number of seconds by 86400.
     * This is based on the standard definition of a day as 24 hours.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the number of days in the duration, may be negative
     */
    fun toDays(): Long {
        return seconds / SECONDS_PER_DAY
    }

    /**
     * Gets the number of hours in this duration.
     *
     *
     * This returns the total number of hours in the duration by dividing the
     * number of seconds by 3600.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the number of hours in the duration, may be negative
     */
    fun toHours(): Long {
        return seconds / SECONDS_PER_HOUR
    }

    /**
     * Gets the number of minutes in this duration.
     *
     *
     * This returns the total number of minutes in the duration by dividing the
     * number of seconds by 60.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the number of minutes in the duration, may be negative
     */
    fun toMinutes(): Long {
        return seconds / SECONDS_PER_MINUTE
    }

    /**
     * Gets the number of seconds in this duration.
     *
     *
     * This returns the total number of whole seconds in the duration.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the whole seconds part of the length of the duration, positive or negative
     * @since 9
     */
    fun toSeconds(): Long {
        return seconds
    }

    /**
     * Converts this duration to the total length in milliseconds.
     *
     *
     * If this duration is too large to fit in a `long` milliseconds, then an
     * exception is thrown.
     *
     *
     * If this duration has greater than millisecond precision, then the conversion
     * will drop any excess precision information as though the amount in nanoseconds
     * was subject to integer division by one million.
     *
     * @return the total length of the duration in milliseconds
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun toMillis(): Long {
        var tempSeconds = seconds
        var tempNanos = nanos.toLong()
        if (tempSeconds < 0) {
            // change the seconds and nano value to
            // handle Long.MIN_VALUE case
            tempSeconds += 1
            tempNanos -= NANOS_PER_SECOND
        }
        var millis = MathUtils.multiplyExact(tempSeconds, 1000)
        millis = MathUtils.addExact(millis, tempNanos / NANOS_PER_MILLI)
        return millis
    }

    /**
     * Converts this duration to the total length in nanoseconds expressed as a `long`.
     *
     *
     * If this duration is too large to fit in a `long` nanoseconds, then an
     * exception is thrown.
     *
     * @return the total length of the duration in nanoseconds
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun toNanos(): Long {
        var tempSeconds = seconds
        var tempNanos = nanos.toLong()
        if (tempSeconds < 0) {
            // change the seconds and nano value to
            // handle Long.MIN_VALUE case
            tempSeconds += 1
            tempNanos -= NANOS_PER_SECOND
        }
        var totalNanos = MathUtils.multiplyExact(tempSeconds, NANOS_PER_SECOND)
        totalNanos = MathUtils.addExact(totalNanos, tempNanos)
        return totalNanos
    }

    /**
     * Extracts the number of days in the duration.
     *
     *
     * This returns the total number of days in the duration by dividing the
     * number of seconds by 86400.
     * This is based on the standard definition of a day as 24 hours.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the number of days in the duration, may be negative
     * @since 9
     */
    fun toDaysPart(): Long {
        return seconds / SECONDS_PER_DAY
    }

    /**
     * Extracts the number of hours part in the duration.
     *
     *
     * This returns the number of remaining hours when dividing [.toHours]
     * by hours in a day.
     * This is based on the standard definition of a day as 24 hours.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the number of hours part in the duration, may be negative
     * @since 9
     */
    fun toHoursPart(): Int {
        return (toHours() % 24).toInt()
    }

    /**
     * Extracts the number of minutes part in the duration.
     *
     *
     * This returns the number of remaining minutes when dividing [.toMinutes]
     * by minutes in an hour.
     * This is based on the standard definition of an hour as 60 minutes.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the number of minutes parts in the duration, may be negative
     * @since 9
     */
    fun toMinutesPart(): Int {
        return (toMinutes() % MINUTES_PER_HOUR).toInt()
    }

    /**
     * Extracts the number of seconds part in the duration.
     *
     *
     * This returns the remaining seconds when dividing [.toSeconds]
     * by seconds in a minute.
     * This is based on the standard definition of a minute as 60 seconds.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the number of seconds parts in the duration, may be negative
     * @since 9
     */
    fun toSecondsPart(): Int {
        return (seconds % SECONDS_PER_MINUTE).toInt()
    }

    /**
     * Extracts the number of milliseconds part of the duration.
     *
     *
     * This returns the milliseconds part by dividing the number of nanoseconds by 1,000,000.
     * The length of the duration is stored using two fields - seconds and nanoseconds.
     * The nanoseconds part is a value from 0 to 999,999,999 that is an adjustment to
     * the length in seconds.
     * The total duration is defined by calling [.getNano] and [.getSeconds].
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the number of milliseconds part of the duration.
     * @since 9
     */
    fun toMillisPart(): Int {
        return nanos / 1000000
    }

    /**
     * Get the nanoseconds part within seconds of the duration.
     *
     *
     * The length of the duration is stored using two fields - seconds and nanoseconds.
     * The nanoseconds part is a value from 0 to 999,999,999 that is an adjustment to
     * the length in seconds.
     * The total duration is defined by calling [.getNano] and [.getSeconds].
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the nanoseconds within the second part of the length of the duration, from 0 to 999,999,999
     * @since 9
     */
    fun toNanosPart(): Int {
        return nanos
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the specified duration added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param duration  the duration to add, positive or negative, not null
     * @return a `Duration` based on this duration with the specified duration added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    operator fun plus(duration: Duration): Duration {
        return plus(duration.seconds, duration.nanos.toLong())
    }

    /**
     * Returns a copy of this duration with the specified duration added.
     *
     *
     * The duration amount is measured in terms of the specified unit.
     * Only a subset of units are accepted by this method.
     * The unit must either have an [exact duration][TemporalUnit.isDurationEstimated] or
     * be [ChronoUnit.DAYS] which is treated as 24 hours. Other units throw an exception.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToAdd  the amount to add, measured in terms of the unit, positive or negative
     * @param unit  the unit that the amount is measured in, must have an exact duration, not null
     * @return a `Duration` based on this duration with the specified duration added, not null
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plus(amountToAdd: Long, unit: TemporalUnit): Duration {
        if (unit === ChronoUnit.DAYS) {
            return plus(MathUtils.multiplyExact(amountToAdd, SECONDS_PER_DAY.toLong()), 0)
        }
        if (unit.isDurationEstimated()) {
            throw UnsupportedTemporalTypeException("Unit must not have an estimated duration")
        }
        if (amountToAdd == 0L) {
            return this
        }
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.NANOS -> plusNanos(amountToAdd)
                ChronoUnit.MICROS ->
                    plusSeconds(amountToAdd / (1000_000L * 1000) * 1000).plusNanos(amountToAdd % (1000_000L * 1000) * 1000)
                ChronoUnit.MILLIS -> plusMillis(amountToAdd)
                ChronoUnit.SECONDS -> plusSeconds(amountToAdd)
                else -> plusSeconds(MathUtils.multiplyExact(unit.getDuration().seconds, amountToAdd))
            }
        }
        val duration = unit.getDuration().multipliedBy(amountToAdd)
        return plusSeconds(duration.seconds).plusNanos(duration.nanos.toLong())
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the specified duration in standard 24 hour days added.
     *
     *
     * The number of days is multiplied by 86400 to obtain the number of seconds to add.
     * This is based on the standard definition of a day as 24 hours.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param daysToAdd  the days to add, positive or negative
     * @return a `Duration` based on this duration with the specified days added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusDays(daysToAdd: Long): Duration {
        return plus(MathUtils.multiplyExact(daysToAdd, SECONDS_PER_DAY.toLong()), 0)
    }

    /**
     * Returns a copy of this duration with the specified duration in hours added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hoursToAdd  the hours to add, positive or negative
     * @return a `Duration` based on this duration with the specified hours added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusHours(hoursToAdd: Long): Duration {
        return plus(MathUtils.multiplyExact(hoursToAdd, SECONDS_PER_HOUR.toLong()), 0)
    }

    /**
     * Returns a copy of this duration with the specified duration in minutes added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutesToAdd  the minutes to add, positive or negative
     * @return a `Duration` based on this duration with the specified minutes added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusMinutes(minutesToAdd: Long): Duration {
        return plus(MathUtils.multiplyExact(minutesToAdd, SECONDS_PER_MINUTE.toLong()), 0)
    }

    /**
     * Returns a copy of this duration with the specified duration in seconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToAdd  the seconds to add, positive or negative
     * @return a `Duration` based on this duration with the specified seconds added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusSeconds(secondsToAdd: Long): Duration {
        return plus(secondsToAdd, 0)
    }

    /**
     * Returns a copy of this duration with the specified duration in milliseconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param millisToAdd  the milliseconds to add, positive or negative
     * @return a `Duration` based on this duration with the specified milliseconds added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusMillis(millisToAdd: Long): Duration {
        return plus(millisToAdd / 1000, millisToAdd % 1000 * 1000000)
    }

    /**
     * Returns a copy of this duration with the specified duration in nanoseconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanosToAdd  the nanoseconds to add, positive or negative
     * @return a `Duration` based on this duration with the specified nanoseconds added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusNanos(nanosToAdd: Long): Duration {
        return plus(0, nanosToAdd)
    }

    /**
     * Returns a copy of this duration with the specified duration added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToAdd  the seconds to add, positive or negative
     * @param nanosToAdd  the nanos to add, positive or negative
     * @return a `Duration` based on this duration with the specified seconds added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    private fun plus(secondsToAdd: Long, nanosToAdd: Long): Duration {
        var nanosToAdd = nanosToAdd
        if (secondsToAdd or nanosToAdd == 0L) {
            return this
        }
        var epochSec = MathUtils.addExact(seconds, secondsToAdd)
        epochSec = MathUtils.addExact(epochSec, nanosToAdd / NANOS_PER_SECOND)
        nanosToAdd %= NANOS_PER_SECOND
        val nanoAdjustment = nanos + nanosToAdd  // safe int+NANOS_PER_SECOND
        return ofSeconds(epochSec, nanoAdjustment)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the specified duration subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param duration  the duration to subtract, positive or negative, not null
     * @return a `Duration` based on this duration with the specified duration subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    operator fun minus(duration: Duration): Duration {
        val secsToSubtract = duration.seconds
        val nanosToSubtract = duration.nanos
        return if (secsToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, (-nanosToSubtract).toLong()).plus(1, 0)
        } else {
            plus(-secsToSubtract, (-nanosToSubtract).toLong())
        }
    }

    /**
     * Returns a copy of this duration with the specified duration subtracted.
     *
     *
     * The duration amount is measured in terms of the specified unit.
     * Only a subset of units are accepted by this method.
     * The unit must either have an [exact duration][TemporalUnit.isDurationEstimated] or
     * be [ChronoUnit.DAYS] which is treated as 24 hours. Other units throw an exception.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToSubtract  the amount to subtract, measured in terms of the unit, positive or negative
     * @param unit  the unit that the amount is measured in, must have an exact duration, not null
     * @return a `Duration` based on this duration with the specified duration subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minus(amountToSubtract: Long, unit: TemporalUnit): Duration {
        return if (amountToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, unit).plus(1, unit)
        } else {
            plus(-amountToSubtract, unit)
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the specified duration in standard 24 hour days subtracted.
     *
     *
     * The number of days is multiplied by 86400 to obtain the number of seconds to subtract.
     * This is based on the standard definition of a day as 24 hours.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param daysToSubtract  the days to subtract, positive or negative
     * @return a `Duration` based on this duration with the specified days subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusDays(daysToSubtract: Long): Duration {
        return if (daysToSubtract == Long.MIN_VALUE) {
            plusDays(Long.MAX_VALUE).plusDays(1)
        } else {
            plusDays(-daysToSubtract)
        }
    }

    /**
     * Returns a copy of this duration with the specified duration in hours subtracted.
     *
     *
     * The number of hours is multiplied by 3600 to obtain the number of seconds to subtract.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hoursToSubtract  the hours to subtract, positive or negative
     * @return a `Duration` based on this duration with the specified hours subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusHours(hoursToSubtract: Long): Duration {
        return if (hoursToSubtract == Long.MIN_VALUE) {
            plusHours(Long.MAX_VALUE).plusHours(1)
        } else {
            plusHours(-hoursToSubtract)
        }
    }

    /**
     * Returns a copy of this duration with the specified duration in minutes subtracted.
     *
     *
     * The number of hours is multiplied by 60 to obtain the number of seconds to subtract.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutesToSubtract  the minutes to subtract, positive or negative
     * @return a `Duration` based on this duration with the specified minutes subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusMinutes(minutesToSubtract: Long): Duration {
        return if (minutesToSubtract == Long.MIN_VALUE) {
            plusMinutes(Long.MAX_VALUE).plusMinutes(1)
        } else {
            plusMinutes(-minutesToSubtract)
        }
    }

    /**
     * Returns a copy of this duration with the specified duration in seconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToSubtract  the seconds to subtract, positive or negative
     * @return a `Duration` based on this duration with the specified seconds subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusSeconds(secondsToSubtract: Long): Duration {
        return if (secondsToSubtract == Long.MIN_VALUE) {
            plusSeconds(Long.MAX_VALUE).plusSeconds(1)
        } else {
            plusSeconds(-secondsToSubtract)
        }
    }

    /**
     * Returns a copy of this duration with the specified duration in milliseconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param millisToSubtract  the milliseconds to subtract, positive or negative
     * @return a `Duration` based on this duration with the specified milliseconds subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusMillis(millisToSubtract: Long): Duration {
        return if (millisToSubtract == Long.MIN_VALUE) {
            plusMillis(Long.MAX_VALUE).plusMillis(1)
        } else {
            plusMillis(-millisToSubtract)
        }
    }

    /**
     * Returns a copy of this duration with the specified duration in nanoseconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanosToSubtract  the nanoseconds to subtract, positive or negative
     * @return a `Duration` based on this duration with the specified nanoseconds subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusNanos(nanosToSubtract: Long): Duration {
        return if (nanosToSubtract == Long.MIN_VALUE) {
            plusNanos(Long.MAX_VALUE).plusNanos(1)
        } else {
            plusNanos(-nanosToSubtract)
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `Duration` truncated to the specified unit.
     *
     *
     * Truncating the duration returns a copy of the original with conceptual fields
     * smaller than the specified unit set to zero.
     * For example, truncating with the [MINUTES][ChronoUnit.MINUTES] unit will
     * round down towards zero to the nearest minute, setting the seconds and
     * nanoseconds to zero.
     *
     *
     * The unit must have a [duration][TemporalUnit.getDuration]
     * that divides into the length of a standard day without remainder.
     * This includes all
     * [time-based units on ][ChronoUnit.isTimeBased]
     * and [DAYS][ChronoUnit.DAYS]. Other ChronoUnits throw an exception.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param unit the unit to truncate to, not null
     * @return a `Duration` based on this duration with the time truncated, not null
     * @throws DateTimeException if the unit is invalid for truncation
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @since 9
     */
    fun truncatedTo(unit: TemporalUnit): Duration {
        if (unit === ChronoUnit.SECONDS && (seconds >= 0 || nanos == 0)) {
            return Duration(seconds, 0)
        } else if (unit === ChronoUnit.NANOS) {
            return this
        }
        val unitDur = unit.getDuration()
        if (unitDur.seconds > LocalTime.SECONDS_PER_DAY) {
            throw UnsupportedTemporalTypeException("Unit is too large to be used for truncation")
        }
        val dur = unitDur.toNanos()
        if (LocalTime.NANOS_PER_DAY % dur != 0L) {
            throw UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder")
        }
        val nod = seconds % LocalTime.SECONDS_PER_DAY * LocalTime.NANOS_PER_SECOND + nanos
        val result = nod / dur * dur
        return plusNanos(result - nod)
    }

    /**
     * Compares this duration to the specified {@code Duration}.
     * <p>
     * The comparison is based on the total length of the durations.
     * It is "consistent with equals", as defined by {@link Comparable}.
     *
     * @param other the other duration to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    override fun compareTo(other: Duration): Int {
        val cmp = this.seconds - other.seconds
        return when {
            cmp > 0L -> 1
            cmp < 0L -> -1
            else -> this.nanos - other.nanos
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this duration is zero length.
     *
     *
     * A `Duration` represents a directed distance between two points on
     * the time-line and can therefore be positive, zero or negative.
     * This method checks whether the length is zero.
     *
     * @return true if this duration has a total length equal to zero
     */
    fun isZero(): Boolean {
        return this.seconds or this.nanos.toLong() == 0L
    }

    /**
     * Checks if this duration is negative, excluding zero.
     *
     *
     * A `Duration` represents a directed distance between two points on
     * the time-line and can therefore be positive, zero or negative.
     * This method checks whether the length is less than zero.
     *
     * @return true if this duration has a total length less than zero
     */
    fun isNegative(): Boolean {
        return this.seconds < 0
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration with the length negated.
     *
     *
     * This method swaps the sign of the total length of this duration.
     * For example, `PT1.3S` will be returned as `PT-1.3S`.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return a `Duration` based on this duration with the amount negated, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun negated(): Duration {
        return multipliedBy(-1)
    }

    /**
     * Returns a copy of this duration with a positive length.
     *
     *
     * This method returns a positive duration by effectively removing the sign from any negative total length.
     * For example, `PT-1.3S` will be returned as `PT1.3S`.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return a `Duration` based on this duration with an absolute length, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun abs(): Duration {
        return if (isNegative()) negated() else this
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this duration multiplied by the scalar.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param multiplicand  the value to multiply the duration by, positive or negative
     * @return a `Duration` based on this duration multiplied by the specified scalar, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun multipliedBy(multiplicand: Long): Duration {
        if (multiplicand == 0L) {
            return ZERO
        }
        return if (multiplicand == 1L) {
            this
        } else create(toBigDecimalSeconds() * BigDecimal.valueOf(multiplicand))
    }

    //-------------------------------------------------------------------------
    /**
     * Adds this duration to the specified temporal object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with this duration added.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.plus].
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * dateTime = thisDuration.addTo(dateTime);
     * dateTime = dateTime.plus(thisDuration);
     * </pre>
     *
     *
     * The calculation will add the seconds, then nanos.
     * Only non-zero amounts will be added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param temporal  the temporal object to adjust, not null
     * @return an object of the same type with the adjustment made, not null
     * @throws DateTimeException if unable to add
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun addTo(temporal: Temporal): Temporal {
        var temporal = temporal
        if (this.seconds != 0L) {
            temporal = temporal.plus(this.seconds, ChronoUnit.SECONDS)
        }
        if (this.nanos != 0) {
            temporal = temporal.plus(this.nanos.toLong(), ChronoUnit.NANOS)
        }
        return temporal
    }

    /**
     * Subtracts this duration from the specified temporal object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with this duration subtracted.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.minus].
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * dateTime = thisDuration.subtractFrom(dateTime);
     * dateTime = dateTime.minus(thisDuration);
     * </pre>
     *
     *
     * The calculation will subtract the seconds, then nanos.
     * Only non-zero amounts will be added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param temporal  the temporal object to adjust, not null
     * @return an object of the same type with the adjustment made, not null
     * @throws DateTimeException if unable to subtract
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun subtractFrom(temporal: Temporal): Temporal {
        var temporal = temporal
        if (this.seconds != 0L) {
            temporal = temporal.minus(this.seconds, ChronoUnit.SECONDS)
        }
        if (this.nanos != 0) {
            temporal = temporal.minus(this.nanos.toLong(), ChronoUnit.NANOS)
        }
        return temporal
    }

    /**
     * Converts this duration to the total length in seconds and
     * fractional nanoseconds expressed as a `BigDecimal`.
     *
     * @return the total length of the duration in seconds, with a scale of 9, not null
     */
    private fun toBigDecimalSeconds(): BigDecimal {
        return BigDecimal.valueOf(seconds) + BigDecimal.valueOf(nanos.toLong(), 9)
    }
}