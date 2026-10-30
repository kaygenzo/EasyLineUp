/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

/**
 * @property valueType
 */
enum class LineupTypeface(val valueType: Int) {
    NORMAL(0),
    HAND_WRITING(1),
    ;

    companion object {
        fun getByValue(value: String?): LineupTypeface {
            return when (value) {
                HAND_WRITING.valueType.toString() -> HAND_WRITING
                else -> NORMAL
            }
        }
    }
}
