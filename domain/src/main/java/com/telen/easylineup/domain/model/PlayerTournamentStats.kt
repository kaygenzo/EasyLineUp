/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property playerId
 * @property playerName
 * @property totalGames non-substitute appearances across every lineup in the tournament
 * @property positionCounts how many of those appearances were at each field position
 */
data class PlayerTournamentStats(
    val playerId: Long,
    val playerName: String,
    val totalGames: Int,
    val positionCounts: Map<FieldPosition, Int>
)
