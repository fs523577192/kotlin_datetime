/*
 * Copyright (c) 1997, 2018, Oracle and/or its affiliates. All rights reserved.
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
package org.firas.datetime.util

import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * An Entry maintaining an immutable key and value.  This class
 * does not support method `setValue`.  This class may be
 * convenient in methods that return thread-safe snapshots of
 * key-value mappings.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @author Wu Yuping (migrate to Kotlin)
 * @see Map
 * @see Collection
 * @since Java 1.6
 */
class SimpleImmutableEntry<K, V>(
    override val key: K,
    override val value: V
) : Map.Entry<K, V> {

    /**
     * Creates an entry representing the same mapping as the
     * specified entry.
     *
     * @param entry the entry to copy
     */
    @JsName("SimpleImmutableEntry_initWithMapEntry")
    constructor(entry: Map.Entry<out K, out V>) :
            this(entry.key, entry.value)

    companion object {
        private const val serialVersionUID = 7138329143949025153L
    }

    /**
     * Compares the specified object with this entry for equality.
     * Returns `true` if the given object is also a map entry and
     * the two entries represent the same mapping.  More formally, two
     * entries `e1` and `e2` represent the same mapping
     * if<pre>
     *   (e1.getKey()==null ?
     *    e2.getKey()==null :
     *    e1.getKey().equals(e2.getKey()))
     *   &amp;&amp;
     *   (e1.getValue()==null ?
     *    e2.getValue()==null :
     *    e1.getValue().equals(e2.getValue()))</pre>
     * This ensures that the `equals` method works properly across
     * different implementations of the `Map.Entry` interface.
     *
     * @param other object to be compared for equality with this map entry
     * @return `true` if the specified object is equal to this map
     *         entry
     * @see    #hashCode
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Map.Entry<*, *>) {
            return false
        }
        return this.key == other.key && this.value == other.value
    }

    /**
     * Returns the hash code value for this map entry.  The hash code
     * of a map entry `e` is defined to be: <pre>
     *   (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
     *   (e.getValue()==null ? 0 : e.getValue().hashCode())</pre>
     * This ensures that `e1.equals(e2)` implies that
     * `e1.hashCode()==e2.hashCode()` for any two Entries
     * `e1` and `e2`, as required by the general
     * contract of {@link Object#hashCode}.
     *
     * @return the hash code value for this map entry
     * @see    #equals
     */
    override fun hashCode(): Int {
        return (if (this.key == null) 0 else key.hashCode()) xor
                (if (this.value == null) 0 else value.hashCode())
    }

    /**
     * Returns a String representation of this map entry.  This
     * implementation returns the string representation of this
     * entry's key followed by the equals character ("`=`")
     * followed by the string representation of this entry's value.
     *
     * @return a String representation of this map entry
     */
    override fun toString(): String {
        return "$key=$value"
    }
}