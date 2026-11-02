/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property lineupName
 * @property lineupId
 * @property position
 * @property playerName
 * @property playerId
 */
data class PlayerInLineup(
    val lineupName: String = "",
    val lineupId: Long = 0,
    val position: Int?,
    val playerName: String?,
    val playerId: Long?
)
