package com.telen.easylineup.utils

import java.util.*
import java.util.regex.Pattern

class StringUtils {
    fun nameToLetters(name: String): String {
        val parts: List<String> = name.trim().split(Pattern.compile("[\\s-]+"), 2).map {
            if(it.isNotBlank())
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
}