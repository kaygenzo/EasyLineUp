/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamRosterSummary

/**
 * Whether closing the edit form of an existing lineup would throw away a change: its name, its
 * tournament or its roster (players in or out, shirt number overrides) differs from what was
 * loaded. A tournament that could not be found ([tournamentId] null) counts as a change.
 */
class HasUnsavedLineupEdits {
    operator fun invoke(
        loaded: Lineup,
        loadedRoster: TeamRosterSummary,
        name: String,
        tournamentId: Long?,
        roster: TeamRosterSummary
    ): Boolean =
        name != loaded.name || tournamentId != loaded.tournamentId || roster != loadedRoster
}
