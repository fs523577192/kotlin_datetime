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

import org.firas.datetime.chrono.ChronoLocalDate
import org.firas.datetime.chrono.IsoChronology
import org.firas.datetime.temporal.*
import org.firas.datetime.util.MathUtils
import org.firas.datetime.zone.ZoneOffset
import kotlin.reflect.KClass

/**
 * A date without a time-zone in the ISO-8601 calendar system,
 * such as `2007-12-03`.
 *
 *
 * `LocalDate` is an immutable date-time object that represents a date,
 * often viewed as year-month-day. Other date fields, such as day-of-year,
 * day-of-week and week-of-year, can also be accessed.
 * For example, the value "2nd October 2007" can be stored in a `LocalDate`.
 *
 *
 * This class does not store or represent a time or time-zone.
 * Instead, it is a description of the date, as used for birthdays.
 * It cannot represent an instant on the time-line without additional information
 * such as an offset or time-zone.
 *
 *
 * The ISO-8601 calendar system is the modern civil calendar system used today
 * in most of the world. It is equivalent to the proleptic Gregorian calendar
 * system, in which today's rules for leap years are applied for all time.
 * For most applications written today, the ISO-8601 rules are entirely suitable.
 * However, any application that makes use of historical dates, and requires them
 * to be accurate will find the ISO-8601 approach unsuitable.
 *
 *
 *
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `LocalDate` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class LocalDate private constructor(
    val year: Int,
    val monthValue: Short,
    val dayOfMonth: Short
): Temporal, TemporalAdjuster, ChronoLocalDate {

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

        /**
         * Obtains an instance of `LocalDate` from a temporal object.
         *
         *
         * This obtains a local date based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `LocalDate`.
         *
         *
         * The conversion uses the {@link TemporalQueries#localDate()} query, which relies
         * on extracting the {@link ChronoField#EPOCH_DAY EPOCH_DAY} field.
         *
         *
         * This method matches the signature of the functional interface [TemporalQuery]
         * allowing it to be used as a query via method reference, `LocalDate::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the local date, not null
         * @throws DateTimeException if unable to convert to a `LocalDate`
         */
        fun from(temporal: TemporalAccessor): LocalDate {
            return temporal.query(TemporalQueries.LOCAL_DATE) ?:
                    throw DateTimeException ("Unable to obtain LocalDate from TemporalAccessor: " +
                            temporal + " of type " + temporal.getKClass().qualifiedName)
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
     * Gets the chronology of this date, which is the ISO calendar system.
     *
     *
     * The `Chronology` represents the calendar system in use.
     * The ISO-8601 calendar system is the modern civil calendar system used today
     * in most of the world. It is equivalent to the proleptic Gregorian calendar
     * system, in which today's rules for leap years are applied for all time.
     *
     * @return the ISO chronology, not null
     */
    override fun getChronology(): IsoChronology {
        return IsoChronology.INSTANCE
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
    override fun isLeapYear(): Boolean {
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
    override fun lengthOfMonth(): Int {
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
    override fun lengthOfYear(): Int {
        return if (isLeapYear()) 366 else 365
    }

    //-----------------------------------------------------------------------
    /**
     * Returns an adjusted copy of this date.
     *
     *
     * This returns a `LocalDate`, based on this one, with the date adjusted.
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
     * result = localDate.with(JULY).with(lastDayOfMonth());
    </pre> *
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
     * @return a `LocalDate` based on `this` with the adjustment made, not null
     * @throws DateTimeException if the adjustment cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(adjuster: TemporalAdjuster): LocalDate {
        // optimizations
        return if (adjuster is LocalDate) {
            adjuster as LocalDate
        } else adjuster.adjustInto(this) as LocalDate
    }

    /**
     * Returns a copy of this date with the specified field set to a new value.
     *
     *
     * This returns a `LocalDate`, based on this one, with the value
     * for the specified field changed.
     * This can be used to change any supported field, such as the year, month or day-of-month.
     * If it is not possible to set the value, because the field is not supported or for
     * some other reason, an exception is thrown.
     *
     *
     * In some cases, changing the specified field can cause the resulting date to become invalid,
     * such as changing the month from 31st January to February would make the day-of-month invalid.
     * In cases like this, the field is responsible for resolving the date. Typically it will choose
     * the previous valid date, which would be the last valid day of February in this example.
     *
     *
     * If the field is a [ChronoField] then the adjustment is implemented here.
     * The supported fields behave as follows:
     *
     *  * `DAY_OF_WEEK` -
     * Returns a `LocalDate` with the specified day-of-week.
     * The date is adjusted up to 6 days forward or backward within the boundary
     * of a Monday to Sunday week.
     *  * `ALIGNED_DAY_OF_WEEK_IN_MONTH` -
     * Returns a `LocalDate` with the specified aligned-day-of-week.
     * The date is adjusted to the specified month-based aligned-day-of-week.
     * Aligned weeks are counted such that the first week of a given month starts
     * on the first day of that month.
     * This may cause the date to be moved up to 6 days into the following month.
     *  * `ALIGNED_DAY_OF_WEEK_IN_YEAR` -
     * Returns a `LocalDate` with the specified aligned-day-of-week.
     * The date is adjusted to the specified year-based aligned-day-of-week.
     * Aligned weeks are counted such that the first week of a given year starts
     * on the first day of that year.
     * This may cause the date to be moved up to 6 days into the following year.
     *  * `DAY_OF_MONTH` -
     * Returns a `LocalDate` with the specified day-of-month.
     * The month and year will be unchanged. If the day-of-month is invalid for the
     * year and month, then a `DateTimeException` is thrown.
     *  * `DAY_OF_YEAR` -
     * Returns a `LocalDate` with the specified day-of-year.
     * The year will be unchanged. If the day-of-year is invalid for the
     * year, then a `DateTimeException` is thrown.
     *  * `EPOCH_DAY` -
     * Returns a `LocalDate` with the specified epoch-day.
     * This completely replaces the date and is equivalent to [.ofEpochDay].
     *  * `ALIGNED_WEEK_OF_MONTH` -
     * Returns a `LocalDate` with the specified aligned-week-of-month.
     * Aligned weeks are counted such that the first week of a given month starts
     * on the first day of that month.
     * This adjustment moves the date in whole week chunks to match the specified week.
     * The result will have the same day-of-week as this date.
     * This may cause the date to be moved into the following month.
     *  * `ALIGNED_WEEK_OF_YEAR` -
     * Returns a `LocalDate` with the specified aligned-week-of-year.
     * Aligned weeks are counted such that the first week of a given year starts
     * on the first day of that year.
     * This adjustment moves the date in whole week chunks to match the specified week.
     * The result will have the same day-of-week as this date.
     * This may cause the date to be moved into the following year.
     *  * `MONTH_OF_YEAR` -
     * Returns a `LocalDate` with the specified month-of-year.
     * The year will be unchanged. The day-of-month will also be unchanged,
     * unless it would be invalid for the new month and year. In that case, the
     * day-of-month is adjusted to the maximum valid value for the new month and year.
     *  * `PROLEPTIC_MONTH` -
     * Returns a `LocalDate` with the specified proleptic-month.
     * The day-of-month will be unchanged, unless it would be invalid for the new month
     * and year. In that case, the day-of-month is adjusted to the maximum valid value
     * for the new month and year.
     *  * `YEAR_OF_ERA` -
     * Returns a `LocalDate` with the specified year-of-era.
     * The era and month will be unchanged. The day-of-month will also be unchanged,
     * unless it would be invalid for the new month and year. In that case, the
     * day-of-month is adjusted to the maximum valid value for the new month and year.
     *  * `YEAR` -
     * Returns a `LocalDate` with the specified year.
     * The month will be unchanged. The day-of-month will also be unchanged,
     * unless it would be invalid for the new month and year. In that case, the
     * day-of-month is adjusted to the maximum valid value for the new month and year.
     *  * `ERA` -
     * Returns a `LocalDate` with the specified era.
     * The year-of-era and month will be unchanged. The day-of-month will also be unchanged,
     * unless it would be invalid for the new month and year. In that case, the
     * day-of-month is adjusted to the maximum valid value for the new month and year.
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
     * @return a `LocalDate` based on `this` with the specified field set, not null
     * @throws DateTimeException if the field cannot be set
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(field: TemporalField, newValue: Long): LocalDate {
        if (field is ChronoField) {
            field.checkValidValue(newValue)
            return when (field) {
                ChronoField.DAY_OF_WEEK -> plusDays(newValue - getDayOfWeek().getValue())
                ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH -> plusDays(newValue -
                        getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH))
                ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR -> plusDays(newValue -
                        getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR))
                ChronoField.DAY_OF_MONTH -> withDayOfMonth(newValue.toInt())
                ChronoField.DAY_OF_YEAR -> withDayOfYear(newValue.toInt())
                ChronoField.EPOCH_DAY -> LocalDate.ofEpochDay(newValue)
                ChronoField.ALIGNED_WEEK_OF_MONTH -> plusWeeks(newValue -
                        getLong(ChronoField.ALIGNED_WEEK_OF_MONTH))
                ChronoField.ALIGNED_WEEK_OF_YEAR -> plusWeeks(newValue -
                        getLong(ChronoField.ALIGNED_WEEK_OF_YEAR))
                ChronoField.MONTH_OF_YEAR -> withMonth(newValue.toInt())
                ChronoField.PROLEPTIC_MONTH -> plusMonths(newValue - getProlepticMonth())
                ChronoField.YEAR_OF_ERA -> withYear((if (year >= 1) newValue else 1 - newValue).toInt())
                ChronoField.YEAR -> withYear(newValue.toInt())
                ChronoField.ERA -> if (getLong(ChronoField.ERA) == newValue) this
                        else withYear(1 - year)
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        return field.adjustInto(this, newValue)
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
     * Returns a copy of this date with the specified amount added.
     *
     *
     * This returns a `LocalDate`, based on this one, with the specified amount added.
     * The amount is typically [Period] but may be any other type implementing
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
     * @param amountToAdd  the amount to add, not null
     * @return a `LocalDate` based on this date with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: TemporalAmount): LocalDate {
        if (amountToAdd is Period) {
            return plusMonths(amountToAdd.toTotalMonths()).plusDays(amountToAdd.days.toLong())
        }
        return amountToAdd.addTo(this) as LocalDate
    }

    /**
     * Returns a copy of this date with the specified amount added.
     *
     *
     * This returns a `LocalDate`, based on this one, with the amount
     * in terms of the unit added. If it is not possible to add the amount, because the
     * unit is not supported or for some other reason, an exception is thrown.
     *
     *
     * In some cases, adding the amount can cause the resulting date to become invalid.
     * For example, adding one month to 31st January would result in 31st February.
     * In cases like this, the unit is responsible for resolving the date.
     * Typically it will choose the previous valid date, which would be the last valid
     * day of February in this example.
     *
     *
     * If the field is a [ChronoUnit] then the addition is implemented here.
     * The supported fields behave as follows:
     *
     *  * `DAYS` -
     * Returns a `LocalDate` with the specified number of days added.
     * This is equivalent to [.plusDays].
     *  * `WEEKS` -
     * Returns a `LocalDate` with the specified number of weeks added.
     * This is equivalent to [.plusWeeks] and uses a 7 day week.
     *  * `MONTHS` -
     * Returns a `LocalDate` with the specified number of months added.
     * This is equivalent to [.plusMonths].
     * The day-of-month will be unchanged unless it would be invalid for the new
     * month and year. In that case, the day-of-month is adjusted to the maximum
     * valid value for the new month and year.
     *  * `YEARS` -
     * Returns a `LocalDate` with the specified number of years added.
     * This is equivalent to [.plusYears].
     * The day-of-month will be unchanged unless it would be invalid for the new
     * month and year. In that case, the day-of-month is adjusted to the maximum
     * valid value for the new month and year.
     *  * `DECADES` -
     * Returns a `LocalDate` with the specified number of decades added.
     * This is equivalent to calling [.plusYears] with the amount
     * multiplied by 10.
     * The day-of-month will be unchanged unless it would be invalid for the new
     * month and year. In that case, the day-of-month is adjusted to the maximum
     * valid value for the new month and year.
     *  * `CENTURIES` -
     * Returns a `LocalDate` with the specified number of centuries added.
     * This is equivalent to calling [.plusYears] with the amount
     * multiplied by 100.
     * The day-of-month will be unchanged unless it would be invalid for the new
     * month and year. In that case, the day-of-month is adjusted to the maximum
     * valid value for the new month and year.
     *  * `MILLENNIA` -
     * Returns a `LocalDate` with the specified number of millennia added.
     * This is equivalent to calling [.plusYears] with the amount
     * multiplied by 1,000.
     * The day-of-month will be unchanged unless it would be invalid for the new
     * month and year. In that case, the day-of-month is adjusted to the maximum
     * valid value for the new month and year.
     *  * `ERAS` -
     * Returns a `LocalDate` with the specified number of eras added.
     * Only two eras are supported so the amount must be one, zero or minus one.
     * If the amount is non-zero then the year is changed such that the year-of-era
     * is unchanged.
     * The day-of-month will be unchanged unless it would be invalid for the new
     * month and year. In that case, the day-of-month is adjusted to the maximum
     * valid value for the new month and year.
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
     * @return a `LocalDate` based on this date with the specified amount added, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: Long, unit: TemporalUnit): LocalDate {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.DAYS -> plusDays(amountToAdd)
                ChronoUnit.WEEKS -> plusWeeks(amountToAdd)
                ChronoUnit.MONTHS -> plusMonths(amountToAdd)
                ChronoUnit.YEARS -> plusYears(amountToAdd)
                ChronoUnit.DECADES -> plusYears(MathUtils.multiplyExact(amountToAdd, 10))
                ChronoUnit.CENTURIES -> plusYears(MathUtils.multiplyExact(amountToAdd, 100))
                ChronoUnit.MILLENNIA -> plusYears(MathUtils.multiplyExact(amountToAdd, 1000))
                ChronoUnit.ERAS -> with(ChronoField.ERA,
                        MathUtils.addExact(getLong(ChronoField.ERA),amountToAdd))
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.addTo(this, amountToAdd)
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

    override operator fun compareTo(other: ChronoLocalDate): Int {
        return if (other is LocalDate) compareTo(other) else -other.compareTo(this)
    }

    operator fun compareTo(other: LocalDate): Int {
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

    override fun toEpochDay(): Long {
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

    /**
     * Combines this date with a time to create a `LocalDateTime`.
     *
     *
     * This returns a `LocalDateTime` formed from this date at the specified time.
     * All possible combinations of date and time are valid.
     *
     * @param time  the time to combine with, not null
     * @return the local date-time formed from this date and the specified time, not null
     */
    override fun atTime(time: LocalTime): LocalDateTime {
        return LocalDateTime.of(this, time)
    }

    /**
     * Combines this date with a time to create a `LocalDateTime`.
     *
     *
     * This returns a `LocalDateTime` formed from this date at the
     * specified hour and minute.
     * The seconds and nanosecond fields will be set to zero.
     * The individual time fields must be within their valid range.
     * All possible combinations of date and time are valid.
     *
     * @param hour  the hour-of-day to use, from 0 to 23
     * @param minute  the minute-of-hour to use, from 0 to 59
     * @return the local date-time formed from this date and the specified time, not null
     * @throws DateTimeException if the value of any field is out of range
     */
    fun atTime(hour: Int, minute: Int): LocalDateTime {
        return atTime(LocalTime.of(hour, minute))
    }

    /**
     * Combines this date with a time to create a `LocalDateTime`.
     *
     *
     * This returns a `LocalDateTime` formed from this date at the
     * specified hour, minute and second.
     * The nanosecond field will be set to zero.
     * The individual time fields must be within their valid range.
     * All possible combinations of date and time are valid.
     *
     * @param hour  the hour-of-day to use, from 0 to 23
     * @param minute  the minute-of-hour to use, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @return the local date-time formed from this date and the specified time, not null
     * @throws DateTimeException if the value of any field is out of range
     */
    fun atTime(hour: Int, minute: Int, second: Int): LocalDateTime {
        return atTime(LocalTime.of(hour, minute, second))
    }

    /**
     * Combines this date with a time to create a `LocalDateTime`.
     *
     *
     * This returns a `LocalDateTime` formed from this date at the
     * specified hour, minute, second and nanosecond.
     * The individual time fields must be within their valid range.
     * All possible combinations of date and time are valid.
     *
     * @param hour  the hour-of-day to use, from 0 to 23
     * @param minute  the minute-of-hour to use, from 0 to 59
     * @param second  the second-of-minute to represent, from 0 to 59
     * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
     * @return the local date-time formed from this date and the specified time, not null
     * @throws DateTimeException if the value of any field is out of range
     */
    fun atTime(hour: Int, minute: Int, second: Int, nanoOfSecond: Int): LocalDateTime {
        return atTime(LocalTime.of(hour, minute, second, nanoOfSecond))
    }

    /**
     * Combines this date with an offset time to create an `OffsetDateTime`.
     *
     *
     * This returns an `OffsetDateTime` formed from this date at the specified time.
     * All possible combinations of date and time are valid.
     *
     * @param time  the time to combine with, not null
     * @return the offset date-time formed from this date and the specified time, not null
     */
    fun atTime(time: OffsetTime): OffsetDateTime {
        return OffsetDateTime.of(LocalDateTime.of(this, time.localTime), time.offset)
    }

    /**
     * Combines this date with the time of midnight to create a `LocalDateTime`
     * at the start of this date.
     *
     *
     * This returns a `LocalDateTime` formed from this date at the time of
     * midnight, 00:00, at the start of this date.
     *
     * @return the local date-time of midnight at the start of this date, not null
     */
    fun atStartOfDay(): LocalDateTime {
        return LocalDateTime.of(this, LocalTime.MIDNIGHT)
    }

    /**
     * Calculates the amount of time until another date in terms of the specified unit.
     *
     *
     * This calculates the amount of time between two `LocalDate`
     * objects in terms of a single `TemporalUnit`.
     * The start and end points are `this` and the specified date.
     * The result will be negative if the end is before the start.
     * The `Temporal` passed to this method is converted to a
     * `LocalDate` using [.from].
     * For example, the amount in days between two dates can be calculated
     * using `startDate.until(endDate, DAYS)`.
     *
     *
     * The calculation returns a whole number, representing the number of
     * complete units between the two dates.
     * For example, the amount in months between 2012-06-15 and 2012-08-14
     * will only be one month as it is one day short of two months.
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
     * The units `DAYS`, `WEEKS`, `MONTHS`, `YEARS`,
     * `DECADES`, `CENTURIES`, `MILLENNIA` and `ERAS`
     * are supported. Other `ChronoUnit` values will throw an exception.
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
     * @param endExclusive  the end date, exclusive, which is converted to a `LocalDate`, not null
     * @param unit  the unit to measure the amount in, not null
     * @return the amount of time between this date and the end date
     * @throws DateTimeException if the amount cannot be calculated, or the end
     * temporal cannot be converted to a `LocalDate`
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = LocalDate.from(endExclusive)
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.DAYS -> daysUntil(end)
                ChronoUnit.WEEKS -> daysUntil(end) / 7
                ChronoUnit.MONTHS -> monthsUntil(end)
                ChronoUnit.YEARS -> monthsUntil(end) / 12
                ChronoUnit.DECADES -> monthsUntil(end) / 120
                ChronoUnit.CENTURIES -> monthsUntil(end) / 1200
                ChronoUnit.MILLENNIA -> monthsUntil(end) / 12000
                ChronoUnit.ERAS -> end.getLong(ChronoField.ERA) - getLong(ChronoField.ERA)
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
    }

    /**
     * Gets the value of the specified field from this date as an `int`.
     *
     *
     * This queries this date for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return valid
     * values based on this date, except `EPOCH_DAY` and `PROLEPTIC_MONTH`
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
        TODO("return ChronoLocalDate.super.get(field)")
    }

    /**
     * Gets the value of the specified field from this date as a `long`.
     *
     *
     * This queries this date for the value of the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return valid
     * values based on this date.
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
            if (field == ChronoField.EPOCH_DAY) {
                return toEpochDay()
            }
            if (field == ChronoField.PROLEPTIC_MONTH) {
                return getProlepticMonth()
            }
            return get0(field).toLong()
        }
        return field.getFrom(this)
    }

    override fun getKClass(): KClass<out Temporal> {
        return LocalDate::class
    }

    internal fun daysUntil(end: LocalDate): Long {
        return end.toEpochDay() - toEpochDay()  // no overflow
    }

    private fun get0(field: TemporalField): Int {
        return when (field) {
            ChronoField.DAY_OF_WEEK -> getDayOfWeek().getValue()
            ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH -> ((this.dayOfMonth - 1) % 7) + 1
            ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR -> ((getDayOfYear() - 1) % 7) + 1
            ChronoField.DAY_OF_MONTH -> this.dayOfMonth.toInt()
            ChronoField.DAY_OF_YEAR -> getDayOfYear()
            ChronoField.EPOCH_DAY -> throw UnsupportedTemporalTypeException(
                    "Invalid field 'EpochDay' for get() method, use getLong() instead")
            ChronoField.ALIGNED_WEEK_OF_MONTH -> ((this.dayOfMonth - 1) / 7) + 1
            ChronoField.ALIGNED_WEEK_OF_YEAR -> ((getDayOfYear() - 1) / 7) + 1
            ChronoField.MONTH_OF_YEAR -> this.monthValue.toInt()
            ChronoField.PROLEPTIC_MONTH -> throw UnsupportedTemporalTypeException(
                    "Invalid field 'ProlepticMonth' for get() method, use getLong() instead")
            ChronoField.YEAR_OF_ERA -> if (this.year >= 1) this.year else 1 - this.year
            ChronoField.YEAR -> this.year
            ChronoField.ERA -> if (this.year >= 1) 1 else 0
            else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
        }
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