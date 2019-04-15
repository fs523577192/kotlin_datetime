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
 * Copyright (c) 2008-2013, Stephen Colebourne & Michael Nascimento Santos
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
import org.firas.datetime.LocalTime
import org.firas.datetime.chrono.ChronoLocalDate
import org.firas.datetime.chrono.Chronology
import org.firas.datetime.temporal.TemporalField
import org.firas.datetime.zone.ZoneId

/**
 * A store of parsed data.
 * <p>
 * This class is used during parsing to collect the data. Part of the parsing process
 * involves handling optional blocks and multiple copies of the data get created to
 * support the necessary backtracking.
 * <p>
 * Once parsing is completed, this class can be used as the resultant {@code TemporalAccessor}.
 * In most cases, it is only exposed once the fields have been resolved.
 *
 * @implSpec
 * This class is a mutable context intended for use from a single thread.
 * Usage of the class is thread-safe within standard parsing as a new instance of this class
 * is automatically created for each parse and parsing is single-threaded
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
internal class Parsed internal constructor() {
    // some fields are accessed using package scope from DateTimeParseContext

    /**
     * The parsed fields.
     */
    internal val fieldValues: MutableMap<TemporalField, Long> = HashMap()

    /**
     * The parsed zone.
     */
    var zone: ZoneId? = null

    /**
     * The parsed chronology.
     */
    var chrono: Chronology? = null

    /**
     * Whether a leap-second is parsed.
     */
    var leapSecond: Boolean = false

    /**
     * The resolver style to use.
    private val resolverStyle: ResolverStyle? = null
     */

    /**
     * The resolved date.
     */
    private var date: ChronoLocalDate? = null

    /**
     * The resolved time.
     */
    private var time: LocalTime? = null

    /**
     * The excess period from time-only parsing.
    var excessDays = Period.ZERO
     */

    /**
     * Creates a copy.
     */
    fun copy(): Parsed {
        // only copy fields used in parsing stage
        val cloned = Parsed()
        // cloned.fieldValues.putAll(this.fieldValues)
        cloned.zone = this.zone
        cloned.chrono = this.chrono
        cloned.leapSecond = this.leapSecond
        return cloned
    }

    private fun updateCheckConflict(cld: ChronoLocalDate?) {
        if (date != null) {
            if (cld != null && date != cld) {
                throw DateTimeException("Conflict found: Fields resolved to two different dates: $date $cld")
            }
        } else if (cld != null) {
            if (chrono != cld.getChronology()) {
                throw DateTimeException("ChronoLocalDate must use the effective parsed chronology: $chrono")
            }
            date = cld
        }
    }
}