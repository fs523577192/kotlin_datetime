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
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
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
package org.firas.datetime.temporal

import org.firas.datetime.DateTimeException
import org.firas.datetime.DayOfWeek
import org.firas.datetime.Duration
import org.firas.datetime.LocalDate
import org.firas.datetime.chrono.Chronology
import org.firas.datetime.chrono.IsoChronology
import org.firas.datetime.format.ResolverStyle
import org.firas.datetime.util.MathUtils
import org.firas.util.Locale
import kotlin.jvm.JvmStatic


/**
 * Fields and units specific to the ISO-8601 calendar system,
 * including quarter-of-year and week-based-year.
 *
 *
 * This class defines fields and units that are specific to the ISO calendar system.
 *
 * ### Quarter of year
 * The ISO-8601 standard is based on the standard civic 12 month year.
 * This is commonly divided into four quarters, often abbreviated as Q1, Q2, Q3 and Q4.
 *
 *
 * January, February and March are in Q1.
 * April, May and June are in Q2.
 * July, August and September are in Q3.
 * October, November and December are in Q4.
 *
 *
 * The complete date is expressed using three fields:
 *
 * * {@link #DAY_OF_QUARTER DAY_OF_QUARTER} - the day within the quarter, from 1 to 90, 91 or 92
 * * {@link #QUARTER_OF_YEAR QUARTER_OF_YEAR} - the quarter within the year, from 1 to 4
 * * {@link ChronoField#YEAR YEAR} - the standard ISO year
 *
 *
 * ### Week based years
 * The ISO-8601 standard was originally intended as a data interchange format,
 * defining a string format for dates and times. However, it also defines an
 * alternate way of expressing the date, based on the concept of week-based-year.
 *
 *
 * The date is expressed using three fields:
 *
 * * {@link ChronoField#DAY_OF_WEEK DAY_OF_WEEK} - the standard field defining the
 *  day-of-week from Monday (1) to Sunday (7)
 * * {@link #WEEK_OF_WEEK_BASED_YEAR} - the week within the week-based-year
 * * {@link #WEEK_BASED_YEAR WEEK_BASED_YEAR} - the week-based-year
 *
 *
 * The week-based-year itself is defined relative to the standard ISO proleptic year.
 * It differs from the standard year in that it always starts on a Monday.
 *
 *
 * The first week of a week-based-year is the first Monday-based week of the standard
 * ISO year that has at least 4 days in the new year.
 *
 * * If January 1st is Monday then week 1 starts on January 1st
 * * If January 1st is Tuesday then week 1 starts on December 31st of the previous standard year
 * * If January 1st is Wednesday then week 1 starts on December 30th of the previous standard year
 * * If January 1st is Thursday then week 1 starts on December 29th of the previous standard year
 * * If January 1st is Friday then week 1 starts on January 4th
 * * If January 1st is Saturday then week 1 starts on January 3rd
 * * If January 1st is Sunday then week 1 starts on January 2nd
 *
 *
 * There are 52 weeks in most week-based years, however on occasion there are 53 weeks.
 *
 *
 * For example:
 *
 * <table class=striped style="text-align: left">
 * <caption>Examples of Week based Years</caption>
 * <thead>
 * <tr><th scope="col">Date</th><th scope="col">Day-of-week</th><th scope="col">Field values</th></tr>
 * </thead>
 * <tbody>
 * <tr><th scope="row">2008-12-28</th><td>Sunday</td><td>Week 52 of week-based-year 2008</td></tr>
 * <tr><th scope="row">2008-12-29</th><td>Monday</td><td>Week 1 of week-based-year 2009</td></tr>
 * <tr><th scope="row">2008-12-31</th><td>Wednesday</td><td>Week 1 of week-based-year 2009</td></tr>
 * <tr><th scope="row">2009-01-01</th><td>Thursday</td><td>Week 1 of week-based-year 2009</td></tr>
 * <tr><th scope="row">2009-01-04</th><td>Sunday</td><td>Week 1 of week-based-year 2009</td></tr>
 * <tr><th scope="row">2009-01-05</th><td>Monday</td><td>Week 2 of week-based-year 2009</td></tr>
 * </tbody>
 * </table>
 *
 * @implSpec
 *
 *
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate from OpenJDK 11)
 */
