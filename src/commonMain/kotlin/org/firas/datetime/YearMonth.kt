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
import kotlin.math.absoluteValue

/**
 * A year-month in the ISO-8601 calendar system, such as {@code 2007-12}.
 * <p>
 * {@code YearMonth} is an immutable date-time object that represents the combination
 * of a year and month. Any field that can be derived from a year and month, such as
 * quarter-of-year, can be obtained.
 * <p>
 * This class does not store or represent a day, time or time-zone.
 * For example, the value "October 2007" can be stored in a {@code YearMonth}.
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
 * {@code YearMonth} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
class YearMonth private constructor(
        val year: Int, val month: Int): Comparable<YearMonth> {

    companion object {
        /**
         * Serialization version.
         */
        private const val serialVersionUID = 4183400860270640070L

        // ----==== Factory methods "of" ====----
        /**
         * Obtains an instance of `YearMonth` from a year and month.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, not null
         * @return the year-month, not null
         * @throws DateTimeException if the year value is invalid
         */
        fun of(year: Int, month: Month): YearMonth {
            return of(year, month.getValue())
        }

        /**
         * Obtains an instance of `YearMonth` from a year and month.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @return the year-month, not null
         * @throws DateTimeException if either field value is invalid
         */
        fun of(year: Int, month: Int): YearMonth {
            ChronoField.YEAR.checkValidValue(year.toLong())
            ChronoField.MONTH_OF_YEAR.checkValidValue(month.toLong())
            return YearMonth(year, month)
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
     *
     * @return the year, from MIN_YEAR to MAX_YEAR
     */
    fun getYear(): Int {
        return this.year
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
        return this.month
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
        return Month.of(this.month)
    }

    //-----------------------------------------------------------------------
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
    fun isLeapYear(): Boolean {
        return Year.isLeap(this.year)
    }

    /**
     * Checks if the day-of-month is valid for this year-month.
     *
     *
     * This method checks whether this year and month and the input day form
     * a valid date.
     *
     * @param dayOfMonth  the day-of-month to validate, from 1 to 31, invalid value returns false
     * @return true if the day is valid for this year-month
     */
    fun isValidDay(dayOfMonth: Int): Boolean {
        return dayOfMonth >= 1 && dayOfMonth <= lengthOfMonth()
    }

    /**
     * Returns the length of the month, taking account of the year.
     *
     *
     * This returns the length of the month in days.
     * For example, a date in January would return 31.
     *
     * @return the length of the month in days, from 28 to 31
     */
    fun lengthOfMonth(): Int {
        return getMonth().length(isLeapYear())
    }

    /**
     * Returns the length of the year.
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
     * Returns a copy of this `YearMonth` with the year altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the returned year-month, from MIN_YEAR to MAX_YEAR
     * @return a `YearMonth` based on this year-month with the requested year, not null
     * @throws DateTimeException if the year value is invalid
     */
    fun withYear(year: Int): YearMonth {
        ChronoField.YEAR.checkValidValue(year.toLong())
        return with(year, this.month)
    }

    /**
     * Returns a copy of this `YearMonth` with the month-of-year altered.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param month  the month-of-year to set in the returned year-month, from 1 (January) to 12 (December)
     * @return a `YearMonth` based on this year-month with the requested month, not null
     * @throws DateTimeException if the month-of-year value is invalid
     */
    fun withMonth(month: Int): YearMonth {
        ChronoField.MONTH_OF_YEAR.checkValidValue(month.toLong())
        return with(this.year, month)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `YearMonth` with the specified number of years added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToAdd  the years to add, may be negative
     * @return a `YearMonth` based on this year-month with the years added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    fun plusYears(yearsToAdd: Int): YearMonth {
        if (yearsToAdd == 0) {
            return this
        }
        val newYear = ChronoField.YEAR.checkValidIntValue(this.year.toLong() + yearsToAdd)  // safe overflow
        return with(newYear, month)
    }

    /**
     * Returns a copy of this `YearMonth` with the specified number of months added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthsToAdd  the months to add, may be negative
     * @return a `YearMonth` based on this year-month with the months added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    fun plusMonths(monthsToAdd: Int): YearMonth {
        if (monthsToAdd == 0) {
            return this
        }
        val monthCount = year * 12L + (month - 1)
        val calcMonths = monthCount + monthsToAdd  // safe overflow
        val newYear = ChronoField.YEAR.checkValidIntValue(MathUtils.floorDiv(calcMonths, 12))
        val newMonth = MathUtils.floorMod(calcMonths, 12) + 1
        return with(newYear, newMonth.toInt())
    }

    /**
     * Returns a copy of this `YearMonth` with the specified number of years subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToSubtract  the years to subtract, may be negative
     * @return a `YearMonth` based on this year-month with the years subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    fun minusYears(yearsToSubtract: Int): YearMonth {
        return if (yearsToSubtract == Int.MIN_VALUE) plusYears(Int.MAX_VALUE).plusYears(1)
                else plusYears(-yearsToSubtract)
    }

    /**
     * Returns a copy of this `YearMonth` with the specified number of months subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthsToSubtract  the months to subtract, may be negative
     * @return a `YearMonth` based on this year-month with the months subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    fun minusMonths(monthsToSubtract: Int): YearMonth {
        return if (monthsToSubtract == Int.MIN_VALUE) plusMonths(Int.MAX_VALUE).plusMonths(1)
                else plusMonths(-monthsToSubtract)
    }

    //-----------------------------------------------------------------------
    /**
     * Combines this year-month with a day-of-month to create a `LocalDate`.
     *
     *
     * This returns a `LocalDate` formed from this year-month and the specified day-of-month.
     *
     *
     * The day-of-month value must be valid for the year-month.
     *
     *
     * This method can be used as part of a chain to produce a date:
     * <pre>
     * LocalDate date = year.atMonth(month).atDay(day);
    </pre> *
     *
     * @param dayOfMonth  the day-of-month to use, from 1 to 31
     * @return the date formed from this year-month and the specified day, not null
     * @throws DateTimeException if the day is invalid for the year-month
     * @see .isValidDay
     */
    fun atDay(dayOfMonth: Int): LocalDate {
        return LocalDate.of(year, month, dayOfMonth)
    }

    /**
     * Returns a `LocalDate` at the end of the month.
     *
     *
     * This returns a `LocalDate` based on this year-month.
     * The day-of-month is set to the last valid day of the month, taking
     * into account leap years.
     *
     *
     * This method can be used as part of a chain to produce a date:
     * <pre>
     * LocalDate date = year.atMonth(month).atEndOfMonth();
    </pre> *
     *
     * @return the last valid date of this year-month, not null
     */
    fun atEndOfMonth(): LocalDate {
        return LocalDate.of(year, month, lengthOfMonth())
    }

    // ----==== Comparison ====----
    /**
     * Compares this year-month to another year-month.
     *
     *
     * The comparison is based first on the value of the year, then on the value of the month.
     * It is "consistent with equals", as defined by [Comparable].
     *
     * @param other  the other year-month to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    override fun compareTo(other: YearMonth): Int {
        var cmp = this.year - other.year
        if (cmp == 0) {
            cmp = this.month - other.month
        }
        return cmp
    }

    /**
     * Checks if this year-month is after the specified year-month.
     *
     * @param other  the other year-month to compare to, not null
     * @return true if this is after the specified year-month
     */
    fun isAfter(other: YearMonth): Boolean {
        return compareTo(other) > 0
    }

    /**
     * Checks if this year-month is before the specified year-month.
     *
     * @param other  the other year-month to compare to, not null
     * @return true if this point is before the specified year-month
     */
    fun isBefore(other: YearMonth): Boolean {
        return compareTo(other) < 0
    }

    // ----==== override methods inherited from Any ----
    /**
     * Checks if this year-month is equal to another year-month.
     *
     *
     * The comparison is based on the time-line position of the year-months.
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other year-month
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is YearMonth) {
            return this.year == other.year && this.month == other.month
        }
        return false
    }

    /**
     * A hash code for this year-month.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return this.year xor (this.month shl 27)
    }

    /**
     * Outputs this year-month as a `String`, such as `2007-12`.
     *
     *
     * The output will be in the format `uuuu-MM`:
     *
     * @return a string representation of this year-month, not null
     */
    override fun toString(): String {
        val absYear = this.year.absoluteValue
        val buf = StringBuilder(9)
        if (absYear < 1000) {
            if (this.year < 0) {
                val temp = "000$absYear"
                buf.append("-").append(temp.substring(temp.length - 4))
            } else {
                val temp = "000$absYear"
                buf.append(temp.substring(temp.length - 4))
            }
        } else {
            buf.append(this.year)
        }
        return buf.append(if (this.month < 10) "-0" else "-")
                .append(this.month)
                .toString()
    }

    /**
     * Returns a copy of this year-month with the new year and month, checking
     * to see if a new object is in fact required.
     *
     * @param newYear  the year to represent, validated from MIN_YEAR to MAX_YEAR
     * @param newMonth  the month-of-year to represent, validated not null
     * @return the year-month, not null
     */
    private fun with(newYear: Int, newMonth: Int): YearMonth {
        return if (this.year == newYear && this.month == newMonth) {
            this
        } else YearMonth(newYear, newMonth)
    }

    private fun getProlepticMonth(): Long {
        return year * 12L + month - 1
    }
}