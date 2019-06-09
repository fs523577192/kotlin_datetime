/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (C) 2010, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package org.firas.datetime.util

/**
 *
 * @author Wu Yuping (migrate from OpenJDK 11)
 */
internal class StringTokenIterator {

    private var text: String
    private var dlms: String? = null        // null if a single char delimiter
    private var delimiterChar: Char = ' ' // delimiter if a single char delimiter

    private var token: String? = null
    private var start: Int = 0
    private var end: Int = 0
    private var done: Boolean = false

    internal constructor(text: String, dlms: String) {
        this.text = text
        if (dlms.length == 1) {
            this.delimiterChar = dlms[0]
        } else {
            this.dlms = dlms
        }
        this.setStart(0)
    }

    internal fun first(): String? {
        this.setStart(0)
        return this.token
    }

    internal fun current(): String? {
        return this.token
    }

    internal fun currentStart(): Int {
        return this.start
    }

    internal fun currentEnd(): Int {
        return this.end
    }

    internal fun isDone(): Boolean {
        return this.done
    }

    operator fun next(): String? {
        if (hasNext()) {
            this.start = end + 1
            this.end = this.nextDelimiter(this.start)
            this.token = this.text.substring(this.start, this.end)
        } else {
            this.start = this.end
            this.token = null
            this.done = true
        }
        return this.token
    }

    operator fun hasNext(): Boolean {
        return this.end < this.text.length
    }

    internal fun setStart(offset: Int): StringTokenIterator {
        if (offset > this.text.length) {
            throw IndexOutOfBoundsException()
        }
        this.start = offset
        this.end = this.nextDelimiter(this.start)
        this.token = this.text.substring(this.start, this.end)
        this.done = false
        return this
    }

    internal fun setText(text: String): StringTokenIterator {
        this.text = text
        this.setStart(0)
        return this
    }

    private fun nextDelimiter(start: Int): Int {
        val dlms = this.dlms
        val textlen = this.text.length
        if (dlms == null) {
            for (idx in start until textlen) {
                if (this.text[idx] == this.delimiterChar) {
                    return idx
                }
            }
        } else {
            val dlmslen = dlms.length
            for (idx in start until textlen) {
                val c = this.text[idx]
                for (i in 0 until dlmslen) {
                    if (c == dlms[i]) {
                        return idx
                    }
                }
            }
        }
        return textlen
    }
}