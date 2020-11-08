package com.telen.easylineup.domain.model

import com.telen.easylineup.domain.model.FieldPosition

enum class TeamStrategy(vararg val positions: FieldPosition)
{
    STANDARD(
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
    SLOWPITCH(
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
}