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
 * A date-time with an offset from UTC/Greenwich in the ISO-8601 calendar system,
 * such as `2007-12-03T10:15:30+01:00`.
 * <p>
 * `OffsetDateTime` is an immutable representation of a date-time with an offset.
 * This class stores all date and time fields, to a precision of nanoseconds,
 * as well as the offset from UTC/Greenwich. For example, the value
 * "2nd October 2007 at 13:45:30.123456789 +02:00" can be stored in an `OffsetDateTime`.
 * <p>
 * `OffsetDateTime`, {@link java.time.ZonedDateTime} and {@link java.time.Instant} all store an instant
 * on the time-line to nanosecond precision.
 * `Instant` is the simplest, simply representing the instant.
 * `OffsetDateTime` adds to the instant the offset from UTC/Greenwich, which allows
 * the local date-time to be obtained.
 * `ZonedDateTime` adds full time-zone rules.
 * <p>
 * It is intended that `ZonedDateTime` or `Instant` is used to model data
 * in simpler applications. This class may be used when modeling date-time concepts in
 * more detail, or when communicating to a database or in a network protocol.
 *
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `OffsetDateTime` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
class OffsetDateTime private constructor(
    val localDateTime: LocalDateTime,
    val offset: ZoneOffset
) {

    companion object {
        /**
         * The minimum supported `OffsetDateTime`, '-999999999-01-01T00:00:00+18:00'.
         * This is the local date-time of midnight at the start of the minimum date
         * in the maximum offset (larger offsets are earlier on the time-line).
         * This combines [LocalDateTime.MIN] and [ZoneOffset.MAX].
         * This could be used by an application as a "far past" date-time.
         */
        val MIN = LocalDateTime.MIN.atOffset(ZoneOffset.MAX)
        /**
         * The maximum supported `OffsetDateTime`, '+999999999-12-31T23:59:59.999999999-18:00'.
         * This is the local date-time just before midnight at the end of the maximum date
         * in the minimum offset (larger negative offsets are later on the time-line).
         * This combines [LocalDateTime.MAX] and [ZoneOffset.MIN].
         * This could be used by an application as a "far future" date-time.
         */
        val MAX = LocalDateTime.MAX.atOffset(ZoneOffset.MIN)

        /**
         * Compares this {@code OffsetDateTime} to another date-time.
         * The comparison is based on the instant.
         *
         * @param datetime1  the first date-time to compare, not null
         * @param datetime2  the other date-time to compare to, not null
         * @return the comparator value, negative if less, positive if greater
         */
        val timeLineOrder = {
            datetime1: OffsetDateTime, datetime2: OffsetDateTime ->
            if (datetime1.offset == datetime2.offset) {
                datetime1.localDateTime.compareTo(datetime2.localDateTime)
            } else {
                val a = datetime1.toEpochSecond()
                val b = datetime2.toEpochSecond()
                if (a > b) 1 else if (a < b) -1 else
                        datetime1.localDateTime.getNano() - datetime2.localDateTime.getNano()
            }
        } as Comparator<OffsetDateTime>

        /**
         * Serialization version.
         */
        private const val serialVersionUID = 2287754244819255394L

        //-----------------------------------------------------------------------
        /**
         * Obtains an instance of `OffsetDateTime` from a date, time and offset.
         *
         *
         * This creates an offset date-time with the specified local date, time and offset.
         *
         * @param date  the local date, not null
         * @param time  the local time, not null
         * @param offset  the zone offset, not null
         * @return the offset date-time, not null
         */
        fun of(date: LocalDate, time: LocalTime, offset: ZoneOffset): OffsetDateTime {
            val dt = LocalDateTime.of(date, time)
            return OffsetDateTime(dt, offset)
        }

        /**
         * Obtains an instance of `OffsetDateTime` from a date-time and offset.
         *
         *
         * This creates an offset date-time with the specified local date-time and offset.
         *
         * @param dateTime  the local date-time, not null
         * @param offset  the zone offset, not null
         * @return the offset date-time, not null
         */
        fun of(dateTime: LocalDateTime, offset: ZoneOffset): OffsetDateTime {
            return OffsetDateTime(dateTime, offset)
        }

        /**
         * Obtains an instance of `OffsetDateTime` from a year, month, day,
         * hour, minute, second, nanosecond and offset.
         *
         *
         * This creates an offset date-time with the seven specified fields.
         *
         *
         * This method exists primarily for writing test cases.
         * Non test-code will typically use other methods to create an offset time.
         * `LocalDateTime` has five additional convenience variants of the
         * equivalent factory method taking fewer arguments.
         * They are not provided here to reduce the footprint of the API.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @param second  the second-of-minute to represent, from 0 to 59
         * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
         * @param offset  the zone offset, not null
         * @return the offset date-time, not null
         * @throws DateTimeException if the value of any field is out of range, or
         * if the day-of-month is invalid for the month-year
         */
        fun of(
            year: Int, month: Int, dayOfMonth: Int,
            hour: Int, minute: Int, second: Int, nanoOfSecond: Int, offset: ZoneOffset
        ): OffsetDateTime {
            val dt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond)
            return OffsetDateTime(dt, offset)
        }
    } // companion object

    /**
     * Converts this date-time to an `Instant`.
     *
     *
     * This returns an `Instant` representing the same point on the
     * time-line as this date-time.
     *
     * @return an `Instant` representing the same instant, not null
     */
    fun toInstant(): Instant {
        return localDateTime.toInstant(offset)
    }

    /**
     * Converts this date-time to the number of seconds from the epoch of 1970-01-01T00:00:00Z.
     *
     *
     * This allows this date-time to be converted to a value of the
     * [epoch-seconds][ChronoField.INSTANT_SECONDS] field. This is primarily
     * intended for low-level conversions rather than general application usage.
     *
     * @return the number of seconds from the epoch of 1970-01-01T00:00:00Z
     */
    fun toEpochSecond(): Long {
        return localDateTime.toEpochSecond(offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified offset ensuring
     * that the result is at the same instant.
     *
     *
     * This method returns an object with the specified `ZoneOffset` and a `LocalDateTime`
     * adjusted by the difference between the two offsets.
     * This will result in the old and new objects representing the same instant.
     * This is useful for finding the local time in a different offset.
     * For example, if this time represents `2007-12-03T10:30+02:00` and the offset specified is
     * `+03:00`, then this method will return `2007-12-03T11:30+03:00`.
     *
     *
     * To change the offset without adjusting the local time use [.withOffsetSameLocal].
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the zone offset to change to, not null
     * @return an `OffsetDateTime` based on this date-time with the requested offset, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun withOffsetSameInstant(offset: ZoneOffset): OffsetDateTime {
        if (offset == this.offset) {
            return this
        }
        val difference = offset.totalSeconds - this.offset.totalSeconds
        val adjusted = localDateTime.plusSeconds(difference.toLong())
        return OffsetDateTime(adjusted, offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the `LocalDate` part of this date-time.
     *
     *
     * This returns a `LocalDate` with the same year, month and day
     * as this date-time.
     *
     * @return the date part of this date-time, not null
     */
    fun toLocalDate(): LocalDate {
        return localDateTime.getDate()
    }

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
    fun getYear(): Int {
        return localDateTime.getYear()
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
    fun getMonthValue(): Int {
        return localDateTime.getMonthValue()
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
    fun getMonth(): Month {
        return localDateTime.getMonth()
    }

    /**
     * Gets the day-of-month field.
     *
     *
     * This method returns the primitive `int` value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    fun getDayOfMonth(): Int {
        return localDateTime.getDayOfMonth()
    }

    /**
     * Gets the day-of-year field.
     *
     *
     * This method returns the primitive `int` value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    fun getDayOfYear(): Int {
        return localDateTime.getDayOfYear()
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
    fun getDayOfWeek(): DayOfWeek {
        return localDateTime.getDayOfWeek()
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the `LocalTime` part of this date-time.
     *
     *
     * This returns a `LocalTime` with the same hour, minute, second and
     * nanosecond as this date-time.
     *
     * @return the time part of this date-time, not null
     */
    fun toLocalTime(): LocalTime {
        return localDateTime.getTime()
    }

    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    fun getHour(): Int {
        return localDateTime.getHour()
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    fun getMinute(): Int {
        return localDateTime.getMinute()
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    fun getSecond(): Int {
        return localDateTime.getSecond()
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    fun getNano(): Int {
        return localDateTime.getNano()
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `OffsetDateTime` with the year altered.
     *
     *
     * The time and offset do not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the result, from MIN_YEAR to MAX_YEAR
     * @return an `OffsetDateTime` based on this date-time with the requested year, not null
     * @throws DateTimeException if the year value is invalid
     */
    fun withYear(year: Int): OffsetDateTime {
        return with(localDateTime.withYear(year), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the month-of-year altered.
     *
     *
     * The time and offset do not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param month  the month-of-year to set in the result, from 1 (January) to 12 (December)
     * @return an `OffsetDateTime` based on this date-time with the requested month, not null
     * @throws DateTimeException if the month-of-year value is invalid
     */
    fun withMonth(month: Int): OffsetDateTime {
        return with(localDateTime.withMonth(month), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the day-of-month altered.
     *
     *
     * If the resulting `OffsetDateTime` is invalid, an exception is thrown.
     * The time and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the result, from 1 to 28-31
     * @return an `OffsetDateTime` based on this date-time with the requested day, not null
     * @throws DateTimeException if the day-of-month value is invalid,
     * or if the day-of-month is invalid for the month-year
     */
    fun withDayOfMonth(dayOfMonth: Int): OffsetDateTime {
        return with(localDateTime.withDayOfMonth(dayOfMonth), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the day-of-year altered.
     *
     *
     * The time and offset do not affect the calculation and will be the same in the result.
     * If the resulting `OffsetDateTime` is invalid, an exception is thrown.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the result, from 1 to 365-366
     * @return an `OffsetDateTime` based on this date with the requested day, not null
     * @throws DateTimeException if the day-of-year value is invalid,
     * or if the day-of-year is invalid for the year
     */
    fun withDayOfYear(dayOfYear: Int): OffsetDateTime {
        return with(localDateTime.withDayOfYear(dayOfYear), offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `OffsetDateTime` with the hour-of-day altered.
     *
     *
     * The date and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to set in the result, from 0 to 23
     * @return an `OffsetDateTime` based on this date-time with the requested hour, not null
     * @throws DateTimeException if the hour value is invalid
     */
    fun withHour(hour: Int): OffsetDateTime {
        return with(localDateTime.withHour(hour), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the minute-of-hour altered.
     *
     *
     * The date and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minute  the minute-of-hour to set in the result, from 0 to 59
     * @return an `OffsetDateTime` based on this date-time with the requested minute, not null
     * @throws DateTimeException if the minute value is invalid
     */
    fun withMinute(minute: Int): OffsetDateTime {
        return with(localDateTime.withMinute(minute), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the second-of-minute altered.
     *
     *
     * The date and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param second  the second-of-minute to set in the result, from 0 to 59
     * @return an `OffsetDateTime` based on this date-time with the requested second, not null
     * @throws DateTimeException if the second value is invalid
     */
    fun withSecond(second: Int): OffsetDateTime {
        return with(localDateTime.withSecond(second), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the nano-of-second altered.
     *
     *
     * The date and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to set in the result, from 0 to 999,999,999
     * @return an `OffsetDateTime` based on this date-time with the requested nanosecond, not null
     * @throws DateTimeException if the nano value is invalid
     */
    fun withNano(nanoOfSecond: Int): OffsetDateTime {
        return with(localDateTime.withNano(nanoOfSecond), offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of years added.
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
     * @return an `OffsetDateTime` based on this date-time with the years added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusYears(years: Long): OffsetDateTime {
        return with(localDateTime.plusYears(years), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of months added.
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
     * @return an `OffsetDateTime` based on this date-time with the months added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusMonths(months: Long): OffsetDateTime {
        return with(localDateTime.plusMonths(months), offset)
    }

    /**
     * Returns a copy of this OffsetDateTime with the specified number of weeks added.
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
     * @return an `OffsetDateTime` based on this date-time with the weeks added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusWeeks(weeks: Long): OffsetDateTime {
        return with(localDateTime.plusWeeks(weeks), offset)
    }

    /**
     * Returns a copy of this OffsetDateTime with the specified number of days added.
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
     * @return an `OffsetDateTime` based on this date-time with the days added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusDays(days: Long): OffsetDateTime {
        return with(localDateTime.plusDays(days), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of hours added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the hours added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusHours(hours: Long): OffsetDateTime {
        return with(localDateTime.plusHours(hours), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of minutes added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the minutes added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusMinutes(minutes: Long): OffsetDateTime {
        return with(localDateTime.plusMinutes(minutes), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of seconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the seconds added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusSeconds(seconds: Long): OffsetDateTime {
        return with(localDateTime.plusSeconds(seconds), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of nanoseconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the nanoseconds added, not null
     * @throws DateTimeException if the unit cannot be added to this type
     */
    fun plusNanos(nanos: Long): OffsetDateTime {
        return with(localDateTime.plusNanos(nanos), offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of years subtracted.
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
     * @return an `OffsetDateTime` based on this date-time with the years subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusYears(years: Long): OffsetDateTime {
        return if (years == Long.MIN_VALUE) plusYears(Long.MAX_VALUE).plusYears(1)
                else plusYears(-years)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of months subtracted.
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
     * @return an `OffsetDateTime` based on this date-time with the months subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusMonths(months: Long): OffsetDateTime {
        return if (months == Long.MIN_VALUE) plusMonths(Long.MAX_VALUE).plusMonths(1)
                else plusMonths(-months)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of weeks subtracted.
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
     * @return an `OffsetDateTime` based on this date-time with the weeks subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusWeeks(weeks: Long): OffsetDateTime {
        return if (weeks == Long.MIN_VALUE) plusWeeks(Long.MAX_VALUE).plusWeeks(1)
                else plusWeeks(-weeks)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of days subtracted.
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
     * @return an `OffsetDateTime` based on this date-time with the days subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusDays(days: Long): OffsetDateTime {
        return if (days == Long.MIN_VALUE) plusDays(Long.MAX_VALUE).plusDays(1)
                else plusDays(-days)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of hours subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the hours subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusHours(hours: Long): OffsetDateTime {
        return if (hours == Long.MIN_VALUE) plusHours(Long.MAX_VALUE).plusHours(1)
                else plusHours(-hours)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of minutes subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the minutes subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusMinutes(minutes: Long): OffsetDateTime {
        return if (minutes == Long.MIN_VALUE) plusMinutes(Long.MAX_VALUE).plusMinutes(1)
                else plusMinutes(-minutes)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of seconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the seconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusSeconds(seconds: Long): OffsetDateTime {
        return if (seconds == Long.MIN_VALUE) plusSeconds(Long.MAX_VALUE).plusSeconds(1)
                else plusSeconds(-seconds)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of nanoseconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the nanoseconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusNanos(nanos: Long): OffsetDateTime {
        return if (nanos == Long.MIN_VALUE) plusNanos(Long.MAX_VALUE).plusNanos(1)
                else plusNanos(-nanos)
    }

    /**
     * Returns a new date-time based on this one, returning `this` where possible.
     *
     * @param dateTime  the date-time to create with, not null
     * @param offset  the zone offset to create with, not null
     */
    private fun with(dateTime: LocalDateTime, offset: ZoneOffset): OffsetDateTime {
        return if (this.localDateTime == dateTime && this.offset == offset) this
                else OffsetDateTime(dateTime, offset)
    }
}