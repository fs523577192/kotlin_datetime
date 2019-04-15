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

import org.firas.datetime.util.SimpleImmutableEntry


/**
 * A provider to obtain the textual form of a date-time field.
 *
 * @implSpec
 * Implementations must be thread-safe.
 * Implementations should cache the textual information.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class DateTimeTextProvider internal constructor() {

    companion object {
        /** Comparator.  */
        private val COMPARATOR = object : Comparator<Map.Entry<String, Long>> {
            override fun compare(obj1: Map.Entry<String, Long>, obj2: Map.Entry<String, Long>): Int {
                return obj2.key.length - obj1.key.length  // longest to shortest
            }
        }

        // Singleton instance
        private val INSTANCE = DateTimeTextProvider()

        /**
         * Gets the provider of text.
         *
         * @return the provider, not null
         */
        internal fun getInstance(): DateTimeTextProvider {
            return INSTANCE
        }

        /**
         * Helper method to create an immutable entry.
         *
         * @param text  the text, not null
         * @param field  the field, not null
         * @return the entry, not null
         */
        private fun <A, B> createEntry(text: A, field: B): Map.Entry<A, B> {
            return SimpleImmutableEntry(text, field)
        }

        /**
         * Stores the text for a single locale.
         *
         *
         * Some fields have a textual representation, such as day-of-week or month-of-year.
         * These textual representations can be captured in this class for printing
         * and parsing.
         *
         *
         * This class is immutable and thread-safe.
         */
        class LocaleStore internal constructor(
            /**
             * Map of value to text.
             */
            private val valueTextMap: Map<TextStyle, Map<Long, String>>
        ) {

            /**
             * Parsable data.
             */
            private val parsable: Map<TextStyle?, List<Map.Entry<String, Long>>>

            /**
             * Constructor.
             *
             * @param valueTextMap  the map of values to text to store, assigned and not altered, not null
             */
            init {
                val map = HashMap<TextStyle?, List<Map.Entry<String, Long>>>()
                val allList = ArrayList<Map.Entry<String, Long>>()
                for (vtmEntry in valueTextMap.entries) {
                    val reverse = HashMap<String, Map.Entry<String, Long>>()
                    for (entry in vtmEntry.value.entries) {
                        if (reverse.put(entry.value, createEntry(entry.value, entry.key)) != null) {
                            // TODO: BUG: this has no effect
                            continue  // not parsable, try next style
                        }
                    }
                    val list = ArrayList(reverse.values)
                    list.sortedWith(COMPARATOR)
                    map[vtmEntry.key] = list
                    allList.addAll(list)
                    map[null] = allList
                }
                allList.sortedWith(COMPARATOR)
                this.parsable = map
            }

            /**
             * Gets the text for the specified field value, locale and style
             * for the purpose of printing.
             *
             * @param value  the value to get text for, not null
             * @param style  the style to get text for, not null
             * @return the text for the field value, null if no text found
             */
            fun getText(value: Long, style: TextStyle): String? {
                return valueTextMap[style]?.get(value)
            }

            /**
             * Gets an iterator of text to field for the specified style for the purpose of parsing.
             * <p>
             * The iterator must be returned in order from the longest text to the shortest.
             *
             * @param style  the style to get text for, null for all parsable text
             * @return the iterator of text to field pairs, in order from longest text to shortest text,
             *  null if the style is not parsable
             */
            fun getTextIterator(style: TextStyle): Iterator<Map.Entry<String, Long>>? {
                return parsable[style]?.iterator()
            }
        }
    }
}