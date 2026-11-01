/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model.export

/**
 * How many teams/players/lineups/tournaments an export/import payload covers - shown before
 * either native file picker takes over, and again after, so the sheet's promise and the actual
 * result can be compared.
 *
 * @property teams
 * @property players
 * @property lineups
 * @property tournaments
 */
data class DataCounts(val teams: Int, val players: Int, val lineups: Int, val tournaments: Int)

fun ExportBase.toDataCounts(): DataCounts = DataCounts(
    teams = teams.size,
    players = teams.sumOf { it.players.size },
    lineups = teams.sumOf { team -> team.tournaments.sumOf { it.lineups.size } },
    tournaments = teams.sumOf { it.tournaments.size }
)
