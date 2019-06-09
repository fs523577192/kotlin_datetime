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

import kotlin.js.JsName
import kotlin.jvm.JvmStatic


/**
 *
 * @author Wu Yuping (migrate from OpenJDK 11)
 */
internal class LanguageTag {

    companion object {
        internal const val SEP = "-"
        internal const val PRIVATEUSE = "x"
        internal const val UNDETERMINED = "und"
        internal const val PRIVUSE_VARIANT_PREFIX = "lvariant"

        // Map contains grandfathered tags and its preferred mappings from
        // http://www.ietf.org/rfc/rfc5646.txt
        // Keys are lower-case strings.
        private val GRANDFATHERED = HashMap<String, Array<String>>()

        init {
            // grandfathered = irregular           ; non-redundant tags registered
            //               / regular             ; during the RFC 3066 era
            //
            // irregular     = "en-GB-oed"         ; irregular tags do not match
            //               / "i-ami"             ; the 'langtag' production and
            //               / "i-bnn"             ; would not otherwise be
            //               / "i-default"         ; considered 'well-formed'
            //               / "i-enochian"        ; These tags are all valid,
            //               / "i-hak"             ; but most are deprecated
            //               / "i-klingon"         ; in favor of more modern
            //               / "i-lux"             ; subtags or subtag
            //               / "i-mingo"           ; combination
            //               / "i-navajo"
            //               / "i-pwn"
            //               / "i-tao"
            //               / "i-tay"
            //               / "i-tsu"
            //               / "sgn-BE-FR"
            //               / "sgn-BE-NL"
            //               / "sgn-CH-DE"
            //
            // regular       = "art-lojban"        ; these tags match the 'langtag'
            //               / "cel-gaulish"       ; production, but their subtags
            //               / "no-bok"            ; are not extended language
            //               / "no-nyn"            ; or variant subtags: their meaning
            //               / "zh-guoyu"          ; is defined by their registration
            //               / "zh-hakka"          ; and all of these are deprecated
            //               / "zh-min"            ; in favor of a more modern
            //               / "zh-min-nan"        ; subtag or sequence of subtags
            //               / "zh-xiang"

            val entries = arrayOf(
                //{"tag",         "preferred"},
                arrayOf("art-lojban", "jbo"),
                arrayOf("cel-gaulish", "xtg-x-cel-gaulish"), // fallback
                arrayOf("en-GB-oed", "en-GB-x-oed"), // fallback
                arrayOf("i-ami", "ami"),
                arrayOf("i-bnn", "bnn"),
                arrayOf("i-default", "en-x-i-default"), // fallback
                arrayOf("i-enochian", "und-x-i-enochian"), // fallback
                arrayOf("i-hak", "hak"),
                arrayOf("i-klingon", "tlh"),
                arrayOf("i-lux", "lb"),
                arrayOf("i-mingo", "see-x-i-mingo"), // fallback
                arrayOf("i-navajo", "nv"),
                arrayOf("i-pwn", "pwn"),
                arrayOf("i-tao", "tao"),
                arrayOf("i-tay", "tay"),
                arrayOf("i-tsu", "tsu"),
                arrayOf("no-bok", "nb"),
                arrayOf("no-nyn", "nn"),
                arrayOf("sgn-BE-FR", "sfb"),
                arrayOf("sgn-BE-NL", "vgt"),
                arrayOf("sgn-CH-DE", "sgg"),
                arrayOf("zh-guoyu", "cmn"),
                arrayOf("zh-hakka", "hak"),
                arrayOf("zh-min", "nan-x-zh-min"), // fallback
                arrayOf("zh-min-nan", "nan"),
                arrayOf("zh-xiang", "hsn")
            )
            for (e in entries) {
                GRANDFATHERED[LocaleUtils.toLowerString(e[0])] = e
            }
        } // Companion.init

        /*
         * BNF in RFC5646
         *
         * Language-Tag  = langtag             ; normal language tags
         *               / privateuse          ; private use tag
         *               / grandfathered       ; grandfathered tags
         *
         *
         * langtag       = language
         *                 ["-" script]
         *                 ["-" region]
         *                 *("-" variant)
         *                 *("-" extension)
         *                 ["-" privateuse]
         *
         * language      = 2*3ALPHA            ; shortest ISO 639 code
         *                 ["-" extlang]       ; sometimes followed by
         *                                     ; extended language subtags
         *               / 4ALPHA              ; or reserved for future use
         *               / 5*8ALPHA            ; or registered language subtag
         *
         * extlang       = 3ALPHA              ; selected ISO 639 codes
         *                 *2("-" 3ALPHA)      ; permanently reserved
         *
         * script        = 4ALPHA              ; ISO 15924 code
         *
         * region        = 2ALPHA              ; ISO 3166-1 code
         *               / 3DIGIT              ; UN M.49 code
         *
         * variant       = 5*8alphanum         ; registered variants
         *               / (DIGIT 3alphanum)
         *
         * extension     = singleton 1*("-" (2*8alphanum))
         *
         *                                     ; Single alphanumerics
         *                                     ; "x" reserved for private use
         * singleton     = DIGIT               ; 0 - 9
         *               / %x41-57             ; A - W
         *               / %x59-5A             ; Y - Z
         *               / %x61-77             ; a - w
         *               / %x79-7A             ; y - z
         *
         * privateuse    = "x" 1*("-" (1*8alphanum))
         *
         */
        @JsName("parse")
        @JvmStatic
        internal fun parse(languageTag: String, parseStatus: ParseStatus?): LanguageTag {
            val sts = if (parseStatus == null) ParseStatus()
                    else {
                        parseStatus.reset()
                        parseStatus
                    }

            // Check if the tag is grandfathered
            val gfmap = GRANDFATHERED[LocaleUtils.toLowerString(languageTag)]
            val itr: StringTokenIterator = if (gfmap != null) {
                // use preferred mapping
                StringTokenIterator(gfmap[1], SEP)
            } else {
                StringTokenIterator(languageTag, SEP)
            }

            val tag = LanguageTag()

            // langtag must start with either language or privateuse
            if (tag.parseLanguage(itr, sts)) {
                tag.parseExtlangs(itr, sts)
                tag.parseScript(itr, sts)
                tag.parseRegion(itr, sts)
                tag.parseVariants(itr, sts)
                tag.parseExtensions(itr, sts)
            }
            tag.parsePrivateuse(itr, sts)

            if (!itr.isDone() && !sts.isError()) {
                val s: String? = itr.current()
                sts.errorIndex = itr.currentStart()
                if (s.isNullOrEmpty()) {
                    sts.errorMessage = "Empty subtag"
                } else {
                    sts.errorMessage = "Invalid subtag: $s"
                }
            }

            return tag
        }

        @JsName("parseLocale")
        @JvmStatic
        internal fun parseLocale(baseLocale: BaseLocale, localeExtensions: LocaleExtensions): LanguageTag {
            val tag = LanguageTag()

            var language = baseLocale.language
            var script = baseLocale.script
            var region = baseLocale.region
            var variant = baseLocale.variant

            var hasSubtag = false

            var privuseVar: String? = null   // store ill-formed variant subtags

            if (isLanguage(language)) {
                // Convert a deprecated language code to its new code
                if (language == "iw") {
                    language = "he"
                } else if (language == "ji") {
                    language = "yi"
                } else if (language == "in") {
                    language = "id"
                }
                tag.language = language
            }

            if (isScript(script)) {
                tag.script = canonicalizeScript(script)
                hasSubtag = true
            }

            if (isRegion(region)) {
                tag.region = canonicalizeRegion(region)
                hasSubtag = true
            }

            // Special handling for no_NO_NY - use nn_NO for language tag
            if (tag.language.equals("no") && tag.region.equals("NO") && variant.equals("NY")) {
                tag.language = "nn"
                variant = ""
            }

            if (variant.length > 0) {
                var variants: MutableList<String>? = null
                var varitr = StringTokenIterator(variant, BaseLocale.SEP)
                while (!varitr.isDone()) {
                    val varStr: String = varitr.current()!!
                    if (!isVariant(varStr)) {
                        break
                    }
                    if (variants == null) {
                        variants = ArrayList()
                    }
                    variants.add(varStr)  // Do not canonicalize!
                    varitr.next()
                }
                if (variants != null) {
                    tag.variants = variants
                    hasSubtag = true
                }
                if (!varitr.isDone()) {
                    // ill-formed variant subtags
                    val prvvList = ArrayList<String>()
                    while (!varitr.isDone()) {
                        val prvv: String = varitr.current()!!
                        if (!isPrivateuseSubtag(prvv)) {
                            // cannot use private use subtag - truncated
                            break
                        }
                        prvvList.add(prvv)
                        varitr.next()
                    }
                    if (prvvList.isNotEmpty()) {
                        privuseVar = prvvList.joinToString(SEP)
                    }
                }
            }

            var extensions: MutableList<String>? = null
            var privateuse: String? = null

            if (localeExtensions != null) {
                val locextKeys = localeExtensions.getKeys()
                for (locextKey in locextKeys) {
                    val ext = localeExtensions.getExtension(locextKey)
                    if (isPrivateusePrefixChar(locextKey)) {
                        privateuse = ext!!.value
                    } else {
                        if (extensions == null) {
                            extensions = ArrayList()
                        }
                        extensions.add(locextKey.toString() + SEP + ext!!.value)
                    }
                }
            }

            if (extensions != null) {
                tag.extensions = extensions
                hasSubtag = true
            }

            // append ill-formed variant subtags to private use
            if (privuseVar != null) {
                if (privateuse == null) {
                    privateuse = PRIVUSE_VARIANT_PREFIX + SEP + privuseVar
                } else {
                    privateuse = privateuse + SEP + PRIVUSE_VARIANT_PREFIX +
                                 SEP + privuseVar.replace(BaseLocale.SEP, SEP)
                }
            }

            if (privateuse != null) {
                tag.privateuse = privateuse
            }

            if (tag.language.isEmpty() && (hasSubtag || privateuse == null)) {
                // use lang "und" when 1) no language is available AND
                // 2) any of other subtags other than private use are available or
                // no private use tag is available
                tag.language = UNDETERMINED
            }

            return tag
        }

        //
        // Language subtag syntax checking methods
        //

        @JsName("isLanguage")
        @JvmStatic
        internal fun isLanguage(s: String): Boolean {
            // language      = 2*3ALPHA            ; shortest ISO 639 code
            //                 ["-" extlang]       ; sometimes followed by
            //                                     ;   extended language subtags
            //               / 4ALPHA              ; or reserved for future use
            //               / 5*8ALPHA            ; or registered language subtag
            val len = s.length
            return len in 2..8 && LocaleUtils.isAlphaString(s)
        }

        @JsName("isExtlang")
        @JvmStatic
        internal fun isExtlang(s: String): Boolean {
            // extlang       = 3ALPHA              ; selected ISO 639 codes
            //                 *2("-" 3ALPHA)      ; permanently reserved
            return s.length == 3 && LocaleUtils.isAlphaString(s)
        }

        @JsName("isScript")
        @JvmStatic
        internal fun isScript(s: String): Boolean {
            // script        = 4ALPHA              ; ISO 15924 code
            return s.length == 4 && LocaleUtils.isAlphaString(s)
        }

        @JsName("isRegion")
        @JvmStatic
        internal fun isRegion(s: String): Boolean {
            // region        = 2ALPHA              ; ISO 3166-1 code
            //               / 3DIGIT              ; UN M.49 code
            return s.length == 2 && LocaleUtils.isAlphaString(s) ||
                    s.length == 3 && LocaleUtils.isNumericString(s)
        }

        @JsName("isVariant")
        @JvmStatic
        internal fun isVariant(s: String): Boolean {
            // variant       = 5*8alphanum         ; registered variants
            //               / (DIGIT 3alphanum)
            val len = s.length
            if (len in 5..8) {
                return LocaleUtils.isAlphaNumericString(s)
            }
            return len == 4
                    && LocaleUtils.isNumeric(s[0])
                    && LocaleUtils.isAlphaNumeric(s[1])
                    && LocaleUtils.isAlphaNumeric(s[2])
                    && LocaleUtils.isAlphaNumeric(s[3])
        }

        @JsName("isExtensionSingleton")
        @JvmStatic
        internal fun isExtensionSingleton(s: String): Boolean {
            // singleton     = DIGIT               ; 0 - 9
            //               / %x41-57             ; A - W
            //               / %x59-5A             ; Y - Z
            //               / %x61-77             ; a - w
            //               / %x79-7A             ; y - z

            return (s.length == 1
                    && LocaleUtils.isAlphaString(s)
                    && !LocaleUtils.caseIgnoreMatch(PRIVATEUSE, s))
        }

        @JsName("isExtensionSingletonChar")
        @JvmStatic
        internal fun isExtensionSingletonChar(c: Char): Boolean {
            return isExtensionSingleton(c.toString())
        }

        @JsName("isExtensionSubtag")
        @JvmStatic
        internal fun isExtensionSubtag(s: String): Boolean {
            // extension     = singleton 1*("-" (2*8alphanum))
            val len = s.length
            return len in 2..8 && LocaleUtils.isAlphaNumericString(s)
        }

        @JsName("isPrivateusePrefix")
        @JvmStatic
        internal fun isPrivateusePrefix(s: String): Boolean {
            // privateuse    = "x" 1*("-" (1*8alphanum))
            return s.length == 1 && LocaleUtils.caseIgnoreMatch(PRIVATEUSE, s)
        }

        @JsName("isPrivateusePrefixChar")
        @JvmStatic
        internal fun isPrivateusePrefixChar(c: Char): Boolean {
            return LocaleUtils.caseIgnoreMatch(PRIVATEUSE, c.toString())
        }

        @JsName("isPrivateuseSubtag")
        @JvmStatic
        internal fun isPrivateuseSubtag(s: String): Boolean {
            // privateuse    = "x" 1*("-" (1*8alphanum))
            val len = s.length
            return len in 1..8 && LocaleUtils.isAlphaNumericString(s)
        }

        //
        // Language subtag canonicalization methods
        //

        @JsName("canonicalizeLanguage")
        @JvmStatic
        internal fun canonicalizeLanguage(s: String): String {
            return LocaleUtils.toLowerString(s)
        }

        @JsName("canonicalizeExtlang")
        @JvmStatic
        internal fun canonicalizeExtlang(s: String): String {
            return LocaleUtils.toLowerString(s)
        }

        @JsName("canonicalizeScript")
        @JvmStatic
        internal fun canonicalizeScript(s: String): String {
            return LocaleUtils.toTitleString(s)
        }

        @JsName("canonicalizeRegion")
        @JvmStatic
        internal fun canonicalizeRegion(s: String): String {
            return LocaleUtils.toUpperString(s)
        }

        @JsName("canonicalizeVariant")
        @JvmStatic
        internal fun canonicalizeVariant(s: String): String {
            return LocaleUtils.toLowerString(s)
        }

        @JsName("canonicalizeExtension")
        @JvmStatic
        internal fun canonicalizeExtension(s: String): String {
            return LocaleUtils.toLowerString(s)
        }

        @JsName("canonicalizeExtensionSingleton")
        @JvmStatic
        internal fun canonicalizeExtensionSingleton(s: String): String {
            return LocaleUtils.toLowerString(s)
        }

        @JsName("canonicalizeExtensionSubtag")
        @JvmStatic
        internal fun canonicalizeExtensionSubtag(s: String): String {
            return LocaleUtils.toLowerString(s)
        }

        @JsName("canonicalizePrivateuse")
        @JvmStatic
        internal fun canonicalizePrivateuse(s: String): String {
            return LocaleUtils.toLowerString(s)
        }

        @JsName("canonicalizePrivateuseSubtag")
        @JvmStatic
        internal fun canonicalizePrivateuseSubtag(s: String): String {
            return LocaleUtils.toLowerString(s)
        }
    } // companion object

