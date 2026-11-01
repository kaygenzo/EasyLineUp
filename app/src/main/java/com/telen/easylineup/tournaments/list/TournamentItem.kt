/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.GetTournamentDisplayStart

/**
 * @property tournament
 * @property lineups
 */
data class TournamentItem(val tournament: Tournament, val lineups: List<Lineup>)

fun TournamentItem.getStart(
    getTournamentDisplayStart: GetTournamentDisplayStart = GetTournamentDisplayStart()
): Long? {
    return getTournamentDisplayStart(tournament, lineups)
}
