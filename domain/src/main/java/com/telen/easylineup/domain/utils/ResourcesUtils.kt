package com.telen.easylineup.domain.utils

import android.content.Context
import com.telen.easylineup.domain.R
import com.telen.easylineup.domain.model.TeamType

fun getPositionShortNames(context: Context, teamType: Int): Array<String> {
    return when (teamType) {
        TeamType.SOFTBALL.id -> {
            context.resources.getStringArray(R.array.field_positions_softball_list)
        }
        else -> {
            context.resources.getStringArray(R.array.field_positions_baseball_list)
        }
    }
}