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
    val position: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val order: Int = 0,
    val lineupName: String = "",
    val tournamentName: String = ""
)
