package com.telen.easylineup.domain.model

data class DpAndFlexConfiguration(
    val dp: PlayerWithPosition?,
    val flex: PlayerWithPosition?,
    val dpLocked: Boolean,
    val flexLocked: Boolean,
    val teamType: Int
)