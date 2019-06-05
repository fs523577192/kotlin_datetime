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
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
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

import org.firas.datetime.chrono.Chronology
import org.firas.datetime.chrono.IsoChronology
import org.firas.datetime.temporal.TemporalField
import org.firas.datetime.util.Locale
import org.firas.datetime.zone.ZoneId
import kotlin.js.JsName

/**
 * Context object used during date and time parsing.
 *
 *
 * This class represents the current state of the parse.
 * It has the ability to store and retrieve the parsed values and manage optional segments.
 * It also provides key information to the parsing methods.
 *
 *
 * Once parsing is complete, the {@link #toUnresolved()} is used to obtain the unresolved
 * result data. The {@link #toResolved()} is used to obtain the resolved result.
 *
 * @implSpec
 * This class is a mutable context intended for use from a single thread.
 * Usage of the class is thread-safe within standard parsing as a new instance of this class
 * is automatically created for each parse and parsing is single-threaded
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
internal class DateTimeParseContext internal constructor(
    private val formatter: DateTimeFormatter,

    /**
     * Whether to parse using case sensitively.
     */
    internal var caseSensitive: Boolean = true,

    /**
     * Whether to parse using strict rules.
     */
    internal var strict: Boolean = true
) {

    /**
     * The list of parsed data.
     */
    private val parsed: MutableList<Parsed> = ArrayList()

    /**
     * List of Consumers&lt;Chronology&gt; to be notified if the Chronology changes.
     */
    private var chronoListeners: MutableList<(Chronology) -> Unit>? = null

    /**
     * Creates a copy of this context.
     * This retains the case sensitive and strict flags.
     */
    internal fun copy(): DateTimeParseContext {
        return DateTimeParseContext(this.formatter, this.caseSensitive, this.strict)
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the locale.
     *
     *
     * This locale is used to control localization in the parse except
     * where localization is controlled by the DecimalStyle.
     *
     * @return the locale, not null
     */
    @JsName("getLocale")
    internal fun getLocale(): Locale {
        return formatter.locale
    }

    /**
     * Gets the DecimalStyle.
     *
     *
     * The DecimalStyle controls the numeric parsing.
     *
     * @return the DecimalStyle, not null
     */
    @JsName("getDecimalStyle")
    internal fun getDecimalStyle(): DecimalStyle {
        return formatter.decimalStyle
    }

    /**
     * Gets the effective chronology during parsing.
     *
     * @return the effective parsing chronology, not null
     */
    @JsName("getEffectiveChronology")
    internal fun getEffectiveChronology(): Chronology {
        var chrono = currentParsed().chrono
        if (chrono == null) {
            chrono = formatter.chrono
            if (chrono == null) {
                chrono = IsoChronology.INSTANCE
            }
        }
        return chrono
    }

    //-----------------------------------------------------------------------
    /**
     * Helper to compare two `CharSequence` instances.
     * This uses [.isCaseSensitive].
     *
     * @param cs1  the first character sequence, not null
     * @param offset1  the offset into the first sequence, valid
     * @param cs2  the second character sequence, not null
     * @param offset2  the offset into the second sequence, valid
     * @param length  the length to check, valid
     * @return true if equal
     */
    @JsName("subSequenceEquals")
    internal fun subSequenceEquals(cs1: CharSequence, offset1: Int,
                                   cs2: CharSequence, offset2: Int, length: Int): Boolean {
        if (offset1 + length > cs1.length || offset2 + length > cs2.length) {
            return false
        }
        if (this.caseSensitive) {
            for (i in 0 until length) {
                val ch1 = cs1[offset1 + i]
                val ch2 = cs2[offset2 + i]
                if (ch1 != ch2) {
                    return false
                }
            }
        } else {
            for (i in 0 until length) {
                val ch1 = cs1[offset1 + i]
                val ch2 = cs2[offset2 + i]
                if (!ch1.equals(ch2, true)) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Helper to compare two `char`.
     * This uses [.isCaseSensitive].
     *
     * @param ch1  the first character
     * @param ch2  the second character
     * @return true if equal
     */
    @JsName("charEquals")
    internal fun charEquals(ch1: Char, ch2: Char): Boolean {
        return ch1.equals(ch2, !this.caseSensitive)
    }

    //-----------------------------------------------------------------------
    /**
     * Starts the parsing of an optional segment of the input.
     */
    @JsName("startOptional")
    internal fun startOptional() {
        parsed.add(currentParsed().copy())
    }

    /**
     * Ends the parsing of an optional segment of the input.
     *
     * @param successful  whether the optional segment was successfully parsed
     */
    @JsName("endOptional")
    internal fun endOptional(successful: Boolean) {
        if (successful) {
            this.parsed.removeAt(this.parsed.size - 2)
        } else {
            this.parsed.removeAt(this.parsed.size - 1)
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the currently active temporal objects.
     *
     * @return the current temporal objects, not null
     */
    private fun currentParsed(): Parsed {
        return this.parsed[this.parsed.size - 1]
    }

    /**
     * Gets the unresolved result of the parse.
     *
     * @return the result of the parse, not null
     */
    @JsName("toUnresolved")
    internal fun toUnresolved(): Parsed {
        return currentParsed()
    }

    /**
     * Adds a Consumer&lt;Chronology&gt; to the list of listeners to be notified
     * if the Chronology changes.
     * @param listener a Consumer&lt;Chronology&gt; to be called when Chronology changes
     */
    @JsName("addChronoChangedListener")
    fun addChronoChangedListener(listener: (Chronology) -> Unit) {
        val _listeners: MutableList<(Chronology) -> Unit> = this.chronoListeners ?: ArrayList()
        _listeners.add(listener)
        this.chronoListeners = _listeners
    }

    /**
     * Stores the parsed zone.
     *
     * This stores the zone that has been parsed.
     * No validation is performed other than ensuring it is not null.
     *
     * @param zone  the parsed zone, not null
     */
    @JsName("setParsed")
    internal fun setParsed(zone: ZoneId) {
        currentParsed().zone = zone
    }

    /**
     * Stores the parsed leap second.
     */
    @JsName("setParsedLeapSecond")
    internal fun setParsedLeapSecond() {
        currentParsed().leapSecond = true
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the first value that was parsed for the specified field.
     *
     *
     * This searches the results of the parse, returning the first value found
     * for the specified field. No attempt is made to derive a value.
     * The field may have an out of range value.
     * For example, the day-of-month might be set to 50, or the hour to 1000.
     *
     * @param field  the field to query from the map, null returns null
     * @return the value mapped to the specified field, null if field was not parsed
     */
    @JsName("getParsed")
    internal fun getParsed(field: TemporalField): Long? {
        return currentParsed().fieldValues.get(field)
    }

    /**
     * Stores the parsed field.
     *
     *
     * This stores a field-value pair that has been parsed.
     * The value stored may be out of range for the field - no checks are performed.
     *
     * @param field  the field to set in the field-value map, not null
     * @param value  the value to set in the field-value map
     * @param errorPos  the position of the field being parsed
     * @param successPos  the position after the field being parsed
     * @return the new position
     */
    @JsName("setParsedField")
    internal fun setParsedField(field: TemporalField, value: Long, errorPos: Int, successPos: Int): Int {
        val old = currentParsed().fieldValues.put(field, value)
        return if (old != null && old.toLong() != value) errorPos.inv() else successPos
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string version of the context for debugging.
     *
     * @return a string representation of the context data, not null
     */
    override fun toString(): String {
        return currentParsed().toString()
    }
}