    private var language: String      // language subtag
    private var script: String        // script subtag
    private var region: String        // region subtag
    private var privateuse: String    // privateuse

    private var extlangs: MutableList<String>   // extlang subtags
    private var variants: MutableList<String>   // variant subtags
    private var extensions: MutableList<String> // extensions

    private constructor() {
        this.language = ""
        this.script = ""
        this.region = ""
        this.privateuse = ""

        this.extlangs = ArrayList(0)
        this.variants = ArrayList(0)
        this.extensions = ArrayList(0)
    }

    private fun parseLanguage(itr: StringTokenIterator, sts: ParseStatus): Boolean {
        if (itr.isDone() || sts.isError()) {
            return false
        }

        var found = false

        val s: String = itr.current()!!
        if (isLanguage(s)) {
            found = true
            this.language = s
            sts.parseLength = itr.currentEnd()
            itr.next()
        }

        return found
    }

    private fun parseExtlangs(itr: StringTokenIterator, sts: ParseStatus): Boolean {
        if (itr.isDone() || sts.isError()) {
            return false
        }

        var found = false

        while (!itr.isDone()) {
            val s: String = itr.current()!!
            if (!isExtlang(s)) {
                break
            }
            found = true
            if (this.extlangs.isEmpty()) {
                this.extlangs = ArrayList(3)
            }
            this.extlangs.add(s)
            sts.parseLength = itr.currentEnd()
            itr.next()

            if (extlangs.size == 3) {
                // Maximum 3 extlangs
                break
            }
        }

        return found
    }

