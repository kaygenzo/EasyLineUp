/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.toPlayerFieldPosition
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import io.reactivex.rxjava3.core.Completable

class SaveBattingOrderAndPositions(
    private val lineupRepository: LineupRepository,
    private val pfpRepository: PlayerFieldPositionRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(lineup: Lineup, players: List<PlayerWithPosition>): Completable {
        return Completable.defer {
            if (lineup.id <= 0) {
                Completable.error(IllegalStateException("The lineup id cannot be less or equal 0"))
            } else {
                val playersOperations: MutableList<Completable> = mutableListOf()
                players.forEach {
                    val playerPosition = it.toPlayerFieldPosition()
                    if (!it.isAssigned() && it.fieldPositionId > 0) {
                        // it is an old position that can be safely removed
                        playersOperations.add(pfpRepository.deletePosition(playerPosition))
                    } else if (it.isAssigned()) {
                        if (playerPosition.id == 0L) {
                            playersOperations.add(
                                pfpRepository.insertPlayerFieldPosition(playerPosition)
                                    .ignoreElement()
                            )
                        } else {
                            playersOperations.add(
                                pfpRepository.updatePlayerFieldPosition(playerPosition)
                            )
                        }
                    }
                }
                lineupRepository.updateLineup(lineup)
                    .andThen(Completable.concat(playersOperations))
            }
        }.subscribeOn(schedulersProvider.io())
    }
}
