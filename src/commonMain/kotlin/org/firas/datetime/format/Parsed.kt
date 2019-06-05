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

import org.firas.datetime.*
import org.firas.datetime.chrono.ChronoLocalDate
import org.firas.datetime.chrono.ChronoLocalDateTime
import org.firas.datetime.chrono.ChronoZonedDateTime
import org.firas.datetime.chrono.Chronology
import org.firas.datetime.temporal.*
import org.firas.datetime.util.MathUtils
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset
import kotlin.js.JsName

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
internal class Parsed internal constructor(): TemporalAccessor {
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
     */
    private var resolverStyle: ResolverStyle? = null

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
     */
    var excessDays = Period.ZERO

    /**
     * Creates a copy.
     */
    @JsName("copy")
    internal fun copy(): Parsed {
        // only copy fields used in parsing stage
        val cloned = Parsed()
        // cloned.fieldValues.putAll(this.fieldValues)
        cloned.zone = this.zone
        cloned.chrono = this.chrono
        cloned.leapSecond = this.leapSecond
        return cloned
    }

    //-----------------------------------------------------------------------
    override fun isSupported(field: TemporalField): Boolean {
        val _date = this.date
        val _time = this.time
        return if (fieldValues.containsKey(field) ||
            _date != null && _date.isSupported(field) ||
            _time != null && _time.isSupported(field)
        ) {
            true
        } else field !is ChronoField && field.isSupportedBy(this)
    }

    override fun getLong(field: TemporalField): Long {
        val value = fieldValues[field]
        if (value != null) {
            return value
        }
        val _date = this.date
        if (_date != null && _date.isSupported(field)) {
            return _date.getLong(field)
        }

        val _time = this.time
        if (_time != null && _time.isSupported(field)) {
            return _time.getLong(field)
        }

        if (field is ChronoField) {
            throw UnsupportedTemporalTypeException("Unsupported field: $field")
        }
        return field.getFrom(this)
    }

    override fun <R> query(query: TemporalQuery<R>): R? {
        if (query == TemporalQueries.ZONE_ID) {
            return zone as R
        } else if (query == TemporalQueries.CHRONO) {
            return chrono as R
        } else if (query == TemporalQueries.LOCAL_DATE) {
            val _date = this.date
            return if (_date != null) LocalDate.from(_date) as R else null
        } else if (query == TemporalQueries.LOCAL_TIME) {
            return time as R
        } else if (query == TemporalQueries.OFFSET) {
            val offsetSecs = fieldValues[ChronoField.OFFSET_SECONDS]
            if (offsetSecs != null) {
                return ZoneOffset.ofTotalSeconds(offsetSecs.toInt()) as R
            }
            if (zone is ZoneOffset) {
                return zone as R
            }
            return query.queryFrom(this)
        } else if (query == TemporalQueries.ZONE) {
            return query.queryFrom(this)
        } else if (query == TemporalQueries.PRECISION) {
            return null  // not a complete date/time
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this)
    }

    override fun toString(): String {
        val buf = StringBuilder(64)
        buf.append(fieldValues).append(',').append(chrono)
        val _zone = this.zone
        if (_zone != null) {
            buf.append(',').append(_zone)
        }

        val _date = this.date
        val _time = this.time
        if (_date != null || _time != null) {
            buf.append(" resolved to ")
            if (_date != null) {
                buf.append(_date)
                if (_time != null) {
                    buf.append('T').append(_time)
                }
            } else {
                buf.append(_time)
            }
        }
        return buf.toString()
    }

    //-----------------------------------------------------------------------
    /**
     * Resolves the fields in this context.
     *
     * @param resolverStyle  the resolver style, not null
     * @param resolverFields  the fields to use for resolving, null for all fields
     * @return this, for method chaining
     * @throws DateTimeException if resolving one field results in a value for
     * another field that is in conflict
     */
    @JsName("resolve")
    internal fun resolve(resolverStyle: ResolverStyle, resolverFields: Set<TemporalField>?): TemporalAccessor {
        if (resolverFields != null) {
            fieldValues.keys.retainAll(resolverFields)
        }
        this.resolverStyle = resolverStyle
        resolveFields()
        resolveTimeLenient()
        crossCheck()
        resolvePeriod()
        resolveFractional()
        resolveInstant()
        return this
    }

