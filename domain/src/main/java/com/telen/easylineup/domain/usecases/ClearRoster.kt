/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.TeamRosterSummary

/** Deselects every roster player, leaving the roster [Constants.STATUS_PARTIAL]. */
class ClearRoster {
    operator fun invoke(roster: TeamRosterSummary): TeamRosterSummary {
        return TeamRosterSummary(
            status = Constants.STATUS_PARTIAL,
            players = roster.players.map { it.copy(status = false) }
        )
    }
}
