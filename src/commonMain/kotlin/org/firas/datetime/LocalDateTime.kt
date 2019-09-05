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

import org.firas.datetime.LocalTime.Companion.HOURS_PER_DAY
import org.firas.datetime.LocalTime.Companion.MICROS_PER_DAY
import org.firas.datetime.LocalTime.Companion.MILLIS_PER_DAY
import org.firas.datetime.LocalTime.Companion.MINUTES_PER_DAY
import org.firas.datetime.LocalTime.Companion.NANOS_PER_DAY
import org.firas.datetime.LocalTime.Companion.NANOS_PER_HOUR
import org.firas.datetime.LocalTime.Companion.NANOS_PER_MINUTE
import org.firas.datetime.LocalTime.Companion.NANOS_PER_SECOND
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_DAY
import org.firas.datetime.chrono.ChronoLocalDateTime
import org.firas.datetime.chrono.Chronology
import org.firas.datetime.temporal.*
import org.firas.datetime.util.MathUtils
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset
import org.firas.datetime.zone.getSystemZoneOffset
import org.firas.lang.getName
import kotlin.js.JsName
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * A date-time without a time-zone in the ISO-8601 calendar system,
 * such as `2007-12-03T10:15:30`.
 * <p>
 * `LocalDateTime` is an immutable date-time object that represents a date-time,
 * often viewed as year-month-day-hour-minute-second. Other date and time fields,
 * such as day-of-year, day-of-week and week-of-year, can also be accessed.
 * Time is represented to nanosecond precision.
 * For example, the value "2nd October 2007 at 13:45.30.123456789" can be
 * stored in a `LocalDateTime`.
 * <p>
 * This class does not store or represent a time-zone.
 * Instead, it is a description of the date, as used for birthdays, combined with
 * the local time as seen on a wall clock.
 * It cannot represent an instant on the time-line without additional information
 * such as an offset or time-zone.
 * <p>
 * The ISO-8601 calendar system is the modern civil calendar system used today
 * in most of the world. It is equivalent to the proleptic Gregorian calendar
 * system, in which today's rules for leap years are applied for all time.
 * For most applications written today, the ISO-8601 rules are entirely suitable.
 * However, any application that makes use of historical dates, and requires them
 * to be accurate will find the ISO-8601 approach unsuitable.
 *
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `LocalDateTime` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class LocalDateTime private constructor(
    private val date: LocalDate,
    private val time: LocalTime
): Comparable<LocalDateTime>, ChronoLocalDateTime<LocalDate> {

    override fun getDate(): LocalDate {
        return date
    }

    override fun getTime(): LocalTime {
        return time
    }

    companion object {
        /**
         * The minimum supported `LocalDateTime`, '-999999999-01-01T00:00:00'.
         * This is the local date-time of midnight at the start of the minimum date.
         * This combines [LocalDate.MIN] and [LocalTime.MIN].
         * This could be used by an application as a "far past" date-time.
         */
        @JvmStatic
        @JvmField
        val MIN = LocalDateTime.of(LocalDate.MIN, LocalTime.MIN)
        /**
         * The maximum supported `LocalDateTime`, '+999999999-12-31T23:59:59.999999999'.
         * This is the local date-time just before midnight at the end of the maximum date.
         * This combines [LocalDate.MAX] and [LocalTime.MAX].
         * This could be used by an application as a "far future" date-time.
         */
        @JvmStatic
        @JvmField
        val MAX = LocalDateTime.of(LocalDate.MAX, LocalTime.MAX)

        /**
         * Serialization version.
         */
        private const val serialVersionUID = 6207766400415563566L

        @JvmStatic
        @JsName("now")
        fun now(): LocalDateTime {
            val currentInstant = Instant.now()
            return ofEpochSecond(currentInstant.epochSecond, currentInstant.nanos,
                    getSystemZoneOffset())
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains an instance of `LocalDateTime` from year, month,
         * day, hour and minute, setting the second and nanosecond to zero.
         *
         *
         * This returns a `LocalDateTime` with the specified year, month,
         * day-of-month, hour and minute.
         * The day must be valid for the year and month, otherwise an exception will be thrown.
         * The second and nanosecond fields will be set to zero.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, not null
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @return the local date-time, not null
         * @throws DateTimeException if the value of any field is out of range,
         * or if the day-of-month is invalid for the month-year
         */
        @JvmStatic
        @JsName("ofYearMonthEnumDayHourAndMinute")
        fun of(year: Int, month: Month, dayOfMonth: Int, hour: Int, minute: Int): LocalDateTime {
            val date = LocalDate.of(year, month, dayOfMonth)
            val time = LocalTime.of(hour, minute)
            return LocalDateTime(date, time)
        }

        /**
         * Obtains an instance of `LocalDateTime` from year, month,
         * day, hour, minute and second, setting the nanosecond to zero.
         *
         *
         * This returns a `LocalDateTime` with the specified year, month,
         * day-of-month, hour, minute and second.
         * The day must be valid for the year and month, otherwise an exception will be thrown.
         * The nanosecond field will be set to zero.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, not null
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @param second  the second-of-minute to represent, from 0 to 59
         * @return the local date-time, not null
         * @throws DateTimeException if the value of any field is out of range,
         * or if the day-of-month is invalid for the month-year
         */
        @JvmStatic
        @JsName("ofYearMonthEnumDayHourMinuteAndSecond")
        fun of(year: Int, month: Month, dayOfMonth: Int, hour: Int, minute: Int, second: Int): LocalDateTime {
            val date = LocalDate.of(year, month, dayOfMonth)
            val time = LocalTime.of(hour, minute, second)
            return LocalDateTime(date, time)
        }

        /**
         * Obtains an instance of `LocalDateTime` from year, month,
         * day, hour, minute, second and nanosecond.
         *
         *
         * This returns a `LocalDateTime` with the specified year, month,
         * day-of-month, hour, minute, second and nanosecond.
         * The day must be valid for the year and month, otherwise an exception will be thrown.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, not null
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @param second  the second-of-minute to represent, from 0 to 59
         * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
         * @return the local date-time, not null
         * @throws DateTimeException if the value of any field is out of range,
         * or if the day-of-month is invalid for the month-year
         */
        @JvmStatic
        @JsName("ofYearMonthEnumDayHourMinuteSecondAndNano")
        fun of(
            year: Int,
            month: Month,
            dayOfMonth: Int,
            hour: Int,
            minute: Int,
            second: Int,
            nanoOfSecond: Int
        ): LocalDateTime {
            val date = LocalDate.of(year, month, dayOfMonth)
            val time = LocalTime.of(hour, minute, second, nanoOfSecond)
            return LocalDateTime(date, time)
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains an instance of `LocalDateTime` from year, month,
         * day, hour and minute, setting the second and nanosecond to zero.
         *
         *
         * This returns a `LocalDateTime` with the specified year, month,
         * day-of-month, hour and minute.
         * The day must be valid for the year and month, otherwise an exception will be thrown.
         * The second and nanosecond fields will be set to zero.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @return the local date-time, not null
         * @throws DateTimeException if the value of any field is out of range,
         * or if the day-of-month is invalid for the month-year
         */
        @JvmStatic
        @JsName("ofYearMonthDayHourAndMinute")
        fun of(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int): LocalDateTime {
            val date = LocalDate.of(year, month, dayOfMonth)
            val time = LocalTime.of(hour, minute)
            return LocalDateTime(date, time)
        }

        /**
         * Obtains an instance of `LocalDateTime` from year, month,
         * day, hour, minute and second, setting the nanosecond to zero.
         *
         *
         * This returns a `LocalDateTime` with the specified year, month,
         * day-of-month, hour, minute and second.
         * The day must be valid for the year and month, otherwise an exception will be thrown.
         * The nanosecond field will be set to zero.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @param second  the second-of-minute to represent, from 0 to 59
         * @return the local date-time, not null
         * @throws DateTimeException if the value of any field is out of range,
         * or if the day-of-month is invalid for the month-year
         */
        @JvmStatic
        @JsName("ofYearMonthDayHourMinuteAndSecond")
        fun of(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int, second: Int): LocalDateTime {
            val date = LocalDate.of(year, month, dayOfMonth)
            val time = LocalTime.of(hour, minute, second)
            return LocalDateTime(date, time)
        }

        /**
         * Obtains an instance of `LocalDateTime` from year, month,
         * day, hour, minute, second and nanosecond.
         *
         *
         * This returns a `LocalDateTime` with the specified year, month,
         * day-of-month, hour, minute, second and nanosecond.
         * The day must be valid for the year and month, otherwise an exception will be thrown.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @param second  the second-of-minute to represent, from 0 to 59
         * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
         * @return the local date-time, not null
         * @throws DateTimeException if the value of any field is out of range,
         * or if the day-of-month is invalid for the month-year
         */
        @JvmStatic
        @JsName("of")
        fun of(
            year: Int,
            month: Int,
            dayOfMonth: Int,
            hour: Int,
            minute: Int,
            second: Int,
            nanoOfSecond: Int
        ): LocalDateTime {
            val date = LocalDate.of(year, month, dayOfMonth)
            val time = LocalTime.of(hour, minute, second, nanoOfSecond)
            return LocalDateTime(date, time)
        }

        /**
         * Obtains an instance of `LocalDateTime` from a date and time.
         *
         * @param date  the local date, not null
         * @param time  the local time, not null
         * @return the local date-time, not null
         */
        @JvmStatic
        @JsName("ofDateAndTime")
        fun of(date: LocalDate, time: LocalTime): LocalDateTime {
            return LocalDateTime(date, time)
        }

        /**
         * Obtains an instance of `LocalDateTime` using seconds from the
         * epoch of 1970-01-01T00:00:00Z.
         *
         *
         * This allows the [epoch-second][ChronoField.INSTANT_SECONDS] field
         * to be converted to a local date-time. This is primarily intended for
         * low-level conversions rather than general application usage.
         *
         * @param epochSecond  the number of seconds from the epoch of 1970-01-01T00:00:00Z
         * @param nanoOfSecond  the nanosecond within the second, from 0 to 999,999,999
         * @param offset  the zone offset, not null
         * @return the local date-time, not null
         * @throws DateTimeException if the result exceeds the supported range,
         * or if the nano-of-second is invalid
         */
        @JvmStatic
        @JsName("ofEpochSecond")
        fun ofEpochSecond(epochSecond: Long, nanoOfSecond: Int, offset: ZoneOffset): LocalDateTime {
            ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond.toLong())
            val localSecond = epochSecond + offset.totalSeconds  // overflow caught later
            val localEpochDay = MathUtils.floorDiv(localSecond, SECONDS_PER_DAY.toLong())
            val secsOfDay = MathUtils.floorMod(localSecond.toInt(), SECONDS_PER_DAY)
            val date = LocalDate.ofEpochDay(localEpochDay)
            val time = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + nanoOfSecond)
            return LocalDateTime(date, time)
        }

        /**
         * Obtains an instance of `LocalDateTime` from a temporal object.
         * <p>
         * This obtains a local date-time based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `LocalDateTime`.
         * <p>
         * The conversion extracts and combines the `LocalDate` and the
         * `LocalTime` from the temporal object.
         * Implementations are permitted to perform optimizations such as accessing
         * those fields that are equivalent to the relevant objects.
         * <p>
         * This method matches the signature of the functional interface {@link TemporalQuery}
         * allowing it to be used as a query via method reference, `LocalDateTime::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the local date-time, not null
         * @throws DateTimeException if unable to convert to a `LocalDateTime`
         */
        @JvmStatic
        @JsName("from")
        fun from(temporal: TemporalAccessor): LocalDateTime {
            if (temporal is LocalDateTime) {
                return temporal
            } else if (temporal is ZonedDateTime) {
                return temporal.toLocalDateTime() as LocalDateTime
            } else if (temporal is OffsetDateTime) {
                return temporal.localDateTime
            }
            try {
                val date = LocalDate.from(temporal)
                val time = LocalTime.from(temporal)
                return LocalDateTime(date, time)
            } catch (ex: DateTimeException) {
                throw DateTimeException("Unable to obtain LocalDateTime from TemporalAccessor: " +
                        temporal + " of type " + temporal::class.getName(), ex)
            }
        }
    } // companion object

    /**
     * Gets the year field.
     *
     *
     * This method returns the primitive `int` value for the year.
     *
     *
     * The year returned by this method is proleptic as per `get(YEAR)`.
     * To obtain the year-of-era, use `get(YEAR_OF_ERA)`.
     *
     * @return the year, from MIN_YEAR to MAX_YEAR
     */
    @JsName("getYear")
    fun getYear(): Int {
        return date.year
    }

    /**
     * Gets the month-of-year field from 1 to 12.
     *
     *
     * This method returns the month as an `int` from 1 to 12.
     * Application code is frequently clearer if the enum [Month]
     * is used by calling [.getMonth].
     *
     * @return the month-of-year, from 1 to 12
     * @see .getMonth
     */
    @JsName("getMonthValue")
    fun getMonthValue(): Int {
        return date.monthValue.toInt()
    }

    /**
     * Gets the month-of-year field using the `Month` enum.
     *
     *
     * This method returns the enum [Month] for the month.
     * This avoids confusion as to what `int` values mean.
     * If you need access to the primitive `int` value then the enum
     * provides the [int value][Month.getValue].
     *
     * @return the month-of-year, not null
     * @see .getMonthValue
     */
    @JsName("getMonth")
    fun getMonth(): Month {
        return date.getMonth()
    }

    /**
     * Gets the day-of-month field.
     *
     *
     * This method returns the primitive `int` value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    @JsName("getDayOfMonth")
    fun getDayOfMonth(): Int {
        return date.dayOfMonth.toInt()
    }

    /**
     * Gets the day-of-year field.
     *
     *
     * This method returns the primitive `int` value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    @JsName("getDayOfYear")
    fun getDayOfYear(): Int {
        return date.getDayOfYear()
    }

    /**
     * Gets the day-of-week field, which is an enum `DayOfWeek`.
     *
     *
     * This method returns the enum [DayOfWeek] for the day-of-week.
     * This avoids confusion as to what `int` values mean.
     * If you need access to the primitive `int` value then the enum
     * provides the [int value][DayOfWeek.getValue].
     *
     *
     * Additional information can be obtained from the `DayOfWeek`.
     * This includes textual names of the values.
     *
     * @return the day-of-week, not null
     */
    @JsName("getDayOfWeek")
    fun getDayOfWeek(): DayOfWeek {
        return date.getDayOfWeek()
    }

    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    @JsName("getHour")
    fun getHour(): Int {
        return time.hour.toInt()
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    @JsName("getMinute")
    fun getMinute(): Int {
        return time.minute.toInt()
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    @JsName("getSecond")
    fun getSecond(): Int {
        return time.second.toInt()
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    @JsName("getNano")
    fun getNano(): Int {
        return time.nano
    }

    /**
     * Combines this date-time with an offset to create an `OffsetDateTime`.
     *
     *
     * This returns an `OffsetDateTime` formed from this date-time at the specified offset.
     * All possible combinations of date-time and offset are valid.
     *
     * @param offset  the offset to combine with, not null
     * @return the offset date-time formed from this date-time and the specified offset, not null
     */
    @JsName("atOffset")
    fun atOffset(offset: ZoneOffset): OffsetDateTime {
        return OffsetDateTime.of(this, offset)
    }

    /**
     * Combines this date-time with a time-zone to create a `ZonedDateTime`.
     *
     *
     * This returns a `ZonedDateTime` formed from this date-time at the
     * specified time-zone. The result will match this date-time as closely as possible.
     * Time-zone rules, such as daylight savings, mean that not every local date-time
     * is valid for the specified zone, thus the local date-time may be adjusted.
     *
     *
     * The local date-time is resolved to a single instant on the time-line.
     * This is achieved by finding a valid offset from UTC/Greenwich for the local
     * date-time as defined by the [rules][ZoneRules] of the zone ID.
     *
     *
     * In most cases, there is only one valid offset for a local date-time.
     * In the case of an overlap, where clocks are set back, there are two valid offsets.
     * This method uses the earlier offset typically corresponding to "summer".
     *
     *
     * In the case of a gap, where clocks jump forward, there is no valid offset.
     * Instead, the local date-time is adjusted to be later by the length of the gap.
     * For a typical one hour daylight savings change, the local date-time will be
     * moved one hour later into the offset typically corresponding to "summer".
     *
     *
     * To obtain the later offset during an overlap, call
     * [ZonedDateTime.withLaterOffsetAtOverlap] on the result of this method.
     * To throw an exception when there is a gap or overlap, use
     * [ZonedDateTime.ofStrict].
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    override fun atZone(zone: ZoneId): ZonedDateTime {
        return ZonedDateTime.of(this, zone)
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified field is supported.
     *
     *
     * This checks if this date-time can be queried for the specified field.
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
     *  * `DAY_OF_WEEK`
     *  * `ALIGNED_DAY_OF_WEEK_IN_MONTH`
     *  * `ALIGNED_DAY_OF_WEEK_IN_YEAR`
     *  * `DAY_OF_MONTH`
     *  * `DAY_OF_YEAR`
     *  * `EPOCH_DAY`
     *  * `ALIGNED_WEEK_OF_MONTH`
     *  * `ALIGNED_WEEK_OF_YEAR`
     *  * `MONTH_OF_YEAR`
     *  * `PROLEPTIC_MONTH`
     *  * `YEAR_OF_ERA`
     *  * `YEAR`
     *  * `ERA`
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
     * @return true if the field is supported on this date-time, false if not
     */
    override fun isSupported(field: TemporalField): Boolean {
        if (field is ChronoField) {
            return field.isDateBased() || field.isTimeBased()
        }
        return field.isSupportedBy(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the range of valid values for the specified field.
     *
     *
     * The range object expresses the minimum and maximum valid values for a field.
     * This date-time is used to enhance the accuracy of the returned range.
     * If it is not possible to return the range, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The [supported fields][.isSupported] will return
     * appropriate range instances.
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.rangeRefinedBy(TemporalAccessor)`
     * passing `this` as the argument.
     * Whether the range can be obtained is determined by the field.
     *
     * @param field  the field to query the range for, not null
     * @return the range of valid values for the field, not null
     * @throws DateTimeException if the range for the field cannot be obtained
     * @throws UnsupportedTemporalTypeException if the field is not supported
     */
    override fun range(field: TemporalField): ValueRange {
        return if (field is ChronoField) {
            if (field.isTimeBased()) time.range(field) else date.range(field)
        } else {
            field.rangeRefinedBy(this)
        }
    }

    /**
     * Gets the value of the specified field from this date-time as an `int`.
     * <p>
     * This queries this date-time for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     * <p>
     * If the field is a {@link ChronoField} then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return valid
     * values based on this date-time, except `NANO_OF_DAY`, `MICRO_OF_DAY`,
     * `EPOCH_DAY` and `PROLEPTIC_MONTH` which are too large to fit in
     * an `int` and throw an `UnsupportedTemporalTypeException`.
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     * <p>
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
            return if (field.isTimeBased()) time.get(field) else date.get(field)
        }
        return TemporalAccessor.get(this, field)
    }

    /**
     * Gets the value of the specified field from this date-time as a `long`.
     *
     *
     * This queries this date-time for the value of the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The [supported fields][.isSupported] will return valid
     * values based on this date-time.
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
        return if (field is ChronoField) {
            if (field.isTimeBased()) time.getLong(field) else date.getLong(field)
        } else {
            field.getFrom(this)
        }
    }

    /**
     * Calculates the amount of time until another date-time in terms of the specified unit.
     *
     *
     * This calculates the amount of time between two `LocalDateTime`
     * objects in terms of a single `TemporalUnit`.
     * The start and end points are `this` and the specified date-time.
     * The result will be negative if the end is before the start.
     * The `Temporal` passed to this method is converted to a
     * `LocalDateTime` using [.from].
     * For example, the amount in days between two date-times can be calculated
     * using `startDateTime.until(endDateTime, DAYS)`.
     *
     *
     * The calculation returns a whole number, representing the number of
     * complete units between the two date-times.
     * For example, the amount in months between 2012-06-15T00:00 and 2012-08-14T23:59
     * will only be one month as it is one minute short of two months.
     *
     *
     * There are two equivalent ways of using this method.
     * The first is to invoke this method.
     * The second is to use [TemporalUnit.between]:
     * <pre>
     * // these two lines are equivalent
     * amount = start.until(end, MONTHS);
     * amount = MONTHS.between(start, end);
    </pre> *
     * The choice should be made based on which makes the code more readable.
     *
     *
     * The calculation is implemented in this method for [ChronoUnit].
     * The units `NANOS`, `MICROS`, `MILLIS`, `SECONDS`,
     * `MINUTES`, `HOURS` and `HALF_DAYS`, `DAYS`,
     * `WEEKS`, `MONTHS`, `YEARS`, `DECADES`,
     * `CENTURIES`, `MILLENNIA` and `ERAS` are supported.
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
     * @param endExclusive  the end date, exclusive, which is converted to a `LocalDateTime`, not null
     * @param unit  the unit to measure the amount in, not null
     * @return the amount of time between this date-time and the end date-time
     * @throws DateTimeException if the amount cannot be calculated, or the end
     * temporal cannot be converted to a `LocalDateTime`
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = LocalDateTime.from(endExclusive)
        if (unit is ChronoUnit) {
            if (unit.isTimeBased()) {
                var amount = date.daysUntil(end.date)
                if (amount == 0L) {
                    return time.until(end.time, unit)
                }
                var timePart: Long = end.time.toNanoOfDay() - time.toNanoOfDay()
                if (amount > 0) {
                    amount -= 1  // safe
                    timePart += NANOS_PER_DAY  // safe
                } else {
                    amount += 1  // safe
                    timePart -= NANOS_PER_DAY  // safe
                }
                when (unit) {
                    ChronoUnit.NANOS ->
                        amount = MathUtils.multiplyExact(amount, NANOS_PER_DAY)
                    ChronoUnit.MICROS -> {
                        amount = MathUtils.multiplyExact(amount, MICROS_PER_DAY)
                        timePart /= 1000
                    }
                    ChronoUnit.MILLIS -> {
                        amount = MathUtils.multiplyExact(amount, MILLIS_PER_DAY)
                        timePart /= 1000000
                    }
                    ChronoUnit.SECONDS -> {
                        amount = MathUtils.multiplyExact(amount, SECONDS_PER_DAY.toLong())
                        timePart /= NANOS_PER_SECOND
                    }
                    ChronoUnit.MINUTES -> {
                        amount = MathUtils.multiplyExact(amount, MINUTES_PER_DAY.toLong())
                        timePart /= NANOS_PER_MINUTE
                    }
                    ChronoUnit.HOURS -> {
                        amount = MathUtils.multiplyExact(amount, HOURS_PER_DAY.toLong())
                        timePart /= NANOS_PER_HOUR
                    }
                    ChronoUnit.HALF_DAYS -> {
                        amount = MathUtils.multiplyExact(amount, 2)
                        timePart /= (NANOS_PER_HOUR * 12)
                    }
                }
                return MathUtils.addExact(amount, timePart)
            }
            var endDate = end.date
            if (endDate.isAfter(date) && end.time.isBefore(time)) {
                endDate = endDate.minusDays(1)
            } else if (endDate.isBefore(date) && end.time.isAfter(time)) {
                endDate = endDate.plusDays(1)
            }
            return date.until(endDate, unit)
        }
        return unit.between(this, end)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns an adjusted copy of this date-time.
     *
     *
     * This returns a `LocalDateTime`, based on this one, with the date-time adjusted.
     * The adjustment takes place using the specified adjuster strategy object.
     * Read the documentation of the adjuster to understand what adjustment will be made.
     *
     *
     * A simple adjuster might simply set the one of the fields, such as the year field.
     * A more complex adjuster might set the date to the last day of the month.
     *
     *
     * A selection of common adjustments is provided in
     * [TemporalAdjusters][java.time.temporal.TemporalAdjusters].
     * These include finding the "last day of the month" and "next Wednesday".
     * Key date-time classes also implement the `TemporalAdjuster` interface,
     * such as [Month] and [MonthDay][java.time.MonthDay].
     * The adjuster is responsible for handling special cases, such as the varying
     * lengths of month and leap years.
     *
     *
     * For example this code returns a date on the last day of July:
     * <pre>
     * import static java.time.Month.*;
     * import static java.time.temporal.TemporalAdjusters.*;
     *
     * result = localDateTime.with(JULY).with(lastDayOfMonth());
     * </pre>
     *
     *
     * The classes [LocalDate] and [LocalTime] implement `TemporalAdjuster`,
     * thus this method can be used to change the date, time or offset:
     * <pre>
     * result = localDateTime.with(date);
     * result = localDateTime.with(time);
     * </pre>
     *
     *
     * The result of this method is obtained by invoking the
     * [TemporalAdjuster.adjustInto] method on the
     * specified adjuster passing `this` as the argument.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster the adjuster to use, not null
     * @return a `LocalDateTime` based on `this` with the adjustment made, not null
     * @throws DateTimeException if the adjustment cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(adjuster: TemporalAdjuster): LocalDateTime {
        // optimizations
        if (adjuster is LocalDate) {
            return with(adjuster, time)
        } else if (adjuster is LocalTime) {
            return with(date, adjuster)
        } else if (adjuster is LocalDateTime) {
            return adjuster
        }
        return adjuster.adjustInto(this) as LocalDateTime
    }

    /**
     * Returns a copy of this date-time with the specified field set to a new value.
     *
     *
     * This returns a `LocalDateTime`, based on this one, with the value
     * for the specified field changed.
     * This can be used to change any supported field, such as the year, month or day-of-month.
     * If it is not possible to set the value, because the field is not supported or for
     * some other reason, an exception is thrown.
     *
     *
     * In some cases, changing the specified field can cause the resulting date-time to become invalid,
     * such as changing the month from 31st January to February would make the day-of-month invalid.
     * In cases like this, the field is responsible for resolving the date. Typically it will choose
     * the previous valid date, which would be the last valid day of February in this example.
     *
     *
     * If the field is a [ChronoField] then the adjustment is implemented here.
     * The [supported fields][.isSupported] will behave as per
     * the matching method on [LocalDate][LocalDate.with]
     * or [LocalTime][LocalTime.with].
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
     * @return a `LocalDateTime` based on `this` with the specified field set, not null
     * @throws DateTimeException if the field cannot be set
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(field: TemporalField, newValue: Long): LocalDateTime {
        if (field is ChronoField) {
            return if (field.isTimeBased()) {
                with(date, time.with(field, newValue))
            } else {
                with(date.with(field, newValue), time)
            }
        }
        return field.adjustInto(this, newValue)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalDateTime` with the year altered.
     *
     *
     * The time does not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the result, from MIN_YEAR to MAX_YEAR
     * @return a `LocalDateTime` based on this date-time with the requested year, not null
     * @throws DateTimeException if the year value is invalid
     */
    @JsName("withYear")
    fun withYear(year: Int): LocalDateTime {
        return with(date.withYear(year), time)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the month-of-year altered.
     *
     *
     * The time does not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param month  the month-of-year to set in the result, from 1 (January) to 12 (December)
     * @return a `LocalDateTime` based on this date-time with the requested month, not null
     * @throws DateTimeException if the month-of-year value is invalid
     */
    @JsName("withMonth")
    fun withMonth(month: Int): LocalDateTime {
        return with(date.withMonth(month), time)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the day-of-month altered.
     *
     *
     * If the resulting date-time is invalid, an exception is thrown.
     * The time does not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the result, from 1 to 28-31
     * @return a `LocalDateTime` based on this date-time with the requested day, not null
     * @throws DateTimeException if the day-of-month value is invalid,
     * or if the day-of-month is invalid for the month-year
     */
    @JsName("withDayOfMonth")
    fun withDayOfMonth(dayOfMonth: Int): LocalDateTime {
        return with(date.withDayOfMonth(dayOfMonth), time)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the day-of-year altered.
     *
     *
     * If the resulting date-time is invalid, an exception is thrown.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the result, from 1 to 365-366
     * @return a `LocalDateTime` based on this date with the requested day, not null
     * @throws DateTimeException if the day-of-year value is invalid,
     * or if the day-of-year is invalid for the year
     */
    @JsName("withDayOfYear")
    fun withDayOfYear(dayOfYear: Int): LocalDateTime {
        return with(date.withDayOfYear(dayOfYear), time)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalDateTime` with the hour-of-day altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to set in the result, from 0 to 23
     * @return a `LocalDateTime` based on this date-time with the requested hour, not null
     * @throws DateTimeException if the hour value is invalid
     */
    @JsName("withHour")
    fun withHour(hour: Int): LocalDateTime {
        val newTime = time.withHour(hour)
        return with(date, newTime)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the minute-of-hour altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minute  the minute-of-hour to set in the result, from 0 to 59
     * @return a `LocalDateTime` based on this date-time with the requested minute, not null
     * @throws DateTimeException if the minute value is invalid
     */
    @JsName("withMinute")
    fun withMinute(minute: Int): LocalDateTime {
        val newTime = time.withMinute(minute)
        return with(date, newTime)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the second-of-minute altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param second  the second-of-minute to set in the result, from 0 to 59
     * @return a `LocalDateTime` based on this date-time with the requested second, not null
     * @throws DateTimeException if the second value is invalid
     */
    @JsName("withSecond")
    fun withSecond(second: Int): LocalDateTime {
        val newTime = time.withSecond(second)
        return with(date, newTime)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the nano-of-second altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to set in the result, from 0 to 999,999,999
     * @return a `LocalDateTime` based on this date-time with the requested nanosecond, not null
     * @throws DateTimeException if the nano value is invalid
     */
    @JsName("withNano")
    fun withNano(nanoOfSecond: Int): LocalDateTime {
        val newTime = time.withNano(nanoOfSecond)
        return with(date, newTime)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalDateTime` with the time truncated.
     *
     *
     * Truncation returns a copy of the original date-time with fields
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
     * @return a `LocalDateTime` based on this date-time with the time truncated, not null
     * @throws DateTimeException if unable to truncate
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     */
    @JsName("truncatedTo(")
    fun truncatedTo(unit: TemporalUnit): LocalDateTime {
        return with(date, time.truncatedTo(unit))
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified amount added.
     *
     *
     * This returns a `LocalDateTime`, based on this one, with the specified amount added.
     * The amount is typically [Period] or [Duration] but may be
     * any other type implementing the [TemporalAmount] interface.
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
     * @param amountToAdd  the amount to add, not null
     * @return a `LocalDateTime` based on this date-time with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: TemporalAmount): LocalDateTime {
        if (amountToAdd is Period) {
            return with(date.plus(amountToAdd), time)
        }
        return amountToAdd.addTo(this) as LocalDateTime
    }

    /**
     * Returns a copy of this date-time with the specified amount added.
     *
     *
     * This returns a `LocalDateTime`, based on this one, with the amount
     * in terms of the unit added. If it is not possible to add the amount, because the
     * unit is not supported or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoUnit] then the addition is implemented here.
     * Date units are added as per [LocalDate.plus].
     * Time units are added as per [LocalTime.plus] with
     * any overflow in days added equivalent to using [.plusDays].
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
     * @return a `LocalDateTime` based on this date-time with the specified amount added, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: Long, unit: TemporalUnit): LocalDateTime {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.NANOS ->
                    plusNanos(amountToAdd)
                ChronoUnit.MICROS ->
                    plusDays(amountToAdd / MICROS_PER_DAY).plusNanos(amountToAdd % MICROS_PER_DAY * 1000)
                ChronoUnit.MILLIS ->
                    plusDays(amountToAdd / MILLIS_PER_DAY).plusNanos(amountToAdd % MILLIS_PER_DAY * 1000000)
                ChronoUnit.SECONDS ->
                    plusSeconds(amountToAdd)
                ChronoUnit.MINUTES ->
                    plusMinutes(amountToAdd)
                ChronoUnit.HOURS ->
                    plusHours(amountToAdd)
                ChronoUnit.HALF_DAYS ->
                    plusDays(amountToAdd / 256).plusHours(amountToAdd % 256 * 12)  // no overflow (256 is multiple of 2)
                else -> with(date.plus(amountToAdd, unit), time)
            }
        }
        return unit.addTo(this, amountToAdd)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalDateTime` with the specified number of years added.
     *
     *
     * This method adds the specified amount to the years field in three steps:
     *
     *  1. Add the input years to the year field
     *  1. Check if the resulting date would be invalid
     *  1. Adjust the day-of-month to the last valid day if necessary
     *
     *
     *
     * For example, 2008-02-29 (leap year) plus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, may be negative
     * @return a `LocalDateTime` based on this date-time with the years added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("plusYears")
    fun plusYears(years: Long): LocalDateTime {
        val newDate = date.plusYears(years)
        return with(newDate, time)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of months added.
     *
     *
     * This method adds the specified amount to the months field in three steps:
     *
     *  1. Add the input months to the month-of-year field
     *  1. Check if the resulting date would be invalid
     *  1. Adjust the day-of-month to the last valid day if necessary
     *
     *
     *
     * For example, 2007-03-31 plus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, may be negative
     * @return a `LocalDateTime` based on this date-time with the months added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("plusMonths")
    fun plusMonths(months: Long): LocalDateTime {
        val newDate = date.plusMonths(months)
        return with(newDate, time)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of weeks added.
     *
     *
     * This method adds the specified amount in weeks to the days field incrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     *
     *
     * For example, 2008-12-31 plus one week would result in 2009-01-07.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to add, may be negative
     * @return a `LocalDateTime` based on this date-time with the weeks added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("plusWeeks")
    fun plusWeeks(weeks: Long): LocalDateTime {
        val newDate = date.plusWeeks(weeks)
        return with(newDate, time)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of days added.
     *
     *
     * This method adds the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     *
     *
     * For example, 2008-12-31 plus one day would result in 2009-01-01.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, may be negative
     * @return a `LocalDateTime` based on this date-time with the days added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("plusDays")
    fun plusDays(days: Long): LocalDateTime {
        val newDate = date.plusDays(days)
        return with(newDate, time)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalDateTime` with the specified number of hours added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, may be negative
     * @return a `LocalDateTime` based on this date-time with the hours added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("plusHours")
    fun plusHours(hours: Long): LocalDateTime {
        return plusWithOverflow(date, hours, 0, 0, 0, 1)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of minutes added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, may be negative
     * @return a `LocalDateTime` based on this date-time with the minutes added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("plusMinutes")
    fun plusMinutes(minutes: Long): LocalDateTime {
        return plusWithOverflow(date, 0, minutes, 0, 0, 1)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of seconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, may be negative
     * @return a `LocalDateTime` based on this date-time with the seconds added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("plusSeconds")
    fun plusSeconds(seconds: Long): LocalDateTime {
        return plusWithOverflow(date, 0, 0, seconds, 0, 1)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of nanoseconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add, may be negative
     * @return a `LocalDateTime` based on this date-time with the nanoseconds added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("plusNanos")
    fun plusNanos(nanos: Long): LocalDateTime {
        return plusWithOverflow(date, 0, 0, 0, nanos, 1)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of years subtracted.
     *
     *
     * This method subtracts the specified amount from the years field in three steps:
     *
     *  1. Subtract the input years from the year field
     *  1. Check if the resulting date would be invalid
     *  1. Adjust the day-of-month to the last valid day if necessary
     *
     *
     *
     * For example, 2008-02-29 (leap year) minus one year would result in the
     * invalid date 2007-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2007-02-28, is selected instead.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @return a `LocalDateTime` based on this date-time with the years subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("minusYears")
    fun minusYears(years: Long): LocalDateTime {
        return if (years == Long.MIN_VALUE) plusYears(Long.MAX_VALUE).plusYears(1) else plusYears(-years)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of months subtracted.
     *
     *
     * This method subtracts the specified amount from the months field in three steps:
     *
     *  1. Subtract the input months from the month-of-year field
     *  1. Check if the resulting date would be invalid
     *  1. Adjust the day-of-month to the last valid day if necessary
     *
     *
     *
     * For example, 2007-03-31 minus one month would result in the invalid date
     * 2007-02-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-02-28, is selected instead.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @return a `LocalDateTime` based on this date-time with the months subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("minusMonths")
    fun minusMonths(months: Long): LocalDateTime {
        return if (months == Long.MIN_VALUE) plusMonths(Long.MAX_VALUE).plusMonths(1) else plusMonths(-months)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of weeks subtracted.
     *
     *
     * This method subtracts the specified amount in weeks from the days field decrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     *
     *
     * For example, 2009-01-07 minus one week would result in 2008-12-31.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to subtract, may be negative
     * @return a `LocalDateTime` based on this date-time with the weeks subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("minusWeeks")
    fun minusWeeks(weeks: Long): LocalDateTime {
        return if (weeks == Long.MIN_VALUE) plusWeeks(Long.MAX_VALUE).plusWeeks(1) else plusWeeks(-weeks)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of days subtracted.
     *
     *
     * This method subtracts the specified amount from the days field decrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     *
     *
     * For example, 2009-01-01 minus one day would result in 2008-12-31.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, may be negative
     * @return a `LocalDateTime` based on this date-time with the days subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("minusDays")
    fun minusDays(days: Long): LocalDateTime {
        return if (days == Long.MIN_VALUE) plusDays(Long.MAX_VALUE).plusDays(1) else plusDays(-days)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalDateTime` with the specified number of hours subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, may be negative
     * @return a `LocalDateTime` based on this date-time with the hours subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("minusHours")
    fun minusHours(hours: Long): LocalDateTime {
        return plusWithOverflow(date, hours, 0, 0, 0, -1)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of minutes subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, may be negative
     * @return a `LocalDateTime` based on this date-time with the minutes subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("minusMinutes")
    fun minusMinutes(minutes: Long): LocalDateTime {
        return plusWithOverflow(date, 0, minutes, 0, 0, -1)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of seconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, may be negative
     * @return a `LocalDateTime` based on this date-time with the seconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("minusSeconds")
    fun minusSeconds(seconds: Long): LocalDateTime {
        return plusWithOverflow(date, 0, 0, seconds, 0, -1)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified number of nanoseconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to subtract, may be negative
     * @return a `LocalDateTime` based on this date-time with the nanoseconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    @JsName("minusNanos")
    fun isAfter(nanos: Long): LocalDateTime {
        return plusWithOverflow(date, 0, 0, 0, nanos, -1)
    }

    // ----==== Comparison ====----
    override fun compareTo(other: LocalDateTime): Int {
        val cmp = this.date.compareTo(other.date)
        return if (cmp == 0) this.time.compareTo(other.time) else cmp
    }

    @JsName("isAfter")
    fun isAfter(other: LocalDateTime): Boolean {
        return this > other
    }

    @JsName("isBefore")
    fun isBefore(other: LocalDateTime): Boolean {
        return this < other
    }

    /**
     * Checks if this date-time is equal to another date-time.
     *
     *
     * Compares this `LocalDateTime` with another ensuring that the date-time is the same.
     * Only objects of type `LocalDateTime` are compared, other types return false.
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other date-time
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is LocalDateTime) {
            return this.date == other.date && this.time == other.time
        }
        return false
    }

    /**
     * A hash code for this date-time.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return date.hashCode() xor time.hashCode()
    }

    /**
     * Outputs this date-time as a `String`, such as `2007-12-03T10:15:30`.
     *
     *
     * The output will be one of the following ISO-8601 formats:
     *
     *  * `uuuu-MM-dd'T'HH:mm`
     *  * `uuuu-MM-dd'T'HH:mm:ss`
     *  * `uuuu-MM-dd'T'HH:mm:ss.SSS`
     *  * `uuuu-MM-dd'T'HH:mm:ss.SSSSSS`
     *  * `uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS`
     *
     * The format used will be the shortest that outputs the full value of
     * the time where the omitted parts are implied to be zero.
     *
     * @return a string representation of this date-time, not null
     */
    override fun toString(): String {
        return date.toString() + 'T' + time.toString()
    }

    override fun getChronology(): Chronology {
        return ChronoLocalDateTime.getChronology(this)
    }

    override fun toInstant(offset: ZoneOffset): Instant {
        return ChronoLocalDateTime.toInstant(this, offset)
    }

    override fun toEpochSecond(offset: ZoneOffset): Long {
        return ChronoLocalDateTime.toEpochSecond(this, offset)
    }

    override fun isSupported(unit: TemporalUnit): Boolean {
        return ChronoLocalDateTime.isSupported(this, unit)
    }

    override fun <R> query(query: TemporalQuery<R>): R? {
        return ChronoLocalDateTime.query(this, query)
    }

    override fun adjustInto(temporal: Temporal): Temporal {
        return ChronoLocalDateTime.adjustInto(this, temporal)
    }

    override fun compareTo(other: ChronoLocalDateTime<*>): Int {
        return ChronoLocalDateTime.compare(this, other)
    }

    override fun isAfter(other: ChronoLocalDateTime<*>): Boolean {
        return ChronoLocalDateTime.isAfter(this, other)
    }

    override fun isBefore(other: ChronoLocalDateTime<*>): Boolean {
        return ChronoLocalDateTime.isBefore(this, other)
    }

    override fun isEqual(other: ChronoLocalDateTime<*>): Boolean {
        return ChronoLocalDateTime.isEqual(this, other)
    }

    override fun minus(amount: TemporalAmount): Temporal {
        return Temporal.minus(this, amount)
    }

    override fun minus(amountToSubtract: Long, unit: TemporalUnit): Temporal {
        return Temporal.minus(this, amountToSubtract, unit)
    }

    /**
     * Returns a copy of this `LocalDateTime` with the specified period added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param newDate  the new date to base the calculation on, not null
     * @param hours  the hours to add, may be negative
     * @param minutes the minutes to add, may be negative
     * @param seconds the seconds to add, may be negative
     * @param nanos the nanos to add, may be negative
     * @param sign  the sign to determine add or subtract
     * @return the combined result, not null
     */
    private fun plusWithOverflow(
        newDate: LocalDate,
        hours: Long,
        minutes: Long,
        seconds: Long,
        nanos: Long,
        sign: Int
    ): LocalDateTime {
        // 9223372036854775808 long, 2147483648 int
        if (hours or minutes or seconds or nanos == 0L) {
            return with(newDate, time)
        }
        var totDays = nanos / NANOS_PER_DAY +             //   max/24*60*60*1B

                seconds / SECONDS_PER_DAY +                //   max/24*60*60

                minutes / MINUTES_PER_DAY +                //   max/24*60

                hours / HOURS_PER_DAY                     //   max/24
        totDays *= sign.toLong()                                   // total max*0.4237...
        var totNanos = nanos % NANOS_PER_DAY +                    //   max  86400000000000

                seconds % SECONDS_PER_DAY * NANOS_PER_SECOND +   //   max  86400000000000

                minutes % MINUTES_PER_DAY * NANOS_PER_MINUTE +   //   max  86400000000000

                hours % HOURS_PER_DAY * NANOS_PER_HOUR          //   max  86400000000000
        val curNoD = time.toNanoOfDay()                       //   max  86400000000000
        totNanos = totNanos * sign + curNoD                    // total 432000000000000
        totDays += MathUtils.floorDiv(totNanos, NANOS_PER_DAY)
        val newNoD = MathUtils.floorMod(totNanos, NANOS_PER_DAY)
        val newTime = if (newNoD == curNoD) time else LocalTime.ofNanoOfDay(newNoD)
        return with(newDate.plusDays(totDays), newTime)
    }

    /**
     * Returns a copy of this date-time with the new date and time, checking
     * to see if a new object is in fact required.
     *
     * @param newDate  the date of the new date-time, not null
     * @param newTime  the time of the new date-time, not null
     * @return the date-time, not null
     */
    private fun with(newDate: LocalDate, newTime: LocalTime): LocalDateTime {
        return if (this.date == newDate && this.time == newTime) {
            this
        } else LocalDateTime(newDate, newTime)
    }
}