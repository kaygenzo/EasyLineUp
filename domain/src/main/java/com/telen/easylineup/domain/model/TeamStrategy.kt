package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.FieldPosition

enum class TeamStrategy(val id: Int, val batterSize: Int, val extraHitterSize: Int, vararg val positions: FieldPosition)
{
    STANDARD(0, 9, 0,
            FieldPosition.CATCHER,
            FieldPosition.PITCHER,
            FieldPosition.FIRST_BASE,
            FieldPosition.SECOND_BASE,
            FieldPosition.THIRD_BASE,
            FieldPosition.SHORT_STOP,
            FieldPosition.LEFT_FIELD,
            FieldPosition.CENTER_FIELD,
            FieldPosition.RIGHT_FIELD,
            FieldPosition.DP_DH
    ),
    SLOWPITCH(1, 10, 0,
            FieldPosition.CATCHER,
            FieldPosition.PITCHER,
            FieldPosition.FIRST_BASE,
            FieldPosition.SECOND_BASE,
            FieldPosition.THIRD_BASE,
            FieldPosition.SHORT_STOP,
            FieldPosition.SLOWPITCH_LF,
            FieldPosition.SLOWPITCH_LCF,
            FieldPosition.SLOWPITCH_RCF,
            FieldPosition.SLOWPITCH_RF,
            FieldPosition.DP_DH
    );/*,
    FIVE_MAN_SLOWPITCH(
            FieldPosition.CATCHER,
            FieldPosition.PITCHER,
            FieldPosition.FIRST_BASE,
            FieldPosition.SECOND_BASE,
            FieldPosition.THIRD_BASE,
            FieldPosition.SHORT_STOP,

            FieldPosition.LEFT_FIELD,
            FieldPosition.CENTER_FIELD,
            FieldPosition.RIGHT_FIELD
    )*/

    fun getDesignatedPlayerOrder(): Int {
        return batterSize + extraHitterSize + 1
    }

    companion object {
        fun getStrategyById(id: Int): TeamStrategy {
            values().forEach {
                if (it.id == id)
                    return it
            }
            return STANDARD
        }
    }
}