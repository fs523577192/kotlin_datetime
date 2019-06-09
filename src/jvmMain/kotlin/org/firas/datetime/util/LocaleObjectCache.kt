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

import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author Wu Yuping
 */
internal actual abstract class LocaleObjectCache<K, V> {

    private val map: MutableMap<K, CacheEntry<K, V>>
    private val queue = ReferenceQueue<V>()

    constructor(initialCapacity: Int, loadFactor: Float, concurrencyLevel: Int) {
        this.map = ConcurrentHashMap(initialCapacity, loadFactor, concurrencyLevel)
    }

    actual constructor(): this(16, 0.75f, 16)

    actual fun get(key: K): V? {
        var _key = key
        var value: V? = null

        cleanStaleEntries()
        var entry = this.map[key]
        if (null != entry) {
            value = entry.get()
        }
        if (null == value) {
            _key = normalizeKey(key)
            val newValue = createObject(_key)
            if (null == _key || null == newValue) {
                // subclass must return non-null key/value object
                return null
            }

            val newEntry = CacheEntry(_key, newValue, this.queue)
            entry = map.putIfAbsent(_key, newEntry)
            if (null == entry) {
                value = newValue
            } else {
                value = entry.get()
                if (null == value) {
                    this.map[_key] = newEntry
                    value = newValue
                }
            }
        }
        return value
    }

    internal actual fun put(key: K, value: V): V? {
        val entry = CacheEntry(key, value, this.queue)
        val oldEntry = this.map.put(key, entry)
        return oldEntry?.get()
    }

    protected actual abstract fun createObject(key: K): V?

    protected actual fun normalizeKey(key: K): K {
        return key
    }

    private fun cleanStaleEntries() {
        var entry = this.queue.poll() as CacheEntry<K, V>
        while (null != entry) {
            map.remove(entry.key, entry)
            entry = this.queue.poll() as CacheEntry<K, V>
        }
    }

    companion object {
        private class CacheEntry<K, V>(internal val key: K, value: V, queue: ReferenceQueue<V>):
                SoftReference<V>(value, queue)
    }
}