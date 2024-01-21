/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType

fun TeamType.getValidPositions(strategy: TeamStrategy): List<FieldPosition> {
    val default = listOf(
        FieldPosition.PITCHER,
        FieldPosition.CATCHER,
        FieldPosition.FIRST_BASE,
        FieldPosition.SECOND_BASE,
        FieldPosition.THIRD_BASE,
        FieldPosition.SHORT_STOP,
        FieldPosition.LEFT_FIELD,
        FieldPosition.CENTER_FIELD,
        FieldPosition.RIGHT_FIELD
    )
    return when (this) {
        TeamType.UNKNOWN, TeamType.BASEBALL -> when (strategy) {
            TeamStrategy.FIVE_MAN_STANDARD -> default - FieldPosition.CENTER_FIELD + FieldPosition.MID_FIELDER

            else -> default
        }

        TeamType.SOFTBALL -> when (strategy) {
            TeamStrategy.SLOWPITCH -> listOf(
                FieldPosition.PITCHER,
                FieldPosition.CATCHER,
                FieldPosition.FIRST_BASE,
                FieldPosition.SECOND_BASE,
                FieldPosition.THIRD_BASE,
                FieldPosition.SHORT_STOP,
                FieldPosition.SLOWPITCH_LF,
                FieldPosition.SLOWPITCH_LCF,
                FieldPosition.SLOWPITCH_RCF,
                FieldPosition.SLOWPITCH_RF
            )

            TeamStrategy.FIVE_MAN_STANDARD -> default - FieldPosition.CENTER_FIELD + FieldPosition.MID_FIELDER

            else -> default
        }

        TeamType.BASEBALL_5 -> listOf(
            FieldPosition.FIRST_BASE,
            FieldPosition.SECOND_BASE,
            FieldPosition.THIRD_BASE,
            FieldPosition.SHORT_STOP,
            FieldPosition.MID_FIELDER
        )
    }
}
