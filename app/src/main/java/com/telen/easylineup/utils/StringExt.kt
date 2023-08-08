package com.telen.easylineup.utils

import java.util.Locale
import java.util.regex.Pattern

fun String.toLetters(): String {
    val parts: List<String> = trim().split(Pattern.compile("[\\s-]+"), 2).map {
        if (it.isNotBlank())
            it[0].toString().toUpperCase(Locale.ROOT)
        else
            "?"
    }
    val builder = StringBuilder()
    parts.forEach {
        builder.append(it)
    }
    return builder.toString()
}