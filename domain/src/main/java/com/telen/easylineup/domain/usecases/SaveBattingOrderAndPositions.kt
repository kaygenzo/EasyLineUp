/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.toPlayerFieldPosition
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.TransactionRunner
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import kotlinx.coroutines.withContext

class SaveBattingOrderAndPositions(
    private val lineupRepository: LineupRepository,
    private val pfpRepository: PlayerFieldPositionRepository,
    private val transactionRunner: TransactionRunner,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineup: Lineup, players: List<PlayerWithPosition>): Result<Unit> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                transactionRunner.inTransaction {
                    check(lineup.id > 0) { "The lineup id cannot be less or equal 0" }

                    lineupRepository.updateLineup(lineup)

                    players.forEach {
                        val playerPosition = it.toPlayerFieldPosition()
                        if (!it.isAssigned() && it.fieldPositionId > 0) {
                            // it is an old position that can be safely removed
                            pfpRepository.deletePosition(playerPosition)
                        } else if (it.isAssigned()) {
                            if (playerPosition.id == 0L) {
                                // A second save of the same draft would insert this row again:
                                // LineupViewModel ignores a save while one is running, and
                                // reads the draft back from the database once it is done.
                                pfpRepository.insertPlayerFieldPosition(playerPosition)
                            } else {
                                pfpRepository.updatePlayerFieldPosition(playerPosition)
                            }
                        }
                    }
                }
            }
        }
}
