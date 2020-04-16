package com.telen.easylineup.repository.model

import androidx.annotation.ColorRes
import com.telen.easylineup.repository.R

enum class FieldPosition(val position: Int, val mask: Int, val xPercent: Float, val yPercent: Float, @ColorRes val color: Int) {
    SUBSTITUTE(0, 0, 0f, 100f, R.color.brown),
    PITCHER(1,      0x01, 50f, 60f, R.color.blue_004a8d),
    CATCHER(2,      0x02, 50f, 87f, R.color.orange),
    FIRST_BASE(3,   0x04, 74f, 57f, R.color.red),
    SECOND_BASE(4,  0x08, 63f, 44f, R.color.blue_5bc0de),
    THIRD_BASE(5,   0x10, 27f, 59f, R.color.light_brown),
    SHORT_STOP(6,   0x20, 37f, 43f, R.color.yellow),
    LEFT_FIELD(7,   0x40, 15f, 15f, R.color.green),
    CENTER_FIELD(8, 0x80, 50f, 10f, R.color.violet),
    RIGHT_FIELD(9,  0x0100, 85f, 15f, R.color.lavanda),
    DP_DH(10,  0x0200, 0f, 100f, R.color.beige);

    companion object {
        fun getFieldPosition(position: Int): FieldPosition? {
            values().forEach {
                if(position == it.position)
                    return it
            }
            return null
        }

        //TODO move in domain
        fun isSubstitute(position: Int): Boolean {
            return position == SUBSTITUTE.position
        }

        //TODO move in domain
        fun isDefensePlayer(position: Int): Boolean {
            return !isSubstitute(position) && position != DP_DH.position
        }

        //TODO move in domain
        fun canBeBatterWhenModeEnabled(position: Int, flags: Int): Boolean {
            return !isSubstitute(position) && (flags and PlayerFieldPosition.FLAG_FLEX > 0)
        }
    }
}