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
 * A year in the ISO-8601 calendar system, such as {@code 2007}.
 * <p>
 * {@code Year} is an immutable date-time object that represents a year.
 * Any field that can be derived from a year can be obtained.
 * <p>
 * <b>Note that years in the ISO chronology only align with years in the
 * Gregorian-Julian system for modern years. Parts of Russia did not switch to the
 * modern Gregorian/ISO rules until 1920.
 * As such, historical years must be treated with caution.</b>
 * <p>
 * This class does not store or represent a month, day, time or time-zone.
 * For example, the value "2007" can be stored in a {@code Year}.
 * <p>
 * Years represented by this class follow the ISO-8601 standard and use
 * the proleptic numbering system. Year 1 is preceded by year 0, then by year -1.
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
 * {@code Year} may have unpredictable results and should be avoided.
 * The {@code equals} method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
class Year private constructor(val value: Int): Comparable<Year> {

    companion object {
        /**
         * The minimum supported year, '-999,999,999'.
         */
        const val MIN_VALUE = -999999999
        /**
         * The maximum supported year, '+999,999,999'.
         */
        const val MAX_VALUE = 999999999

        /**
         * Serialization version.
         */
        private const val serialVersionUID = -23038383694477807L

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
         * @param year  the year to check
         * @return true if the year is leap, false otherwise
         */
        fun isLeap(year: Int): Boolean {
            return year and 3 == 0 && (year % 100 != 0 || year % 400 == 0)
        }

        /**
         * Obtains an instance of `Year`.
         *
         *
         * This method accepts a year value from the proleptic ISO calendar system.
         *
         *
         * The year 2AD/CE is represented by 2.<br></br>
         * The year 1AD/CE is represented by 1.<br></br>
         * The year 1BC/BCE is represented by 0.<br></br>
         * The year 2BC/BCE is represented by -1.<br></br>
         *
         * @param isoYear  the ISO proleptic year to represent, from `MIN_VALUE` to `MAX_VALUE`
         * @return the year, not null
         * @throws DateTimeException if the field is invalid
         */
        fun of(isoYear: Int): Year {
            ChronoField.YEAR.checkValidValue(isoYear.toLong())
            return Year(isoYear)
        }


    } // companion object

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
    fun isLeap(): Boolean {
        return Year.isLeap(this.value)
    }

    /**
     * Gets the length of this year in days.
     *
     * @return the length of this year in days, 365 or 366
     */
    fun length(): Int {
        return if (isLeap()) 366 else 365
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `Year` with the specified number of years added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToAdd  the years to add, may be negative
     * @return a `Year` based on this year with the years added, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    fun plusYears(yearsToAdd: Int): Year {
        return if (yearsToAdd == 0) {
            this
        } else {
            of(ChronoField.YEAR.checkValidIntValue(this.value.toLong() + yearsToAdd))
        }
        // overflow safe
    }

    /**
     * Returns a copy of this `Year` with the specified number of years subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToSubtract  the years to subtract, may be negative
     * @return a `Year` based on this year with the year subtracted, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    fun minusYears(yearsToSubtract: Int): Year {
        return if (yearsToSubtract == Int.MIN_VALUE) plusYears(Int.MAX_VALUE).plusYears(1)
                else plusYears(-yearsToSubtract)
    }

    //-----------------------------------------------------------------------
    /**
     * Combines this year with a day-of-year to create a `LocalDate`.
     *
     *
     * This returns a `LocalDate` formed from this year and the specified day-of-year.
     *
     *
     * The day-of-year value 366 is only valid in a leap year.
     *
     * @param dayOfYear  the day-of-year to use, from 1 to 365-366
     * @return the local date formed from this year and the specified date of year, not null
     * @throws DateTimeException if the day of year is zero or less, 366 or greater or equal
     * to 366 and this is not a leap year
     */
    fun atDay(dayOfYear: Int): LocalDate {
        return LocalDate.ofYearDay(value, dayOfYear)
    }

    // ----==== Comparison ====----
    /**
     * Compares this year to another year.
     *
     *
     * The comparison is based on the value of the year.
     * It is "consistent with equals", as defined by [Comparable].
     *
     * @param other  the other year to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    override fun compareTo(other: Year): Int {
        return this.value - other.value
    }

    /**
     * Checks if this year is after the specified year.
     *
     * @param other  the other year to compare to, not null
     * @return true if this is after the specified year
     */
    fun isAfter(other: Year): Boolean {
        return this.value > other.value
    }

    /**
     * Checks if this year is before the specified year.
     *
     * @param other  the other year to compare to, not null
     * @return true if this point is before the specified year
     */
    fun isBefore(other: Year): Boolean {
        return this.value < other.value
    }

    // ----==== override methods inherited from Any ----
    /**
     * Checks if this year is equal to another year.
     *
     *
     * The comparison is based on the time-line position of the years.
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other year
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other is Year) (this.value == other.value) else false
    }

    /**
     * A hash code for this year.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return this.value
    }

    /**
     * Outputs this year as a `String`.
     *
     * @return a string representation of this year, not null
     */
    override fun toString(): String {
        return this.value.toString()
    }
}