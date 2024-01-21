/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.tournaments.list

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament

/**
 * @property tournament
 * @property lineups
 */
data class TournamentItem(val tournament: Tournament, val lineups: List<Lineup>)

fun TournamentItem.getStart(): Long? {
    return tournament.startTime.takeIf { it > 0L } ?: lineups.minOfOrNull {
        it.eventTimeInMillis.takeIf { it > 0L } ?: it.createdTimeInMillis
    }
}

fun TournamentItem.getEnd(): Long? {
    return tournament.endTime.takeIf { it > 0L } ?: lineups.maxOfOrNull {
        it.eventTimeInMillis.takeIf { it > 0L } ?: it.createdTimeInMillis
    }
}
