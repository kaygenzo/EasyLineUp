/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import androidx.annotation.ColorRes
import com.telen.easylineup.domain.R

/**
 * @property id
 * @property defaultColorRes
 */
enum class Sex(val id: Int, @ColorRes val defaultColorRes: Int) {
    UNKNOWN(0, 0),
    MALE(1, R.color.default_men_color),
    FEMALE(2, R.color.default_women_color),
    ;

    companion object {
        fun getById(id: Int): Sex {
            return values().firstOrNull { it.id == id } ?: UNKNOWN
        }
    }
}
