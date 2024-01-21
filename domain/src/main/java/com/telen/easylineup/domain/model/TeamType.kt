/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.telen.easylineup.domain.R

/**
 * @property id
 * @property position
 * @property title
 * @property sportResId
 * @property defaultStrategy
 */
enum class TeamType(
    val id: Int,
    val position: Int,
    @StringRes val title: Int,
    @DrawableRes val sportResId: Int,
    val defaultStrategy: TeamStrategy
) {
    UNKNOWN(0, -1, 0, 0, TeamStrategy.STANDARD),
    BASEBALL(
        1,
        0,
        R.string.title_baseball,
        R.drawable.pitcher_baseball_team,
        TeamStrategy.STANDARD
    ),
    SOFTBALL(
        2,
        1,
        R.string.title_softball,
        R.drawable.pitcher_softball_team,
        TeamStrategy.STANDARD
    ),
    BASEBALL_5(
        3,
        2,
        R.string.title_baseball_5,
        R.drawable.pitcher_baseball5_team,
        TeamStrategy.B5_DEFAULT
    ),
    ;

    fun getStrategies(): Array<TeamStrategy> {
        return when (this) {
            SOFTBALL -> arrayOf(
                TeamStrategy.STANDARD,
                TeamStrategy.FIVE_MAN_STANDARD,
                TeamStrategy.SLOWPITCH
            )

            BASEBALL_5 -> arrayOf(TeamStrategy.B5_DEFAULT)
            else -> arrayOf(TeamStrategy.STANDARD, TeamStrategy.FIVE_MAN_STANDARD)
        }
    }

    fun getStrategiesDisplayName(context: Context): Array<String>? {
        return when (this) {
            SOFTBALL -> context.resources.getStringArray(R.array.softball_strategy_array)

            BASEBALL -> context.resources.getStringArray(R.array.baseball_strategy_array)

            else -> null
        }
    }

    companion object {
        fun getTypeById(id: Int): TeamType {
            values().forEach {
                if (it.id == id) {
                    return it
                }
            }
            return UNKNOWN
        }
    }
}
