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

/**
 * The positions actually played by anyone in this tournament, in field order - not one
 * strategy's theoretical set: a team that played under more than one strategy (e.g. softball's
 * Slowpitch, with 4 outfielders instead of 3) would otherwise lose real data.
 */
fun List<PlayerTournamentStatsProfile>.playedPositions(): List<FieldPosition> =
    flatMap { it.positionCounts.keys }.distinct().sortedBy { it.id }

/** Alphabetical (case-insensitive), the order a coach scans a list of names in. */
fun List<PlayerTournamentStatsProfile>.sortedByName(): List<PlayerTournamentStatsProfile> =
    sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.playerName })
