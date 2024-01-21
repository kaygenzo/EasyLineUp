/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property player
 * @property status
 * @property playerNumberOverlay
 */
data class RosterPlayerStatus(
    val player: Player,
    var status: Boolean = true,
    val playerNumberOverlay: PlayerNumberOverlay?
)
