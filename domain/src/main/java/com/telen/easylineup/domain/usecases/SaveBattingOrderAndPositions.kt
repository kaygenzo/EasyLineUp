/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.toPlayerFieldPosition
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.repository.LineupRepository
import com.telen.easylineup.domain.ports.repository.PlayerFieldPositionRepository
import kotlinx.coroutines.withContext

class SaveBattingOrderAndPositions(
    private val lineupRepository: LineupRepository,
    private val pfpRepository: PlayerFieldPositionRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineup: Lineup, players: List<PlayerWithPosition>): Result<Unit> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                check(lineup.id > 0) { "The lineup id cannot be less or equal 0" }

                lineupRepository.updateLineup(lineup)

                // Snapshot before iterating: each suspend write below can trigger a reactive
                // DB-observing Flow upstream (ViewModel's shareIn'd _players) to concurrently
                // clear()/addAll() this same backing list, which would otherwise throw a
                // ConcurrentModificationException mid-loop.
                players.toList().forEach {
                    val playerPosition = it.toPlayerFieldPosition()
                    if (!it.isAssigned() && it.fieldPositionId > 0) {
                        // it is an old position that can be safely removed
                        pfpRepository.deletePosition(playerPosition)
                    } else if (it.isAssigned()) {
                        if (playerPosition.id == 0L) {
                            // Write the new row id back onto the in-memory player: without
                            // this, a second save() in the same session (e.g. a Save
                            // double-tap) would still see fieldPositionId == 0 and insert
                            // a duplicate row instead of updating the one just created.
                            it.fieldPositionId = pfpRepository.insertPlayerFieldPosition(
                                playerPosition
                            )
                        } else {
                            pfpRepository.updatePlayerFieldPosition(playerPosition)
                        }
                    }
                }
            }
        }
}
