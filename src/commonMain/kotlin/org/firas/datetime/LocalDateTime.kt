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
import org.firas.datetime.LocalTime.Companion.MINUTES_PER_DAY
import org.firas.datetime.LocalTime.Companion.NANOS_PER_DAY
import org.firas.datetime.LocalTime.Companion.NANOS_PER_HOUR
import org.firas.datetime.LocalTime.Companion.NANOS_PER_MINUTE
import org.firas.datetime.LocalTime.Companion.NANOS_PER_SECOND
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_DAY
import org.firas.datetime.chrono.ChronoLocalDateTime
import org.firas.datetime.util.MathUtils
import org.firas.datetime.zone.ZoneOffset



/**
 * A date-time without a time-zone in the ISO-8601 calendar system,
 * such as {@code 2007-12-03T10:15:30}.
 * <p>
 * {@code LocalDateTime} is an immutable date-time object that represents a date-time,
 * often viewed as year-month-day-hour-minute-second. Other date and time fields,
 * such as day-of-year, day-of-week and week-of-year, can also be accessed.
 * Time is represented to nanosecond precision.
 * For example, the value "2nd October 2007 at 13:45.30.123456789" can be
 * stored in a {@code LocalDateTime}.
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
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code LocalDateTime} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping
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
        val MIN = LocalDateTime.of(LocalDate.MIN, LocalTime.MIN)
        /**
         * The maximum supported `LocalDateTime`, '+999999999-12-31T23:59:59.999999999'.
         * This is the local date-time just before midnight at the end of the maximum date.
         * This combines [LocalDate.MAX] and [LocalTime.MAX].
         * This could be used by an application as a "far future" date-time.
         */
        val MAX = LocalDateTime.of(LocalDate.MAX, LocalTime.MAX)

        /**
         * Serialization version.
         */
        private const val serialVersionUID = 6207766400415563566L

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
        fun of(date: LocalDate, time: LocalTime): LocalDateTime {
            return LocalDateTime(date, time)
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
    fun getDayOfWeek(): DayOfWeek {
        return date.getDayOfWeek()
    }

    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    fun getHour(): Int {
        return time.hour.toInt()
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    fun getMinute(): Int {
        return time.minute.toInt()
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    fun getSecond(): Int {
        return time.second.toInt()
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
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
    fun atOffset(offset: ZoneOffset): OffsetDateTime {
        return OffsetDateTime.of(this, offset)
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
    fun withNano(nanoOfSecond: Int): LocalDateTime {
        val newTime = time.withNano(nanoOfSecond)
        return with(date, newTime)
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
    fun minusNanos(nanos: Long): LocalDateTime {
        return plusWithOverflow(date, 0, 0, 0, nanos, -1)
    }

    override fun compareTo(other: LocalDateTime): Int {
        val cmp = this.date.compareTo(other.date)
        return if (cmp == 0) this.time.compareTo(other.time) else cmp
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