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

import org.firas.datetime.chrono.Chronology
import org.firas.datetime.chrono.IsoChronology
import org.firas.datetime.temporal.*
import org.firas.datetime.util.MathUtils
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

/**
 * A year-month in the ISO-8601 calendar system, such as `2007-12`.
 *
 *
 * `YearMonth` is an immutable date-time object that represents the combination
 * of a year and month. Any field that can be derived from a year and month, such as
 * quarter-of-year, can be obtained.
 *
 *
 * This class does not store or represent a day, time or time-zone.
 * For example, the value "October 2007" can be stored in a `YearMonth`.
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
 * `YearMonth` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class YearMonth private constructor(
        val year: Int,
        val monthValue: Int): Temporal, Comparable<YearMonth> {

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

        /**
         * Obtains an instance of `YearMonth` from a temporal object.
         *
         *
         * This obtains a year-month based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `YearMonth`.
         *
         *
         * The conversion extracts the {@link ChronoField#YEAR YEAR} and
         * {@link ChronoField#MONTH_OF_YEAR MONTH_OF_YEAR} fields.
         * The extraction is only permitted if the temporal object has an ISO
         * chronology, or can be converted to a `LocalDate`.
         *
         *
         * This method matches the signature of the functional interface [TemporalQuery]
         * allowing it to be used as a query via method reference, `YearMonth::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the year-month, not null
         * @throws DateTimeException if unable to convert to a `YearMonth`
         */
        fun from(temporal: TemporalAccessor): YearMonth {
            if (temporal is YearMonth) {
                return temporal
            }
            try {
                var temporal = temporal
                if (!IsoChronology.INSTANCE.equals(Chronology.from(temporal))) {
                    temporal = LocalDate.from(temporal)
                }
                return of(temporal.get(ChronoField.YEAR), temporal.get(ChronoField.MONTH_OF_YEAR))
            } catch (ex: DateTimeException) {
                throw DateTimeException ("Unable to obtain YearMonth from TemporalAccessor: " +
                        temporal + " of type " + temporal.getKClass().qualifiedName, ex)
            }
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
        return Month.of(this.monthValue)
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
     * Checks if the specified field is supported.
     *
     *
     * This checks if this year-month can be queried for the specified field.
     * If false, then calling the [range][.range],
     * [get][.get] and [.with]
     * methods will throw an exception.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The supported fields are:
     *
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
     * @return true if the field is supported on this year-month, false if not
     */
    override fun isSupported(field: TemporalField): Boolean {
        return if (field is ChronoField) {
            field == ChronoField.YEAR || field == ChronoField.MONTH_OF_YEAR
                    || field == ChronoField.PROLEPTIC_MONTH || field == ChronoField.YEAR_OF_ERA
                    || field == ChronoField.ERA
        } else field.isSupportedBy(this)
    }

    /**
     * Checks if the specified unit is supported.
     *
     *
     * This checks if the specified unit can be added to, or subtracted from, this year-month.
     * If false, then calling the [.plus] and
     * [minus][.minus] methods will throw an exception.
     *
     *
     * If the unit is a [ChronoUnit] then the query is implemented here.
     * The supported units are:
     *
     *  * `MONTHS`
     *  * `YEARS`
     *  * `DECADES`
     *  * `CENTURIES`
     *  * `MILLENNIA`
     *  * `ERAS`
     *
     * All other `ChronoUnit` instances will return false.
     *
     *
     * If the unit is not a `ChronoUnit`, then the result of this method
     * is obtained by invoking `TemporalUnit.isSupportedBy(Temporal)`
     * passing `this` as the argument.
     * Whether the unit is supported is determined by the unit.
     *
     * @param unit  the unit to check, null returns false
     * @return true if the unit can be added/subtracted, false if not
     */
    override fun isSupported(unit: TemporalUnit): Boolean {
        return if (unit is ChronoUnit) {
            unit == ChronoUnit.MONTHS || unit == ChronoUnit.YEARS || unit == ChronoUnit.DECADES
                    || unit == ChronoUnit.CENTURIES || unit == ChronoUnit.MILLENNIA || unit == ChronoUnit.ERAS
        } else unit.isSupportedBy(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the range of valid values for the specified field.
     *
     *
     * The range object expresses the minimum and maximum valid values for a field.
     * This year-month is used to enhance the accuracy of the returned range.
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
        if (field === ChronoField.YEAR_OF_ERA) {
            return if (this.year <= 0) ValueRange.of(1L, Year.MAX_VALUE + 1L)
                    else ValueRange.of(1L, Year.MAX_VALUE.toLong())
        } else {
            TODO("return Temporal.super.range(field)")
        }
    }

    /**
     * Gets the value of the specified field from this year-month as an `int`.
     *
     *
     * This queries this year-month for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The [supported fields][.isSupported] will return valid
     * values based on this year-month, except `PROLEPTIC_MONTH` which is too
     * large to fit in an `int` and throw a `DateTimeException`.
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
     * the value is outside the range of valid values for the field
     * @throws UnsupportedTemporalTypeException if the field is not supported or
     * the range of values exceeds an `int`
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun get(field: TemporalField): Int {
        return range(field).checkValidIntValue(getLong(field), field)
    }

    /**
     * Gets the value of the specified field from this year-month as a `long`.
     *
     *
     * This queries this year-month for the value of the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The [supported fields][.isSupported] will return valid
     * values based on this year-month.
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
            return when (field) {
                ChronoField.MONTH_OF_YEAR -> this.monthValue.toLong()
                ChronoField.PROLEPTIC_MONTH -> getProlepticMonth()
                ChronoField.YEAR_OF_ERA -> if (this.year < 1) 1L - this.year else this.year.toLong()
                ChronoField.YEAR -> this.year.toLong()
                ChronoField.ERA -> (if (year < 1) 0 else 1).toLong()
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        return field.getFrom(this)
    }
    /**
     * Returns a copy of this year-month with the specified field set to a new value.
     *
     *
     * This returns a `YearMonth`, based on this one, with the value
     * for the specified field changed.
     * This can be used to change any supported field, such as the year or month.
     * If it is not possible to set the value, because the field is not supported or for
     * some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the adjustment is implemented here.
     * The supported fields behave as follows:
     *
     *  * `MONTH_OF_YEAR` -
     * Returns a `YearMonth` with the specified month-of-year.
     * The year will be unchanged.
     *  * `PROLEPTIC_MONTH` -
     * Returns a `YearMonth` with the specified proleptic-month.
     * This completely replaces the year and month of this object.
     *  * `YEAR_OF_ERA` -
     * Returns a `YearMonth` with the specified year-of-era
     * The month and era will be unchanged.
     *  * `YEAR` -
     * Returns a `YearMonth` with the specified year.
     * The month will be unchanged.
     *  * `ERA` -
     * Returns a `YearMonth` with the specified era.
     * The month and year-of-era will be unchanged.
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
     * @return a `YearMonth` based on `this` with the specified field set, not null
     * @throws DateTimeException if the field cannot be set
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(field: TemporalField, newValue: Long): YearMonth {
        if (field is ChronoField) {
            field.checkValidValue(newValue)
            return when (field) {
                ChronoField.MONTH_OF_YEAR -> withMonth(newValue.toInt())
                ChronoField.PROLEPTIC_MONTH -> plusMonths((newValue - getProlepticMonth()).toInt())
                ChronoField.YEAR_OF_ERA -> withYear((if (this.year < 1) 1 - newValue else newValue).toInt())
                ChronoField.YEAR -> withYear(newValue.toInt())
                ChronoField.ERA -> if (getLong(ChronoField.ERA) == newValue) this
                        else withYear(1 - this.year)
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        return field.adjustInto(this, newValue)
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
        return with(year, this.monthValue)
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
     * Returns a copy of this year-month with the specified amount added.
     *
     *
     * This returns a `YearMonth`, based on this one, with the specified amount added.
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
     * @return a `YearMonth` based on this year-month with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: TemporalAmount): YearMonth {
        return amountToAdd.addTo(this) as YearMonth
    }

    /**
     * Returns a copy of this year-month with the specified amount added.
     *
     *
     * This returns a `YearMonth`, based on this one, with the amount
     * in terms of the unit added. If it is not possible to add the amount, because the
     * unit is not supported or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoUnit] then the addition is implemented here.
     * The supported fields behave as follows:
     *
     *  * `MONTHS` -
     * Returns a `YearMonth` with the specified number of months added.
     * This is equivalent to [.plusMonths].
     *  * `YEARS` -
     * Returns a `YearMonth` with the specified number of years added.
     * This is equivalent to [.plusYears].
     *  * `DECADES` -
     * Returns a `YearMonth` with the specified number of decades added.
     * This is equivalent to calling [.plusYears] with the amount
     * multiplied by 10.
     *  * `CENTURIES` -
     * Returns a `YearMonth` with the specified number of centuries added.
     * This is equivalent to calling [.plusYears] with the amount
     * multiplied by 100.
     *  * `MILLENNIA` -
     * Returns a `YearMonth` with the specified number of millennia added.
     * This is equivalent to calling [.plusYears] with the amount
     * multiplied by 1,000.
     *  * `ERAS` -
     * Returns a `YearMonth` with the specified number of eras added.
     * Only two eras are supported so the amount must be one, zero or minus one.
     * If the amount is non-zero then the year is changed such that the year-of-era
     * is unchanged.
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
     * @return a `YearMonth` based on this year-month with the specified amount added, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: Long, unit: TemporalUnit): YearMonth {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.MONTHS -> plusMonths(amountToAdd.toInt())
                ChronoUnit.YEARS -> plusYears(amountToAdd.toInt())
                ChronoUnit.DECADES -> plusYears(MathUtils.multiplyExact(amountToAdd, 10).toInt())
                ChronoUnit.CENTURIES -> plusYears(MathUtils.multiplyExact(amountToAdd, 100).toInt())
                ChronoUnit.MILLENNIA -> plusYears(MathUtils.multiplyExact(amountToAdd, 1000).toInt())
                ChronoUnit.ERAS -> with(ChronoField.ERA,
                        MathUtils.addExact(getLong(ChronoField.ERA), amountToAdd))
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.addTo(this, amountToAdd)
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
        return with(newYear, monthValue)
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
        val monthCount = this.year * 12L + (this.monthValue - 1)
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
     * </pre>
     *
     * @param dayOfMonth  the day-of-month to use, from 1 to 31
     * @return the date formed from this year-month and the specified day, not null
     * @throws DateTimeException if the day is invalid for the year-month
     * @see .isValidDay
     */
    fun atDay(dayOfMonth: Int): LocalDate {
        return LocalDate.of(year, monthValue, dayOfMonth)
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
     * </pre>
     *
     * @return the last valid date of this year-month, not null
     */
    fun atEndOfMonth(): LocalDate {
        return LocalDate.of(this.year, this.monthValue, lengthOfMonth())
    }

    /**
     * Calculates the amount of time until another year-month in terms of the specified unit.
     *
     *
     * This calculates the amount of time between two `YearMonth`
     * objects in terms of a single `TemporalUnit`.
     * The start and end points are `this` and the specified year-month.
     * The result will be negative if the end is before the start.
     * The `Temporal` passed to this method is converted to a
     * `YearMonth` using [.from].
     * For example, the amount in years between two year-months can be calculated
     * using `startYearMonth.until(endYearMonth, YEARS)`.
     *
     *
     * The calculation returns a whole number, representing the number of
     * complete units between the two year-months.
     * For example, the amount in decades between 2012-06 and 2032-05
     * will only be one decade as it is one month short of two decades.
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
     * The units `MONTHS`, `YEARS`, `DECADES`,
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
     * @param endExclusive  the end date, exclusive, which is converted to a `YearMonth`, not null
     * @param unit  the unit to measure the amount in, not null
     * @return the amount of time between this year-month and the end year-month
     * @throws DateTimeException if the amount cannot be calculated, or the end
     * temporal cannot be converted to a `YearMonth`
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = YearMonth.from(endExclusive)
        if (unit is ChronoUnit) {
            val monthsUntil = end.getProlepticMonth() - getProlepticMonth()  // no overflow
            return when (unit) {
                ChronoUnit.MONTHS -> monthsUntil
                ChronoUnit.YEARS -> monthsUntil / 12
                ChronoUnit.DECADES -> monthsUntil / 120
                ChronoUnit.CENTURIES -> monthsUntil / 1200
                ChronoUnit.MILLENNIA -> monthsUntil / 12000
                ChronoUnit.ERAS -> end.getLong(ChronoField.ERA) - getLong(ChronoField.ERA)
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
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
            cmp = this.monthValue - other.monthValue
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
            return this.year == other.year && this.monthValue == other.monthValue
        }
        return false
    }

    /**
     * A hash code for this year-month.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return this.year xor (this.monthValue shl 27)
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
        return buf.append(if (this.monthValue < 10) "-0" else "-")
                .append(this.monthValue)
                .toString()
    }

    override fun getKClass(): KClass<out Temporal> {
        return YearMonth::class
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
        return if (this.year == newYear && this.monthValue == newMonth) {
            this
        } else YearMonth(newYear, newMonth)
    }

    private fun getProlepticMonth(): Long {
        return this.year * 12L + this.monthValue - 1
    }
}