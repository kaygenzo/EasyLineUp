/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.TransactionRunner
import kotlinx.coroutines.withContext

/**
 * Saves an edited lineup's fields, roster and per-lineup shirt numbers as one change: if any
 * step fails, none of them is kept, so the lineup never ends up with its new name but its old
 * roster. Validation errors from [UpdateLineup] (`LineupNameEmptyException`,
 * `TournamentNameEmptyException`) come back unchanged, before anything is written.
 */
class SaveLineupChanges(
    private val updateLineup: UpdateLineup,
    private val updateLineupRoster: UpdateLineupRoster,
    private val savePlayerNumberOverlay: SavePlayerNumberOverlay,
    private val transactionRunner: TransactionRunner,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineup: Lineup, roster: List<RosterPlayerStatus>): Result<Unit> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                transactionRunner.inTransaction {
                    updateLineup(lineup).getOrThrow()
                    updateLineupRoster(lineup.id, roster).getOrThrow()
                    savePlayerNumberOverlay(roster).getOrThrow()
                }
            }
        }
}
