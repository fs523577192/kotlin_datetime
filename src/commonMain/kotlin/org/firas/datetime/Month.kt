/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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

/**
 * A month-of-year, such as 'July'.
 * <p>
 * {@code Month} is an enum representing the 12 months of the year -
 * January, February, March, April, May, June, July, August, September, October,
 * November and December.
 * <p>
 * In addition to the textual enum name, each month-of-year has an {@code int} value.
 * The {@code int} value follows normal usage and the ISO-8601 standard,
 * from 1 (January) to 12 (December). It is recommended that applications use the enum
 * rather than the {@code int} value to ensure code clarity.
 * <p>
 * <b>Do not use {@code ordinal()} to obtain the numeric representation of {@code Month}.
 * Use {@code getValue()} instead.</b>
 * <p>
 * This enum represents a common concept that is found in many calendar systems.
 * As such, this enum may be used by any calendar system that has the month-of-year
 * concept defined exactly equivalent to the ISO-8601 calendar system.
 *
 * @implSpec
 * This is an immutable and thread-safe enum.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
enum class Month {
    /**
     * The singleton instance for the month of January with 31 days.
     * This has the numeric value of {@code 1}.
     */
    JANUARY,
    /**
     * The singleton instance for the month of February with 28 days, or 29 in a leap year.
     * This has the numeric value of {@code 2}.
     */
    FEBRUARY,
    /**
     * The singleton instance for the month of March with 31 days.
     * This has the numeric value of {@code 3}.
     */
    MARCH,
    /**
     * The singleton instance for the month of April with 30 days.
     * This has the numeric value of {@code 4}.
     */
    APRIL,
    /**
     * The singleton instance for the month of May with 31 days.
     * This has the numeric value of {@code 5}.
     */
    MAY,
    /**
     * The singleton instance for the month of June with 30 days.
     * This has the numeric value of {@code 6}.
     */
    JUNE,
    /**
     * The singleton instance for the month of July with 31 days.
     * This has the numeric value of {@code 7}.
     */
    JULY,
    /**
     * The singleton instance for the month of August with 31 days.
     * This has the numeric value of {@code 8}.
     */
    AUGUST,
    /**
     * The singleton instance for the month of September with 30 days.
     * This has the numeric value of {@code 9}.
     */
    SEPTEMBER,
    /**
     * The singleton instance for the month of October with 31 days.
     * This has the numeric value of {@code 10}.
     */
    OCTOBER,
    /**
     * The singleton instance for the month of November with 30 days.
     * This has the numeric value of {@code 11}.
     */
    NOVEMBER,
    /**
     * The singleton instance for the month of December with 31 days.
     * This has the numeric value of {@code 12}.
     */
    DECEMBER;

    companion object {
        /**
         * Obtains an instance of `Month` from an `int` value.
         *
         *
         * `Month` is an enum representing the 12 months of the year.
         * This factory allows the enum to be obtained from the `int` value.
         * The `int` value follows the ISO-8601 standard, from 1 (January) to 12 (December).
         *
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @return the month-of-year, not null
         * @throws DateTimeException if the month-of-year is invalid
         */
        fun of(month: Int): Month {
            if (month < 1 || month > 12) {
                throw DateTimeException("Invalid value for MonthOfYear: $month")
            }
            return Month.values()[month - 1]
        }
    }

    /**
     * Gets the month-of-year `int` value.
     *
     *
     * The values are numbered following the ISO-8601 standard,
     * from 1 (January) to 12 (December).
     *
     * @return the month-of-year, from 1 (January) to 12 (December)
     */
    fun getValue(): Int {
        return ordinal + 1
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the month-of-year that is the specified number of months after this one.
     *
     *
     * The calculation rolls around the end of the year from December to January.
     * The specified period may be negative.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, positive or negative
     * @return the resulting month, not null
     */
    operator fun plus(months: Long): Month {
        val amount = (months % 12).toInt()
        return Month.values()[(ordinal + (amount + 12)) % 12] // amount + 12 to make it non-negative
    }

    /**
     * Returns the month-of-year that is the specified number of months before this one.
     *
     *
     * The calculation rolls around the start of the year from January to December.
     * The specified period may be negative.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, positive or negative
     * @return the resulting month, not null
     */
    operator fun minus(months: Long): Month {
        return plus(-(months % 12))
    }

    // ----==== days in a month ====----
    /**
     * Gets the length of this month in days.
     *
     *
     * This takes a flag to determine whether to return the length for a leap year or not.
     *
     *
     * February has 28 days in a standard year and 29 days in a leap year.
     * April, June, September and November have 30 days.
     * All other months have 31 days.
     *
     * @param leapYear  true if the length is required for a leap year
     * @return the length of this month in days, from 28 to 31
     */
    fun length(leapYear: Boolean): Int {
        return when (this) {
            FEBRUARY -> if (leapYear) 29 else 28
            APRIL, JUNE, SEPTEMBER, NOVEMBER -> 30
            else -> 31
        }
    }

    /**
     * Gets the minimum length of this month in days.
     *
     *
     * February has a minimum length of 28 days.
     * April, June, September and November have 30 days.
     * All other months have 31 days.
     *
     * @return the minimum length of this month in days, from 28 to 31
     */
    fun minLength(): Int {
        return when (this) {
            FEBRUARY -> 28
            APRIL, JUNE, SEPTEMBER, NOVEMBER -> 30
            else -> 31
        }
    }

    /**
     * Gets the maximum length of this month in days.
     *
     *
     * February has a maximum length of 29 days.
     * April, June, September and November have 30 days.
     * All other months have 31 days.
     *
     * @return the maximum length of this month in days, from 29 to 31
     */
    fun maxLength(): Int {
        return when (this) {
            FEBRUARY -> 29
            APRIL, JUNE, SEPTEMBER, NOVEMBER -> 30
            else -> 31
        }
    }

    /**
     * Gets the day-of-year corresponding to the first day of this month.
     *
     *
     * This returns the day-of-year that this month begins on, using the leap
     * year flag to determine the length of February.
     *
     * @param leapYear  true if the length is required for a leap year
     * @return the day of year corresponding to the first day of this month, from 1 to 336
     */
    fun firstDayOfYear(leapYear: Boolean): Int {
        val leap = if (leapYear) 1 else 0
        return when (this) {
            JANUARY -> 1
            FEBRUARY -> 32
            MARCH -> 60 + leap
            APRIL -> 91 + leap
            MAY -> 121 + leap
            JUNE -> 152 + leap
            JULY -> 182 + leap
            AUGUST -> 213 + leap
            SEPTEMBER -> 244 + leap
            OCTOBER -> 274 + leap
            NOVEMBER -> 305 + leap
            DECEMBER -> 335 + leap
        }
    }

    /**
     * Gets the month corresponding to the first month of this quarter.
     *
     *
     * The year can be divided into four quarters.
     * This method returns the first month of the quarter for the base month.
     * January, February and March return January.
     * April, May and June return April.
     * July, August and September return July.
     * October, November and December return October.
     *
     * @return the first month of the quarter corresponding to this month, not null
     */
    fun firstMonthOfQuarter(): Month {
        return Month.values()[ordinal / 3 * 3]
    }
}