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
    private val month: Short,
    private val day: Short
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
    } // companion object

    override fun compareTo(other: LocalDate): Int {
        if (this.year > other.year) {
            return 1
        } else if (this.year < other.year) {
            return -1
        }
        if (this.month > other.month) {
            return 1
        } else if (this.month < other.month) {
            return -1
        }
        return if (this.day > other.day) 1 else if (this.day < other.day) -1 else 0
    }
}