    private fun parseScript(itr: StringTokenIterator, sts: ParseStatus): Boolean {
        if (itr.isDone() || sts.isError()) {
            return false
        }

        var found = false

        val s: String = itr.current()!!
        if (isScript(s)) {
            found = true
            this.script = s
            sts.parseLength = itr.currentEnd()
            itr.next()
        }

        return found
    }

    private fun parseRegion(itr: StringTokenIterator, sts: ParseStatus): Boolean {
        if (itr.isDone() || sts.isError()) {
            return false
        }

        var found = false

        val s: String = itr.current()!!
        if (isRegion(s)) {
            found = true
            this.region = s
            sts.parseLength = itr.currentEnd()
            itr.next()
        }

        return found
    }

    private fun parseVariants(itr: StringTokenIterator, sts: ParseStatus): Boolean {
        if (itr.isDone() || sts.isError()) {
            return false
        }

        var found = false

        while (!itr.isDone()) {
            val s: String = itr.current()!!
            if (!isVariant(s)) {
                break
            }
            found = true
            if (this.variants.isEmpty()) {
                this.variants = ArrayList(3)
            }
            this.variants.add(s)
            sts.parseLength = itr.currentEnd()
            itr.next()
        }

        return found
    }

    private fun parseExtensions(itr: StringTokenIterator, sts: ParseStatus): Boolean {
        if (itr.isDone() || sts.isError()) {
            return false
        }

        var found = false

        while (!itr.isDone()) {
            var s: String = itr.current()!!
            if (isExtensionSingleton(s)) {
                val start = itr.currentStart()
                val singleton = s
                val sb = StringBuilder().append(singleton)

                itr.next()
                while (!itr.isDone()) {
                    s = itr.current()!!
                    if (isExtensionSubtag(s)) {
                        sb.append(SEP).append(s)
                        sts.parseLength = itr.currentEnd()
                    } else {
                        break
                    }
                    itr.next()
                }

                if (sts.parseLength <= start) {
                    sts.errorIndex = start
                    sts.errorMessage = "Incomplete extension '$singleton'"
                    break
                }

                if (this.extensions.isEmpty()) {
                    this.extensions = ArrayList(4)
                }
                this.extensions.add(sb.toString())
                found = true
            } else {
                break
            }
        }
        return found
    } // private fun parseExtensions(itr: StringTokenIterator, sts: ParseStatus)

