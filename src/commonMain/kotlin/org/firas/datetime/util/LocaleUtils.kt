/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
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
 *******************************************************************************
 * Copyright (C) 2009-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package org.firas.datetime.util

import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * Collection of static utility methods for Locale support. The
 * methods which manipulate characters or strings support ASCII only.
 * @author Wu Yuping (migrate from Java 11)
 */
internal class LocaleUtils private constructor() {

    companion object {

        /**
         * Compares two ASCII Strings s1 and s2, ignoring case.
         */
        @JsName("caseIgnoreMatch")
        @JvmStatic
        internal fun caseIgnoreMatch(s1: String, s2: String): Boolean {
            if (s1 === s2) {
                return true
            }

            val len = s1.length
            if (len != s2.length) {
                return false
            }

            for (i in 0 until len) {
                val c1 = s1[i]
                val c2 = s2[i]
                if (c1 != c2 && toLower(c1) !== toLower(c2)) {
                    return false
                }
            }
            return true
        }

        @JsName("caseIgnoreCompare")
        @JvmStatic
        internal fun caseIgnoreCompare(s1: String, s2: String): Int {
            return if (s1 === s2) 0
                    else toLowerString(s1).compareTo(toLowerString(s2))
        }

        @JsName("toUpper")
        @JvmStatic
        internal fun toUpper(c: Char): Char {
            return if (isLower(c)) (c.toInt() - 0x20).toChar() else c
        }

        @JsName("toLower")
        @JvmStatic
        internal fun toLower(c: Char): Char {
            return if (isUpper(c)) (c.toInt() + 0x20).toChar() else c
        }

        /**
         * Converts the given ASCII String to lower-case.
         */
        @JsName("toLowerString")
        @JvmStatic
        internal fun toLowerString(s: String): String {
            val len = s.length
            var idx = 0
            while (idx < len) {
                if (isUpper(s[idx])) {
                    break
                }
                idx += 1
            }
            if (idx == len) {
                return s
            }

            val buf = CharArray(len)
            for (i in 0 until len) {
                val c = s[i]
                buf[i] = if (i < idx) c else toLower(c)
            }
            return String(buf)
        }

        @JsName("toUpperString")
        @JvmStatic
        internal fun toUpperString(s: String): String {
            val len = s.length
            var idx = 0
            while (idx < len) {
                if (isLower(s[idx])) {
                    break
                }
                idx += 1
            }
            if (idx == len) {
                return s
            }

            val buf = CharArray(len)
            for (i in 0 until len) {
                val c = s[i]
                buf[i] = if (i < idx) c else toUpper(c)
            }
            return String(buf)
        }

        @JsName("toTitleString")
        @JvmStatic
        internal fun toTitleString(s: String): String {
            val len: Int = s.length
            if (0 == len) {
                return s
            }
            var idx = 0
            if (!isLower(s[idx])) {
                idx = 1
                while (idx < len) {
                    if (isUpper(s[idx])) {
                        break
                    }
                    idx += 1
                }
            }
            if (idx == len) {
                return s
            }

            val buf = CharArray(len)
            for (i in 0 until len) {
                val c = s[i]
                if (i == 0 && idx == 0) {
                    buf[i] = toUpper(c)
                } else if (i < idx) {
                    buf[i] = c
                } else {
                    buf[i] = toLower(c)
                }
            }
            return String(buf)
        }

        @JsName("isAlpha")
        @JvmStatic
        internal fun isAlpha(c: Char): Boolean {
            return c in 'A'..'Z' || c in 'a'..'z'
        }

        @JsName("isAlphaString")
        @JvmStatic
        internal fun isAlphaString(s: String): Boolean {
            val len = s.length
            for (i in 0 until len) {
                if (!isAlpha(s[i])) {
                    return false
                }
            }
            return true
        }

        @JsName("isNumeric")
        @JvmStatic
        internal fun isNumeric(c: Char): Boolean {
            return c in '0'..'9'
        }

        @JsName("isNumericString")
        @JvmStatic
        internal fun isNumericString(s: String): Boolean {
            val len = s.length
            for (i in 0 until len) {
                if (!isNumeric(s[i])) {
                    return false
                }
            }
            return true
        }

        @JsName("isAlphaNumeric")
        @JvmStatic
        internal fun isAlphaNumeric(c: Char): Boolean {
            return isAlpha(c) || isNumeric(c)
        }

        @JsName("isAlphaNumericString")
        @JvmStatic
        internal fun isAlphaNumericString(s: String): Boolean {
            val len = s.length
            for (i in 0 until len) {
                if (!isAlphaNumeric(s[i])) {
                    return false
                }
            }
            return true
        }

        private fun isUpper(c: Char): Boolean {
            return c in 'A'..'Z'
        }

        private fun isLower(c: Char): Boolean {
            return c in 'a'..'z'
        }
    } // companion object
}