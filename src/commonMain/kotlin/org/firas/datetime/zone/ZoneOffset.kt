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
package org.firas.datetime.zone

import org.firas.datetime.DateTimeException
import org.firas.datetime.LocalTime.Companion.MINUTES_PER_HOUR
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_HOUR
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_MINUTE
import org.firas.datetime.temporal.*
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

/**
 * A time-zone offset from Greenwich/UTC, such as `+02:00`.
 *
 *
 * A time-zone offset is the amount of time that a time-zone differs from Greenwich/UTC.
 * This is usually a fixed number of hours and minutes.
 *
 *
 * Different parts of the world have different time-zone offsets.
 * The rules for how offsets vary by place and time of year are captured in the
 * [ZoneId] class.
 *
 *
 * For example, Paris is one hour ahead of Greenwich/UTC in winter and two hours
 * ahead in summer. The `ZoneId` instance for Paris will reference two
 * `ZoneOffset` instances - a `+01:00` instance for winter,
 * and a `+02:00` instance for summer.
 *
 *
 * In 2008, time-zone offsets around the world extended from -12:00 to +14:00.
 * To prevent any problems with that range being extended, yet still provide
 * validation, the range of offsets is restricted to -18:00 to 18:00 inclusive.
 *
 *
 * This class is designed for use with the ISO calendar system.
 * The fields of hours, minutes and seconds make assumptions that are valid for the
 * standard ISO definitions of those fields. This class may be used with other
 * calendar systems providing the definition of the time fields matches those
 * of the ISO calendar system.
 *
 *
 * Instances of `ZoneOffset` must be compared using {@link #equals}.
 * Implementations may choose to cache certain common offsets, however
 * applications must not rely on such caching.
 *
 *
 *
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `ZoneOffset` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class ZoneOffset private constructor(
    val totalSeconds: Int
): ZoneId(), TemporalAccessor, TemporalAdjuster, Comparable<ZoneOffset> {

    private val id: String = buildId(this.totalSeconds)

    companion object {
        /**
         * The abs maximum seconds.
         */
        private val MAX_SECONDS = 18 * SECONDS_PER_HOUR

        /**
         * Serialization version.
         */
        private const val serialVersionUID = 2357656521762053153L

        /**
         * The time-zone offset for UTC, with an ID of 'Z'.
         */
        val UTC = ZoneOffset.ofTotalSeconds(0)
        /**
         * Constant for the minimum supported offset.
         */
        val MIN = ZoneOffset.ofTotalSeconds(-MAX_SECONDS)
        /**
         * Constant for the maximum supported offset.
         */
        val MAX = ZoneOffset.ofTotalSeconds(MAX_SECONDS)

        /**
         * Obtains an instance of `ZoneOffset` using the ID.
         *
         *
         * This method parses the string ID of a `ZoneOffset` to
         * return an instance. The parsing accepts all the formats generated by
         * [.getId], plus some additional formats:
         *
         *  * `Z` - for UTC
         *  * `+h`
         *  * `+hh`
         *  * `+hh:mm`
         *  * `-hh:mm`
         *  * `+hhmm`
         *  * `-hhmm`
         *  * `+hh:mm:ss`
         *  * `-hh:mm:ss`
         *  * `+hhmmss`
         *  * `-hhmmss`
         *
         * Note that  means either the plus or minus symbol.
         *
         *
         * The ID of the returned offset will be normalized to one of the formats
         * described by [.getId].
         *
         *
         * The maximum supported range is from +18:00 to -18:00 inclusive.
         *
         * @param offsetId  the offset ID, not null
         * @return the zone-offset, not null
         * @throws DateTimeException if the offset ID is invalid
         */
        fun of(offsetId: String): ZoneOffset {
            var offsetId = offsetId
            // "Z" is always in the cache
            val offset = ID_CACHE[offsetId]
            if (offset != null) {
                return offset
            }

            // parse - +h, +hh, +hhmm, +hh:mm, +hhmmss, +hh:mm:ss
            val hours: Int
            val minutes: Int
            val seconds: Int
            when (offsetId.length) {
                2 -> {
                    offsetId = offsetId[0] + "0" + offsetId[1]  // fallthru
                    hours = parseNumber(offsetId, 1, false)
                    minutes = 0
                    seconds = 0
                }
                3 -> {
                    hours = parseNumber(offsetId, 1, false)
                    minutes = 0
                    seconds = 0
                }
                5 -> {
                    hours = parseNumber(offsetId, 1, false)
                    minutes = parseNumber(offsetId, 3, false)
                    seconds = 0
                }
                6 -> {
                    hours = parseNumber(offsetId, 1, false)
                    minutes = parseNumber(offsetId, 4, true)
                    seconds = 0
                }
                7 -> {
                    hours = parseNumber(offsetId, 1, false)
                    minutes = parseNumber(offsetId, 3, false)
                    seconds = parseNumber(offsetId, 5, false)
                }
                9 -> {
                    hours = parseNumber(offsetId, 1, false)
                    minutes = parseNumber(offsetId, 4, true)
                    seconds = parseNumber(offsetId, 7, true)
                }
                else -> throw DateTimeException("Invalid ID for ZoneOffset, invalid format: $offsetId")
            }
            val first = offsetId[0]
            if (first != '+' && first != '-') {
                throw DateTimeException("Invalid ID for ZoneOffset, plus/minus not found when expected: $offsetId")
            }
            return if (first == '-') {
                ofHoursMinutesSeconds(-hours, -minutes, -seconds)
            } else {
                ofHoursMinutesSeconds(hours, minutes, seconds)
            }
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains an instance of `ZoneOffset` using an offset in hours.
         *
         * @param hours  the time-zone offset in hours, from -18 to +18
         * @return the zone-offset, not null
         * @throws DateTimeException if the offset is not in the required range
         */
        fun ofHours(hours: Int): ZoneOffset {
            return ofHoursMinutesSeconds(hours, 0, 0)
        }

        /**
         * Obtains an instance of `ZoneOffset` using an offset in
         * hours and minutes.
         *
         *
         * The sign of the hours and minutes components must match.
         * Thus, if the hours is negative, the minutes must be negative or zero.
         * If the hours is zero, the minutes may be positive, negative or zero.
         *
         * @param hours  the time-zone offset in hours, from -18 to +18
         * @param minutes  the time-zone offset in minutes, from 0 to 59, sign matches hours
         * @return the zone-offset, not null
         * @throws DateTimeException if the offset is not in the required range
         */
        fun ofHoursMinutes(hours: Int, minutes: Int): ZoneOffset {
            return ofHoursMinutesSeconds(hours, minutes, 0)
        }

        /**
         * Obtains an instance of `ZoneOffset` using an offset in
         * hours, minutes and seconds.
         *
         *
         * The sign of the hours, minutes and seconds components must match.
         * Thus, if the hours is negative, the minutes and seconds must be negative or zero.
         *
         * @param hours  the time-zone offset in hours, from -18 to +18
         * @param minutes  the time-zone offset in minutes, from 0 to 59, sign matches hours and seconds
         * @param seconds  the time-zone offset in seconds, from 0 to 59, sign matches hours and minutes
         * @return the zone-offset, not null
         * @throws DateTimeException if the offset is not in the required range
         */
        fun ofHoursMinutesSeconds(hours: Int, minutes: Int, seconds: Int): ZoneOffset {
            validate(hours, minutes, seconds)
            val totalSeconds = totalSeconds(hours, minutes, seconds)
            return ofTotalSeconds(totalSeconds)
        }

        /**
         * Obtains an instance of `ZoneOffset` specifying the total offset in seconds
         *
         *
         * The offset must be in the range `-18:00` to `+18:00`, which corresponds to -64800 to +64800.
         *
         * @param totalSeconds  the total time-zone offset in seconds, from -64800 to +64800
         * @return the ZoneOffset, not null
         * @throws DateTimeException if the offset is not in the required range
         */
        fun ofTotalSeconds(totalSeconds: Int): ZoneOffset {
            if (totalSeconds < -MAX_SECONDS || totalSeconds > MAX_SECONDS) {
                throw DateTimeException("Zone offset not in valid range: -18:00 to +18:00")
            }
            if (totalSeconds % (15 * SECONDS_PER_MINUTE) == 0) {
                var result = SECONDS_CACHE[totalSeconds]
                if (result == null) {
                    result = ZoneOffset(totalSeconds)
                    SECONDS_CACHE.put(totalSeconds, result)
                    result = SECONDS_CACHE[totalSeconds]
                    ID_CACHE.put(result!!.id, result)
                }
                return result
            } else {
                return ZoneOffset(totalSeconds)
            }
        }

        /**
         * Obtains an instance of `ZoneOffset` from a temporal object.
         *
         *
         * This obtains an offset based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `ZoneOffset`.
         *
         *
         * A `TemporalAccessor` represents some form of date and time information.
         * This factory converts the arbitrary temporal object to an instance of `ZoneOffset`.
         *
         *
         * The conversion uses the {@link TemporalQueries#offset()} query, which relies
         * on extracting the {@link ChronoField#OFFSET_SECONDS OFFSET_SECONDS} field.
         *
         *
         * This method matches the signature of the functional interface {@link TemporalQuery}
         * allowing it to be used as a query via method reference, `ZoneOffset::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the zone-offset, not null
         * @throws DateTimeException if unable to convert to an `ZoneOffset`
         */
        fun from(temporal: TemporalAccessor): ZoneOffset {
            return temporal.query(TemporalQueries.OFFSET) ?: throw DateTimeException(
                    "Unable to obtain ZoneOffset from TemporalAccessor: " +
                    temporal + " of type " + temporal.getKClass().qualifiedName)
        }

        private fun buildId(totalSeconds: Int): String {
            if (totalSeconds == 0) {
                return "Z"
            } else {
                val absTotalSeconds = totalSeconds.absoluteValue
                val buf = StringBuilder()
                val absHours = absTotalSeconds / SECONDS_PER_HOUR
                val absMinutes = absTotalSeconds / SECONDS_PER_MINUTE % MINUTES_PER_HOUR
                buf.append(if (totalSeconds < 0) "-" else "+")
                    .append(if (absHours < 10) "0" else "").append(absHours)
                    .append(if (absMinutes < 10) ":0" else ":").append(absMinutes)
                val absSeconds = absTotalSeconds % SECONDS_PER_MINUTE
                if (absSeconds != 0) {
                    buf.append(if (absSeconds < 10) ":0" else ":").append(absSeconds)
                }
                return buf.toString()
            }
        }

        /**
         * Parse a two digit zero-prefixed number.
         *
         * @param offsetId  the offset ID, not null
         * @param pos  the position to parse, valid
         * @param precededByColon  should this number be prefixed by a precededByColon
         * @return the parsed number, from 0 to 99
         */
        private fun parseNumber(offsetId: CharSequence, pos: Int, precededByColon: Boolean): Int {
            if (precededByColon && offsetId[pos - 1] != ':') {
                throw DateTimeException("Invalid ID for ZoneOffset, colon not found when expected: $offsetId")
            }
            val ch1 = offsetId[pos]
            val ch2 = offsetId[pos + 1]
            if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
                throw DateTimeException("Invalid ID for ZoneOffset, non numeric characters found: $offsetId")
            }
            return (ch1.toInt() - 48) * 10 + (ch2.toInt() - 48)
        }

        /**
         * Validates the offset fields.
         *
         * @param hours  the time-zone offset in hours, from -18 to +18
         * @param minutes  the time-zone offset in minutes, from 0 to 59
         * @param seconds  the time-zone offset in seconds, from 0 to 59
         * @throws DateTimeException if the offset is not in the required range
         */
        private fun validate(hours: Int, minutes: Int, seconds: Int) {
            if (hours < -18 || hours > 18) {
                throw DateTimeException(
                    "Zone offset hours not in valid range: value $hours" +
                            " is not in the range -18 to 18"
                )
            }
            if (hours > 0) {
                if (minutes < 0 || seconds < 0) {
                    throw DateTimeException("Zone offset minutes and seconds must be positive because hours is positive")
                }
            } else if (hours < 0) {
                if (minutes > 0 || seconds > 0) {
                    throw DateTimeException("Zone offset minutes and seconds must be negative because hours is negative")
                }
            } else if ((minutes > 0 && seconds < 0) || (minutes < 0 && seconds > 0)) {
                throw DateTimeException("Zone offset minutes and seconds must have the same sign")
            }
            if (minutes < -59 || minutes > 59) {
                throw DateTimeException("Zone offset minutes not in valid range: value " +
                            "$minutes is not in the range -59 to 59")
            }
            if (seconds < -59 || seconds > 59) {
                throw DateTimeException("Zone offset seconds not in valid range: value " +
                            "$seconds is not in the range -59 to 59")
            }
            if (hours.absoluteValue == 18 && (minutes or seconds) != 0) {
                throw DateTimeException("Zone offset not in valid range: -18:00 to +18:00")
            }
        }

        /**
         * Calculates the total offset in seconds.
         *
         * @param hours  the time-zone offset in hours, from -18 to +18
         * @param minutes  the time-zone offset in minutes, from 0 to 59, sign matches hours and seconds
         * @param seconds  the time-zone offset in seconds, from 0 to 59, sign matches hours and minutes
         * @return the total in seconds
         */
        private fun totalSeconds(hours: Int, minutes: Int, seconds: Int): Int {
            return hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds
        }
    } // companion object

    override fun getId(): String {
        return this.id
    }

    /**
     * Gets the associated time-zone rules.
     *
     *
     * The rules will always return this offset when queried.
     * The implementation class is immutable, thread-safe and serializable.
     *
     * @return the rules, not null
     */
    override fun getRules(): ZoneRules {
        return ZoneRules.of(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified field is supported.
     *
     *
     * This checks if this offset can be queried for the specified field.
     * If false, then calling the [range][.range] and
     * [get][.get] methods will throw an exception.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The `OFFSET_SECONDS` field returns true.
     * All other `ChronoField` instances will return false.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.isSupportedBy(TemporalAccessor)`
     * passing `this` as the argument.
     * Whether the field is supported is determined by the field.
     *
     * @param field  the field to check, null returns false
     * @return true if the field is supported on this offset, false if not
     */
    override fun isSupported(field: TemporalField): Boolean {
        return if (field is ChronoField) {
            field === ChronoField.OFFSET_SECONDS
        } else {
            field.isSupportedBy(this)
        }
    }

    /**
     * Gets the value of the specified field from this offset as an `int`.
     *
     *
     * This queries this offset for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The `OFFSET_SECONDS` field returns the value of the offset.
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.getFrom(TemporalAccessor)`
     * passing `this` as the argument. Whether the value can be obtained,
     * and what the value represents, is determined by the field.
     *
     * @param field  the field to get, not null
     * @return the value for the field
     * @throws DateTimeException if a value for the field cannot be obtained or
     * the value is outside the range of valid values for the field
     * @throws UnsupportedTemporalTypeException if the field is not supported or
     * the range of values exceeds an `int`
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun get(field: TemporalField): Int {
        if (field === ChronoField.OFFSET_SECONDS) {
            return this.totalSeconds
        } else if (field is ChronoField) {
            throw UnsupportedTemporalTypeException("Unsupported field: $field")
        }
        return range(field).checkValidIntValue(getLong(field), field)
    }

    /**
     * Gets the value of the specified field from this offset as a `long`.
     *
     *
     * This queries this offset for the value of the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The `OFFSET_SECONDS` field returns the value of the offset.
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.getFrom(TemporalAccessor)`
     * passing `this` as the argument. Whether the value can be obtained,
     * and what the value represents, is determined by the field.
     *
     * @param field  the field to get, not null
     * @return the value for the field
     * @throws DateTimeException if a value for the field cannot be obtained
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun getLong(field: TemporalField): Long {
        if (field === ChronoField.OFFSET_SECONDS) {
            return totalSeconds.toLong()
        } else if (field is ChronoField) {
            throw UnsupportedTemporalTypeException("Unsupported field: $field")
        }
        return field.getFrom(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Queries this offset using the specified query.
     *
     *
     * This queries this offset using the specified query strategy object.
     * The `TemporalQuery` object defines the logic to be used to
     * obtain the result. Read the documentation of the query to understand
     * what the result of this method will be.
     *
     *
     * The result of this method is obtained by invoking the
     * [TemporalQuery.queryFrom] method on the
     * specified query passing `this` as the argument.
     *
     * @param <R> the type of the result
     * @param query  the query to invoke, not null
     * @return the query result, null may be returned (defined by the query)
     * @throws DateTimeException if unable to query (defined by the query)
     * @throws ArithmeticException if numeric overflow occurs (defined by the query)
     */
    override fun <R> query(query: TemporalQuery<R>): R {
        return if (query === TemporalQueries.OFFSET || query === TemporalQueries.ZONE) {
            this as R
        } else {
            TODO("super@TemporalAccessor.query(query)")
        }
    }

    /**
     * Adjusts the specified temporal object to have the same offset as this object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with the offset changed to be the same as this.
     *
     *
     * The adjustment is equivalent to using [Temporal.with]
     * passing [ChronoField.OFFSET_SECONDS] as the field.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.with]:
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * temporal = thisOffset.adjustInto(temporal);
     * temporal = temporal.with(thisOffset);
     * </pre>
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param temporal  the target object to be adjusted, not null
     * @return the adjusted object, not null
     * @throws DateTimeException if unable to make the adjustment
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun adjustInto(temporal: Temporal): Temporal {
        return temporal.with(ChronoField.OFFSET_SECONDS, this.totalSeconds.toLong())
    }

    /**
     * Compares this offset to another offset in descending order.
     *
     *
     * The offsets are compared in the order that they occur for the same time
     * of day around the world. Thus, an offset of `+10:00` comes before an
     * offset of `+09:00` and so on down to `-18:00`.
     *
     *
     * The comparison is "consistent with equals", as defined by [Comparable].
     *
     * @param other  the other date to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     * @throws NullPointerException if `other` is null
     */
    override fun compareTo(other: ZoneOffset): Int {
        // abs(totalSeconds) <= MAX_SECONDS, so no overflow can happen here
        return other.totalSeconds - totalSeconds
    }

    /**
     * Checks if this offset is equal to another offset.
     *
     *
     * The comparison is based on the amount of the offset in seconds.
     * This is equivalent to a comparison by ID.
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other offset
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other is ZoneOffset) {
            totalSeconds == other.totalSeconds
        } else false
    }

    /**
     * A hash code for this offset.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return this.totalSeconds
    }

    /**
     * Outputs this offset as a `String`, using the normalized ID.
     *
     * @return a string representation of this offset, not null
     */
    override fun toString(): String {
        return this.id
    }

    override fun getKClass(): KClass<out TemporalAccessor> {
        return ZoneOffset::class
    }
}

/** Cache of time-zone offset by offset in seconds.  */
internal expect val SECONDS_CACHE: MutableMap<Int, ZoneOffset>
/** Cache of time-zone offset by ID.  */
internal expect val ID_CACHE: MutableMap<String, ZoneOffset>

expect fun getSystemZoneOffset(): ZoneOffset
