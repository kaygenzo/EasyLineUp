/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerTournamentStatsProfile
import com.telen.easylineup.domain.model.PlayingTimeSummary

/** The average/lowest/highest playing time across a tournament's roster, for the fairness check. */
class GetPlayingTimeSummary {
    operator fun invoke(profiles: List<PlayerTournamentStatsProfile>): PlayingTimeSummary {
        val totalGamesList = profiles.map { it.totalGames }
        if (totalGamesList.isEmpty()) {
            return PlayingTimeSummary(average = 0f, lowest = 0, highest = 0)
        }
        return PlayingTimeSummary(
            average = totalGamesList.average().toFloat(),
            lowest = totalGamesList.min(),
            highest = totalGamesList.max()
        )
    }
}