class IsoFields private constructor() {

    init {
        throw AssertionError("Not instantiable")
    }

    companion object {
        /**
         * The field that represents the day-of-quarter.
         *
         *
         * This field allows the day-of-quarter value to be queried and set.
         * The day-of-quarter has values from 1 to 90 in Q1 of a standard year, from 1 to 91
         * in Q1 of a leap year, from 1 to 91 in Q2 and from 1 to 92 in Q3 and Q4.
         *
         *
         * The day-of-quarter can only be calculated if the day-of-year, month-of-year and year
         * are available.
         *
         *
         * When setting this field, the value is allowed to be partially lenient, taking any
         * value from 1 to 92. If the quarter has less than 92 days, then day 92, and
         * potentially day 91, is in the following quarter.
         *
         *
         * In the resolving phase of parsing, a date can be created from a year,
         * quarter-of-year and day-of-quarter.
         *
         *
         * In [strict mode][ResolverStyle.STRICT], all three fields are
         * validated against their range of valid values. The day-of-quarter field
         * is validated from 1 to 90, 91 or 92 depending on the year and quarter.
         *
         *
         * In [smart mode][ResolverStyle.SMART], all three fields are
         * validated against their range of valid values. The day-of-quarter field is
         * validated between 1 and 92, ignoring the actual range based on the year and quarter.
         * If the day-of-quarter exceeds the actual range by one day, then the resulting date
         * is one day later. If the day-of-quarter exceeds the actual range by two days,
         * then the resulting date is two days later.
         *
         *
         * In [lenient mode][ResolverStyle.LENIENT], only the year is validated
         * against the range of valid values. The resulting date is calculated equivalent to
         * the following three stage approach. First, create a date on the first of January
         * in the requested year. Then take the quarter-of-year, subtract one, and add the
         * amount in quarters to the date. Finally, take the day-of-quarter, subtract one,
         * and add the amount in days to the date.
         *
         *
         * This unit is an immutable and thread-safe singleton.
         */
        @JvmStatic
        val DAY_OF_QUARTER: TemporalField = Field.DAY_OF_QUARTER

        /**
         * The field that represents the quarter-of-year.
         *
         *
         * This field allows the quarter-of-year value to be queried and set.
         * The quarter-of-year has values from 1 to 4.
         *
         *
         * The quarter-of-year can only be calculated if the month-of-year is available.
         *
         *
         * In the resolving phase of parsing, a date can be created from a year,
         * quarter-of-year and day-of-quarter.
         * See [.DAY_OF_QUARTER] for details.
         *
         *
         * This unit is an immutable and thread-safe singleton.
         */
        @JvmStatic
        val QUARTER_OF_YEAR: TemporalField = Field.QUARTER_OF_YEAR

        /**
         * The field that represents the week-of-week-based-year.
         *
         *
         * This field allows the week of the week-based-year value to be queried and set.
         * The week-of-week-based-year has values from 1 to 52, or 53 if the
         * week-based-year has 53 weeks.
         *
         *
         * In the resolving phase of parsing, a date can be created from a
         * week-based-year, week-of-week-based-year and day-of-week.
         *
         *
         * In [strict mode][ResolverStyle.STRICT], all three fields are
         * validated against their range of valid values. The week-of-week-based-year
         * field is validated from 1 to 52 or 53 depending on the week-based-year.
         *
         *
         * In [smart mode][ResolverStyle.SMART], all three fields are
         * validated against their range of valid values. The week-of-week-based-year
         * field is validated between 1 and 53, ignoring the week-based-year.
         * If the week-of-week-based-year is 53, but the week-based-year only has
         * 52 weeks, then the resulting date is in week 1 of the following week-based-year.
         *
         *
         * In [lenient mode][ResolverStyle.LENIENT], only the week-based-year
         * is validated against the range of valid values. If the day-of-week is outside
         * the range 1 to 7, then the resulting date is adjusted by a suitable number of
         * weeks to reduce the day-of-week to the range 1 to 7. If the week-of-week-based-year
         * value is outside the range 1 to 52, then any excess weeks are added or subtracted
         * from the resulting date.
         *
         *
         * This unit is an immutable and thread-safe singleton.
         */
        @JvmStatic
        val WEEK_OF_WEEK_BASED_YEAR: TemporalField = Field.WEEK_OF_WEEK_BASED_YEAR

        /**
         * The field that represents the week-based-year.
         *
         *
         * This field allows the week-based-year value to be queried and set.
         *
         *
         * The field has a range that matches [LocalDate.MAX] and [LocalDate.MIN].
         *
         *
         * In the resolving phase of parsing, a date can be created from a
         * week-based-year, week-of-week-based-year and day-of-week.
         * See [.WEEK_OF_WEEK_BASED_YEAR] for details.
         *
         *
         * This unit is an immutable and thread-safe singleton.
         */
        @JvmStatic
        val WEEK_BASED_YEAR: TemporalField = Field.WEEK_BASED_YEAR

        /**
         * The unit that represents week-based-years for the purpose of addition and subtraction.
         *
         *
         * This allows a number of week-based-years to be added to, or subtracted from, a date.
         * The unit is equal to either 52 or 53 weeks.
         * The estimated duration of a week-based-year is the same as that of a standard ISO
         * year at `365.2425 Days`.
         *
         *
         * The rules for addition add the number of week-based-years to the existing value
         * for the week-based-year field. If the resulting week-based-year only has 52 weeks,
         * then the date will be in week 1 of the following week-based-year.
         *
         *
         * This unit is an immutable and thread-safe singleton.
         */
        @JvmStatic
        val WEEK_BASED_YEARS: TemporalUnit = Unit.WEEK_BASED_YEARS

        /**
         * Unit that represents the concept of a quarter-year.
         * For the ISO calendar system, it is equal to 3 months.
         * The estimated duration of a quarter-year is one quarter of `365.2425 Days`.
         *
         *
         * This unit is an immutable and thread-safe singleton.
         */
        @JvmStatic
        val QUARTER_YEARS: TemporalUnit = Unit.QUARTER_YEARS

        //-----------------------------------------------------------------------
        /**
         * Implementation of the field.
         */
        private enum class Field: TemporalField {
            DAY_OF_QUARTER {
                override fun getBaseUnit(): TemporalUnit {
                    return ChronoUnit.DAYS
                }

                override fun getRangeUnit(): TemporalUnit {
                    return QUARTER_YEARS
                }

                override fun range(): ValueRange {
                    return ValueRange.of(1, 90, 92)
                }

                override fun isSupportedBy(temporal: TemporalAccessor): Boolean {
                    return temporal.isSupported(ChronoField.DAY_OF_YEAR) && temporal.isSupported(ChronoField.MONTH_OF_YEAR) &&
                            temporal.isSupported(ChronoField.YEAR) && isIso(temporal)
                }

                override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange {
                    if (!isSupportedBy(temporal)) {
                        throw UnsupportedTemporalTypeException("Unsupported field: DayOfQuarter")
                    }
                    val qoy = temporal.getLong(QUARTER_OF_YEAR)
                    if (qoy == 1L) {
                        val year = temporal.getLong(ChronoField.YEAR)
                        return if (IsoChronology.INSTANCE.isLeapYear(year)) ValueRange.of(1, 91)
                        else ValueRange.of(1, 90)
                    } else if (qoy == 2L) {
                        return ValueRange.of(1, 91)
                    } else if (qoy == 3L || qoy == 4L) {
                        return ValueRange.of(1, 92)
                    } // else value not from 1 to 4, so drop through
                    return range()
                }

                override fun getFrom(temporal: TemporalAccessor): Long {
                    if (!isSupportedBy(temporal)) {
                        throw UnsupportedTemporalTypeException("Unsupported field: DayOfQuarter")
                    }
                    val doy = temporal.get(ChronoField.DAY_OF_YEAR)
                    val moy = temporal.get(ChronoField.MONTH_OF_YEAR)
                    val year = temporal.getLong(ChronoField.YEAR)
                    return (doy - QUARTER_DAYS[((moy - 1) / 3) + if (IsoChronology.INSTANCE.isLeapYear(year)) 4 else 0]).toLong()
                }

                override fun <R : Temporal> adjustInto(temporal: R, newValue: Long): R {
                    // calls getFrom() to check if supported
                    val curValue = getFrom(temporal)
                    range().checkValidValue(newValue, this)  // leniently check from 1 to 92 TODO: check
                    return temporal.with(
                        ChronoField.DAY_OF_YEAR,
                        temporal.getLong(ChronoField.DAY_OF_YEAR) + (newValue - curValue)
                    ) as R
                }

                override fun resolve(
                    fieldValues: MutableMap<TemporalField, Long>,
                    partialTemporal: TemporalAccessor,
                    resolverStyle: ResolverStyle
                ): TemporalAccessor? {
                    val yearLong = fieldValues.get(ChronoField.YEAR)
                    val qoyLong = fieldValues.get(QUARTER_OF_YEAR)
                    if (yearLong == null || qoyLong == null) {
                        return null
                    }
                    val y = ChronoField.YEAR.checkValidIntValue(yearLong)  // always validate
                    var doq = fieldValues.get(DAY_OF_QUARTER)!!
                    ensureIso(partialTemporal)
                    var date: LocalDate
                    if (resolverStyle == ResolverStyle.LENIENT) {
                        date = LocalDate.of(y, 1, 1).plusMonths(
                            MathUtils.multiplyExact(MathUtils.subtractExact(qoyLong, 1), 3)
                        )
                        doq = MathUtils.subtractExact(doq, 1)
                    } else {
                        val qoy = QUARTER_OF_YEAR.range().checkValidIntValue(qoyLong, QUARTER_OF_YEAR)  // validated
                        date = LocalDate.of(y, ((qoy - 1) * 3) + 1, 1)
                        if (doq < 1 || doq > 90) {
                            if (resolverStyle == ResolverStyle.STRICT) {
                                rangeRefinedBy(date).checkValidValue(doq, this)  // only allow exact range
                            } else {  // SMART
                                range().checkValidValue(doq, this)  // allow 1-92 rolling into next quarter
                            }
                        }
                        doq--
                    }
                    fieldValues.remove(this)
                    fieldValues.remove(ChronoField.YEAR)
                    fieldValues.remove(QUARTER_OF_YEAR)
                    return date.plusDays(doq)
                }

                override fun toString(): String {
                    return "DayOfQuarter"
                }
            },
            QUARTER_OF_YEAR {
                override fun getBaseUnit(): TemporalUnit {
                    return QUARTER_YEARS
                }

                override fun getRangeUnit(): TemporalUnit {
                    return ChronoUnit.YEARS
                }

                override fun range(): ValueRange {
                    return ValueRange.of(1, 4)
                }

                override fun isSupportedBy(temporal: TemporalAccessor): Boolean {
                    return temporal.isSupported(ChronoField.MONTH_OF_YEAR) && isIso(temporal)
                }

                override fun getFrom(temporal: TemporalAccessor): Long {
                    if (!isSupportedBy(temporal)) {
                        throw UnsupportedTemporalTypeException("Unsupported field: QuarterOfYear")
                    }
                    val moy = temporal.getLong(ChronoField.MONTH_OF_YEAR)
                    return ((moy + 2) / 3)
                }

                override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange {
                    if (!isSupportedBy(temporal)) {
                        throw UnsupportedTemporalTypeException("Unsupported field: QuarterOfYear")
                    }
                    return super.rangeRefinedBy(temporal)
                }

                override fun <R : Temporal> adjustInto(temporal: R, newValue: Long): R {
                    // calls getFrom() to check if supported
                    val curValue = getFrom(temporal)
                    range().checkValidValue(newValue, this)  // strictly check from 1 to 4
                    return temporal.with(
                        ChronoField.MONTH_OF_YEAR,
                        temporal.getLong(ChronoField.MONTH_OF_YEAR) + (newValue - curValue) * 3
                    ) as R
                }

                override fun toString(): String {
                    return "QuarterOfYear"
                }
            },
            WEEK_OF_WEEK_BASED_YEAR {
                /*
                fun getDisplayName(locale: Locale) {
                    LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased()
                        .getLocaleResources(
                            CalendarDataUtility
                                .findRegionOverride(locale))
                    ResourceBundle rb = lr.getJavaTimeFormatData()
                    return rb.containsKey("field.week") ? rb.getString("field.week") : toString()
                }
                */
                override fun getBaseUnit(): TemporalUnit {
                    return ChronoUnit.WEEKS
                }

                override fun getRangeUnit(): TemporalUnit {
                    return WEEK_BASED_YEARS
                }

                override fun range(): ValueRange {
                    return ValueRange.of(1, 52, 53)
                }

                override fun isSupportedBy(temporal: TemporalAccessor): Boolean {
                    return temporal.isSupported(ChronoField.EPOCH_DAY) && isIso(temporal)
                }

                override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange {
                    if (!isSupportedBy(temporal)) {
                        throw UnsupportedTemporalTypeException("Unsupported field: WeekOfWeekBasedYear")
                    }
                    return getWeekRange(LocalDate.from(temporal))
                }

                override fun getFrom(temporal: TemporalAccessor): Long {
                    if (!isSupportedBy(temporal)) {
                        throw UnsupportedTemporalTypeException("Unsupported field: WeekOfWeekBasedYear")
                    }
                    return getWeek(LocalDate.from(temporal)).toLong()
                }

                override fun <R : Temporal> adjustInto(temporal: R, newValue: Long): R {
                    // calls getFrom() to check if supported
                    range().checkValidValue(newValue, this)  // lenient range
                    return temporal.plus(
                        MathUtils.subtractExact(newValue, getFrom(temporal)),
                        ChronoUnit.WEEKS
                    ) as R
                }

                override fun resolve(
                    fieldValues: MutableMap<TemporalField, Long>,
                    partialTemporal: TemporalAccessor,
                    resolverStyle: ResolverStyle
                ): TemporalAccessor? {
                    val wbyLong = fieldValues.get(WEEK_BASED_YEAR)
                    val dowLong = fieldValues.get(ChronoField.DAY_OF_WEEK)
                    if (wbyLong == null || dowLong == null) {
                        return null
                    }
                    val wby = WEEK_BASED_YEAR.range().checkValidIntValue(wbyLong, WEEK_BASED_YEAR)  // always validate
                    val wowby = fieldValues.get(WEEK_OF_WEEK_BASED_YEAR)!!
                    ensureIso(partialTemporal)
                    var date = LocalDate.of(wby, 1, 4)
                    if (resolverStyle == ResolverStyle.LENIENT) {
                        var dow = dowLong  // unvalidated
                        if (dow > 7) {
                            date = date.plusWeeks((dow - 1) / 7)
                            dow = ((dow - 1) % 7) + 1
                        } else if (dow < 1) {
                            date = date.plusWeeks(MathUtils.subtractExact(dow, 7) / 7)
                            dow = ((dow + 6) % 7) + 1
                        }
                        date = date.plusWeeks(MathUtils.subtractExact(wowby, 1)).with(ChronoField.DAY_OF_WEEK, dow)
                    } else {
                        val dow = ChronoField.DAY_OF_WEEK.checkValidIntValue(dowLong)  // validated
                        if (wowby < 1 || wowby > 52) {
                            if (resolverStyle == ResolverStyle.STRICT) {
                                getWeekRange(date).checkValidValue(wowby, this)  // only allow exact range
                            } else {  // SMART
                                range().checkValidValue(wowby, this)  // allow 1-53 rolling into next year
                            }
                        }
                        date = date.plusWeeks(wowby - 1).with(ChronoField.DAY_OF_WEEK, dow.toLong())
                    }
                    fieldValues.remove(this)
                    fieldValues.remove(WEEK_BASED_YEAR)
                    fieldValues.remove(ChronoField.DAY_OF_WEEK)
                    return date
                }

                override fun toString(): String {
                    return "WeekOfWeekBasedYear"
                }
            },
            WEEK_BASED_YEAR {
                override fun getBaseUnit(): TemporalUnit {
                    return WEEK_BASED_YEARS
                }

                override fun getRangeUnit(): TemporalUnit {
                    return ChronoUnit.FOREVER
                }

                override fun range(): ValueRange {
                    return ChronoField.YEAR.range()
                }

                override fun isSupportedBy(temporal: TemporalAccessor): Boolean {
                    return temporal.isSupported(ChronoField.EPOCH_DAY) && isIso(temporal)
                }

                override fun getFrom(temporal: TemporalAccessor): Long {
                    if (!isSupportedBy(temporal)) {
                        throw UnsupportedTemporalTypeException("Unsupported field: WeekBasedYear")
                    }
                    return getWeekBasedYear(LocalDate.from(temporal)).toLong()
                }

                override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange {
                    if (!isSupportedBy(temporal)) {
                        throw UnsupportedTemporalTypeException("Unsupported field: WeekBasedYear")
                    }
                    return super.rangeRefinedBy(temporal)
                }

                override fun <R : Temporal> adjustInto(temporal: R, newValue: Long): R {
                    if (!isSupportedBy(temporal)) {
                        throw UnsupportedTemporalTypeException("Unsupported field: WeekBasedYear")
                    }
                    val newWby = range().checkValidIntValue(newValue, WEEK_BASED_YEAR)  // strict check
                    val date = LocalDate.from(temporal)
                    val dow = date.get(ChronoField.DAY_OF_WEEK)
                    var week = getWeek(date)
                    if (week == 53 && getWeekRange(newWby) == 52) {
                        week = 52
                    }
                    var resolved = LocalDate.of(newWby, 1, 4)  // 4th is guaranteed to be in week one
                    val days = (dow - resolved.get(ChronoField.DAY_OF_WEEK)) + ((week - 1) * 7)
                    resolved = resolved.plusDays(days.toLong())
                    return temporal.with(resolved) as R
                }

                override fun toString(): String {
                    return "WeekBasedYear"
                }
            };

            override fun isDateBased(): Boolean {
                return true
            }

            override fun isTimeBased(): Boolean {
                return false
            }

            override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange {
                return range()
            }

            companion object {
                //-------------------------------------------------------------------------
                @JvmStatic
                private val QUARTER_DAYS = intArrayOf(0, 90, 181, 273, 0, 91, 182, 274)


                @JvmStatic
                private fun ensureIso(temporal: TemporalAccessor) {
                    if (!isIso(temporal)) {
                        throw DateTimeException("Resolve requires IsoChronology")
                    }
                }

                @JvmStatic
                private fun getWeekRange(date: LocalDate): ValueRange {
                    val wby = getWeekBasedYear(date)
                    return ValueRange.of(1, getWeekRange(wby).toLong())
                }

                @JvmStatic
                private fun getWeekRange(wby: Int): Int {
                    val date = LocalDate.of(wby, 1, 1)
                    // 53 weeks if standard year starts on Thursday, or Wed in a leap year
                    return if (date.getDayOfWeek() == DayOfWeek.THURSDAY ||
                            date.getDayOfWeek() == DayOfWeek.WEDNESDAY && date.isLeapYear()) 53
                    else 52
                }

                @JvmStatic
                private fun getWeek(date: LocalDate): Int {
                    val dow0 = date.getDayOfWeek().ordinal
                    val doy0 = date.getDayOfYear() - 1
                    val doyThu0 = doy0 + (3 - dow0)  // adjust to mid-week Thursday (which is 3 indexed from zero)
                    val alignedWeek = doyThu0 / 7
                    val firstThuDoy0 = doyThu0 - alignedWeek * 7
                    var firstMonDoy0 = firstThuDoy0 - 3
                    if (firstMonDoy0 < -3) {
                        firstMonDoy0 += 7
                    }
                    if (doy0 < firstMonDoy0) {
                        return getWeekRange(date.withDayOfYear(180).minusYears(1)).getMaximum() as Int
                    }
                    var week = (doy0 - firstMonDoy0) / 7 + 1
                    if (week == 53) {
                        if (!(firstMonDoy0 == -3 || firstMonDoy0 == -2 && date.isLeapYear())) {
                            week = 1
                        }
                    }
                    return week
                }

                @JvmStatic
                private fun getWeekBasedYear(date: LocalDate): Int {
                    var year = date.year
                    var doy = date.getDayOfYear()
                    if (doy <= 3) {
                        val dow = date.getDayOfWeek().ordinal
                        if (doy - dow < -2) {
                            year--
                        }
                    } else if (doy >= 363) {
                        val dow = date.getDayOfWeek().ordinal
                        doy = doy - 363 - if (date.isLeapYear()) 1 else 0
                        if (doy - dow >= 0) {
                            year++
                        }
                    }
                    return year
                }
            } // Field.Companion
        } // enum Field

        /**
         * Implementation of the unit.
         */
        private enum class Unit(
            private val _name: String,
            private val duration: Duration
        ): TemporalUnit {
            /**
             * Unit that represents the concept of a week-based-year.
             */
            WEEK_BASED_YEARS("WeekBasedYears", Duration.ofSeconds(31556952L)),

            /**
             * Unit that represents the concept of a quarter-year.
             */
            QUARTER_YEARS("QuarterYears", Duration.ofSeconds(31556952L / 4));

            override fun getDuration(): Duration {
                return this.duration
            }

            override fun isDurationEstimated(): Boolean {
                return true
            }

            override fun isDateBased(): Boolean {
                return true
            }

            override fun isTimeBased(): Boolean {
                return false
            }

            override fun isSupportedBy(temporal: Temporal): Boolean {
                return temporal.isSupported(ChronoField.EPOCH_DAY) && isIso(temporal)
            }

            override fun <R : Temporal> addTo(temporal: R, amount: Long): R {
                return when (this) {
                    WEEK_BASED_YEARS -> temporal.with(WEEK_BASED_YEAR,
                        MathUtils.addExact(temporal[WEEK_BASED_YEAR].toLong(), amount)) as R
                    QUARTER_YEARS -> temporal.plus(amount / 4, ChronoUnit.YEARS)
                        .plus(amount % 4 * 3, ChronoUnit.MONTHS) as R
                    else -> throw IllegalStateException("Unreachable")
                }
            }

            override fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long {
                if (temporal1Inclusive::class != temporal2Exclusive::class) {
                    return temporal1Inclusive.until(temporal2Exclusive, this)
                }
                return when (this) {
                    WEEK_BASED_YEARS -> MathUtils.subtractExact(
                        temporal2Exclusive.getLong(WEEK_BASED_YEAR),
                        temporal1Inclusive.getLong(WEEK_BASED_YEAR)
                    )
                    QUARTER_YEARS -> temporal1Inclusive.until(temporal2Exclusive, ChronoUnit.MONTHS) / 3
                    else -> throw IllegalStateException("Unreachable")
                }
            }

            override fun toString(): String {
                return this._name
            }
        } // enum Unit

        internal fun isIso(temporal: TemporalAccessor): Boolean {
            return Chronology.from(temporal) == IsoChronology.INSTANCE
        }
    } // companion object
}