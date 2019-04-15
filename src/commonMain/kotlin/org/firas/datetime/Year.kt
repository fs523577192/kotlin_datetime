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
import kotlin.reflect.KClass

/**
 * A year in the ISO-8601 calendar system, such as `2007`.
 *
 *
 * `Year` is an immutable date-time object that represents a year.
 * Any field that can be derived from a year can be obtained.
 *
 *
 * <b>Note that years in the ISO chronology only align with years in the
 * Gregorian-Julian system for modern years. Parts of Russia did not switch to the
 * modern Gregorian/ISO rules until 1920.
 * As such, historical years must be treated with caution.</b>
 *
 *
 * This class does not store or represent a month, day, time or time-zone.
 * For example, the value "2007" can be stored in a `Year`.
 *
 *
 * Years represented by this class follow the ISO-8601 standard and use
 * the proleptic numbering system. Year 1 is preceded by year 0, then by year -1.
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
 * `Year` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class Year private constructor(val value: Int): Temporal, Comparable<Year> {

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

        /**
         * Obtains an instance of `Year` from a temporal object.
         *
         *
         * This obtains a year based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `Year`.
         *
         *
         * The conversion extracts the {@link ChronoField#YEAR year} field.
         * The extraction is only permitted if the temporal object has an ISO
         * chronology, or can be converted to a `LocalDate`.
         *
         *
         * This method matches the signature of the functional interface {@link TemporalQuery}
         * allowing it to be used as a query via method reference, `Year::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the year, not null
         * @throws DateTimeException if unable to convert to a `Year`
         */
        fun from(temporal: TemporalAccessor): Year {
            if (temporal is Year) {
                return temporal
            }
            try {
                var temporal = temporal
                if (!IsoChronology.INSTANCE.equals(Chronology.from(temporal))) {
                    temporal = LocalDate.from(temporal)
                }
                return of(temporal.get(ChronoField.YEAR))
            } catch (ex: DateTimeException) {
                throw DateTimeException ("Unable to obtain Year from TemporalAccessor: " +
                        temporal + " of type " + temporal.getClassName(), ex)
            }
        }
    } // companion object

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified field is supported.
     *
     *
     * This checks if this year can be queried for the specified field.
     * If false, then calling the [range][.range],
     * [get][.get] and [.with]
     * methods will throw an exception.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The supported fields are:
     *
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
     * @return true if the field is supported on this year, false if not
     */
    override fun isSupported(field: TemporalField): Boolean {
        return if (field is ChronoField) {
            field == ChronoField.YEAR || field == ChronoField.YEAR_OF_ERA || field == ChronoField.ERA
        } else field.isSupportedBy(this)
    }

    /**
     * Checks if the specified unit is supported.
     *
     *
     * This checks if the specified unit can be added to, or subtracted from, this year.
     * If false, then calling the [.plus] and
     * [minus][.minus] methods will throw an exception.
     *
     *
     * If the unit is a [ChronoUnit] then the query is implemented here.
     * The supported units are:
     *
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
            unit == ChronoUnit.YEARS || unit == ChronoUnit.DECADES ||
                    unit == ChronoUnit.CENTURIES || unit == ChronoUnit.MILLENNIA || unit == ChronoUnit.ERAS
        } else unit.isSupportedBy(this)
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

    /**
     * Returns a copy of this year with the specified field set to a new value.
     *
     *
     * This returns a `Year`, based on this one, with the value
     * for the specified field changed.
     * If it is not possible to set the value, because the field is not supported or for
     * some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the adjustment is implemented here.
     * The supported fields behave as follows:
     *
     *  * `YEAR_OF_ERA` -
     * Returns a `Year` with the specified year-of-era
     * The era will be unchanged.
     *  * `YEAR` -
     * Returns a `Year` with the specified year.
     * This completely replaces the date and is equivalent to [.of].
     *  * `ERA` -
     * Returns a `Year` with the specified era.
     * The year-of-era will be unchanged.
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
     * @return a `Year` based on `this` with the specified field set, not null
     * @throws DateTimeException if the field cannot be set
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(field: TemporalField, newValue: Long): Year {
        if (field is ChronoField) {
            field.checkValidValue(newValue)
            return when (field) {
                ChronoField.YEAR_OF_ERA -> Year.of(
                        (if (this.value < 1) 1 - newValue else newValue).toInt())
                ChronoField.YEAR -> Year.of(newValue.toInt())
                ChronoField.ERA -> if (getLong(ChronoField.ERA) == newValue) this
                        else Year.of(1 - this.value)
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        return field.adjustInto(this, newValue)
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the specified field from this year as an `int`.
     *
     *
     * This queries this year for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The [supported fields][.isSupported] will return valid
     * values based on this year.
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
     * Gets the value of the specified field from this year as a `long`.
     *
     *
     * This queries this year for the value of the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The [supported fields][.isSupported] will return valid
     * values based on this year.
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
                ChronoField.YEAR_OF_ERA -> if (this.value < 1) 1L - this.value else this.value.toLong()
                ChronoField.YEAR -> this.value.toLong()
                ChronoField.ERA -> if (this.value < 1) 0L else 1L
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        return field.getFrom(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this year with the specified amount added.
     *
     *
     * This returns a `Year`, based on this one, with the specified amount added.
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
     * @return a `Year` based on this year with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: TemporalAmount): Year {
        return amountToAdd.addTo(this) as Year
    }

    /**
     * Returns a copy of this year with the specified amount added.
     *
     *
     * This returns a `Year`, based on this one, with the amount
     * in terms of the unit added. If it is not possible to add the amount, because the
     * unit is not supported or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoUnit] then the addition is implemented here.
     * The supported fields behave as follows:
     *
     *  * `YEARS` -
     * Returns a `Year` with the specified number of years added.
     * This is equivalent to [.plusYears].
     *  * `DECADES` -
     * Returns a `Year` with the specified number of decades added.
     * This is equivalent to calling [.plusYears] with the amount
     * multiplied by 10.
     *  * `CENTURIES` -
     * Returns a `Year` with the specified number of centuries added.
     * This is equivalent to calling [.plusYears] with the amount
     * multiplied by 100.
     *  * `MILLENNIA` -
     * Returns a `Year` with the specified number of millennia added.
     * This is equivalent to calling [.plusYears] with the amount
     * multiplied by 1,000.
     *  * `ERAS` -
     * Returns a `Year` with the specified number of eras added.
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
     * @return a `Year` based on this year with the specified amount added, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: Long, unit: TemporalUnit): Year {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.YEARS -> plusYears(amountToAdd.toInt())
                ChronoUnit.DECADES -> plusYears(MathUtils.multiplyExact(amountToAdd.toInt(), 10))
                ChronoUnit.CENTURIES -> plusYears(MathUtils.multiplyExact(amountToAdd.toInt(), 100))
                ChronoUnit.MILLENNIA -> plusYears(MathUtils.multiplyExact(amountToAdd.toInt(), 1000))
                ChronoUnit.ERAS -> with(ChronoField.ERA, MathUtils.addExact(
                        getLong(ChronoField.ERA), amountToAdd))
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.addTo(this, amountToAdd)
    }

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

    /**
     * Calculates the amount of time until another year in terms of the specified unit.
     *
     *
     * This calculates the amount of time between two `Year`
     * objects in terms of a single `TemporalUnit`.
     * The start and end points are `this` and the specified year.
     * The result will be negative if the end is before the start.
     * The `Temporal` passed to this method is converted to a
     * `Year` using [.from].
     * For example, the amount in decades between two year can be calculated
     * using `startYear.until(endYear, DECADES)`.
     *
     *
     * The calculation returns a whole number, representing the number of
     * complete units between the two years.
     * For example, the amount in decades between 2012 and 2031
     * will only be one decade as it is one year short of two decades.
     *
     *
     * There are two equivalent ways of using this method.
     * The first is to invoke this method.
     * The second is to use [TemporalUnit.between]:
     * <pre>
     * // these two lines are equivalent
     * amount = start.until(end, YEARS);
     * amount = YEARS.between(start, end);
    </pre> *
     * The choice should be made based on which makes the code more readable.
     *
     *
     * The calculation is implemented in this method for [ChronoUnit].
     * The units `YEARS`, `DECADES`, `CENTURIES`,
     * `MILLENNIA` and `ERAS` are supported.
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
     * @param endExclusive  the end date, exclusive, which is converted to a `Year`, not null
     * @param unit  the unit to measure the amount in, not null
     * @return the amount of time between this year and the end year
     * @throws DateTimeException if the amount cannot be calculated, or the end
     * temporal cannot be converted to a `Year`
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = Year.from(endExclusive)
        if (unit is ChronoUnit) {
            val yearsUntil = end.value.toLong() - this.value  // no overflow
            return when (unit) {
                ChronoUnit.YEARS -> yearsUntil
                ChronoUnit.DECADES -> yearsUntil / 10
                ChronoUnit.CENTURIES -> yearsUntil / 100
                ChronoUnit.MILLENNIA -> yearsUntil / 1000
                ChronoUnit.ERAS -> end.getLong(ChronoField.ERA) - getLong(ChronoField.ERA)
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
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