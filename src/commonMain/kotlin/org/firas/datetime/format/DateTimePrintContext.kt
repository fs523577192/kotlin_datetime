/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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
package org.firas.datetime.format

import org.firas.datetime.DateTimeException
import org.firas.datetime.Instant
import org.firas.datetime.chrono.ChronoLocalDate
import org.firas.datetime.chrono.Chronology
import org.firas.datetime.chrono.IsoChronology
import org.firas.datetime.temporal.*
import org.firas.datetime.util.Locale
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset
import kotlin.js.JsName
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * Context object used during date and time printing.
 *
 * This class provides a single wrapper to items used in the format.
 *
 * @implSpec
 * This class is a mutable context intended for use from a single thread.
 * Usage of the class is thread-safe within standard printing as the framework creates
 * a new instance of the class for each format and printing is single-threaded.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class DateTimePrintContext private constructor(
    /**
     * The formatter, not null.
     */
    private var formatter: DateTimeFormatter,

    /**
     * The temporal being output.
     */
    val temporal: TemporalAccessor? = null
) {

    /**
     * Whether the current formatter is optional.
     */
    private var optional: Int = 0

    /**
     * Creates a new instance of the context.
     *
     * @param temporal  the temporal object being output, not null
     * @param formatter  the formatter controlling the format, not null
     */
    @JsName("DateTimePrintContext_init")
    internal constructor(temporal: TemporalAccessor, formatter: DateTimeFormatter):
            this(formatter, adjust(temporal, formatter))

    companion object {
        @JsName("adjust")
        @JvmStatic
        fun adjust(temporal: TemporalAccessor, formatter: DateTimeFormatter): TemporalAccessor {
            // normal case first (early return is an optimization)
            var overrideChrono = formatter.chrono
            var overrideZone = formatter.zone
            if (overrideChrono == null && overrideZone == null) {
                return temporal
            }

            // ensure minimal change (early return is an optimization)
            val temporalChrono = temporal.query(TemporalQueries.CHRONO)
            val temporalZone = temporal.query(TemporalQueries.ZONE_ID)
            if (overrideChrono == temporalChrono) {
                overrideChrono = null
            }
            if (overrideZone == temporalZone) {
                overrideZone = null
            }
            if (overrideChrono == null && overrideZone == null) {
                return temporal
            }

            // make adjustment
            val effectiveChrono = if (overrideChrono != null) overrideChrono else temporalChrono
            if (overrideZone != null) {
                // if have zone and instant, calculation is simple, defaulting chrono if necessary
                if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
                    val chrono = effectiveChrono ?: IsoChronology.INSTANCE
                    return chrono.zonedDateTime(Instant.from(temporal), overrideZone)
                }
                // block changing zone on OffsetTime, and similar problem cases
                if (overrideZone.normalized() is ZoneOffset && temporal.isSupported(ChronoField.OFFSET_SECONDS) &&
                    temporal.get(ChronoField.OFFSET_SECONDS) != overrideZone.getRules()!!.getOffset(Instant.EPOCH)!!.totalSeconds
                ) {
                    throw DateTimeException("Unable to apply override zone '$overrideZone' because the" +
                                " temporal object being formatted has a different offset but" +
                                " does not represent an instant: $temporal")
                }
            }
            val effectiveZone = overrideZone ?: temporalZone
            val effectiveDate: ChronoLocalDate? =
                if (overrideChrono != null) {
                    if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
                        effectiveChrono!!.date(temporal)
                    } else {
                        // check for date fields other than epoch-day, ignoring case of converting null to ISO
                        if (!(overrideChrono == IsoChronology.INSTANCE && temporalChrono == null)) {
                            for (f in ChronoField.values()) {
                                if (f.isDateBased() && temporal.isSupported(f)) {
                                    throw DateTimeException(
                                        "Unable to apply override chronology '$overrideChrono" +
                                                "' because the temporal object being formatted contains date fields but" +
                                                " does not represent a whole date: $temporal"
                                    )
                                }
                            }
                        }
                        null
                    }
                } else {
                    null
                }

            // combine available data
            // this is a non-standard temporal that is almost a pure delegate
            // this better handles map-like underlying temporal instances
            return DateTimePrintContext_TemporalAccessor(temporal,
                    effectiveDate, effectiveChrono!!, effectiveZone!!)
        } // fun adjust(temporal: TemporalAccessor, formatter: DateTimeFormatter)

        private class DateTimePrintContext_TemporalAccessor internal constructor(
            internal val temporal: TemporalAccessor,
            internal val effectiveDate: ChronoLocalDate?,
            internal val effectiveChrono: Chronology,
            internal val effectiveZone: ZoneId
        ) : TemporalAccessor {
            override fun isSupported(field: TemporalField): Boolean {
                if (effectiveDate != null && field.isDateBased()) {
                    return effectiveDate.isSupported(field)
                }
                return temporal.isSupported(field)
            }

            override fun range(field: TemporalField): ValueRange {
                if (effectiveDate != null && field.isDateBased()) {
                    return effectiveDate.range(field)
                }
                return temporal.range(field)
            }

            override fun getLong(field: TemporalField): Long {
                if (effectiveDate != null && field.isDateBased()) {
                    return effectiveDate.getLong(field)
                }
                return temporal.getLong(field)
            }

            override fun <R> query(query: TemporalQuery<R>): R? {
                if (query == TemporalQueries.CHRONO) {
                    return effectiveChrono as R
                }
                if (query == TemporalQueries.ZONE_ID) {
                    return effectiveZone as R
                }
                if (query == TemporalQueries.PRECISION) {
                    return temporal.query(query)
                }
                return query.queryFrom(this)
            }

            override fun toString(): String {
                return temporal.toString() +
                        (if (effectiveChrono != null) " with chronology $effectiveChrono" else "") +
                        if (effectiveZone != null) " with zone $effectiveZone" else ""
            }
        }
    } // companion object

    /**
     * Gets the locale.
     *
     *
     * This locale is used to control localization in the format output except
     * where localization is controlled by the DecimalStyle.
     *
     * @return the locale, not null
     */
    @JsName("getLocale")
    fun getLocale(): Locale {
        return formatter.locale
    }

    /**
     * Gets the DecimalStyle.
     *
     *
     * The DecimalStyle controls the localization of numeric output.
     *
     * @return the DecimalStyle, not null
     */
    @JsName("getDecimalStyle")
    fun getDecimalStyle(): DecimalStyle {
        return formatter.decimalStyle
    }

    //-----------------------------------------------------------------------
    /**
     * Starts the printing of an optional segment of the input.
     */
    @JsName("startOptional")
    fun startOptional() {
        this.optional += 1
    }

    /**
     * Ends the printing of an optional segment of the input.
     */
    @JsName("endOptional")
    fun endOptional() {
        this.optional -= 1
    }

    /**
     * Gets a value using a query.
     *
     * @param query  the query to use, not null
     * @return the result, null if not found and optional is true
     * @throws DateTimeException if the type is not available and the section is not optional
     */
    @JsName("getValueWithQuery")
    fun <R> getValue(query: TemporalQuery<R>): R? {
        val result = this.temporal!!.query(query)
        if (result == null && this.optional == 0) {
            throw DateTimeException(
                "Unable to extract $query from temporal " + this.temporal
            )
        }
        return result
    }

    /**
     * Gets the value of the specified field.
     *
     *
     * This will return the value for the specified field.
     *
     * @param field  the field to find, not null
     * @return the value, null if not found and optional is true
     * @throws DateTimeException if the field is not available and the section is not optional
     */
    @JsName("getValueWithField")
    fun getValue(field: TemporalField): Long? {
        return if (this.optional > 0 && !this.temporal!!.isSupported(field)) null
                else this.temporal!!.getLong(field)
    }

    //-----------------------------------------------------------------------
    override fun toString(): String {
        return "$temporal"
    }
}