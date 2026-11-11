/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import java.io.Serializable

/**
 * @property player
 * @property status
 * @property playerNumberOverlay
 */
data class RosterPlayerStatus(
    val player: Player,
    val status: Boolean = true,
    val playerNumberOverlay: PlayerNumberOverlay?
) : Serializable
