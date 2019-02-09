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

import org.firas.datetime.zone.ZoneOffset
import org.firas.datetime.temporal.ChronoField

/**
 * A time with an offset from UTC/Greenwich in the ISO-8601 calendar system,
 * such as `10:15:30+01:00`.
 * <p>
 * `OffsetTime` is an immutable date-time object that represents a time, often
 * viewed as hour-minute-second-offset.
 * This class stores all time fields, to a precision of nanoseconds,
 * as well as a zone offset.
 * For example, the value "13:45:30.123456789+02:00" can be stored
 * in an `OffsetTime`.
 *
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `OffsetTime` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
class OffsetTime private constructor(
    private val time: LocalTime,
    val offset: ZoneOffset
): Comparable<OffsetTime> {

    companion object {
        /**
         * The minimum supported `OffsetTime`, '00:00:00+18:00'.
         * This is the time of midnight at the start of the day in the maximum offset
         * (larger offsets are earlier on the time-line).
         * This combines [LocalTime.MIN] and [ZoneOffset.MAX].
         * This could be used by an application as a "far past" date.
         */
        val MIN = LocalTime.MIN.atOffset(ZoneOffset.MAX)
        /**
         * The maximum supported `OffsetTime`, '23:59:59.999999999-18:00'.
         * This is the time just before midnight at the end of the day in the minimum offset
         * (larger negative offsets are later on the time-line).
         * This combines [LocalTime.MAX] and [ZoneOffset.MIN].
         * This could be used by an application as a "far future" date.
         */
        val MAX = LocalTime.MAX.atOffset(ZoneOffset.MIN)

        /**
         * Serialization version.
         */
        private const val serialVersionUID = 7264499704384272492L

        /**
         * Obtains an instance of `OffsetTime` from a local time and an offset.
         *
         * @param time  the local time, not null
         * @param offset  the zone offset, not null
         * @return the offset time, not null
         */
        fun of(time: LocalTime, offset: ZoneOffset): OffsetTime {
            return OffsetTime(time, offset)
        }

        /**
         * Obtains an instance of `OffsetTime` from an hour, minute, second and nanosecond.
         *
         *
         * This creates an offset time with the four specified fields.
         *
         *
         * This method exists primarily for writing test cases.
         * Non test-code will typically use other methods to create an offset time.
         * `LocalTime` has two additional convenience variants of the
         * equivalent factory method taking fewer arguments.
         * They are not provided here to reduce the footprint of the API.
         *
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @param second  the second-of-minute to represent, from 0 to 59
         * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
         * @param offset  the zone offset, not null
         * @return the offset time, not null
         * @throws DateTimeException if the value of any field is out of range
         */
        fun of(hour: Int, minute: Int, second: Int, nanoOfSecond: Int, offset: ZoneOffset): OffsetTime {
            return OffsetTime(LocalTime.of(hour, minute, second, nanoOfSecond), offset)
        }

    } // companion object

    /**
     * Converts this `OffsetTime` to the number of seconds since the epoch
     * of 1970-01-01T00:00:00Z.
     *
     *
     * This combines this offset time with the specified date to calculate the
     * epoch-second value, which is the number of elapsed seconds from
     * 1970-01-01T00:00:00Z.
     * Instants on the time-line after the epoch are positive, earlier
     * are negative.
     *
     * @param date the localdate, not null
     * @return the number of seconds since the epoch of 1970-01-01T00:00:00Z, may be negative
     * @since 9
     */
    fun toEpochSecond(date: LocalDate): Long {
        val epochDay = date.toEpochDay()
        var secs = epochDay * 86400 + time.toSecondOfDay()
        secs -= offset.totalSeconds
        return secs
    }

    /**
     * Compares this `OffsetTime` to another time.
     *
     *
     * The comparison is based first on the UTC equivalent instant, then on the local time.
     * It is "consistent with equals", as defined by [Comparable].
     *
     *
     * For example, the following is the comparator order:
     *
     *  1. `10:30+01:00`
     *  1. `11:00+01:00`
     *  1. `12:00+02:00`
     *  1. `11:30+01:00`
     *  1. `12:00+01:00`
     *  1. `12:30+01:00`
     *
     * Values #2 and #3 represent the same instant on the time-line.
     * When two values represent the same instant, the local time is compared
     * to distinguish them. This step is needed to make the ordering
     * consistent with `equals()`.
     *
     *
     * To compare the underlying local time of two `TemporalAccessor` instances,
     * use [ChronoField.NANO_OF_DAY] as a comparator.
     *
     * @param other  the other time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    override fun compareTo(other: OffsetTime): Int {
        if (this.offset == other.offset) {
            return time.compareTo(other.time)
        }
        val a = toEpochNano()
        val b = other.toEpochNano()
        return if (a > b) 1 else if (a < b) -1 else time.compareTo(other.time)
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the instant of this `OffsetTime` is after that of the
     * specified time applying both times to a common date.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the instant of the time. This is equivalent to converting both
     * times to an instant using the same date and comparing the instants.
     *
     * @param other  the other time to compare to, not null
     * @return true if this is after the instant of the specified time
     */
    fun isAfter(other: OffsetTime): Boolean {
        return toEpochNano() > other.toEpochNano()
    }

    /**
     * Checks if the instant of this `OffsetTime` is before that of the
     * specified time applying both times to a common date.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the instant of the time. This is equivalent to converting both
     * times to an instant using the same date and comparing the instants.
     *
     * @param other  the other time to compare to, not null
     * @return true if this is before the instant of the specified time
     */
    fun isBefore(other: OffsetTime): Boolean {
        return toEpochNano() < other.toEpochNano()
    }

    /**
     * Checks if the instant of this `OffsetTime` is equal to that of the
     * specified time applying both times to a common date.
     *
     *
     * This method differs from the comparison in [.compareTo] and [.equals]
     * in that it only compares the instant of the time. This is equivalent to converting both
     * times to an instant using the same date and comparing the instants.
     *
     * @param other  the other time to compare to, not null
     * @return true if this is equal to the instant of the specified time
     */
    fun isEqual(other: OffsetTime): Boolean {
        return toEpochNano() === other.toEpochNano()
    }

    /**
     * Checks if this time is equal to another time.
     *
     *
     * The comparison is based on the local-time and the offset.
     * To compare for the same instant on the time-line, use [.isEqual].
     *
     *
     * Only objects of type `OffsetTime` are compared, other types return false.
     * To compare the underlying local time of two `TemporalAccessor` instances,
     * use [ChronoField.NANO_OF_DAY] as a comparator.
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other time
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is OffsetTime) {
            return time == other.time && offset == other.offset
        }
        return false
    }

    /**
     * A hash code for this time.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return time.hashCode() xor offset.hashCode()
    }

    /**
     * Outputs this time as a `String`, such as `10:15:30+01:00`.
     *
     *
     * The output will be one of the following ISO-8601 formats:
     *
     *  * `HH:mmXXXXX`
     *  * `HH:mm:ssXXXXX`
     *  * `HH:mm:ss.SSSXXXXX`
     *  * `HH:mm:ss.SSSSSSXXXXX`
     *  * `HH:mm:ss.SSSSSSSSSXXXXX`
     *
     * The format used will be the shortest that outputs the full value of
     * the time where the omitted parts are implied to be zero.
     *
     * @return a string representation of this time, not null
     */
    override fun toString(): String {
        return time.toString() + offset.toString()
    }

    /**
     * Converts this time to epoch nanos based on 1970-01-01Z.
     *
     * @return the epoch nanos value
     */
    private fun toEpochNano(): Long {
        val nod = time.toNanoOfDay()
        val offsetNanos = offset.totalSeconds * LocalTime.NANOS_PER_SECOND
        return nod - offsetNanos
    }

    /**
     * Returns a new time based on this one, returning `this` where possible.
     *
     * @param time  the time to create with, not null
     * @param offset  the zone offset to create with, not null
     */
    private fun with(time: LocalTime, offset: ZoneOffset): OffsetTime {
        return if (this.time == time && this.offset == offset) {
            this
        } else OffsetTime(time, offset)
    }
}