    private fun parsePrivateuse(itr: StringTokenIterator, sts: ParseStatus): Boolean {
        if (itr.isDone() || sts.isError()) {
            return false
        }

        var found = false

        var s: String = itr.current()!!
        if (isPrivateusePrefix(s)) {
            val start = itr.currentStart()
            val sb = StringBuilder(s)

            itr.next()
            while (!itr.isDone()) {
                s = itr.current()!!
                if (!isPrivateuseSubtag(s)) {
                    break
                }
                sb.append(SEP).append(s)
                sts.parseLength = itr.currentEnd()

                itr.next()
            }

            if (sts.parseLength <= start) {
                // need at least 1 private subtag
                sts.errorIndex = start
                sts.errorMessage = "Incomplete privateuse"
            } else {
                privateuse = sb.toString()
                found = true
            }
        }

        return found
    }

    //
    // Getter methods for language subtag fields
    //
    @JsName("getLanguage")
    internal fun getLanguage(): String {
        return this.language
    }

    @JsName("getExtlangs")
    internal fun getExtlangs(): List<String> {
        return this.extlangs
    }

    @JsName("getScript")
    internal fun getScript(): String {
        return this.script
    }

    @JsName("getRegion")
    internal fun getRegion(): String {
        return this.region
    }

    @JsName("getVariants")
    internal fun getVariants(): List<String> {
        return this.variants
    }

    @JsName("getExtensions")
    internal fun getExtensions(): List<String> {
        return this.extensions
    }

    @JsName("getPrivateuse")
    internal fun getPrivateuse(): String {
        return this.privateuse
    }

    override fun toString(): String {
        val sb = StringBuilder()

        if (this.language.isNotEmpty()) {
            sb.append(this.language)

            for (extlang in this.extlangs) {
                sb.append(SEP).append(extlang)
            }

            if (this.script.isNotEmpty()) {
                sb.append(SEP).append(this.script)
            }

            if (this.region.isNotEmpty()) {
                sb.append(SEP).append(this.region)
            }

            for (variant in this.variants) {
                sb.append(SEP).append(variant)
            }

            for (extension in this.extensions) {
                sb.append(SEP).append(extension)
            }
        }
        if (this.privateuse.isNotEmpty()) {
            if (sb.isNotEmpty()) {
                sb.append(SEP)
            }
            sb.append(this.privateuse)
        }

        return sb.toString()
    }
}