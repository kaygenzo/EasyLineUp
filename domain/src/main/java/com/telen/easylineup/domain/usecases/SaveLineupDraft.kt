/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.TransactionRunner
import kotlinx.coroutines.withContext

/**
 * Saves everything a lineup edit session changed, in one transaction: the lineup itself
 * (including a roster changed during the session), every position and batting slot, and the
 * per-lineup shirt numbers from [rosterNumbers] - empty when the roster was not edited.
 */
class SaveLineupDraft(
    private val saveBattingOrderAndPositions: SaveBattingOrderAndPositions,
    private val savePlayerNumberOverlay: SavePlayerNumberOverlay,
    private val transactionRunner: TransactionRunner,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(
        lineup: Lineup,
        players: List<PlayerWithPosition>,
        rosterNumbers: List<RosterPlayerStatus>
    ): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            transactionRunner.inTransaction {
                saveBattingOrderAndPositions(lineup, players).getOrThrow()
                savePlayerNumberOverlay(rosterNumbers).getOrThrow()
            }
        }
    }
}
