/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property position
 * @property x
 * @property y
 * @property order
 * @property lineupName
 * @property tournamentName
 */
data class PositionWithLineup(
    var position: Int = 0,
    var x: Float = 0f,
    var y: Float = 0f,
    var order: Int = 0,
    var lineupName: String = "",
    var tournamentName: String = ""
)
