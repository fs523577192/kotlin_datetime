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

import org.firas.datetime.temporal.*
import org.firas.datetime.zone.ZoneOffset
import org.firas.lang.getName
import kotlin.js.JsName
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * A time without time-zone in the ISO-8601 calendar system,
 * such as `10:15:30`.
 *
 *
 * `LocalTime` is an immutable date-time object that represents a time,
 * often viewed as hour-minute-second.
 * Time is represented to nanosecond precision.
 * For example, the value "13:45.30.123456789" can be stored in a `LocalTime`.
 *
 *
 * It does not store or represent a date or time-zone.
 * Instead, it is a description of the local time as seen on a wall clock.
 * It cannot represent an instant on the time-line without additional information
 * such as an offset or time-zone.
 *
 *
 * The ISO-8601 calendar system is the modern civil calendar system used today
 * in most of the world. This API assumes that all calendar systems use the same
 * representation, this class, for time-of-day.
 *
 *
 * This is a <a href="{@docRoot}/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `LocalTime` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class LocalTime private constructor(
    val hour: Byte,
    val minute: Byte,
    val second: Byte,
    val nano: Int
): Temporal, TemporalAdjuster, Comparable<LocalTime> {

    companion object {
        /**
         * The minimum supported `LocalTime`, '00:00'.
         * This is the time of midnight at the start of the day.
         */
        @JvmStatic
        @JvmField
        val MIN: LocalTime
        /**
         * The maximum supported `LocalTime`, '23:59:59.999999999'.
         * This is the time just before midnight at the end of the day.
         */
        @JvmStatic
        @JvmField
        val MAX: LocalTime
        /**
         * The time of midnight at the start of the day, '00:00'.
         */
        @JvmStatic
        @JvmField
        val MIDNIGHT: LocalTime
        /**
         * The time of noon in the middle of the day, '12:00'.
         */
        @JvmStatic
        @JvmField
        val NOON: LocalTime
        /**
         * Constants for the local time of each hour.
         */
        @JvmStatic
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
        @JvmField
        internal const val HOURS_PER_DAY = 24
        /**
         * Minutes per hour.
         */
        @JvmField
        internal const val MINUTES_PER_HOUR = 60
        /**
         * Minutes per day.
         */
        @JvmField
        internal const val MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY
        /**
         * Seconds per minute.
         */
        @JvmField
        internal const val SECONDS_PER_MINUTE = 60
        /**
         * Seconds per hour.
         */
        @JvmField
        internal const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
        /**
         * Seconds per day.
         */
        @JvmField
        internal const val SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY
        /**
         * Milliseconds per day.
         */
        @JvmField
        internal const val MILLIS_PER_DAY = SECONDS_PER_DAY * 1000L
        /**
         * Microseconds per day.
         */
        @JvmField
        internal const val MICROS_PER_DAY = SECONDS_PER_DAY * 1000_000L
        /**
         * Nanos per millisecond.
         */
        @JvmField
        internal const val NANOS_PER_MILLI = 1000_000L
        /**
         * Nanos per second.
         */
        @JvmField
        internal const val NANOS_PER_SECOND = 1000_000_000L
        /**
         * Nanos per minute.
         */
        @JvmField
        internal const val NANOS_PER_MINUTE = NANOS_PER_SECOND * SECONDS_PER_MINUTE
        /**
         * Nanos per hour.
         */
        @JvmField
        internal const val NANOS_PER_HOUR = NANOS_PER_MINUTE * MINUTES_PER_HOUR
        /**
         * Nanos per day.
         */
        @JvmField
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
        @JvmStatic
        @JsName("ofHourAndMinute")
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
        @JvmStatic
        @JsName("ofHourMinuteAndSecond")
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
        @JvmStatic
        @JsName("ofHourMinuteSecondAndNano")
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
        @JvmStatic
        @JsName("ofSecondOfDay")
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
        @JvmStatic
        @JsName("ofNanoOfDay")
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
         * Obtains an instance of `LocalTime` from a temporal object.
         *
         *
         * This obtains a local time based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `LocalTime`.
         * <p>
         * The conversion uses the {@link TemporalQueries#localTime()} query, which relies
         * on extracting the {@link ChronoField#NANO_OF_DAY NANO_OF_DAY} field.
         * <p>
         * This method matches the signature of the functional interface {@link TemporalQuery}
         * allowing it to be used as a query via method reference, `LocalTime::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the local time, not null
         * @throws DateTimeException if unable to convert to a `LocalTime`
         */
        @JvmStatic
        @JsName("from")
        fun from(temporal: TemporalAccessor): LocalTime {
            return temporal.query(TemporalQueries.LOCAL_TIME) ?:
                    throw DateTimeException("Unable to obtain LocalTime from TemporalAccessor: " +
                            temporal + " of type " + temporal::class.getName())
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
     * Gets the value of the specified field from this time as an `int`.
     *
     *
     * This queries this time for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a {@link ChronoField} then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return valid
     * values based on this time, except `NANO_OF_DAY` and `MICRO_OF_DAY`
     * which are too large to fit in an `int` and throw an `UnsupportedTemporalTypeException`.
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
     *         the value is outside the range of valid values for the field
     * @throws UnsupportedTemporalTypeException if the field is not supported or
     *         the range of values exceeds an `int`
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun get(field: TemporalField): Int {
        if (field is ChronoField) {
            return get0(field)
        }
        return TemporalAccessor.get(this, field)
    }

    /**
     * Gets the value of the specified field from this time as a `Long`.
     *
     *
     * This queries this time for the value of the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return valid
     * values based on this time.
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
        if (field is ChronoField) {
            if (field == ChronoField.NANO_OF_DAY) {
                return toNanoOfDay()
            }
            if (field == ChronoField.MICRO_OF_DAY) {
                return toNanoOfDay() / 1000
            }
            return get0(field).toLong()
        }
        return field.getFrom(this)
    }

    /**
     * Checks if the specified field is supported.
     *
     *
     * This checks if this time can be queried for the specified field.
     * If false, then calling the [range][.range],
     * [get][.get] and [.with]
     * methods will throw an exception.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The supported fields are:
     *
     *  * `NANO_OF_SECOND`
     *  * `NANO_OF_DAY`
     *  * `MICRO_OF_SECOND`
     *  * `MICRO_OF_DAY`
     *  * `MILLI_OF_SECOND`
     *  * `MILLI_OF_DAY`
     *  * `SECOND_OF_MINUTE`
     *  * `SECOND_OF_DAY`
     *  * `MINUTE_OF_HOUR`
     *  * `MINUTE_OF_DAY`
     *  * `HOUR_OF_AMPM`
     *  * `CLOCK_HOUR_OF_AMPM`
     *  * `HOUR_OF_DAY`
     *  * `CLOCK_HOUR_OF_DAY`
     *  * `AMPM_OF_DAY`
     *
     * All other `ChronoField` instances will return false.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.isSupportedBy(TemporalAccessor)`
     * passing `this` as the argument.
     * Whether the field is supported is determined by the field.
     *
     * @param field  the field to check, null returns false
     * @return true if the field is supported on this time, false if not
     */
    override fun isSupported(field: TemporalField): Boolean {
        return if (field is ChronoField) {
            field.isTimeBased()
        } else field.isSupportedBy(this)
    }

    /**
     * Checks if the specified unit is supported.
     *
     *
     * This checks if the specified unit can be added to, or subtracted from, this time.
     * If false, then calling the [.plus] and
     * [minus][.minus] methods will throw an exception.
     *
     *
     * If the unit is a [ChronoUnit] then the query is implemented here.
     * The supported units are:
     *
     *  * `NANOS`
     *  * `MICROS`
     *  * `MILLIS`
     *  * `SECONDS`
     *  * `MINUTES`
     *  * `HOURS`
     *  * `HALF_DAYS`
     *
     * All other `ChronoUnit` instances will return false.
     *
     *
     * If the unit is not a `ChronoUnit`, then the result of this method
     * is obtained by invoking `TemporalUnit.isSupportedBy(Temporal)`
     * passing `this` as the argument.
     * Whether the unit is supported is determined by the unit.
     *
     * @param unit  the unit to check, null returns false
     * @return true if the unit can be added/subtracted, false if not
     */
    // override for Javadoc
    override fun isSupported(unit: TemporalUnit): Boolean {
        return if (unit is ChronoUnit) {
            unit.isTimeBased()
        } else unit.isSupportedBy(this)
    }

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

    /**
     * Returns a copy of this time with the specified field set to a new value.
     *
     *
     * This returns a `LocalTime`, based on this one, with the value
     * for the specified field changed.
     * This can be used to change any supported field, such as the hour, minute or second.
     * If it is not possible to set the value, because the field is not supported or for
     * some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the adjustment is implemented here.
     * The supported fields behave as follows:
     *
     *  * `NANO_OF_SECOND` -
     * Returns a `LocalTime` with the specified nano-of-second.
     * The hour, minute and second will be unchanged.
     *  * `NANO_OF_DAY` -
     * Returns a `LocalTime` with the specified nano-of-day.
     * This completely replaces the time and is equivalent to [.ofNanoOfDay].
     *  * `MICRO_OF_SECOND` -
     * Returns a `LocalTime` with the nano-of-second replaced by the specified
     * micro-of-second multiplied by 1,000.
     * The hour, minute and second will be unchanged.
     *  * `MICRO_OF_DAY` -
     * Returns a `LocalTime` with the specified micro-of-day.
     * This completely replaces the time and is equivalent to using [.ofNanoOfDay]
     * with the micro-of-day multiplied by 1,000.
     *  * `MILLI_OF_SECOND` -
     * Returns a `LocalTime` with the nano-of-second replaced by the specified
     * milli-of-second multiplied by 1,000,000.
     * The hour, minute and second will be unchanged.
     *  * `MILLI_OF_DAY` -
     * Returns a `LocalTime` with the specified milli-of-day.
     * This completely replaces the time and is equivalent to using [.ofNanoOfDay]
     * with the milli-of-day multiplied by 1,000,000.
     *  * `SECOND_OF_MINUTE` -
     * Returns a `LocalTime` with the specified second-of-minute.
     * The hour, minute and nano-of-second will be unchanged.
     *  * `SECOND_OF_DAY` -
     * Returns a `LocalTime` with the specified second-of-day.
     * The nano-of-second will be unchanged.
     *  * `MINUTE_OF_HOUR` -
     * Returns a `LocalTime` with the specified minute-of-hour.
     * The hour, second-of-minute and nano-of-second will be unchanged.
     *  * `MINUTE_OF_DAY` -
     * Returns a `LocalTime` with the specified minute-of-day.
     * The second-of-minute and nano-of-second will be unchanged.
     *  * `HOUR_OF_AMPM` -
     * Returns a `LocalTime` with the specified hour-of-am-pm.
     * The AM/PM, minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     *  * `CLOCK_HOUR_OF_AMPM` -
     * Returns a `LocalTime` with the specified clock-hour-of-am-pm.
     * The AM/PM, minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     *  * `HOUR_OF_DAY` -
     * Returns a `LocalTime` with the specified hour-of-day.
     * The minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     *  * `CLOCK_HOUR_OF_DAY` -
     * Returns a `LocalTime` with the specified clock-hour-of-day.
     * The minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     *  * `AMPM_OF_DAY` -
     * Returns a `LocalTime` with the specified AM/PM.
     * The hour-of-am-pm, minute-of-hour, second-of-minute and nano-of-second will be unchanged.
     *
     *
     *
     * In all cases, if the new value is outside the valid range of values for the field
     * then a `DateTimeException` will be thrown.
     *
     *
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.adjustInto(Temporal, long)`
     * passing `this` as the argument. In this case, the field determines
     * whether and how to adjust the instant.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param field  the field to set in the result, not null
     * @param newValue  the new value of the field in the result
     * @return a `LocalTime` based on `this` with the specified field set, not null
     * @throws DateTimeException if the field cannot be set
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(field: TemporalField, newValue: Long): LocalTime {
        if (field is ChronoField) {
            field.checkValidValue(newValue)
            return when (field) {
                ChronoField.NANO_OF_SECOND -> withNano(newValue.toInt())
                ChronoField.NANO_OF_DAY -> LocalTime.ofNanoOfDay(newValue)
                ChronoField.MICRO_OF_SECOND -> withNano(newValue.toInt() * 1000)
                ChronoField.MICRO_OF_DAY -> LocalTime.ofNanoOfDay(newValue * 1000)
                ChronoField.MILLI_OF_SECOND -> withNano(newValue.toInt() * 1000000)
                ChronoField.MILLI_OF_DAY -> LocalTime.ofNanoOfDay(newValue * 1000000)
                ChronoField.SECOND_OF_MINUTE -> withSecond(newValue.toInt())
                ChronoField.SECOND_OF_DAY -> plusSeconds(newValue - toSecondOfDay())
                ChronoField.MINUTE_OF_HOUR -> withMinute(newValue.toInt())
                ChronoField.MINUTE_OF_DAY -> plusMinutes(newValue - (hour * 60 + minute))
                ChronoField.HOUR_OF_AMPM -> plusHours(newValue - hour % 12)
                ChronoField.CLOCK_HOUR_OF_AMPM -> plusHours((if (newValue == 12L) 0 else newValue) - hour % 12)
                ChronoField.HOUR_OF_DAY -> withHour(newValue.toInt())
                ChronoField.CLOCK_HOUR_OF_DAY -> withHour((if (newValue == 24L) 0 else newValue).toInt())
                ChronoField.AMPM_OF_DAY -> plusHours((newValue - hour / 12) * 12)
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        return field.adjustInto(this, newValue)
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

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalTime` with the time truncated.
     *
     *
     * Truncation returns a copy of the original time with fields
     * smaller than the specified unit set to zero.
     * For example, truncating with the [minutes][ChronoUnit.MINUTES] unit
     * will set the second-of-minute and nano-of-second field to zero.
     *
     *
     * The unit must have a [duration][TemporalUnit.getDuration]
     * that divides into the length of a standard day without remainder.
     * This includes all supplied time units on [ChronoUnit] and
     * [DAYS][ChronoUnit.DAYS]. Other units throw an exception.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param unit  the unit to truncate to, not null
     * @return a `LocalTime` based on this time with the time truncated, not null
     * @throws DateTimeException if unable to truncate
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     */
    fun truncatedTo(unit: TemporalUnit): LocalTime {
        if (unit === ChronoUnit.NANOS) {
            return this
        }
        val unitDur = unit.getDuration()
        if (unitDur.seconds > SECONDS_PER_DAY) {
            throw UnsupportedTemporalTypeException("Unit is too large to be used for truncation")
        }
        val dur = unitDur.toNanos()
        if (NANOS_PER_DAY % dur != 0L) {
            throw UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder")
        }
        val nod = toNanoOfDay()
        return ofNanoOfDay(nod / dur * dur)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this time with the specified amount added.
     *
     *
     * This returns a `LocalTime`, based on this one, with the specified amount added.
     * The amount is typically [Duration] but may be any other type implementing
     * the [TemporalAmount] interface.
     *
     *
     * The calculation is delegated to the amount object by calling
     * [TemporalAmount.addTo]. The amount implementation is free
     * to implement the addition in any way it wishes, however it typically
     * calls back to [.plus]. Consult the documentation
     * of the amount implementation to determine if it can be successfully added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amount  the amount to add, not null
     * @return a `LocalTime` based on this time with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amount: TemporalAmount): LocalTime {
        return amount.addTo(this) as LocalTime
    }

    /**
     * Returns a copy of this time with the specified amount added.
     *
     *
     * This returns a `LocalTime`, based on this one, with the amount
     * in terms of the unit added. If it is not possible to add the amount, because the
     * unit is not supported or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoUnit] then the addition is implemented here.
     * The supported fields behave as follows:
     *
     *  * `NANOS` -
     * Returns a `LocalTime` with the specified number of nanoseconds added.
     * This is equivalent to [.plusNanos].
     *  * `MICROS` -
     * Returns a `LocalTime` with the specified number of microseconds added.
     * This is equivalent to [.plusNanos] with the amount
     * multiplied by 1,000.
     *  * `MILLIS` -
     * Returns a `LocalTime` with the specified number of milliseconds added.
     * This is equivalent to [.plusNanos] with the amount
     * multiplied by 1,000,000.
     *  * `SECONDS` -
     * Returns a `LocalTime` with the specified number of seconds added.
     * This is equivalent to [.plusSeconds].
     *  * `MINUTES` -
     * Returns a `LocalTime` with the specified number of minutes added.
     * This is equivalent to [.plusMinutes].
     *  * `HOURS` -
     * Returns a `LocalTime` with the specified number of hours added.
     * This is equivalent to [.plusHours].
     *  * `HALF_DAYS` -
     * Returns a `LocalTime` with the specified number of half-days added.
     * This is equivalent to [.plusHours] with the amount
     * multiplied by 12.
     *
     *
     *
     * All other `ChronoUnit` instances will throw an `UnsupportedTemporalTypeException`.
     *
     *
     * If the field is not a `ChronoUnit`, then the result of this method
     * is obtained by invoking `TemporalUnit.addTo(Temporal, long)`
     * passing `this` as the argument. In this case, the unit determines
     * whether and how to perform the addition.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToAdd  the amount of the unit to add to the result, may be negative
     * @param unit  the unit of the amount to add, not null
     * @return a `LocalTime` based on this time with the specified amount added, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: Long, unit: TemporalUnit): LocalTime {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.NANOS -> plusNanos(amountToAdd)
                ChronoUnit.MICROS -> plusNanos(amountToAdd % MICROS_PER_DAY * 1000)
                ChronoUnit.MILLIS -> plusNanos(amountToAdd % MILLIS_PER_DAY * 1000000)
                ChronoUnit.SECONDS -> plusSeconds(amountToAdd)
                ChronoUnit.MINUTES -> plusMinutes(amountToAdd)
                ChronoUnit.HOURS -> plusHours(amountToAdd)
                ChronoUnit.HALF_DAYS -> plusHours(amountToAdd % 2 * 12)
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.addTo(this, amountToAdd)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalTime` with the specified number of hours added.
     *
     *
     * This adds the specified number of hours to this time, returning a new time.
     * The calculation wraps around midnight.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hoursToAdd  the hours to add, may be negative
     * @return a `LocalTime` based on this time with the hours added, not null
     */
    fun plusHours(hoursToAdd: Long): LocalTime {
        if (hoursToAdd == 0L) {
            return this
        }
        val newHour = ((hoursToAdd % HOURS_PER_DAY).toInt() + this.hour + HOURS_PER_DAY) % HOURS_PER_DAY
        return create(newHour, this.minute.toInt(), this.second.toInt(), this.nano)
    }

    /**
     * Returns a copy of this `LocalTime` with the specified number of minutes added.
     *
     *
     * This adds the specified number of minutes to this time, returning a new time.
     * The calculation wraps around midnight.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutesToAdd  the minutes to add, may be negative
     * @return a `LocalTime` based on this time with the minutes added, not null
     */
    fun plusMinutes(minutesToAdd: Long): LocalTime {
        if (minutesToAdd == 0L) {
            return this
        }
        val mofd = this.hour * MINUTES_PER_HOUR + this.minute
        val newMofd = ((minutesToAdd % MINUTES_PER_DAY).toInt() + mofd + MINUTES_PER_DAY) % MINUTES_PER_DAY
        if (mofd == newMofd) {
            return this
        }
        val newHour = newMofd / MINUTES_PER_HOUR
        val newMinute = newMofd % MINUTES_PER_HOUR
        return create(newHour, newMinute, this.second.toInt(), this.nano)
    }

    /**
     * Returns a copy of this `LocalTime` with the specified number of seconds added.
     *
     *
     * This adds the specified number of seconds to this time, returning a new time.
     * The calculation wraps around midnight.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondstoAdd  the seconds to add, may be negative
     * @return a `LocalTime` based on this time with the seconds added, not null
     */
    fun plusSeconds(secondstoAdd: Long): LocalTime {
        if (secondstoAdd == 0L) {
            return this
        }
        val sofd = this.hour * SECONDS_PER_HOUR +
                this.minute * SECONDS_PER_MINUTE + this.second
        val newSofd = ((secondstoAdd % SECONDS_PER_DAY).toInt() + sofd + SECONDS_PER_DAY) % SECONDS_PER_DAY
        if (sofd == newSofd) {
            return this
        }
        val newHour = newSofd / SECONDS_PER_HOUR
        val newMinute = newSofd / SECONDS_PER_MINUTE % MINUTES_PER_HOUR
        val newSecond = newSofd % SECONDS_PER_MINUTE
        return create(newHour, newMinute, newSecond, this.nano)
    }

    /**
     * Returns a copy of this `LocalTime` with the specified number of nanoseconds added.
     *
     *
     * This adds the specified number of nanoseconds to this time, returning a new time.
     * The calculation wraps around midnight.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanosToAdd  the nanos to add, may be negative
     * @return a `LocalTime` based on this time with the nanoseconds added, not null
     */
    fun plusNanos(nanosToAdd: Long): LocalTime {
        if (nanosToAdd == 0L) {
            return this
        }
        val nofd = toNanoOfDay()
        val newNofd = (nanosToAdd % NANOS_PER_DAY + nofd + NANOS_PER_DAY) % NANOS_PER_DAY
        if (nofd == newNofd) {
            return this
        }
        val newHour = (newNofd / NANOS_PER_HOUR).toInt()
        val newMinute = (newNofd / NANOS_PER_MINUTE % MINUTES_PER_HOUR).toInt()
        val newSecond = (newNofd / NANOS_PER_SECOND % SECONDS_PER_MINUTE).toInt()
        val newNano = (newNofd % NANOS_PER_SECOND).toInt()
        return create(newHour, newMinute, newSecond, newNano)
    }

    //-----------------------------------------------------------------------
    /**
     * Queries this time using the specified query.
     *
     *
     * This queries this time using the specified query strategy object.
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
    override fun <R> query(query: TemporalQuery<R>): R? {
        if (query == TemporalQueries.CHRONO || query == TemporalQueries.ZONE_ID ||
            query == TemporalQueries.ZONE || query == TemporalQueries.OFFSET
        ) {
            return null
        } else if (query == TemporalQueries.LOCAL_TIME) {
            return this as R
        } else if (query === TemporalQueries.LOCAL_DATE) {
            return null
        } else if (query == TemporalQueries.PRECISION) {
            return ChronoUnit.NANOS as R
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this)
    }

    /**
     * Adjusts the specified temporal object to have the same time as this object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with the time changed to be the same as this.
     *
     *
     * The adjustment is equivalent to using [Temporal.with]
     * passing [ChronoField.NANO_OF_DAY] as the field.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.with]:
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * temporal = thisLocalTime.adjustInto(temporal);
     * temporal = temporal.with(thisLocalTime);
    </pre> *
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
        return temporal.with(ChronoField.NANO_OF_DAY, toNanoOfDay())
    }

    /**
     * Calculates the amount of time until another time in terms of the specified unit.
     *
     *
     * This calculates the amount of time between two `LocalTime`
     * objects in terms of a single `TemporalUnit`.
     * The start and end points are `this` and the specified time.
     * The result will be negative if the end is before the start.
     * The `Temporal` passed to this method is converted to a
     * `LocalTime` using [.from].
     * For example, the amount in hours between two times can be calculated
     * using `startTime.until(endTime, HOURS)`.
     *
     *
     * The calculation returns a whole number, representing the number of
     * complete units between the two times.
     * For example, the amount in hours between 11:30 and 13:29 will only
     * be one hour as it is one minute short of two hours.
     *
     *
     * There are two equivalent ways of using this method.
     * The first is to invoke this method.
     * The second is to use [TemporalUnit.between]:
     * <pre>
     * // these two lines are equivalent
     * amount = start.until(end, MINUTES);
     * amount = MINUTES.between(start, end);
    </pre> *
     * The choice should be made based on which makes the code more readable.
     *
     *
     * The calculation is implemented in this method for [ChronoUnit].
     * The units `NANOS`, `MICROS`, `MILLIS`, `SECONDS`,
     * `MINUTES`, `HOURS` and `HALF_DAYS` are supported.
     * Other `ChronoUnit` values will throw an exception.
     *
     *
     * If the unit is not a `ChronoUnit`, then the result of this method
     * is obtained by invoking `TemporalUnit.between(Temporal, Temporal)`
     * passing `this` as the first argument and the converted input temporal
     * as the second argument.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param endExclusive  the end time, exclusive, which is converted to a `LocalTime`, not null
     * @param unit  the unit to measure the amount in, not null
     * @return the amount of time between this time and the end time
     * @throws DateTimeException if the amount cannot be calculated, or the end
     * temporal cannot be converted to a `LocalTime`
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = LocalTime.from(endExclusive)
        if (unit is ChronoUnit) {
            val nanosUntil = end.toNanoOfDay() - toNanoOfDay()  // no overflow
            return when (unit) {
                ChronoUnit.NANOS -> nanosUntil
                ChronoUnit.MICROS -> nanosUntil / 1000
                ChronoUnit.MILLIS -> nanosUntil / 1000000
                ChronoUnit.SECONDS -> nanosUntil / NANOS_PER_SECOND
                ChronoUnit.MINUTES -> nanosUntil / NANOS_PER_MINUTE
                ChronoUnit.HOURS -> nanosUntil / NANOS_PER_HOUR
                ChronoUnit.HALF_DAYS -> nanosUntil / (12 * NANOS_PER_HOUR)
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
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

    override fun with(adjuster: TemporalAdjuster): Temporal {
        return Temporal.with(this, adjuster)
    }

    override fun minus(amount: TemporalAmount): Temporal {
        return Temporal.minus(this, amount)
    }

    override fun minus(amountToSubtract: Long, unit: TemporalUnit): Temporal {
        return Temporal.minus(this, amountToSubtract, unit)
    }

    override fun range(field: TemporalField): ValueRange {
        return TemporalAccessor.range(this, field)
    }

    private fun get0(field: TemporalField): Int {
        return when (field as ChronoField) {
            ChronoField.NANO_OF_SECOND -> this.nano
            ChronoField.NANO_OF_DAY -> throw UnsupportedTemporalTypeException(
                    "Invalid field 'NanoOfDay' for get() method, use getLong() instead")
            ChronoField.MICRO_OF_SECOND -> this.nano / 1000
            ChronoField.MICRO_OF_DAY -> throw UnsupportedTemporalTypeException(
                    "Invalid field 'MicroOfDay' for get() method, use getLong() instead")
            ChronoField.MILLI_OF_SECOND -> this.nano / 1000000
            ChronoField.MILLI_OF_DAY -> (toNanoOfDay() / 1000000).toInt()
            ChronoField.SECOND_OF_MINUTE -> this.second.toInt()
            ChronoField.SECOND_OF_DAY -> toSecondOfDay()
            ChronoField.MINUTE_OF_HOUR -> this.minute.toInt()
            ChronoField.MINUTE_OF_DAY -> this.hour * 60 + this.minute
            ChronoField.HOUR_OF_AMPM -> this.hour % 12
            ChronoField.CLOCK_HOUR_OF_AMPM -> {
                val ham = this.hour % 12
                if (ham % 12 == 0) 12 else ham
            }
            ChronoField.HOUR_OF_DAY -> this.hour.toInt()
            ChronoField.CLOCK_HOUR_OF_DAY -> if (this.hour.toInt() == 0) 24 else hour.toInt()
            ChronoField.AMPM_OF_DAY -> hour / 12
            else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
        }
    }
}