    private fun resolveFields() {
        // resolve ChronoField
        resolveInstantFields()
        resolveDateFields()
        resolveTimeFields()

        // if any other fields, handle them
        // any lenient date resolution should return epoch-day
        if (this.fieldValues.isNotEmpty()) {
            var changedCount = 0
            while (changedCount < 50) {
                var breakWhile = true
                for (entry in fieldValues.entries) {
                    val targetField = entry.key
                    var resolvedObject = targetField.resolve(fieldValues, this, resolverStyle!!)
                    if (resolvedObject != null) {
                        if (resolvedObject is ChronoZonedDateTime<*>) {
                            if (zone == null) {
                                zone = resolvedObject.getZone()
                            } else if (zone != resolvedObject.getZone()) {
                                throw DateTimeException("ChronoZonedDateTime must use the effective parsed zone: $zone");
                            }
                            resolvedObject = resolvedObject.toLocalDateTime()
                        }
                        if (resolvedObject is ChronoLocalDateTime<*>) {
                            updateCheckConflict(resolvedObject.getTime(), Period.ZERO)
                            updateCheckConflict(resolvedObject.getDate())
                            changedCount += 1
                            breakWhile = false
                            break  // have to restart to avoid concurrent modification
                        }
                        if (resolvedObject is ChronoLocalDate) {
                            updateCheckConflict(resolvedObject)
                            changedCount += 1
                            breakWhile = false
                            break  // have to restart to avoid concurrent modification
                        }
                        if (resolvedObject is LocalTime) {
                            updateCheckConflict(resolvedObject, Period.ZERO)
                            changedCount += 1
                            breakWhile = false
                            break  // have to restart to avoid concurrent modification
                        }
                        throw DateTimeException("Method resolve() can only return ChronoZonedDateTime, " +
                                "ChronoLocalDateTime, ChronoLocalDate or LocalTime")
                    } else if (!fieldValues.containsKey(targetField)) {
                        changedCount += 1
                        breakWhile = false
                        break  // have to restart to avoid concurrent modification
                    }
                }
                if (breakWhile) {
                    break
                }
            }
            if (changedCount == 50) {  // catch infinite loops
                throw DateTimeException("One of the parsed fields has an incorrectly implemented resolve method")
            }
            // if something changed then have to redo ChronoField resolve
            if (changedCount > 0) {
                resolveInstantFields()
                resolveDateFields()
                resolveTimeFields()
            }
        }
    } // private fun resolveFields()

    private fun updateCheckConflict(targetField: TemporalField, changeField: TemporalField, changeValue: Long) {
        val old = fieldValues.put(changeField, changeValue)
        if (old != null && old.toLong() != changeValue) {
            throw DateTimeException("Conflict found: $changeField $old differs from $changeField" +
                        " $changeValue while resolving  $targetField")
        }
    }

    private fun resolveInstantFields() {
        // resolve parsed instant seconds to date and time if zone available
        if (fieldValues.containsKey(ChronoField.INSTANT_SECONDS)) {
            val _zone = this.zone
            if (_zone != null) {
                resolveInstantFields0(_zone)
            } else {
                val offsetSecs = fieldValues.get(ChronoField.OFFSET_SECONDS)
                if (offsetSecs != null) {
                    val offset = ZoneOffset.ofTotalSeconds(offsetSecs.toInt())
                    resolveInstantFields0(offset)
                }
            }
        }
    }

    private fun resolveInstantFields0(selectedZone: ZoneId) {
        val instant = Instant.ofEpochSecond(this.fieldValues.remove(ChronoField.INSTANT_SECONDS)!!)
        val zdt: ChronoZonedDateTime<*> = this.chrono!!.zonedDateTime(instant, selectedZone)
        updateCheckConflict(zdt.toLocalDate())
        updateCheckConflict(ChronoField.INSTANT_SECONDS, ChronoField.SECOND_OF_DAY,
                zdt.toLocalTime().toSecondOfDay().toLong())
    }

    private fun resolveDateFields() {
        updateCheckConflict(this.chrono!!.resolveDate(this.fieldValues, resolverStyle!!))
    }

