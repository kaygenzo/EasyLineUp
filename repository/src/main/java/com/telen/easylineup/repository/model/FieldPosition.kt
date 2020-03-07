package com.telen.easylineup.repository.model

import androidx.annotation.ColorRes
import com.telen.easylineup.repository.R

enum class FieldPosition(val position: Int, val mask: Int, val xPercent: Float, val yPercent: Float, @ColorRes val color: Int) {
    SUBSTITUTE(0, 0, 0f, 100f, R.color.brown),
    PITCHER(1,      0b1, 50f, 60f, R.color.blue_004a8d),
    CATCHER(2,      0b10, 50f, 87f, R.color.orange),
    FIRST_BASE(3,   0b100, 74f, 57f, R.color.red),
    SECOND_BASE(4,  0b1000, 63f, 44f, R.color.blue_5bc0de),
    THIRD_BASE(5,   0b10000, 27f, 59f, R.color.light_brown),
    SHORT_STOP(6,   0b100000, 37f, 43f, R.color.yellow),
    LEFT_FIELD(7,   0b1000000, 15f, 15f, R.color.green),
    CENTER_FIELD(8, 0b10000000, 50f, 10f, R.color.violet),
    RIGHT_FIELD(9,  0b100000000, 85f, 15f, R.color.lavanda),
    DH(10,  0b1000000000, 0f, 100f, R.color.beige);

    companion object {
        fun getFieldPosition(position: Int): FieldPosition? {
            values().forEach {
                if(position == it.position)
                    return it
            }
            return null
        }

        fun isSubstitute(position: Int): Boolean {
            return position == SUBSTITUTE.position
        }

        fun isDefensePlayer(position: Int): Boolean {
            return !isSubstitute(position) && position != DH.position
        }

        fun canBeBatterWhenDH(position: Int): Boolean {
            return !isSubstitute(position) && position != PITCHER.position
        }
    }
}