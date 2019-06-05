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
package org.firas.datetime.temporal

import org.firas.datetime.LocalDate
import org.firas.datetime.LocalTime
import org.firas.datetime.chrono.Chronology
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset
import kotlin.jvm.JvmStatic

/**
 * Common implementations of `TemporalQuery`.
 *
 * This class provides common implementations of {@link TemporalQuery}.
 * These are defined here as they must be constants, and the definition
 * of lambdas does not guarantee that. By assigning them once here,
 * they become 'normal' Java constants.
 *
 * Queries are a key tool for extracting information from temporal objects.
 * They exist to externalize the process of querying, permitting different
 * approaches, as per the strategy design pattern.
 * Examples might be a query that checks if the date is the day before February 29th
 * in a leap year, or calculates the number of days to your next birthday.
 *
 * The {@link TemporalField} interface provides another mechanism for querying
 * temporal objects. That interface is limited to returning a `long`.
 * By contrast, queries can return any type.
 *
 * There are two equivalent ways of using a `TemporalQuery`.
 * The first is to invoke the method on this interface directly.
 * The second is to use {@link TemporalAccessor#query(TemporalQuery)}:
 * ```
 *   // these two lines are equivalent, but the second approach is recommended
 *   temporal = thisQuery.queryFrom(temporal);
 *   temporal = temporal.query(thisQuery);
 * ```
 * It is recommended to use the second approach, `query(TemporalQuery)`,
 * as it is a lot clearer to read in code.
 *
 * The most common implementations are method references, such as
 * `LocalDate::from` and `ZoneId::from`.
 * Additional common queries are provided to return:
 *
 * * a Chronology,
 * * a LocalDate,
 * * a LocalTime,
 * * a ZoneOffset,
 * * a precision,
 * * a zone, or
 * * a zoneId.
 *
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class TemporalQueries private constructor() {
    // note that it is vital that each method supplies a constant, not a
    // calculated value, as they will be checked for using ==
    // it is also vital that each constant is different (due to the == checking)
    // as such, alterations to this code must be done with care

    companion object {
        /**
         * A strict query for the `ZoneId`.
         */
        @JvmStatic
        val ZONE_ID: TemporalQuery<ZoneId?> = object : TemporalQuery<ZoneId?> {
            override fun queryFrom(temporal: TemporalAccessor): ZoneId? {
                return temporal.query(TemporalQueries.ZONE_ID)
            }

            override fun toString(): String {
                return "ZoneId"
            }
        }

        /**
         * A query for the `Chronology`.
         */
        @JvmStatic
        val CHRONO: TemporalQuery<Chronology?> = object : TemporalQuery<Chronology?> {
            override fun queryFrom(temporal: TemporalAccessor): Chronology? {
                return temporal.query(TemporalQueries.CHRONO)
            }

            override fun toString(): String {
                return "Chronology"
            }
        }


        /**
         * A query for the smallest supported unit.
         */
        @JvmStatic
        val PRECISION: TemporalQuery<TemporalUnit?> = object : TemporalQuery<TemporalUnit?> {
            override fun queryFrom(temporal: TemporalAccessor): TemporalUnit? {
                return temporal.query(TemporalQueries.PRECISION)
            }

            override fun toString(): String {
                return "Precision"
            }
        }

        //-----------------------------------------------------------------------
        /**
         * A query for `ZoneOffset` returning null if not found.
         */
        @JvmStatic
        val OFFSET: TemporalQuery<ZoneOffset?> = object : TemporalQuery<ZoneOffset?> {
            override fun queryFrom(temporal: TemporalAccessor): ZoneOffset? {
                return if (temporal.isSupported(ChronoField.OFFSET_SECONDS)) {
                    ZoneOffset.ofTotalSeconds(temporal[ChronoField.OFFSET_SECONDS])
                } else null
            }

            override fun toString(): String {
                return "ZoneOffset"
            }
        }

        /**
         * A lenient query for the `ZoneId`, falling back to the `ZoneOffset`.
         */
        @JvmStatic
        val ZONE: TemporalQuery<ZoneId?> = object : TemporalQuery<ZoneId?> {
            override fun queryFrom(temporal: TemporalAccessor): ZoneId? {
                val zone = temporal.query(ZONE_ID)
                return if (zone != null) zone else temporal.query(OFFSET)
            }

            override fun toString(): String {
                return "Zone"
            }
        }

        /**
         * A query for `LocalDate` returning null if not found.
         */
        @JvmStatic
        val LOCAL_DATE: TemporalQuery<LocalDate?> = object : TemporalQuery<LocalDate?> {
            override fun queryFrom(temporal: TemporalAccessor): LocalDate? {
                return if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
                    LocalDate.ofEpochDay(temporal.getLong(ChronoField.EPOCH_DAY))
                } else null
            }

            override fun toString(): String {
                return "LocalDate"
            }
        }

        /**
         * A query for `LocalTime` returning null if not found.
         */
        @JvmStatic
        val LOCAL_TIME: TemporalQuery<LocalTime?> = object : TemporalQuery<LocalTime?> {
            override fun queryFrom(temporal: TemporalAccessor): LocalTime? {
                return if (temporal.isSupported(ChronoField.NANO_OF_DAY)) {
                    LocalTime.ofNanoOfDay(temporal.getLong(ChronoField.NANO_OF_DAY))
                } else null
            }

            override fun toString(): String {
                return "LocalTime"
            }
        }
    }
}