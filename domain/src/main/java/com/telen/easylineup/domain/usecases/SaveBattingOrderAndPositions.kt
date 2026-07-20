/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.toPlayerFieldPosition
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class SaveBattingOrderAndPositions(
    private val lineupRepository: LineupRepository,
    private val pfpRepository: PlayerFieldPositionRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(lineup: Lineup, players: List<PlayerWithPosition>): Result<Unit> =
        runCatchingCancellable {
            withContext(dispatcherProvider.io()) {
                if (lineup.id <= 0) {
                    throw IllegalStateException("The lineup id cannot be less or equal 0")
                }

                lineupRepository.updateLineup(lineup).await()

                players.forEach {
                    val playerPosition = it.toPlayerFieldPosition()
                    if (!it.isAssigned() && it.fieldPositionId > 0) {
                        // it is an old position that can be safely removed
                        pfpRepository.deletePosition(playerPosition).await()
                    } else if (it.isAssigned()) {
                        if (playerPosition.id == 0L) {
                            pfpRepository.insertPlayerFieldPosition(playerPosition).await()
                        } else {
                            pfpRepository.updatePlayerFieldPosition(playerPosition).await()
                        }
                    }
                }
            }
        }
}
