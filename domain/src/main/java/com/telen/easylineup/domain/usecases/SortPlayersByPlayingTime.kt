/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerTournamentStatsProfile

/** Ranks a tournament's roster by playing time, most-played first - the fairness screen's point. */
class SortPlayersByPlayingTime {
    operator fun invoke(
        profiles: List<PlayerTournamentStatsProfile>
    ): List<PlayerTournamentStatsProfile> {
        return profiles.sortedByDescending { it.totalGames }
    }
}
