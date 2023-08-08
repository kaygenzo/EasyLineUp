package com.telen.easylineup.domain.model

enum class TeamStrategy(val id: Int, val batterSize: Int, vararg val positions: FieldPosition)
{
    STANDARD(0, 9,
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
    SLOWPITCH(1, 10,
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
    ),
    B5_DEFAULT(2, 5,
            FieldPosition.FIRST_BASE,
            FieldPosition.SECOND_BASE,
            FieldPosition.THIRD_BASE,
            FieldPosition.SHORT_STOP,
            FieldPosition.MID_FIELDER
    ),
    FIVE_MAN_STANDARD(3, 9,
        FieldPosition.CATCHER,
        FieldPosition.PITCHER,
        FieldPosition.FIRST_BASE,
        FieldPosition.SECOND_BASE,
        FieldPosition.THIRD_BASE,
        FieldPosition.SHORT_STOP,
        FieldPosition.LEFT_FIELD,
        FieldPosition.MID_FIELDER,
        FieldPosition.RIGHT_FIELD,
        FieldPosition.DP_DH
    );

    fun getDesignatedPlayerOrder(extraHitterSize: Int): Int {
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