/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property flag
 */
enum class PlayerSide(val flag: Int) {
    LEFT(0b01),
    RIGHT(0b10),
    BOTH(0b11),
    ;

    companion object {
        fun getSideByValue(value: Int): PlayerSide? {
            values().forEach {
                if (value == it.flag) {
                    return it
                }
            }
            return null
        }
    }
}
