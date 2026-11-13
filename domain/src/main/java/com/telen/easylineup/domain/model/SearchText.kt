/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import java.text.Normalizer
import java.util.Locale

private val combiningMarks = Regex("\\p{Mn}+")

/**
 * Whether this text contains [query] the way a person searching means it: whatever the case or
 * the accents, in any alphabet ("ete" and "ÉTÉ" both find "Été"), and every character taken
 * literally. A blank query matches everything.
 */
fun String.matchesSearch(query: String): Boolean =
    query.isBlank() || foldForSearch().contains(query.trim().foldForSearch())

private fun String.foldForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(combiningMarks, "")
        .lowercase(Locale.ROOT)
