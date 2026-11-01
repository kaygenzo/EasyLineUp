/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * [PlayerTournamentStats] merged with the roster's own [Player] for the shirt number/photo the
 * stats alone don't carry.
 *
 * @property playerId
 * @property playerName
 * @property shirtNumber
 * @property image
 * @property totalGames
 * @property positionCounts
 */
data class PlayerTournamentStatsProfile(
    val playerId: Long,
    val playerName: String,
    val shirtNumber: Int,
    val image: String?,
    val totalGames: Int,
    val positionCounts: Map<FieldPosition, Int>
)
