/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.navigation

import kotlinx.serialization.Serializable

/**
 * Every destination the app's single [androidx.navigation.compose.NavHost] can show, with
 * type-safe route arguments. A lineup is one route, fixed or editable as [Lineup.editable]
 * says.
 */
sealed interface Routes {
    @Serializable
    data object Dashboard : Routes

    @Serializable
    data object Roster : Routes

    @Serializable
    data object Tournaments : Routes

    @Serializable
    data object Settings : Routes

    @Serializable
    data class PlayerDetails(val playerId: Long) : Routes

    @Serializable
    data class PlayerEdit(val playerId: Long = 0L) : Routes

    @Serializable
    data object LineupCreation : Routes

    @Serializable
    data object TeamDetails : Routes

    @Serializable
    data class TournamentStatistics(
        val tournamentId: Long,
        val tournamentName: String,
        val teamType: Int
    ) : Routes

    @Serializable
    data class Lineup(
        val lineupId: Long,
        val editable: Boolean,
        val fromShortcut: Boolean = false
    ) : Routes
}
