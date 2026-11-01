/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isSubstitute

/**
 * The lineup's substitutes, batting order first (so the row reads left-to-right in the order
 * they'd bat), falling back to alphabetical for substitutes sharing the same order - bench-only
 * ones all share the same "not batting" order value.
 */
class SortSubstitutesForDisplay {
    operator fun invoke(players: List<PlayerWithPosition>): List<PlayerWithPosition> {
        return players.filter { it.isSubstitute() }
            .sortedWith(compareBy({ it.order }, { it.playerName.lowercase() }))
    }
}
