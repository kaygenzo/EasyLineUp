/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property id
 */
enum class Sex(val id: Int) {
    UNKNOWN(0),
    MALE(1),
    FEMALE(2),
    ;

    companion object {
        fun getById(id: Int): Sex {
            return values().firstOrNull { it.id == id } ?: UNKNOWN
        }
    }
}