    private fun updateCheckConflict(cld: ChronoLocalDate?) {
        val _date = this.date
        if (_date != null) {
            if (cld != null && _date != cld) {
                throw DateTimeException("Conflict found: Fields resolved to two different dates: $_date $cld")
            }
        } else if (cld != null) {
            if (this.chrono != cld.getChronology()) {
                throw DateTimeException("ChronoLocalDate must use the effective parsed chronology: " + this.chrono)
            }
            this.date = cld
        }
    }

    private fun resolveTimeFields() {
        // simplify fields
        if (fieldValues.containsKey(ChronoField.CLOCK_HOUR_OF_DAY)) {
            // lenient allows anything, smart allows 0-24, strict allows 1-24
            val ch = fieldValues.remove(ChronoField.CLOCK_HOUR_OF_DAY)!!
            if (resolverStyle == ResolverStyle.STRICT || (resolverStyle == ResolverStyle.SMART && ch != 0L)) {
                ChronoField.CLOCK_HOUR_OF_DAY.checkValidValue(ch)
            }
            updateCheckConflict(ChronoField.CLOCK_HOUR_OF_DAY, ChronoField.HOUR_OF_DAY,
                    if (ch == 24L) 0 else ch)
        }
        if (fieldValues.containsKey(ChronoField.CLOCK_HOUR_OF_AMPM)) {
            // lenient allows anything, smart allows 0-12, strict allows 1-12
            val ch = fieldValues.remove(ChronoField.CLOCK_HOUR_OF_AMPM)!!
            if (resolverStyle == ResolverStyle.STRICT || (resolverStyle == ResolverStyle.SMART && ch != 0L)) {
                ChronoField.CLOCK_HOUR_OF_AMPM.checkValidValue(ch)
            }
            updateCheckConflict(ChronoField.CLOCK_HOUR_OF_AMPM, ChronoField.HOUR_OF_AMPM,
                    if (ch == 12L) 0 else ch)
        }
        if (fieldValues.containsKey(ChronoField.AMPM_OF_DAY) && fieldValues.containsKey(ChronoField.HOUR_OF_AMPM)) {
            val ap = fieldValues.remove(ChronoField.AMPM_OF_DAY)!!
            val hap = fieldValues.remove(ChronoField.HOUR_OF_AMPM)!!
            if (resolverStyle == ResolverStyle.LENIENT) {
                updateCheckConflict(ChronoField.AMPM_OF_DAY,
                    ChronoField.HOUR_OF_DAY, MathUtils.addExact(MathUtils.multiplyExact(ap, 12), hap))
            } else {  // STRICT or SMART
                ChronoField.AMPM_OF_DAY.checkValidValue(ap)
                ChronoField.HOUR_OF_AMPM.checkValidValue(ap)
                updateCheckConflict(ChronoField.AMPM_OF_DAY, ChronoField.HOUR_OF_DAY, ap * 12 + hap)
            }
        }
        if (fieldValues.containsKey(ChronoField.NANO_OF_DAY)) {
            val nod = fieldValues.remove(ChronoField.NANO_OF_DAY)!!
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.NANO_OF_DAY.checkValidValue(nod)
            }
            updateCheckConflict(ChronoField.NANO_OF_DAY, ChronoField.HOUR_OF_DAY, nod / 3600_000_000_000L)
            updateCheckConflict(ChronoField.NANO_OF_DAY, ChronoField.MINUTE_OF_HOUR, (nod / 60_000_000_000L) % 60)
            updateCheckConflict(ChronoField.NANO_OF_DAY, ChronoField.SECOND_OF_MINUTE, (nod / 1_000_000_000L) % 60)
            updateCheckConflict(ChronoField.NANO_OF_DAY, ChronoField.NANO_OF_SECOND, nod % 1_000_000_000L)
        }
        if (fieldValues.containsKey(ChronoField.MICRO_OF_DAY)) {
            val cod = fieldValues.remove(ChronoField.MICRO_OF_DAY)!!
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.MICRO_OF_DAY.checkValidValue(cod)
            }
            updateCheckConflict(ChronoField.MICRO_OF_DAY, ChronoField.SECOND_OF_DAY, cod / 1_000_000L)
            updateCheckConflict(ChronoField.MICRO_OF_DAY, ChronoField.MICRO_OF_SECOND, cod % 1_000_000L)
        }
        if (fieldValues.containsKey(ChronoField.MILLI_OF_DAY)) {
            val lod = fieldValues.remove(ChronoField.MILLI_OF_DAY)!!
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.MILLI_OF_DAY.checkValidValue(lod)
            }
            updateCheckConflict(ChronoField.MILLI_OF_DAY, ChronoField.SECOND_OF_DAY, lod / 1_000)
            updateCheckConflict(ChronoField.MILLI_OF_DAY, ChronoField.MILLI_OF_SECOND, lod % 1_000)
        }
        if (fieldValues.containsKey(ChronoField.SECOND_OF_DAY)) {
            val sod = fieldValues.remove(ChronoField.SECOND_OF_DAY)!!
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.SECOND_OF_DAY.checkValidValue(sod)
            }
            updateCheckConflict(ChronoField.SECOND_OF_DAY, ChronoField.HOUR_OF_DAY, sod / 3600)
            updateCheckConflict(ChronoField.SECOND_OF_DAY, ChronoField.MINUTE_OF_HOUR, (sod / 60) % 60)
            updateCheckConflict(ChronoField.SECOND_OF_DAY, ChronoField.SECOND_OF_MINUTE, sod % 60)
        }
        if (fieldValues.containsKey(ChronoField.MINUTE_OF_DAY)) {
            val mod = fieldValues.remove(ChronoField.MINUTE_OF_DAY)!!
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.MINUTE_OF_DAY.checkValidValue(mod)
            }
            updateCheckConflict(ChronoField.MINUTE_OF_DAY, ChronoField.HOUR_OF_DAY, mod / 60)
            updateCheckConflict(ChronoField.MINUTE_OF_DAY, ChronoField.MINUTE_OF_HOUR, mod % 60)
        }

        // combine partial second fields strictly, leaving lenient expansion to later
        if (fieldValues.containsKey(ChronoField.NANO_OF_SECOND)) {
            var nos = fieldValues.get(ChronoField.NANO_OF_SECOND)!!
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.NANO_OF_SECOND.checkValidValue(nos)
            }
            if (fieldValues.containsKey(ChronoField.MICRO_OF_SECOND)) {
                val cos = fieldValues.remove(ChronoField.MICRO_OF_SECOND)!!
                if (resolverStyle != ResolverStyle.LENIENT) {
                    ChronoField.MICRO_OF_SECOND.checkValidValue(cos)
                }
                nos = cos * 1000 + (nos % 1000)
                updateCheckConflict(ChronoField.MICRO_OF_SECOND, ChronoField.NANO_OF_SECOND, nos)
            }
            if (fieldValues.containsKey(ChronoField.MILLI_OF_SECOND)) {
                val los = fieldValues.remove(ChronoField.MILLI_OF_SECOND)!!
                if (resolverStyle != ResolverStyle.LENIENT) {
                    ChronoField.MILLI_OF_SECOND.checkValidValue(los)
                }
                updateCheckConflict(ChronoField.MILLI_OF_SECOND, ChronoField.NANO_OF_SECOND, los * 1_000_000L + (nos % 1_000_000L))
            }
        }

        // convert to time if all four fields available (optimization)
        if (fieldValues.containsKey(ChronoField.HOUR_OF_DAY) && fieldValues.containsKey(ChronoField.MINUTE_OF_HOUR) &&
                fieldValues.containsKey(ChronoField.SECOND_OF_MINUTE) && fieldValues.containsKey(ChronoField.NANO_OF_SECOND)) {
            val hod = fieldValues.remove(ChronoField.HOUR_OF_DAY)!!
            val moh = fieldValues.remove(ChronoField.MINUTE_OF_HOUR)!!
            val som = fieldValues.remove(ChronoField.SECOND_OF_MINUTE)!!
            val nos = fieldValues.remove(ChronoField.NANO_OF_SECOND)!!
            resolveTime(hod, moh, som, nos)
        }
    } // private fun resolveTimeFields()

    private fun resolveTimeLenient() {
        // leniently create a time from incomplete information
        // done after everything else as it creates information from nothing
        // which would break updateCheckConflict(field)

        if (time == null) {
            // NANO_OF_SECOND merged with MILLI/MICRO above
            if (fieldValues.containsKey(ChronoField.MILLI_OF_SECOND)) {
                val los = fieldValues.remove(ChronoField.MILLI_OF_SECOND)!!
                if (fieldValues.containsKey(ChronoField.MICRO_OF_SECOND)) {
                    // merge milli-of-second and micro-of-second for better error message
                    val cos = los * 1_000 + (fieldValues.get(ChronoField.MICRO_OF_SECOND)!! % 1_000)
                    updateCheckConflict(ChronoField.MILLI_OF_SECOND, ChronoField.MICRO_OF_SECOND, cos)
                    fieldValues.remove(ChronoField.MICRO_OF_SECOND)
                    fieldValues.put(ChronoField.NANO_OF_SECOND, cos * 1_000L)
                } else {
                    // convert milli-of-second to nano-of-second
                    fieldValues.put(ChronoField.NANO_OF_SECOND, los * 1_000_000L)
                }
            } else if (fieldValues.containsKey(ChronoField.MICRO_OF_SECOND)) {
                // convert micro-of-second to nano-of-second
                val cos = fieldValues.remove(ChronoField.MICRO_OF_SECOND)!!
                fieldValues.put(ChronoField.NANO_OF_SECOND, cos * 1_000L)
            }

            // merge hour/minute/second/nano leniently
            val hod = fieldValues.get(ChronoField.HOUR_OF_DAY)
            if (hod != null) {
                val moh = fieldValues.get(ChronoField.MINUTE_OF_HOUR)
                val som = fieldValues.get(ChronoField.SECOND_OF_MINUTE)
                val nos = fieldValues.get(ChronoField.NANO_OF_SECOND)

                // check for invalid combinations that cannot be defaulted
                if ((moh == null && (som != null || nos != null)) ||
                        (moh != null && som == null && nos != null)) {
                    return
                }

                // default as necessary and build time
                val mohVal = moh ?: 0
                val somVal = som ?: 0
                val nosVal = nos ?: 0
                resolveTime(hod, mohVal, somVal, nosVal)
                fieldValues.remove(ChronoField.HOUR_OF_DAY)
                fieldValues.remove(ChronoField.MINUTE_OF_HOUR)
                fieldValues.remove(ChronoField.SECOND_OF_MINUTE)
                fieldValues.remove(ChronoField.NANO_OF_SECOND)
            }
        }

        // validate remaining
        if (resolverStyle != ResolverStyle.LENIENT && fieldValues.isNotEmpty()) {
            for (entry in fieldValues.entries) {
                val field = entry.key
                if (field is ChronoField && field.isTimeBased()) {
                    field.checkValidValue(entry.value)
                }
            }
        }
    } // private fun resolveTimeLenient()

    private fun resolveTime(hod: Long, moh: Long, som: Long, nos: Long) {
        if (resolverStyle === ResolverStyle.LENIENT) {
            var totalNanos = MathUtils.multiplyExact(hod, 3600_000_000_000L)
            totalNanos = MathUtils.addExact(totalNanos, MathUtils.multiplyExact(moh, 60_000_000_000L))
            totalNanos = MathUtils.addExact(totalNanos, MathUtils.multiplyExact(som, 1_000_000_000L))
            totalNanos = MathUtils.addExact(totalNanos, nos)
            val excessDays = MathUtils.floorDiv(totalNanos, 86400_000_000_000L).toInt()  // safe int cast
            val nod = MathUtils.floorMod(totalNanos, 86400_000_000_000L)
            updateCheckConflict(LocalTime.ofNanoOfDay(nod), Period.ofDays(excessDays))
        } else {  // STRICT or SMART
            val mohVal = ChronoField.MINUTE_OF_HOUR.checkValidIntValue(moh)
            val nosVal = ChronoField.NANO_OF_SECOND.checkValidIntValue(nos)
            // handle 24:00 end of day
            if (resolverStyle === ResolverStyle.SMART && hod == 24L && mohVal == 0 && som == 0L && nosVal == 0) {
                updateCheckConflict(LocalTime.MIDNIGHT, Period.ofDays(1))
            } else {
                val hodVal = ChronoField.HOUR_OF_DAY.checkValidIntValue(hod)
                val somVal = ChronoField.SECOND_OF_MINUTE.checkValidIntValue(som)
                updateCheckConflict(LocalTime.of(hodVal, mohVal, somVal, nosVal), Period.ZERO)
            }
        }
    }

    private fun resolvePeriod() {
        // add whole days if we have both date and time
        val _date = this.date
        if (_date != null && this.time != null && !this.excessDays.isZero()) {
            this.date = _date.plus(excessDays) as ChronoLocalDate
            excessDays = Period.ZERO
        }
    }

    private fun resolveFractional() {
        // ensure fractional seconds available as ChronoField requires
        // resolveTimeLenient() will have merged MICRO_OF_SECOND/MILLI_OF_SECOND to NANO_OF_SECOND
        if (time == null &&
                (fieldValues.containsKey(ChronoField.INSTANT_SECONDS) ||
                    fieldValues.containsKey(ChronoField.SECOND_OF_DAY) ||
                    fieldValues.containsKey(ChronoField.SECOND_OF_MINUTE))) {
            if (fieldValues.containsKey(ChronoField.NANO_OF_SECOND)) {
                val nos = fieldValues.get(ChronoField.NANO_OF_SECOND)!!
                fieldValues.put(ChronoField.MICRO_OF_SECOND, nos / 1000)
                fieldValues.put(ChronoField.MILLI_OF_SECOND, nos / 1000000)
            } else {
                fieldValues.put(ChronoField.NANO_OF_SECOND, 0L)
                fieldValues.put(ChronoField.MICRO_OF_SECOND, 0L)
                fieldValues.put(ChronoField.MILLI_OF_SECOND, 0L)
            }
        }
    }

    private fun resolveInstant() {
        // add instant seconds if we have date, time and zone
        // Offset (if present) will be given priority over the zone.
        val _date = this.date
        val _time = this.time
        if (_date != null && _time != null) {
            val offsetSecs = fieldValues.get(ChronoField.OFFSET_SECONDS)
            if (offsetSecs != null) {
                val offset = ZoneOffset.ofTotalSeconds(offsetSecs.toInt())
                val instant = _date.atTime(_time).atZone(offset).toEpochSecond()
                fieldValues.put(ChronoField.INSTANT_SECONDS, instant)
            } else {
                val _zone = this.zone
                if (_zone != null) {
                    val instant = _date.atTime(_time).atZone(_zone).toEpochSecond()
                    fieldValues.put(ChronoField.INSTANT_SECONDS, instant)
                }
            }
        }
    }

    private fun updateCheckConflict(timeToSet: LocalTime, periodToSet: Period) {
        val _time = this.time
        if (_time != null) {
            if (_time != timeToSet) {
                throw DateTimeException("Conflict found: Fields resolved to different times: $_time $timeToSet")
            }
            if (!this.excessDays.isZero() && !periodToSet.isZero() && this.excessDays != periodToSet) {
                throw DateTimeException("Conflict found: Fields resolved to different excess periods: $excessDays $periodToSet")
            } else {
                this.excessDays = periodToSet
            }
        } else {
            this.time = timeToSet
            this.excessDays = periodToSet
        }
    }

    private fun crossCheck() {
        val _date = this.date
        val _time = this.time
        // only cross-check date, time and date-time
        // avoid object creation if possible
        if (_date != null) {
            crossCheck(_date)
        }
        if (_time != null) {
            crossCheck(_time)
            if (_date != null && fieldValues.isNotEmpty()) {
                crossCheck(_date.atTime(_time))
            }
        }
    }

    private fun crossCheck(target: TemporalAccessor) {
        val it = fieldValues.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val field = entry.key
            if (target.isSupported(field)) {
                val val1: Long
                try {
                    val1 = target.getLong(field)
                } catch (ex: RuntimeException) {
                    continue
                }

                val val2 = entry.value
                if (val1 != val2) {
                    throw DateTimeException("Conflict found: Field $field $val1" +
                                " differs from $field $val2 derived from $target")
                }
                it.remove()
            }
        }
    }
}