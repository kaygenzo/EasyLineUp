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
    var lineupName: String = "",
    var lineupId: Long = 0,
    var position: Int?,
    var playerName: String?,
    var playerId: Long?
)
