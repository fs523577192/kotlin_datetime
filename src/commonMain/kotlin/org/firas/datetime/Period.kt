/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2012, 2013 Stephen Colebourne & Michael Nascimento Santos
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

import org.firas.datetime.chrono.ChronoPeriod
import org.firas.datetime.chrono.IsoChronology
import org.firas.datetime.format.DateTimeParseException
import org.firas.datetime.temporal.*
import org.firas.datetime.util.MathUtils
import org.firas.util.Integers

/**
 * A date-based amount of time in the ISO-8601 calendar system,
 * such as '2 years, 3 months and 4 days'.
 *
 *
 * This class models a quantity or amount of time in terms of years, months and days.
 * See [Duration] for the time-based equivalent to this class.
 *
 *
 * Durations and periods differ in their treatment of daylight savings time
 * when added to [ZonedDateTime]. A `Duration` will add an exact
 * number of seconds, thus a duration of one day is always exactly 24 hours.
 * By contrast, a `Period` will add a conceptual day, trying to maintain
 * the local time.
 *
 *
 * For example, consider adding a period of one day and a duration of one day to
 * 18:00 on the evening before a daylight savings gap. The `Period` will add
 * the conceptual day and result in a `ZonedDateTime` at 18:00 the following day.
 * By contrast, the `Duration` will add exactly 24 hours, resulting in a
 * `ZonedDateTime` at 19:00 the following day (assuming a one hour DST gap).
 *
 *
 * The supported units of a period are [ChronoUnit#YEARS YEARS],
 * [ChronoUnit#MONTHS MONTHS] and [ChronoUnit#DAYS DAYS].
 * All three fields are always present, but may be set to zero.
 *
 *
 * The ISO-8601 calendar system is the modern civil calendar system used today
 * in most of the world. It is equivalent to the proleptic Gregorian calendar
 * system, in which today's rules for leap years are applied for all time.
 *
 *
 * The period is modeled as a directed amount of time, meaning that individual parts of the
 * period may be negative.
 *
 *
 *
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `Period` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
class Period private constructor(
    /**
     * The number of years.
     */
    val years: Int,

    /**
     * The number of months.
     */
    val months: Int,

    /**
     * The number of days.
     */
    val days: Int
): ChronoPeriod {

    companion object {
        /**
         * A constant for a period of zero.
         */
        val ZERO = Period(0, 0, 0)

        /**
         * Serialization version.
         */
        private const val serialVersionUID = -3587258372562876L

        /**
         * The pattern for parsing.
         */
        private val PATTERN = Regex(
            "([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?",
            RegexOption.IGNORE_CASE
        )

        /**
         * The set of supported units.
         */
        private val SUPPORTED_UNITS = listOf<TemporalUnit>(
                ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS)

        //-----------------------------------------------------------------------
        /**
         * Obtains a `Period` representing a number of years.
         *
         *
         * The resulting period will have the specified years.
         * The months and days units will be zero.
         *
         * @param years  the number of years, positive or negative
         * @return the period of years, not null
         */
        fun ofYears(years: Int): Period {
            return create(years, 0, 0)
        }

        /**
         * Obtains a `Period` representing a number of months.
         *
         *
         * The resulting period will have the specified months.
         * The years and days units will be zero.
         *
         * @param months  the number of months, positive or negative
         * @return the period of months, not null
         */
        fun ofMonths(months: Int): Period {
            return create(0, months, 0)
        }

        /**
         * Obtains a `Period` representing a number of weeks.
         *
         *
         * The resulting period will be day-based, with the amount of days
         * equal to the number of weeks multiplied by 7.
         * The years and months units will be zero.
         *
         * @param weeks  the number of weeks, positive or negative
         * @return the period, with the input weeks converted to days, not null
         */
        fun ofWeeks(weeks: Int): Period {
            return create(0, 0, MathUtils.multiplyExact(weeks, 7))
        }

        /**
         * Obtains a `Period` representing a number of days.
         *
         *
         * The resulting period will have the specified days.
         * The years and months units will be zero.
         *
         * @param days  the number of days, positive or negative
         * @return the period of days, not null
         */
        fun ofDays(days: Int): Period {
            return create(0, 0, days)
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains a `Period` representing a number of years, months and days.
         *
         *
         * This creates an instance based on years, months and days.
         *
         * @param years  the amount of years, may be negative
         * @param months  the amount of months, may be negative
         * @param days  the amount of days, may be negative
         * @return the period of years, months and days, not null
         */
        fun of(years: Int, months: Int, days: Int): Period {
            return create(years, months, days)
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains a `Period` consisting of the number of years, months,
         * and days between two dates.
         *
         *
         * The start date is included, but the end date is not.
         * The period is calculated by removing complete months, then calculating
         * the remaining number of days, adjusting to ensure that both have the same sign.
         * The number of months is then split into years and months based on a 12 month year.
         * A month is considered if the end day-of-month is greater than or equal to the start day-of-month.
         * For example, from `2010-01-15` to `2011-03-18` is one year, two months and three days.
         *
         *
         * The result of this method can be a negative period if the end is before the start.
         * The negative sign will be the same in each of year, month and day.
         *
         * @param startDateInclusive  the start date, inclusive, not null
         * @param endDateExclusive  the end date, exclusive, not null
         * @return the period between this date and the end date, not null
         * @see ChronoLocalDate.until
         */
        fun between(startDateInclusive: LocalDate, endDateExclusive: LocalDate): Period {
            TODO()
            // return startDateInclusive.until(endDateExclusive)
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains an instance of `Period` from a temporal amount.
         *
         *
         * This obtains a period based on the specified amount.
         * A `TemporalAmount` represents an  amount of time, which may be
         * date-based or time-based, which this factory extracts to a `Period`.
         *
         *
         * The conversion loops around the set of units from the amount and uses
         * the {@link ChronoUnit#YEARS YEARS}, {@link ChronoUnit#MONTHS MONTHS}
         * and {@link ChronoUnit#DAYS DAYS} units to create a period.
         * If any other units are found then an exception is thrown.
         *
         *
         * If the amount is a `ChronoPeriod` then it must use the ISO chronology.
         *
         * @param amount  the temporal amount to convert, not null
         * @return the equivalent period, not null
         * @throws DateTimeException if unable to convert to a `Period`
         * @throws ArithmeticException if the amount of years, months or days exceeds an int
         */
        fun from(amount: TemporalAmount): Period {
            if (amount is Period) {
                return amount
            }
            if (amount is ChronoPeriod) {
                if (IsoChronology.INSTANCE != amount.getChronology()) {
                    throw DateTimeException("Period requires ISO chronology: $amount")
                }
            }
            var years = 0
            var months = 0
            var days = 0
            amount.getUnits().forEach { unit ->
                val unitAmount = amount.get(unit)
                if (unit == ChronoUnit.YEARS) {
                    years = MathUtils.toIntExact(unitAmount)
                } else if (unit == ChronoUnit.MONTHS) {
                    months = MathUtils.toIntExact(unitAmount)
                } else if (unit == ChronoUnit.DAYS) {
                    days = MathUtils.toIntExact(unitAmount)
                } else {
                    throw DateTimeException("Unit must be Years, Months or Days, but was $unit")
                }
            }
            return create(years, months, days)
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains a `Period` from a text string such as `PnYnMnD`.
         *
         *
         * This will parse the string produced by `toString()` which is
         * based on the ISO-8601 period formats `PnYnMnD` and `PnW`.
         *
         *
         * The string starts with an optional sign, denoted by the ASCII negative
         * or positive symbol. If negative, the whole period is negated.
         * The ASCII letter "P" is next in upper or lower case.
         * There are then four sections, each consisting of a number and a suffix.
         * At least one of the four sections must be present.
         * The sections have suffixes in ASCII of "Y", "M", "W" and "D" for
         * years, months, weeks and days, accepted in upper or lower case.
         * The suffixes must occur in order.
         * The number part of each section must consist of ASCII digits.
         * The number may be prefixed by the ASCII negative or positive symbol.
         * The number must parse to an `Int`.
         *
         *
         * The leading plus/minus sign, and negative values for other units are
         * not part of the ISO-8601 standard. In addition, ISO-8601 does not
         * permit mixing between the `PnYnMnD` and `PnW` formats.
         * Any week-based input is multiplied by 7 and treated as a number of days.
         *
         *
         * For example, the following are valid inputs:
         * <pre>
         *   "P2Y"             -- Period.ofYears(2)
         *   "P3M"             -- Period.ofMonths(3)
         *   "P4W"             -- Period.ofWeeks(4)
         *   "P5D"             -- Period.ofDays(5)
         *   "P1Y2M3D"         -- Period.of(1, 2, 3)
         *   "P1Y2M3W4D"       -- Period.of(1, 2, 25)
         *   "P-1Y2M"          -- Period.of(-1, 2, 0)
         *   "-P1Y2M"          -- Period.of(-1, -2, 0)
         * </pre>
         *
         * @param text  the text to parse, not null
         * @return the parsed period, not null
         * @throws DateTimeParseException if the text cannot be parsed to a period
         */
         fun parse(text: CharSequence): Period {
            val matchResults = PATTERN.matchEntire(text)
            if (null != matchResults && matchResults.groups.size > 5) {
                val negate = if ("-" == matchResults.groupValues[1]) -1 else 1
                try {
                    val years = parseNumber(matchResults.groupValues[2], negate)
                    val months = parseNumber(matchResults.groupValues[3], negate)
                    val weeks = parseNumber(matchResults.groupValues[4], negate)
                    val days = MathUtils.addExact(parseNumber(matchResults.groupValues[5], negate),
                            MathUtils.multiplyExact(weeks, 7))
                    return create(years, months, days)
                } catch (ex: NumberFormatException) {
                    throw DateTimeParseException ("Text cannot be parsed to a Period", text, 0, ex)
                }
            }
            throw DateTimeParseException ("Text cannot be parsed to a Period", text, 0)
        }

        private fun parseNumber(text: String, negate: Int): Int {
            val intValue = text.toInt(10)
            try {
                return MathUtils.multiplyExact(intValue, negate)
            } catch (ex: ArithmeticException) {
                throw DateTimeParseException("Text cannot be parsed to a Period", text, 0, ex)
            }
        }

        //-----------------------------------------------------------------------
        /**
         * Creates an instance.
         *
         * @param years  the amount
         * @param months  the amount
         * @param days  the amount
         */
        private fun create(years: Int, months: Int, days: Int): Period {
            return if (years or months or days == 0) {
                ZERO
            } else Period(years, months, days)
        }
    } // companion object

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the requested unit.
     *
     *
     * This returns a value for each of the three supported units,
     * [YEARS][ChronoUnit.YEARS], [MONTHS][ChronoUnit.MONTHS] and
     * [DAYS][ChronoUnit.DAYS].
     * All other units throw an exception.
     *
     * @param unit the `TemporalUnit` for which to return the value
     * @return the long value of the unit
     * @throws DateTimeException if the unit is not supported
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     */
    override operator fun get(unit: TemporalUnit): Long {
        return if (unit === ChronoUnit.YEARS) {
            this.years.toLong()
        } else if (unit === ChronoUnit.MONTHS) {
            this.months.toLong()
        } else if (unit === ChronoUnit.DAYS) {
            this.days.toLong()
        } else {
            throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
        }
    }

    /**
     * Gets the set of units supported by this period.
     *
     *
     * The supported units are [YEARS][ChronoUnit.YEARS],
     * [MONTHS][ChronoUnit.MONTHS] and [DAYS][ChronoUnit.DAYS].
     * They are returned in the order years, months, days.
     *
     *
     * This set can be used in conjunction with [.get]
     * to access the entire state of the period.
     *
     * @return a list containing the years, months and days units, not null
     */
    override fun getUnits(): List<TemporalUnit> {
        return SUPPORTED_UNITS
    }

    /**
     * Gets the chronology of this period, which is the ISO calendar system.
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

    //-----------------------------------------------------------------------
    /**
     * Checks if all three units of this period are zero.
     *
     *
     * A zero period has the value zero for the years, months and days units.
     *
     * @return true if this period is zero-length
     */
    override fun isZero(): Boolean {
        return (this == ZERO)
    }

    /**
     * Checks if any of the three units of this period are negative.
     * <p>
     * This checks whether the years, months or days units are less than zero.
     *
     * @return true if any unit of this period is negative
     */
    override fun isNegative(): Boolean {
        return years < 0 || months < 0 || days < 0
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this period with the specified amount of years.
     *
     *
     * This sets the amount of the years unit in a copy of this period.
     * The months and days units are unaffected.
     *
     *
     * The months unit is not automatically normalized with the years unit.
     * This means that a period of "15 months" is different to a period
     * of "1 year and 3 months".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to represent, may be negative
     * @return a `Period` based on this period with the requested years, not null
     */
    fun withYears(years: Int): Period {
        return if (years == this.years) {
            this
        } else create(years, months, days)
    }

    /**
     * Returns a copy of this period with the specified amount of months.
     *
     *
     * This sets the amount of the months unit in a copy of this period.
     * The years and days units are unaffected.
     *
     *
     * The months unit is not automatically normalized with the years unit.
     * This means that a period of "15 months" is different to a period
     * of "1 year and 3 months".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to represent, may be negative
     * @return a `Period` based on this period with the requested months, not null
     */
    fun withMonths(months: Int): Period {
        return if (months == this.months) {
            this
        } else create(years, months, days)
    }

    /**
     * Returns a copy of this period with the specified amount of days.
     *
     *
     * This sets the amount of the days unit in a copy of this period.
     * The years and months units are unaffected.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to represent, may be negative
     * @return a `Period` based on this period with the requested days, not null
     */
    fun withDays(days: Int): Period {
        return if (days == this.days) {
            this
        } else create(years, months, days)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this period with the specified period added.
     *
     *
     * This operates separately on the years, months and days.
     * No normalization is performed.
     *
     *
     * For example, "1 year, 6 months and 3 days" plus "2 years, 2 months and 2 days"
     * returns "3 years, 8 months and 5 days".
     *
     *
     * The specified amount is typically an instance of `Period`.
     * Other types are interpreted using [Period.from].
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToAdd  the amount to add, not null
     * @return a `Period` based on this period with the requested period added, not null
     * @throws DateTimeException if the specified amount has a non-ISO chronology or
     * contains an invalid unit
     * @throws ArithmeticException if numeric overflow occurs
     */
    override operator fun plus(amountToAdd: TemporalAmount): Period {
        val isoAmount = Period.from(amountToAdd)
        return create(
            MathUtils.addExact(years, isoAmount.years),
            MathUtils.addExact(months, isoAmount.months),
            MathUtils.addExact(days, isoAmount.days)
        )
    }

    /**
     * Returns a copy of this period with the specified years added.
     *
     *
     * This adds the amount to the years unit in a copy of this period.
     * The months and days units are unaffected.
     * For example, "1 year, 6 months and 3 days" plus 2 years returns "3 years, 6 months and 3 days".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToAdd  the years to add, positive or negative
     * @return a `Period` based on this period with the specified years added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusYears(yearsToAdd: Long): Period {
        return if (yearsToAdd == 0L) {
            this
        } else create(MathUtils.toIntExact(MathUtils.addExact(years.toLong(), yearsToAdd)), months, days)
    }

    /**
     * Returns a copy of this period with the specified months added.
     *
     *
     * This adds the amount to the months unit in a copy of this period.
     * The years and days units are unaffected.
     * For example, "1 year, 6 months and 3 days" plus 2 months returns "1 year, 8 months and 3 days".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthsToAdd  the months to add, positive or negative
     * @return a `Period` based on this period with the specified months added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusMonths(monthsToAdd: Long): Period {
        return if (monthsToAdd == 0L) {
            this
        } else create(years, MathUtils.toIntExact(MathUtils.addExact(months.toLong(), monthsToAdd)), days)
    }

    /**
     * Returns a copy of this period with the specified days added.
     *
     *
     * This adds the amount to the days unit in a copy of this period.
     * The years and months units are unaffected.
     * For example, "1 year, 6 months and 3 days" plus 2 days returns "1 year, 6 months and 5 days".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param daysToAdd  the days to add, positive or negative
     * @return a `Period` based on this period with the specified days added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusDays(daysToAdd: Long): Period {
        return if (daysToAdd == 0L) {
            this
        } else create(years, months, MathUtils.toIntExact(MathUtils.addExact(days.toLong(), daysToAdd)))
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this period with the specified period subtracted.
     *
     *
     * This operates separately on the years, months and days.
     * No normalization is performed.
     *
     *
     * For example, "1 year, 6 months and 3 days" minus "2 years, 2 months and 2 days"
     * returns "-1 years, 4 months and 1 day".
     *
     *
     * The specified amount is typically an instance of `Period`.
     * Other types are interpreted using [Period.from].
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToSubtract  the amount to subtract, not null
     * @return a `Period` based on this period with the requested period subtracted, not null
     * @throws DateTimeException if the specified amount has a non-ISO chronology or
     * contains an invalid unit
     * @throws ArithmeticException if numeric overflow occurs
     */
    override operator fun minus(amountToSubtract: TemporalAmount): Period {
        val isoAmount = Period.from(amountToSubtract)
        return create(
            MathUtils.subtractExact(years, isoAmount.years),
            MathUtils.subtractExact(months, isoAmount.months),
            MathUtils.subtractExact(days, isoAmount.days)
        )
    }

    /**
     * Returns a copy of this period with the specified years subtracted.
     *
     *
     * This subtracts the amount from the years unit in a copy of this period.
     * The months and days units are unaffected.
     * For example, "1 year, 6 months and 3 days" minus 2 years returns "-1 years, 6 months and 3 days".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToSubtract  the years to subtract, positive or negative
     * @return a `Period` based on this period with the specified years subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusYears(yearsToSubtract: Long): Period {
        return if (yearsToSubtract == Long.MIN_VALUE) plusYears(Long.MAX_VALUE).plusYears(1)
        else plusYears(-yearsToSubtract)
    }

    /**
     * Returns a copy of this period with the specified months subtracted.
     *
     *
     * This subtracts the amount from the months unit in a copy of this period.
     * The years and days units are unaffected.
     * For example, "1 year, 6 months and 3 days" minus 2 months returns "1 year, 4 months and 3 days".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthsToSubtract  the years to subtract, positive or negative
     * @return a `Period` based on this period with the specified months subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusMonths(monthsToSubtract: Long): Period {
        return if (monthsToSubtract == Long.MIN_VALUE) plusMonths(Long.MAX_VALUE).plusMonths(1)
        else plusMonths(-monthsToSubtract)
    }

    /**
     * Returns a copy of this period with the specified days subtracted.
     *
     *
     * This subtracts the amount from the days unit in a copy of this period.
     * The years and months units are unaffected.
     * For example, "1 year, 6 months and 3 days" minus 2 days returns "1 year, 6 months and 1 day".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param daysToSubtract  the months to subtract, positive or negative
     * @return a `Period` based on this period with the specified days subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusDays(daysToSubtract: Long): Period {
        return if (daysToSubtract == Long.MIN_VALUE) plusDays(Long.MAX_VALUE).plusDays(1)
        else plusDays(-daysToSubtract)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a new instance with each element in this period multiplied
     * by the specified scalar.
     *
     *
     * This returns a period with each of the years, months and days units
     * individually multiplied.
     * For example, a period of "2 years, -3 months and 4 days" multiplied by
     * 3 will return "6 years, -9 months and 12 days".
     * No normalization is performed.
     *
     * @param scalar  the scalar to multiply by, not null
     * @return a `Period` based on this period with the amounts multiplied by the scalar, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun multipliedBy(scalar: Int): Period {
        return if (this === ZERO || scalar == 1) {
            this
        } else create(
            MathUtils.multiplyExact(this.years, scalar),
            MathUtils.multiplyExact(this.months, scalar),
            MathUtils.multiplyExact(this.days, scalar)
        )
    }

    /**
     * Returns a new instance with each amount in this period negated.
     *
     *
     * This returns a period with each of the years, months and days units
     * individually negated.
     * For example, a period of "2 years, -3 months and 4 days" will be
     * negated to "-2 years, 3 months and -4 days".
     * No normalization is performed.
     *
     * @return a `Period` based on this period with the amounts negated, not null
     * @throws ArithmeticException if numeric overflow occurs, which only happens if
     * one of the units has the value `Long.MIN_VALUE`
     */
    override fun negated(): Period {
        return multipliedBy(-1)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this period with the years and months normalized.
     *
     *
     * This normalizes the years and months units, leaving the days unit unchanged.
     * The months unit is adjusted to have an absolute value less than 12,
     * with the years unit being adjusted to compensate. For example, a period of
     * "1 Year and 15 months" will be normalized to "2 years and 3 months".
     *
     *
     * The sign of the years and months units will be the same after normalization.
     * For example, a period of "1 year and -25 months" will be normalized to
     * "-1 year and -1 month".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return a `Period` based on this period with excess months normalized to years, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun normalized(): Period {
        val totalMonths = toTotalMonths()
        val splitYears = totalMonths / 12
        val splitMonths = (totalMonths % 12).toInt()  // no overflow
        return if (splitYears == this.years.toLong() && splitMonths == this.months) {
            this
        } else create(MathUtils.toIntExact(splitYears), splitMonths, this.days)
    }

    /**
     * Gets the total number of months in this period.
     *
     *
     * This returns the total number of months in the period by multiplying the
     * number of years by 12 and adding the number of months.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return the total number of months in the period, may be negative
     */
    fun toTotalMonths(): Long {
        return this.years * 12L + this.months  // no overflow
    }

    //-------------------------------------------------------------------------
    /**
     * Adds this period to the specified temporal object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with this period added.
     * If the temporal has a chronology, it must be the ISO chronology.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.plus].
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * dateTime = thisPeriod.addTo(dateTime);
     * dateTime = dateTime.plus(thisPeriod);
     * </pre>
     *
     *
     * The calculation operates as follows.
     * First, the chronology of the temporal is checked to ensure it is ISO chronology or null.
     * Second, if the months are zero, the years are added if non-zero, otherwise
     * the combination of years and months is added if non-zero.
     * Finally, any days are added.
     *
     *
     * This approach ensures that a partial period can be added to a partial date.
     * For example, a period of years and/or months can be added to a `YearMonth`,
     * but a period including days cannot.
     * The approach also adds years and months together when necessary, which ensures
     * correct behaviour at the end of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param temporal  the temporal object to adjust, not null
     * @return an object of the same type with the adjustment made, not null
     * @throws DateTimeException if unable to add
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun addTo(temporal: Temporal): Temporal {
        var temporal = temporal
        validateChrono(temporal)
        if (this.months == 0) {
            if (this.years != 0) {
                temporal = temporal.plus(this.years.toLong(), ChronoUnit.YEARS)
            }
        } else {
            val totalMonths = toTotalMonths()
            if (totalMonths != 0L) {
                temporal = temporal.plus(totalMonths, ChronoUnit.MONTHS)
            }
        }
        if (this.days != 0) {
            temporal = temporal.plus(this.days.toLong(), ChronoUnit.DAYS)
        }
        return temporal
    }

    /**
     * Subtracts this period from the specified temporal object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with this period subtracted.
     * If the temporal has a chronology, it must be the ISO chronology.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.minus].
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * dateTime = thisPeriod.subtractFrom(dateTime);
     * dateTime = dateTime.minus(thisPeriod);
     * </pre>
     *
     *
     * The calculation operates as follows.
     * First, the chronology of the temporal is checked to ensure it is ISO chronology or null.
     * Second, if the months are zero, the years are subtracted if non-zero, otherwise
     * the combination of years and months is subtracted if non-zero.
     * Finally, any days are subtracted.
     *
     *
     * This approach ensures that a partial period can be subtracted from a partial date.
     * For example, a period of years and/or months can be subtracted from a `YearMonth`,
     * but a period including days cannot.
     * The approach also subtracts years and months together when necessary, which ensures
     * correct behaviour at the end of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param temporal  the temporal object to adjust, not null
     * @return an object of the same type with the adjustment made, not null
     * @throws DateTimeException if unable to subtract
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun subtractFrom(temporal: Temporal): Temporal {
        var temporal = temporal
        validateChrono(temporal)
        if (this.months == 0) {
            if (this.years != 0) {
                temporal = temporal.minus(this.years.toLong(), ChronoUnit.YEARS)
            }
        } else {
            val totalMonths = toTotalMonths()
            if (totalMonths != 0L) {
                temporal = temporal.minus(totalMonths, ChronoUnit.MONTHS)
            }
        }
        if (this.days != 0) {
            temporal = temporal.minus(this.days.toLong(), ChronoUnit.DAYS)
        }
        return temporal
    }

    // ----==== Override methods inherited from Any ====----
    /**
     * Checks if this period is equal to another period.
     *
     *
     * The comparison is based on the type `Period` and each of the three amounts.
     * To be equal, the years, months and days units must be individually equal.
     * Note that this means that a period of "15 Months" is not equal to a period
     * of "1 Year and 3 Months".
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other period
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is Period) {
            return years == other.years &&
                    months == other.months &&
                    days == other.days
        }
        return false
    }

    /**
     * A hash code for this period.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return years + Integers.rotateLeft(months, 8) + Integers.rotateLeft(days, 16)
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this period as a `String`, such as `P6Y3M1D`.
     *
     *
     * The output will be in the ISO-8601 period format.
     * A zero period will be represented as zero days, 'P0D'.
     *
     * @return a string representation of this period, not null
     */
    override fun toString(): String {
        if (this == ZERO) {
            return "P0D"
        } else {
            val buf = StringBuilder()
            buf.append('P')
            if (this.years != 0) {
                buf.append(this.years).append('Y')
            }
            if (this.months != 0) {
                buf.append(this.months).append('M')
            }
            if (this.days != 0) {
                buf.append(this.days).append('D')
            }
            return buf.toString()
        }
    }

    /**
     * Validates that the temporal has the correct chronology.
     */
    private fun validateChrono(temporal: TemporalAccessor) {
        val temporalChrono = temporal.query(TemporalQueries.CHRONO)
        if (temporalChrono != null && IsoChronology.INSTANCE != temporalChrono) {
            throw DateTimeException("Chronology mismatch, expected: ISO, actual: " + temporalChrono.getId())
        }
    }
}