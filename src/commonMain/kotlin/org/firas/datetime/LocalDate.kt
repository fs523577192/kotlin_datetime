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

import org.firas.datetime.temporal.ChronoField
import org.firas.datetime.util.MathUtils
import org.firas.datetime.zone.ZoneOffset

/**
 * A date without a time-zone in the ISO-8601 calendar system,
 * such as {@code 2007-12-03}.
 * <p>
 * {@code LocalDate} is an immutable date-time object that represents a date,
 * often viewed as year-month-day. Other date fields, such as day-of-year,
 * day-of-week and week-of-year, can also be accessed.
 * For example, the value "2nd October 2007" can be stored in a {@code LocalDate}.
 * <p>
 * This class does not store or represent a time or time-zone.
 * Instead, it is a description of the date, as used for birthdays.
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
 * {@code LocalDate} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
class LocalDate private constructor(
    private val year: Int,
    private val monthValue: Short,
    private val dayOfMonth: Short
): Comparable<LocalDate> {

    companion object {
        /**
         * The minimum supported `LocalDate`, '-999999999-01-01'.
         * This could be used by an application as a "far past" date.
         */
        val MIN = LocalDate.of(Year.MIN_VALUE, 1, 1)
        /**
         * The maximum supported `LocalDate`, '+999999999-12-31'.
         * This could be used by an application as a "far future" date.
         */
        val MAX = LocalDate.of(Year.MAX_VALUE, 12, 31)
        /**
         * The epoch year `LocalDate`, '1970-01-01'.
         */
        val EPOCH = LocalDate.of(1970, 1, 1)

        /**
         * Serialization version.
         */
        private const val serialVersionUID = 2942565459149668126L

        /**
         * The number of days in a 400 year cycle.
         */
        private const val DAYS_PER_CYCLE = 146097

        /**
         * The number of days from year zero to year 1970.
         * There are five 400 year cycles from year zero to 2000.
         * There are 7 leap years from 1970 to 2000.
         */
        internal const val DAYS_0000_TO_1970 = DAYS_PER_CYCLE * 5L - (30L * 365L + 7L)

        // ----==== Factory methods "of" ====----
        /**
         * Obtains an instance of `LocalDate` from a year, month and day.
         *
         *
         * This returns a `LocalDate` with the specified year, month and day-of-month.
         * The day must be valid for the year and month, otherwise an exception will be thrown.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, not null
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @return the local date, not null
         * @throws DateTimeException if the value of any field is out of range,
         * or if the day-of-month is invalid for the month-year
         */
        fun of(year: Int, month: Month, dayOfMonth: Int): LocalDate {
            ChronoField.YEAR.checkValidValue(year.toLong())
            ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth.toLong())
            return create(year, month.getValue(), dayOfMonth)
        }

        /**
         * Obtains an instance of `LocalDate` from a year, month and day.
         *
         *
         * This returns a `LocalDate` with the specified year, month and day-of-month.
         * The day must be valid for the year and month, otherwise an exception will be thrown.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @return the local date, not null
         * @throws DateTimeException if the value of any field is out of range,
         * or if the day-of-month is invalid for the month-year
         */
        fun of(year: Int, month: Int, dayOfMonth: Int): LocalDate {
            ChronoField.YEAR.checkValidValue(year.toLong())
            ChronoField.MONTH_OF_YEAR.checkValidValue(month.toLong())
            ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth.toLong())
            return create(year, month, dayOfMonth)
        }

        // ----==== Factory method "ofYearDay" ====----
        /**
         * Obtains an instance of `LocalDate` from a year and day-of-year.
         *
         *
         * This returns a `LocalDate` with the specified year and day-of-year.
         * The day-of-year must be valid for the year, otherwise an exception will be thrown.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param dayOfYear  the day-of-year to represent, from 1 to 366
         * @return the local date, not null
         * @throws DateTimeException if the value of any field is out of range,
         * or if the day-of-year is invalid for the year
         */
        fun ofYearDay(year: Int, dayOfYear: Int): LocalDate {
            ChronoField.YEAR.checkValidValue(year.toLong())
            ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear.toLong())
            val leap = Year.isLeap(year)
            if (dayOfYear == 366 && !leap) {
                throw DateTimeException("Invalid date 'DayOfYear 366' as '$year' is not a leap year")
            }
            var moy = Month.of((dayOfYear - 1) / 31 + 1)
            val monthEnd = moy.firstDayOfYear(leap) + moy.length(leap) - 1
            if (dayOfYear > monthEnd) {
                moy = moy.plus(1)
            }
            val dom = dayOfYear - moy.firstDayOfYear(leap) + 1
            return LocalDate(year, moy.getValue().toShort(), dom.toShort())
        }

        /**
         * Obtains an instance of `LocalDate` from the epoch day count.
         *
         *
         * This returns a `LocalDate` with the specified epoch-day.
         * The [EPOCH_DAY][ChronoField.EPOCH_DAY] is a simple incrementing count
         * of days where day 0 is 1970-01-01. Negative numbers represent earlier days.
         *
         * @param epochDay  the Epoch Day to convert, based on the epoch 1970-01-01
         * @return the local date, not null
         * @throws DateTimeException if the epoch day exceeds the supported date range
         */
        fun ofEpochDay(epochDay: Long): LocalDate {
            ChronoField.EPOCH_DAY.checkValidValue(epochDay)
            var zeroDay = epochDay + DAYS_0000_TO_1970
            // find the march-based year
            zeroDay -= 60  // adjust to 0000-03-01 so leap day is at end of four year cycle
            var adjust: Long = 0
            if (zeroDay < 0) {
                // adjust negative years to positive for calculation
                val adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1
                adjust = adjustCycles * 400
                zeroDay += -adjustCycles * DAYS_PER_CYCLE
            }
            var yearEst = (400 * zeroDay + 591) / DAYS_PER_CYCLE
            var doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
            if (doyEst < 0) {
                // fix estimate
                yearEst -= 1
                doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
            }
            yearEst += adjust  // reset any negative year
            val marchDoy0 = doyEst.toInt()

            // convert march-based values back to january-based
            val marchMonth0 = (marchDoy0 * 5 + 2) / 153
            val month = (marchMonth0 + 2) % 12 + 1
            val dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1
            yearEst += (marchMonth0 / 10).toLong()

            // check year now we are certain it is correct
            val year = ChronoField.YEAR.checkValidIntValue(yearEst)
            return LocalDate(year, month.toShort(), dom.toShort())
        }

        //-----------------------------------------------------------------------
        /**
         * Creates a local date from the year, month and day fields.
         *
         * @param year  the year to represent, validated from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, from 1 to 12, validated
         * @param dayOfMonth  the day-of-month to represent, validated from 1 to 31
         * @return the local date, not null
         * @throws DateTimeException if the day-of-month is invalid for the month-year
         */
        private fun create(year: Int, month: Int, dayOfMonth: Int): LocalDate {
            if (dayOfMonth > 28) {
                var dom = 31
                when (month) {
                    2 -> dom = (if (Year.isLeap(year)) 29 else 28)
                    4, 6, 9, 11 -> dom = 30
                }
                if (dayOfMonth > dom) {
                    if (dayOfMonth == 29) {
                        throw DateTimeException("Invalid date 'February 29' as '$year' is not a leap year")
                    } else {
                        throw DateTimeException("Invalid date '" + Month.of(month).name + " " + dayOfMonth + "'")
                    }
                }
            }
            return LocalDate(year, month.toShort(), dayOfMonth.toShort())
        }

        /**
         * Resolves the date, resolving days past the end of month.
         *
         * @param year  the year to represent, validated from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, validated from 1 to 12
         * @param day  the day-of-month to represent, validated from 1 to 31
         * @return the resolved date, not null
         */
        private fun resolvePreviousValid(year: Int, month: Short, day: Short): LocalDate {
            var day = day.toInt()
            when (month.toInt()) {
                2 -> day = minOf(day, if (Year.isLeap(year)) 29 else 28)
                4, 6, 9, 11 -> day = minOf(day, 30)
            }
            return LocalDate(year, month, day.toShort())
        }
    } // companion object

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
        return Month.of(this.monthValue.toInt())
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
        return getMonth().firstDayOfYear(isLeapYear()) + this.dayOfMonth - 1
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
        val dow0 = MathUtils.floorMod(toEpochDay() + 3, 7).toInt()
        return DayOfWeek.of(dow0 + 1)
    }

    /**
     * Checks if the year is a leap year, according to the ISO proleptic
     * calendar system rules.
     *
     *
     * This method applies the current rules for leap years across the whole time-line.
     * In general, a year is a leap year if it is divisible by four without
     * remainder. However, years divisible by 100, are not leap years, with
     * the exception of years divisible by 400 which are.
     *
     *
     * For example, 1904 is a leap year it is divisible by 4.
     * 1900 was not a leap year as it is divisible by 100, however 2000 was a
     * leap year as it is divisible by 400.
     *
     *
     * The calculation is proleptic - applying the same rules into the far future and far past.
     * This is historically inaccurate, but is correct for the ISO-8601 standard.
     *
     * @return true if the year is leap, false otherwise
     */
    // @Override // override for Javadoc and performance
    fun isLeapYear(): Boolean {
        return Year.isLeap(this.year)
    }

    /**
     * Returns the length of the month represented by this date.
     *
     *
     * This returns the length of the month in days.
     * For example, a date in January would return 31.
     *
     * @return the length of the month in days
     */
    fun lengthOfMonth(): Int {
        return when (this.monthValue.toInt()) {
            2 -> if (isLeapYear()) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    /**
     * Returns the length of the year represented by this date.
     *
     *
     * This returns the length of the year in days, either 365 or 366.
     *
     * @return 366 if the year is leap, 365 otherwise
     */
    fun lengthOfYear(): Int {
        return if (isLeapYear()) 366 else 365
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalDate` with the year altered.
     *
     *
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the result, from MIN_YEAR to MAX_YEAR
     * @return a `LocalDate` based on this date with the requested year, not null
     * @throws DateTimeException if the year value is invalid
     */
    fun withYear(year: Int): LocalDate {
        if (this.year == year) {
            return this
        }
        ChronoField.YEAR.checkValidValue(year.toLong())
        return resolvePreviousValid(year, this.monthValue, this.dayOfMonth)
    }

    /**
     * Returns a copy of this `LocalDate` with the month-of-year altered.
     *
     *
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param month  the month-of-year to set in the result, from 1 (January) to 12 (December)
     * @return a `LocalDate` based on this date with the requested month, not null
     * @throws DateTimeException if the month-of-year value is invalid
     */
    fun withMonth(month: Int): LocalDate {
        if (this.monthValue.toInt() == month) {
            return this
        }
        ChronoField.MONTH_OF_YEAR.checkValidValue(month.toLong())
        return resolvePreviousValid(this.year, month.toShort(), this.dayOfMonth)
    }

    /**
     * Returns a copy of this `LocalDate` with the day-of-month altered.
     *
     *
     * If the resulting date is invalid, an exception is thrown.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the result, from 1 to 28-31
     * @return a `LocalDate` based on this date with the requested day, not null
     * @throws DateTimeException if the day-of-month value is invalid,
     * or if the day-of-month is invalid for the month-year
     */
    fun withDayOfMonth(dayOfMonth: Int): LocalDate {
        return if (this.dayOfMonth.toInt() == dayOfMonth) {
            this
        } else of(year, this.monthValue.toInt(), dayOfMonth)
    }

    /**
     * Returns a copy of this `LocalDate` with the day-of-year altered.
     *
     *
     * If the resulting date is invalid, an exception is thrown.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the result, from 1 to 365-366
     * @return a `LocalDate` based on this date with the requested day, not null
     * @throws DateTimeException if the day-of-year value is invalid,
     * or if the day-of-year is invalid for the year
     */
    fun withDayOfYear(dayOfYear: Int): LocalDate {
        return if (this.getDayOfYear() == dayOfYear) {
            this
        } else ofYearDay(this.year, dayOfYear)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `LocalDate` with the specified number of years added.
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
     * @param yearsToAdd  the years to add, may be negative
     * @return a `LocalDate` based on this date with the years added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusYears(yearsToAdd: Long): LocalDate {
        if (yearsToAdd == 0L) {
            return this
        }
        val newYear = ChronoField.YEAR.checkValidIntValue(year + yearsToAdd)  // safe overflow
        return resolvePreviousValid(newYear, this.monthValue, this.dayOfMonth)
    }

    /**
     * Returns a copy of this `LocalDate` with the specified number of months added.
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
     * @param monthsToAdd  the months to add, may be negative
     * @return a `LocalDate` based on this date with the months added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusMonths(monthsToAdd: Long): LocalDate {
        if (monthsToAdd == 0L) {
            return this
        }
        val monthCount = year * 12L + (this.monthValue - 1)
        val calcMonths = monthCount + monthsToAdd  // safe overflow
        val newYear = ChronoField.YEAR.checkValidIntValue(MathUtils.floorDiv(calcMonths, 12))
        val newMonth = MathUtils.floorMod(calcMonths, 12) + 1
        return resolvePreviousValid(newYear, newMonth.toShort(), this.dayOfMonth)
    }

    /**
     * Returns a copy of this `LocalDate` with the specified number of weeks added.
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
     * @param weeksToAdd  the weeks to add, may be negative
     * @return a `LocalDate` based on this date with the weeks added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusWeeks(weeksToAdd: Long): LocalDate {
        return plusDays(MathUtils.multiplyExact(weeksToAdd, 7))
    }

    /**
     * Returns a copy of this `LocalDate` with the specified number of days added.
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
     * @param daysToAdd  the days to add, may be negative
     * @return a `LocalDate` based on this date with the days added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusDays(daysToAdd: Long): LocalDate {
        if (daysToAdd == 0L) {
            return this
        }
        val dom = this.dayOfMonth + daysToAdd
        if (dom > 0) {
            if (dom <= 28) {
                return LocalDate(year, this.monthValue, dom.toShort())
            } else if (dom <= 59) { // 59th Jan is 28th Feb, 59th Feb is 31st Mar
                val monthLen = lengthOfMonth()
                return if (dom <= monthLen) {
                    LocalDate(year, this.monthValue, dom.toShort())
                } else if (this.monthValue < 12) {
                    LocalDate(year, (this.monthValue.toInt() + 1).toShort(), (dom - monthLen).toShort())
                } else {
                    ChronoField.YEAR.checkValidValue(year.toLong() + 1)
                    LocalDate(year + 1, 1, (dom - monthLen).toShort())
                }
            }
        }

        val mjDay = MathUtils.addExact(toEpochDay(), daysToAdd)
        return LocalDate.ofEpochDay(mjDay)
    }

    /**
     * Returns a copy of this `LocalDate` with the specified number of years subtracted.
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
     * @param yearsToSubtract  the years to subtract, may be negative
     * @return a `LocalDate` based on this date with the years subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusYears(yearsToSubtract: Long): LocalDate {
        return if (yearsToSubtract == Long.MIN_VALUE)
            plusYears(Long.MAX_VALUE).plusYears(1) else plusYears(-yearsToSubtract)
    }

    /**
     * Returns a copy of this `LocalDate` with the specified number of months subtracted.
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
     * @param monthsToSubtract  the months to subtract, may be negative
     * @return a `LocalDate` based on this date with the months subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusMonths(monthsToSubtract: Long): LocalDate {
        return if (monthsToSubtract == Long.MIN_VALUE)
            plusMonths(Long.MAX_VALUE).plusMonths(1) else plusMonths(-monthsToSubtract)
    }

    /**
     * Returns a copy of this `LocalDate` with the specified number of weeks subtracted.
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
     * @param weeksToSubtract  the weeks to subtract, may be negative
     * @return a `LocalDate` based on this date with the weeks subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusWeeks(weeksToSubtract: Long): LocalDate {
        return if (weeksToSubtract == Long.MIN_VALUE)
            plusWeeks(Long.MAX_VALUE).plusWeeks(1) else plusWeeks(-weeksToSubtract)
    }

    /**
     * Returns a copy of this `LocalDate` with the specified number of days subtracted.
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
     * @param daysToSubtract  the days to subtract, may be negative
     * @return a `LocalDate` based on this date with the days subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusDays(daysToSubtract: Long): LocalDate {
        return if (daysToSubtract == Long.MIN_VALUE)
            plusDays(Long.MAX_VALUE).plusDays(1) else plusDays(-daysToSubtract)
    }

    override fun compareTo(other: LocalDate): Int {
        if (this.year > other.year) {
            return 1
        } else if (this.year < other.year) {
            return -1
        }
        if (this.monthValue > other.monthValue) {
            return 1
        } else if (this.monthValue < other.monthValue) {
            return -1
        }
        return if (this.dayOfMonth > other.dayOfMonth) 1 else if (this.dayOfMonth < other.dayOfMonth) -1 else 0
    }

    fun toEpochDay(): Long {
        val y = this.year
        val m = this.monthValue
        var total: Long = 0
        total += 365 * y
        if (y >= 0) {
            total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400
        } else {
            total -= y / -4 - y / -100 + y / -400
        }
        total += (367 * m - 362) / 12
        total += dayOfMonth - 1
        if (m > 2) {
            total -= 1
            if (!isLeapYear()) {
                total -= 1
            }
        }
        return total - DAYS_0000_TO_1970
    }

    /**
     * Converts this `LocalDate` to the number of seconds since the epoch
     * of 1970-01-01T00:00:00Z.
     *
     *
     * This combines this local date with the specified time and
     * offset to calculate the epoch-second value, which is the
     * number of elapsed seconds from 1970-01-01T00:00:00Z.
     * Instants on the time-line after the epoch are positive, earlier
     * are negative.
     *
     * @param time the local time, not null
     * @param offset the zone offset, not null
     * @return the number of seconds since the epoch of 1970-01-01T00:00:00Z, may be negative
     * @since Java 9
     */
    fun toEpochSecond(time: LocalTime, offset: ZoneOffset): Long {
        var secs = toEpochDay() * LocalTime.SECONDS_PER_DAY + time.toSecondOfDay()
        secs -= offset.totalSeconds.toLong()
        return secs
    }

    internal fun daysUntil(end: LocalDate): Long {
        return end.toEpochDay() - toEpochDay()  // no overflow
    }

    private fun monthsUntil(end: LocalDate): Long {
        val packed1 = getProlepticMonth() * 32L + this.dayOfMonth  // no overflow
        val packed2 = end.getProlepticMonth() * 32L + end.dayOfMonth  // no overflow
        return (packed2 - packed1) / 32
    }

    private fun getProlepticMonth(): Long {
        return this.year * 12L + this.monthValue - 1
    }
}