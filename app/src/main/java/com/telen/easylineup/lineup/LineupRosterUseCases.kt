/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup

import com.telen.easylineup.domain.usecases.ApplyRosterToLineup
import com.telen.easylineup.domain.usecases.ClearRoster
import com.telen.easylineup.domain.usecases.GetRoster
import com.telen.easylineup.domain.usecases.OverrideRosterPlayerNumber
import com.telen.easylineup.domain.usecases.ToggleRosterPlayer

/**
 * The use cases behind [LineupViewModel]'s roster sheet, grouped into one constructor
 * parameter: Koin's viewModelOf takes at most 22, and the ViewModel needs more than that.
 */
class LineupRosterUseCases(
    val getRoster: GetRoster,
    val applyRosterToLineup: ApplyRosterToLineup,
    val toggleRosterPlayer: ToggleRosterPlayer,
    val clearRoster: ClearRoster,
    val overrideRosterPlayerNumber: OverrideRosterPlayerNumber
)
