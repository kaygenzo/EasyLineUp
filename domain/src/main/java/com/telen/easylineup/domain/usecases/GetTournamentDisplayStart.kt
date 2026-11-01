/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Tournament

/**
 * The date to show for a tournament: its own start time when it has one, or else the earliest
 * of its lineups' event times (falling back to each lineup's creation time when it has none
 * either) - for tournaments created without explicit dates.
 */
class GetTournamentDisplayStart {
    operator fun invoke(tournament: Tournament, lineups: List<Lineup>): Long? {
        return tournament.startTime.takeIf { it > 0L } ?: lineups.minOfOrNull {
            it.eventTimeInMillis.takeIf { time -> time > 0L } ?: it.createdTimeInMillis
        }
    }
}
