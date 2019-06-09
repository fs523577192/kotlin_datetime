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
 *
 * @author Wu Yuping (migrate from OpenJDK 11)
 */
internal class BaseLocale {

    companion object {
        internal const val SEP = "_"

        @JvmStatic
        private val CACHE = Cache()

        // Called for creating the Locale.* constants. No argument
        // validation is performed.
        @JsName("createInstance")
        @JvmStatic
        fun createInstance(language: String, region: String): BaseLocale {
            val base = BaseLocale(language, "", region, "", false)
            CACHE.put(BaseLocaleKey(base), base)
            return base
        }

        private class Cache: LocaleObjectCache<BaseLocaleKey, BaseLocale>() {

            override fun createObject(key: BaseLocaleKey): BaseLocale? {
                return normalizeBaseLocaleKey(key).getBaseLocale()
            }
        }
    }

    internal val language: String
    internal val script: String
    internal val region: String
    internal val variant: String

    internal constructor(language: String, script: String, region: String,
                         variant: String, normalize: Boolean) {
        if (normalize) {
            this.language = LocaleUtils.toLowerString(language)
            this.script = LocaleUtils.toTitleString(script)
            this.region = LocaleUtils.toUpperString(region)
        } else {
            this.language = language
            this.script = script
            this.region = region
        }
        this.variant = variant
    }

}

internal expect class BaseLocaleKey {

    internal constructor(locale: BaseLocale)

    internal constructor(language: String?, script: String?, region: String?,
                         variant: String?, normalize: Boolean)

    internal fun getBaseLocale(): BaseLocale?
}

internal expect fun normalizeBaseLocaleKey(key: BaseLocaleKey): BaseLocaleKey
