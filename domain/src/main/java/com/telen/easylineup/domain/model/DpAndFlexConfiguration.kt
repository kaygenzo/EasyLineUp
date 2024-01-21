/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property dp
 * @property flex
 * @property dpLocked
 * @property flexLocked
 * @property teamType
 */
data class DpAndFlexConfiguration(
    val dp: PlayerWithPosition?,
    val flex: PlayerWithPosition?,
    val dpLocked: Boolean,
    val flexLocked: Boolean,
    val teamType: Int
)
