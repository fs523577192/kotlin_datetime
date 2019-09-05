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
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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
import org.firas.datetime.chrono.ChronoLocalDate
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * Framework-level interface defining read-only access to a temporal object,
 * such as a date, time, offset or some combination of these.
 *
 * This is the base interface type for date, time and offset objects.
 * It is implemented by those classes that can provide information
 * as {@linkplain TemporalField fields} or {@linkplain TemporalQuery queries}.
 *
 * Most date and time information can be represented as a number.
 * These are modeled using `TemporalField` with the number held using
 * a `Long` to handle large values. Year, month and day-of-month are
 * simple examples of fields, but they also include instant and offsets.
 * See [ChronoField] for the standard set of fields.
 *
 * Two pieces of date/time information cannot be represented by numbers,
 * the {@linkplain java.time.chrono.Chronology chronology} and the
 * {@linkplain java.time.ZoneId time-zone}.
 * These can be accessed via {@linkplain #query(TemporalQuery) queries} using
 * the static methods defined on [TemporalQuery].
 *
 * A sub-interface, [Temporal], extends this definition to one that also
 * supports adjustment and manipulation on more complete temporal objects.
 *
 * This interface is a framework-level interface that should not be widely
 * used in application code. Instead, applications should create and pass
 * around instances of concrete types, such as `LocalDate`.
 * There are many reasons for this, part of which is that implementations
 * of this interface may be in calendar systems other than ISO.
 * See [ChronoLocalDate] for a fuller discussion of the issues.
 *
 * @implSpec
 * This interface places no restrictions on the mutability of implementations,
 * however immutability is strongly recommended.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
interface TemporalAccessor {

    companion object {
        // Write default implementation here because
        // 1. the default implementation mechanism in Kotlin is different from
        //    the default implementation mechanism in Java;
        // 2. Kotlin does not support calling implementation of a base class
        //    when the derived class has an overridden implementation

        @JvmStatic
        @JsName("range")
        fun range(temporalAccessor: TemporalAccessor, field: TemporalField): ValueRange {
            if (field is ChronoField) {
                if (temporalAccessor.isSupported(field)) {
                    return field.range()
                }
                throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
            return field.rangeRefinedBy(temporalAccessor)
        }

        @JvmStatic
        @JsName("get")
        fun get(temporalAccessor: TemporalAccessor, field: TemporalField): Int {
            val range = range(temporalAccessor, field)
            if (!range.isIntValue()) {
                throw UnsupportedTemporalTypeException("Invalid field $field for get() method, use getLong() instead")
            }
            val value = temporalAccessor.getLong(field)
            if (!range.isValidValue(value)) {
                throw DateTimeException("Invalid value for $field (valid values $range): $value")
            }
            return value.toInt()
        }

        @JvmStatic
        @JsName("query")
        fun <R> query(temporalAccessor: TemporalAccessor, query: TemporalQuery<R>): R? {
            return if (query === TemporalQueries.ZONE_ID
                || query === TemporalQueries.CHRONO
                || query === TemporalQueries.PRECISION
            ) {
                null
            } else query.queryFrom(temporalAccessor)
        }
    }

    /**
     * Checks if the specified field is supported.
     *
     *
     * This checks if the date-time can be queried for the specified field.
     * If false, then calling the [range][.range] and [get][.get]
     * methods will throw an exception.
     *
     * @implSpec
     * Implementations must check and handle all fields defined in [ChronoField].
     * If the field is supported, then true must be returned, otherwise false must be returned.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.isSupportedBy(TemporalAccessor)`
     * passing `this` as the argument.
     *
     *
     * Implementations must ensure that no observable state is altered when this
     * read-only method is invoked.
     *
     * @param field  the field to check, null returns false
     * @return true if this date-time can be queried for the field, false if not
     */
    @JsName("isFieldSupported")
    fun isSupported(field: TemporalField): Boolean

    /**
     * Gets the range of valid values for the specified field.
     *
     *
     * All fields can be expressed as a `long` integer.
     * This method returns an object that describes the valid range for that value.
     * The value of this temporal object is used to enhance the accuracy of the returned range.
     * If the date-time cannot return the range, because the field is unsupported or for
     * some other reason, an exception will be thrown.
     *
     *
     * Note that the result only describes the minimum and maximum valid values
     * and it is important not to read too much into them. For example, there
     * could be values within the range that are invalid for the field.
     *
     * @implSpec
     * Implementations must check and handle all fields defined in [ChronoField].
     * If the field is supported, then the range of the field must be returned.
     * If unsupported, then an `UnsupportedTemporalTypeException` must be thrown.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.rangeRefinedBy(TemporalAccessorl)`
     * passing `this` as the argument.
     *
     *
     * Implementations must ensure that no observable state is altered when this
     * read-only method is invoked.
     *
     *
     * The default implementation must behave equivalent to this code:
     * ```
     * if (field instanceof ChronoField) {
     *     if (isSupported(field)) {
     *         return field.range();
     *     }
     *     throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
     * }
     * return field.rangeRefinedBy(this);
     * ```
     *
     * @param field  the field to query the range for, not null
     * @return the range of valid values for the field, not null
     * @throws DateTimeException if the range for the field cannot be obtained
     * @throws UnsupportedTemporalTypeException if the field is not supported
     */
    @JsName("range")
    fun range(field: TemporalField): ValueRange

    /**
     * Gets the value of the specified field as an `int`.
     *
     *
     * This queries the date-time for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If the date-time cannot return the value, because the field is unsupported or for
     * some other reason, an exception will be thrown.
     *
     * @implSpec
     * Implementations must check and handle all fields defined in [ChronoField].
     * If the field is supported and has an `int` range, then the value of
     * the field must be returned.
     * If unsupported, then an `UnsupportedTemporalTypeException` must be thrown.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.getFrom(TemporalAccessor)`
     * passing `this` as the argument.
     *
     *
     * Implementations must ensure that no observable state is altered when this
     * read-only method is invoked.
     *
     *
     * The default implementation must behave equivalent to this code:
     * ```
     * if (range(field).isIntValue()) {
     *     return range(field).checkValidIntValue(getLong(field), field);
     * }
     * throw new UnsupportedTemporalTypeException("Invalid field " + field + " + for get() method, use getLong() instead");
     * ```
     *
     * @param field  the field to get, not null
     * @return the value for the field, within the valid range of values
     * @throws DateTimeException if a value for the field cannot be obtained or
     * the value is outside the range of valid values for the field
     * @throws UnsupportedTemporalTypeException if the field is not supported or
     * the range of values exceeds an `int`
     * @throws ArithmeticException if numeric overflow occurs
     */
    @JsName("get")
    operator fun get(field: TemporalField): Int

    /**
     * Gets the value of the specified field as a `long`.
     *
     *
     * This queries the date-time for the value of the specified field.
     * The returned value may be outside the valid range of values for the field.
     * If the date-time cannot return the value, because the field is unsupported or for
     * some other reason, an exception will be thrown.
     *
     * @implSpec
     * Implementations must check and handle all fields defined in [ChronoField].
     * If the field is supported, then the value of the field must be returned.
     * If unsupported, then an `UnsupportedTemporalTypeException` must be thrown.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.getFrom(TemporalAccessor)`
     * passing `this` as the argument.
     *
     *
     * Implementations must ensure that no observable state is altered when this
     * read-only method is invoked.
     *
     * @param field  the field to get, not null
     * @return the value for the field
     * @throws DateTimeException if a value for the field cannot be obtained
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    @JsName("getLong")
    fun getLong(field: TemporalField): Long

    /**
     * Queries this date-time.
     *
     *
     * This queries this date-time using the specified query strategy object.
     *
     *
     * Queries are a key tool for extracting information from date-times.
     * They exists to externalize the process of querying, permitting different
     * approaches, as per the strategy design pattern.
     * Examples might be a query that checks if the date is the day before February 29th
     * in a leap year, or calculates the number of days to your next birthday.
     *
     *
     * The most common query implementations are method references, such as
     * `LocalDate::from` and `ZoneId::from`.
     * Additional implementations are provided as static methods on [TemporalQuery].
     *
     * @implSpec
     * The default implementation must behave equivalent to this code:
     * ```
     * if (query == TemporalQueries.ZONE_ID ||
     *     query == TemporalQueries.CHRONO || query == TemporalQueries.PRECISION) {
     *     return null;
     * }
     * return query.queryFrom(this);
     * ```
     * Future versions are permitted to add further queries to the if statement.
     *
     *
     * All classes implementing this interface and overriding this method must call
     * `TemporalAccessor.super.query(query)`. JDK classes may avoid calling
     * super if they provide behavior equivalent to the default behaviour, however
     * non-JDK classes may not utilize this optimization and must call `super`.
     *
     *
     * If the implementation can supply a value for one of the queries listed in the
     * if statement of the default implementation, then it must do so.
     * For example, an application-defined `HourMin` class storing the hour
     * and minute must override this method as follows:
     * ```
     * if (query == TemporalQueries.precision()) {
     *     return MINUTES;
     * }
     * return TemporalAccessor.super.query(query);
     * ```
     *
     *
     * Implementations must ensure that no observable state is altered when this
     * read-only method is invoked.
     *
     * @param <R> the type of the result
     * @param query  the query to invoke, not null
     * @return the query result, null may be returned (defined by the query)
     * @throws DateTimeException if unable to query
     * @throws ArithmeticException if numeric overflow occurs
     */
    @JsName("query")
    fun <R> query(query: TemporalQuery<R>): R?
}
