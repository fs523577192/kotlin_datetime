/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import org.firas.datetime.temporal.ChronoField
import org.firas.datetime.zone.ZoneOffset



/**
 * A time without time-zone in the ISO-8601 calendar system,
 * such as {@code 10:15:30}.
 * <p>
 * {@code LocalTime} is an immutable date-time object that represents a time,
 * often viewed as hour-minute-second.
 * Time is represented to nanosecond precision.
 * For example, the value "13:45.30.123456789" can be stored in a {@code LocalTime}.
 * <p>
 * It does not store or represent a date or time-zone.
 * Instead, it is a description of the local time as seen on a wall clock.
 * It cannot represent an instant on the time-line without additional information
 * such as an offset or time-zone.
 * <p>
 * The ISO-8601 calendar system is the modern civil calendar system used today
 * in most of the world. This API assumes that all calendar systems use the same
 * representation, this class, for time-of-day.
 *
 * <p>
 * This is a <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code LocalTime} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
class LocalTime private constructor(
    val hour: Byte,
    val minute: Byte,
    val second: Byte,
    val nano: Int
): Comparable<LocalTime> {

    companion object {
        /**
         * The minimum supported `LocalTime`, '00:00'.
         * This is the time of midnight at the start of the day.
         */
        val MIN: LocalTime
        /**
         * The maximum supported `LocalTime`, '23:59:59.999999999'.
         * This is the time just before midnight at the end of the day.
         */
        val MAX: LocalTime
        /**
         * The time of midnight at the start of the day, '00:00'.
         */
        val MIDNIGHT: LocalTime
        /**
         * The time of noon in the middle of the day, '12:00'.
         */
        val NOON: LocalTime
        /**
         * Constants for the local time of each hour.
         */
        private val HOURS = Array(24) {
            LocalTime(it.toByte(), 0, 0, 0)
        }
        init {
            MIDNIGHT = HOURS[0]
            NOON = HOURS[12]
            MIN = HOURS[0]
            MAX = LocalTime(23, 59, 59, 999_999_999)
        }

        /**
         * Hours per day.
         */
        internal const val HOURS_PER_DAY = 24
        /**
         * Minutes per hour.
         */
        internal const val MINUTES_PER_HOUR = 60
        /**
         * Minutes per day.
         */
        internal const val MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY
        /**
         * Seconds per minute.
         */
        internal const val SECONDS_PER_MINUTE = 60
        /**
         * Seconds per hour.
         */
        internal const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
        /**
         * Seconds per day.
         */
        internal const val SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY
        /**
         * Milliseconds per day.
         */
        internal const val MILLIS_PER_DAY = SECONDS_PER_DAY * 1000L
        /**
         * Microseconds per day.
         */
        internal const val MICROS_PER_DAY = SECONDS_PER_DAY * 1000_000L
        /**
         * Nanos per second.
         */
        internal const val NANOS_PER_SECOND = 1000_000_000L
        /**
         * Nanos per minute.
         */
        internal const val NANOS_PER_MINUTE = NANOS_PER_SECOND * SECONDS_PER_MINUTE
        /**
         * Nanos per hour.
         */
        internal const val NANOS_PER_HOUR = NANOS_PER_MINUTE * MINUTES_PER_HOUR
        /**
         * Nanos per day.
         */
        internal const val NANOS_PER_DAY = NANOS_PER_HOUR * HOURS_PER_DAY

        /**
         * Serialization version.
         */
        private const val serialVersionUID = 6414437269572265201L

        /**
         * Obtains an instance of `LocalTime` from an hour and minute.
         *
         *
         * This returns a `LocalTime` with the specified hour and minute.
         * The second and nanosecond fields will be set to zero.
         *
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @return the local time, not null
         * @throws DateTimeException if the value of any field is out of range
         */
        fun of(hour: Int, minute: Int): LocalTime {
            ChronoField.HOUR_OF_DAY.checkValidValue(hour.toLong())
            if (minute == 0) {
                return HOURS[hour]  // for performance
            }
            ChronoField.MINUTE_OF_HOUR.checkValidValue(minute.toLong())
            return LocalTime(hour.toByte(), minute.toByte(), 0, 0)
        }

        /**
         * Obtains an instance of `LocalTime` from an hour, minute and second.
         *
         *
         * This returns a `LocalTime` with the specified hour, minute and second.
         * The nanosecond field will be set to zero.
         *
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @param second  the second-of-minute to represent, from 0 to 59
         * @return the local time, not null
         * @throws DateTimeException if the value of any field is out of range
         */
        fun of(hour: Int, minute: Int, second: Int): LocalTime {
            ChronoField.HOUR_OF_DAY.checkValidValue(hour.toLong())
            if (minute or second == 0) {
                return HOURS[hour]  // for performance
            }
            ChronoField.MINUTE_OF_HOUR.checkValidValue(minute.toLong())
            ChronoField.SECOND_OF_MINUTE.checkValidValue(second.toLong())
            return LocalTime(hour.toByte(), minute.toByte(), second.toByte(), 0)
        }

        /**
         * Obtains an instance of `LocalTime` from an hour, minute, second and nanosecond.
         *
         *
         * This returns a `LocalTime` with the specified hour, minute, second and nanosecond.
         *
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @param second  the second-of-minute to represent, from 0 to 59
         * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
         * @return the local time, not null
         * @throws DateTimeException if the value of any field is out of range
         */
        fun of(hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalTime {
            ChronoField.HOUR_OF_DAY.checkValidValue(hour.toLong())
            ChronoField.MINUTE_OF_HOUR.checkValidValue(minute.toLong())
            ChronoField.SECOND_OF_MINUTE.checkValidValue(second.toLong())
            ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond.toLong())
            return create(hour, minute, second, nanoOfSecond)
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains an instance of `LocalTime` from a second-of-day value.
         *
         *
         * This returns a `LocalTime` with the specified second-of-day.
         * The nanosecond field will be set to zero.
         *
         * @param secondOfDay  the second-of-day, from `0` to `24 * 60 * 60 - 1`
         * @return the local time, not null
         * @throws DateTimeException if the second-of-day value is invalid
         */
        fun ofSecondOfDay(secondOfDay: Long): LocalTime {
            var secondOfDay = secondOfDay
            ChronoField.SECOND_OF_DAY.checkValidValue(secondOfDay)
            val hours = (secondOfDay / SECONDS_PER_HOUR).toInt()
            secondOfDay -= hours * SECONDS_PER_HOUR
            val minutes = (secondOfDay / SECONDS_PER_MINUTE).toInt()
            secondOfDay -= minutes * SECONDS_PER_MINUTE
            return create(hours, minutes, secondOfDay.toInt(), 0)
        }

        /**
         * Obtains an instance of `LocalTime` from a nanos-of-day value.
         *
         *
         * This returns a `LocalTime` with the specified nanosecond-of-day.
         *
         * @param nanoOfDay  the nano of day, from `0` to `24 * 60 * 60 * 1,000,000,000 - 1`
         * @return the local time, not null
         * @throws DateTimeException if the nanos of day value is invalid
         */
        fun ofNanoOfDay(nanoOfDay: Long): LocalTime {
            var nanoOfDay = nanoOfDay
            ChronoField.NANO_OF_DAY.checkValidValue(nanoOfDay)
            val hours = (nanoOfDay / NANOS_PER_HOUR).toInt()
            nanoOfDay -= hours * NANOS_PER_HOUR
            val minutes = (nanoOfDay / NANOS_PER_MINUTE).toInt()
            nanoOfDay -= minutes * NANOS_PER_MINUTE
            val seconds = (nanoOfDay / NANOS_PER_SECOND).toInt()
            nanoOfDay -= seconds * NANOS_PER_SECOND
            return create(hours, minutes, seconds, nanoOfDay.toInt())
        }

        /**
         * Creates a local time from the hour, minute, second and nanosecond fields.
         *
         *
         * This factory may return a cached value, but applications must not rely on this.
         *
         * @param hour  the hour-of-day to represent, validated from 0 to 23
         * @param minute  the minute-of-hour to represent, validated from 0 to 59
         * @param second  the second-of-minute to represent, validated from 0 to 59
         * @param nanoOfSecond  the nano-of-second to represent, validated from 0 to 999,999,999
         * @return the local time, not null
         */
        private fun create(hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalTime {
            return if (minute or second or nanoOfSecond == 0) {
                HOURS[hour]
            } else {
                LocalTime(hour.toByte(), minute.toByte(), second.toByte(), nanoOfSecond)
            }
        }
    } // companion object

    /**
     * Combines this time with an offset to create an `OffsetTime`.
     *
     *
     * This returns an `OffsetTime` formed from this time at the specified offset.
     * All possible combinations of time and offset are valid.
     *
     * @param offset  the offset to combine with, not null
     * @return the offset time formed from this time and the specified offset, not null
     */
    fun atOffset(offset: ZoneOffset): OffsetTime {
        return OffsetTime.of(this, offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalTime` with the hour-of-day altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to set in the result, from 0 to 23
     * @return a `LocalTime` based on this time with the requested hour, not null
     * @throws DateTimeException if the hour value is invalid
     */
    fun withHour(hour: Int): LocalTime {
        if (this.hour.toInt() == hour) {
            return this
        }
        ChronoField.HOUR_OF_DAY.checkValidValue(hour.toLong())
        return create(hour, this.minute.toInt(), this.second.toInt(), this.nano)
    }

    /**
     * Returns a copy of this `LocalTime` with the minute-of-hour altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minute  the minute-of-hour to set in the result, from 0 to 59
     * @return a `LocalTime` based on this time with the requested minute, not null
     * @throws DateTimeException if the minute value is invalid
     */
    fun withMinute(minute: Int): LocalTime {
        if (this.minute.toInt() == minute) {
            return this
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute.toLong())
        return create(this.hour.toInt(), minute, this.second.toInt(), this.nano)
    }

    /**
     * Returns a copy of this `LocalTime` with the second-of-minute altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param second  the second-of-minute to set in the result, from 0 to 59
     * @return a `LocalTime` based on this time with the requested second, not null
     * @throws DateTimeException if the second value is invalid
     */
    fun withSecond(second: Int): LocalTime {
        if (this.second.toInt() == second) {
            return this
        }
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second.toLong())
        return create(this.hour.toInt(), this.minute.toInt(), second, this.nano)
    }

    /**
     * Returns a copy of this `LocalTime` with the nano-of-second altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to set in the result, from 0 to 999,999,999
     * @return a `LocalTime` based on this time with the requested nanosecond, not null
     * @throws DateTimeException if the nanos value is invalid
     */
    fun withNano(nanoOfSecond: Int): LocalTime {
        if (this.nano == nanoOfSecond) {
            return this
        }
        ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond.toLong())
        return create(this.hour.toInt(), this.minute.toInt(), this.second.toInt(), nanoOfSecond)
    }

    // ----==== Comparison ====----
    /**
     * Compares this `LocalTime` to another time.
     *
     *
     * The comparison is based on the time-line position of the local times within a day.
     * It is "consistent with equals", as defined by [Comparable].
     *
     * @param other  the other time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     * @throws NullPointerException if `other` is null
     */
    override fun compareTo(other: LocalTime): Int {
        if (this.hour > other.hour) {
            return 1
        } else if (this.hour < other.hour) {
            return -1
        }
        if (this.minute > other.minute) {
            return 1
        } else if (this.minute < other.minute) {
            return -1
        }
        if (this.second > other.second) {
            return 1
        } else if (this.second < other.second) {
            return -1
        }
        return if (this.nano < other.nano) 1 else if (this.nano > other.nano) -1 else 0
    }

    /**
     * Checks if this `LocalTime` is after the specified time.
     *
     *
     * The comparison is based on the time-line position of the time within a day.
     *
     * @param other  the other time to compare to, not null
     * @return true if this is after the specified time
     * @throws NullPointerException if `other` is null
     */
    fun isAfter(other: LocalTime): Boolean {
        return compareTo(other) > 0
    }

    /**
     * Checks if this `LocalTime` is before the specified time.
     *
     *
     * The comparison is based on the time-line position of the time within a day.
     *
     * @param other  the other time to compare to, not null
     * @return true if this point is before the specified time
     * @throws NullPointerException if `other` is null
     */
    fun isBefore(other: LocalTime): Boolean {
        return compareTo(other) < 0
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this time is equal to another time.
     *
     *
     * The comparison is based on the time-line position of the time within a day.
     *
     *
     * Only otherects of type `LocalTime` are compared, other types return false.
     * To compare the date of two `TemporalAccessor` instances, use
     * [ChronoField.NANO_OF_DAY] as a comparator.
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other time
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is LocalTime) {
            return this.hour == other.hour && this.minute == other.minute &&
                    this.second == other.second && this.nano == other.nano
        }
        return false
    }

    /**
     * A hash code for this time.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        val nod = toNanoOfDay()
        return (nod xor nod.ushr(32)).toInt()
    }

    //-----------------------------------------------------------------------
    /**
     * Extracts the time as seconds of day,
     * from `0` to `24 * 60 * 60 - 1`.
     *
     * @return the second-of-day equivalent to this time
     */
    fun toSecondOfDay(): Int {
        var total = hour * SECONDS_PER_HOUR
        total += minute * SECONDS_PER_MINUTE
        total += second
        return total
    }

    /**
     * Extracts the time as nanos of day,
     * from `0` to `24 * 60 * 60 * 1,000,000,000 - 1`.
     *
     * @return the nano of day equivalent to this time
     */
    fun toNanoOfDay(): Long {
        var total = hour * NANOS_PER_HOUR
        total += minute * NANOS_PER_MINUTE
        total += second * NANOS_PER_SECOND
        total += nano
        return total
    }
}