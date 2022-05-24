package com.telen.easylineup.team.createTeam.teamType

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class TeamTypeCardItem(
    val type: Int,
    @StringRes val title: Int,
    @DrawableRes val ballResourceId: Int,
    @DrawableRes val compatBallResourceId: Int,
    @DrawableRes val representationId: Int
)
