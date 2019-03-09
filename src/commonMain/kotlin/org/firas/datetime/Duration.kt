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
 * @author Wu Yuping
 */
class Duration private constructor(
        private val seconds: Long,
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
        </pre> *
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
        } else create(toSeconds() * BigDecimal.valueOf(multiplicand))
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
    </pre> *
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
    </pre> *
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
    private fun toSeconds(): BigDecimal {
        return BigDecimal.valueOf(seconds) + BigDecimal.valueOf(nanos.toLong(), 9)
    }
}