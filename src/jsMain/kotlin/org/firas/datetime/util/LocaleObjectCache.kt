package org.firas.datetime.util

import org.firas.util.putIfAbsent

/**
 *
 * @author Wu Yuping
 */
internal actual abstract class LocaleObjectCache<K, V> {

    private val map: MutableMap<K, CacheEntry<K, V>>

    constructor(initialCapacity: Int, loadFactor: Float) {
        this.map = HashMap(initialCapacity, loadFactor)
    }

    actual constructor(): this(16, 0.75f)

    actual fun get(key: K): V? {
        var _key = key
        var value: V? = null
        var entry = this.map[key]
        if (null != entry) {
            value = entry.value
        }
        if (null == value) {
            _key = normalizeKey(key)
            val newValue = createObject(_key)
            if (null == _key || null == newValue) {
                // subclass must return non-null key/value object
                return null
            }

            val newEntry = CacheEntry(_key, newValue)
            entry = map.putIfAbsent(_key, newEntry)

            if (null == entry) {
                value = newValue
            } else {
                value = entry.value
                if (null == value) {
                    map[_key] = newEntry
                    value = newValue
                }
            }
        }
        return value
    }

    internal actual fun put(key: K, value: V): V? {
        val entry = CacheEntry(key, value)
        val oldEntry = this.map.put(key, entry)
        return oldEntry?.value
    }

    protected actual abstract fun createObject(key: K): V?

    protected actual fun normalizeKey(key: K): K {
        return key
    }

    companion object {
        private class CacheEntry<K, V>(
                internal val key: K, internal val value: V)
    }
}
