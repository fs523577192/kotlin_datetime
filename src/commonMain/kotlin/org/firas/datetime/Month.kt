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

import org.firas.datetime.chrono.Chronology
import org.firas.datetime.chrono.IsoChronology
import org.firas.datetime.temporal.*
import kotlin.reflect.KClass

/**
 * A month-of-year, such as 'July'.
 * <p>
 * `Month` is an enum representing the 12 months of the year -
 * January, February, March, April, May, June, July, August, September, October,
 * November and December.
 * <p>
 * In addition to the textual enum name, each month-of-year has an `int` value.
 * The `int` value follows normal usage and the ISO-8601 standard,
 * from 1 (January) to 12 (December). It is recommended that applications use the enum
 * rather than the `int` value to ensure code clarity.
 * <p>
 * <b>Do not use `ordinal()` to obtain the numeric representation of `Month`.
 * Use `getValue()` instead.</b>
 * <p>
 * This enum represents a common concept that is found in many calendar systems.
 * As such, this enum may be used by any calendar system that has the month-of-year
 * concept defined exactly equivalent to the ISO-8601 calendar system.
 *
 * @implSpec
 * This is an immutable and thread-safe enum.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
enum class Month: TemporalAccessor {
    /**
     * The singleton instance for the month of January with 31 days.
     * This has the numeric value of `1`.
     */
    JANUARY,
    /**
     * The singleton instance for the month of February with 28 days, or 29 in a leap year.
     * This has the numeric value of `2`.
     */
    FEBRUARY,
    /**
     * The singleton instance for the month of March with 31 days.
     * This has the numeric value of `3`.
     */
    MARCH,
    /**
     * The singleton instance for the month of April with 30 days.
     * This has the numeric value of `4`.
     */
    APRIL,
    /**
     * The singleton instance for the month of May with 31 days.
     * This has the numeric value of `5`.
     */
    MAY,
    /**
     * The singleton instance for the month of June with 30 days.
     * This has the numeric value of `6`.
     */
    JUNE,
    /**
     * The singleton instance for the month of July with 31 days.
     * This has the numeric value of `7`.
     */
    JULY,
    /**
     * The singleton instance for the month of August with 31 days.
     * This has the numeric value of `8`.
     */
    AUGUST,
    /**
     * The singleton instance for the month of September with 30 days.
     * This has the numeric value of `9`.
     */
    SEPTEMBER,
    /**
     * The singleton instance for the month of October with 31 days.
     * This has the numeric value of `10`.
     */
    OCTOBER,
    /**
     * The singleton instance for the month of November with 30 days.
     * This has the numeric value of `11`.
     */
    NOVEMBER,
    /**
     * The singleton instance for the month of December with 31 days.
     * This has the numeric value of `12`.
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

        /**
         * Obtains an instance of `Month` from a temporal object.
         *
         *
         * This obtains a month based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `Month`.
         *
         *
         * The conversion extracts the {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} field.
         * The extraction is only permitted if the temporal object has an ISO
         * chronology, or can be converted to a `LocalDate`.
         *
         *
         * This method matches the signature of the functional interface [TemporalQuery]
         * allowing it to be used as a query via method reference, `Month::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the month-of-year, not null
         * @throws DateTimeException if unable to convert to a `Month`
         */
        fun from(temporal: TemporalAccessor): Month {
            if (temporal is Month) {
                return temporal
            }
            try {
                var temporal = temporal
                if (IsoChronology.INSTANCE != Chronology.from(temporal)) {
                    temporal = LocalDate.from(temporal)
                }
                return of(temporal.get(ChronoField.MONTH_OF_YEAR))
            } catch (ex: DateTimeException) {
                throw DateTimeException(
                    "Unable to obtain Month from TemporalAccessor: " +
                            temporal + " of type " + temporal.getClassName(), ex
                )
            }
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

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified field is supported.
     * <p>
     * This checks if this month-of-year can be queried for the specified field.
     * If false, then calling the {@link #range(TemporalField) range} and
     * {@link #get(TemporalField) get} methods will throw an exception.
     * <p>
     * If the field is {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} then
     * this method returns true.
     * All other `ChronoField` instances will return false.
     * <p>
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.isSupportedBy(TemporalAccessor)`
     * passing `this` as the argument.
     * Whether the field is supported is determined by the field.
     *
     * @param field  the field to check, null returns false
     * @return true if the field is supported on this month-of-year, false if not
     */
    override fun isSupported(field: TemporalField): Boolean {
        if (field is ChronoField) {
            return field == ChronoField.MONTH_OF_YEAR
        }
        return field.isSupportedBy(this)
    }

    /**
     * Gets the range of valid values for the specified field.
     * <p>
     * The range object expresses the minimum and maximum valid values for a field.
     * This month is used to enhance the accuracy of the returned range.
     * If it is not possible to return the range, because the field is not supported
     * or for some other reason, an exception is thrown.
     * <p>
     * If the field is {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} then the
     * range of the month-of-year, from 1 to 12, will be returned.
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     * <p>
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
        if (field == ChronoField.MONTH_OF_YEAR) {
            return field.range()
        }
        TODO("return TemporalAccessor.super.range(field)")
    }

    /**
     * Gets the value of the specified field from this month-of-year as an `int`.
     *
     *
     * This queries this month for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} then the
     * value of the month-of-year, from 1 to 12, will be returned.
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.getFrom(TemporalAccessor)`
     * passing `this` as the argument. Whether the value can be obtained,
     * and what the value represents, is determined by the field.
     *
     * @param field  the field to get, not null
     * @return the value for the field, within the valid range of values
     * @throws DateTimeException if a value for the field cannot be obtained or
     *         the value is outside the range of valid values for the field
     * @throws UnsupportedTemporalTypeException if the field is not supported or
     *         the range of values exceeds an `int`
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun get(field: TemporalField): Int {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return getValue()
        }
        TODO("return TemporalAccessor.super.get(field)")
    }

    /**
     * Gets the value of the specified field from this month-of-year as a `long`.
     *
     *
     * This queries this month for the value of the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} then the
     * value of the month-of-year, from 1 to 12, will be returned.
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
        if (field == ChronoField.MONTH_OF_YEAR) {
            return getValue().toLong()
        } else if (field is ChronoField) {
            throw UnsupportedTemporalTypeException("Unsupported field: $field")
        }
        return field.getFrom(this)
    }
}