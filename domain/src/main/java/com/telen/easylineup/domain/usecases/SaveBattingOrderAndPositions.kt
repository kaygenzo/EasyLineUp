package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.toPlayerFieldPosition
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class SaveBattingOrderAndPositions(
    private val lineupRepository: LineupRepository,
    private val pfpRepository: PlayerFieldPositionRepository
) : UseCase<SaveBattingOrderAndPositions.RequestValues,
        SaveBattingOrderAndPositions.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.defer {
            if (requestValues.lineup.id <= 0) {
                Single.error(IllegalStateException("The lineup id cannot be less or equal 0"))
            } else {
                val playersOperations: MutableList<Completable> = mutableListOf()
                requestValues.players.forEach {
                    val playerPosition = it.toPlayerFieldPosition()
                    if (!it.isAssigned() && it.fieldPositionID > 0) {
                        // it is an old position that can be safely removed
                        playersOperations.add(pfpRepository.deletePosition(playerPosition))
                    } else if (playerPosition.id == 0L) {
                        playersOperations.add(
                            pfpRepository
                                .insertPlayerFieldPosition(playerPosition).ignoreElement()
                        )
                    } else {
                        playersOperations.add(
                            pfpRepository
                                .updatePlayerFieldPosition(playerPosition)
                        )
                    }
                }
                lineupRepository.updateLineup(requestValues.lineup)
                    .andThen(Completable.concat(playersOperations))
                    .andThen(Single.just(ResponseValue()))
            }
        }
    }

    class RequestValues(val lineup: Lineup, val players: List<PlayerWithPosition>) :
        UseCase.RequestValues

    class ResponseValue : UseCase.ResponseValue
}