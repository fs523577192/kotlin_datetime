package org.firas.datetime.util

/**
 *
 */
internal expect abstract class LocaleObjectCache<K, V>() {
    fun get(key: K): V?
    internal fun put(key: K, value: V): V?
    protected abstract fun createObject(key: K): V?
    protected fun normalizeKey(key: K): K
}