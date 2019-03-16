/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
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
package org.firas.datetime.temporal

import org.firas.datetime.DayOfWeek

/**
 * Common and useful TemporalAdjusters.
 *
 *
 * Adjusters are a key tool for modifying temporal objects.
 * They exist to externalize the process of adjustment, permitting different
 * approaches, as per the strategy design pattern.
 * Examples might be an adjuster that sets the date avoiding weekends, or one that
 * sets the date to the last day of the month.
 *
 *
 * There are two equivalent ways of using a {@code TemporalAdjuster}.
 * The first is to invoke the method on the interface directly.
 * The second is to use {@link Temporal#with(TemporalAdjuster)}:
 * <pre>
 *   // these two lines are equivalent, but the second approach is recommended
 *   temporal = thisAdjuster.adjustInto(temporal);
 *   temporal = temporal.with(thisAdjuster);
 * </pre>
 * It is recommended to use the second approach, {@code with(TemporalAdjuster)},
 * as it is a lot clearer to read in code.
 *
 *
 * This class contains a standard set of adjusters, available as static methods.
 * These include:
 *
 *
 * * finding the first or last day of the month
 * * finding the first day of next month
 * * finding the first or last day of the year
 * * finding the first day of next year
 * * finding the first or last day-of-week within a month, such as "first Wednesday in June"
 * * finding the next or previous day-of-week, such as "next Thursday"
 *
 *
 * @implSpec
 * All the implementations supplied by the static methods are immutable.
 *
 * @see TemporalAdjuster
 * @since Java 1.8
 * @author Wu Yuping
 */
class TemporalAdjusters private constructor() {

    companion object {
        /**
         * Returns the next-or-same day-of-week adjuster, which adjusts the date to the
         * first occurrence of the specified day-of-week after the date being adjusted
         * unless it is already on that day in which case the same object is returned.
         *
         *
         * The ISO calendar system behaves as follows:<br>
         * The input 2011-01-15 (a Saturday) for parameter (MONDAY) will return 2011-01-17 (two days later).<br>
         * The input 2011-01-15 (a Saturday) for parameter (WEDNESDAY) will return 2011-01-19 (four days later).<br>
         * The input 2011-01-15 (a Saturday) for parameter (SATURDAY) will return 2011-01-15 (same as input).
         *
         *
         * The behavior is suitable for use with most calendar systems.
         * It uses the {@code DAY_OF_WEEK} field and the {@code DAYS} unit,
         * and assumes a seven day week.
         *
         * @param dayOfWeek  the day-of-week to check for or move the date to, not null
         * @return the next-or-same day-of-week adjuster, not null
         */
        fun nextOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster {
            val dowValue = dayOfWeek.getValue()
            return { temporal: Temporal ->
                val calDow = temporal.get(ChronoField.DAY_OF_WEEK)
                if (calDow == dowValue) {
                    temporal
                }
                val daysDiff = calDow - dowValue
                temporal.plus(if (daysDiff >= 0) 7L - daysDiff else -daysDiff.toLong(), ChronoUnit.DAYS)
            } as TemporalAdjuster
        }

        /**
         * Returns the previous-or-same day-of-week adjuster, which adjusts the date to the
         * first occurrence of the specified day-of-week before the date being adjusted
         * unless it is already on that day in which case the same object is returned.
         * <p>
         * The ISO calendar system behaves as follows:<br>
         * The input 2011-01-15 (a Saturday) for parameter (MONDAY) will return 2011-01-10 (five days earlier).<br>
         * The input 2011-01-15 (a Saturday) for parameter (WEDNESDAY) will return 2011-01-12 (three days earlier).<br>
         * The input 2011-01-15 (a Saturday) for parameter (SATURDAY) will return 2011-01-15 (same as input).
         * <p>
         * The behavior is suitable for use with most calendar systems.
         * It uses the {@code DAY_OF_WEEK} field and the {@code DAYS} unit,
         * and assumes a seven day week.
         *
         * @param dayOfWeek  the day-of-week to check for or move the date to, not null
         * @return the previous-or-same day-of-week adjuster, not null
         */
        fun previousOrSame(dayOfWeek: DayOfWeek): TemporalAdjuster {
            val dowValue = dayOfWeek.getValue()
            return { temporal: Temporal ->
                val calDow = temporal.get(ChronoField.DAY_OF_WEEK)
                if (calDow == dowValue) {
                    temporal
                }
                val daysDiff = dowValue - calDow
                temporal.minus(if (daysDiff >= 0) 7L - daysDiff else -daysDiff.toLong(), ChronoUnit.DAYS)
            } as TemporalAdjuster
        }
    }
}