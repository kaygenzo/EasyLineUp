package com.telen.easylineup.domain.model

import androidx.annotation.ColorRes
import com.telen.easylineup.domain.R

enum class Sex(val id: Int, @ColorRes val colorRes: Int) {
    UNKNOWN(0, 0),
    MALE(1, R.color.red),
    FEMALE(2, R.color.yellow);

    companion object {
        fun getById(id: Int): Sex {
            return values().firstOrNull { it.id == id } ?: UNKNOWN
        }
    }
}