/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

/** A player is flagged as underused once their playing time drops under half the team average. */
class IsPlayingTimeUnderused {
    operator fun invoke(totalGames: Int, average: Float): Boolean {
        return average > 0 && totalGames < average / 2f
    }
}
