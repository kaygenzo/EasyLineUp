/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.TeamType

@StringRes
fun TeamType.titleRes(): Int = when (this) {
    TeamType.BASEBALL -> R.string.title_baseball
    TeamType.SOFTBALL -> R.string.title_softball
    TeamType.BASEBALL_5 -> R.string.title_baseball_5
    TeamType.UNKNOWN -> 0
}

@DrawableRes
fun TeamType.sportDrawableRes(): Int = when (this) {
    TeamType.BASEBALL -> R.drawable.pitcher_baseball_team
    TeamType.SOFTBALL -> R.drawable.pitcher_softball_team
    TeamType.BASEBALL_5 -> R.drawable.pitcher_baseball5_team
    TeamType.UNKNOWN -> 0
}

fun TeamType.getStrategiesDisplayName(context: Context): Array<String>? = when (this) {
    TeamType.SOFTBALL -> context.resources.getStringArray(R.array.softball_strategy_array)
    TeamType.BASEBALL -> context.resources.getStringArray(R.array.baseball_strategy_array)
    else -> null
}
