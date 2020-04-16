package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.*
import io.reactivex.Single

class DeletePlayerFieldPosition(private val dao: PlayerFieldPositionsDao): UseCase<DeletePlayerFieldPosition.RequestValues, DeletePlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return try {
            val player = requestValues.players.first { it.position == requestValues.position.position }
            val toDelete = mutableListOf<PlayerFieldPosition>()
            if(requestValues.lineupMode == MODE_ENABLED &&
                    (requestValues.position == FieldPosition.DP_DH || player.flags and PlayerFieldPosition.FLAG_FLEX > 0)) {
                toDelete.addAll(requestValues.players
                        .filter { it.position == FieldPosition.DP_DH.position || it.flags and PlayerFieldPosition.FLAG_FLEX > 0 }
                        .map { it.toPlayerFieldPosition() }
                )
            }
            else {
                toDelete.add(player.toPlayerFieldPosition())
            }

            dao.deletePositions(toDelete).andThen(Single.just(ResponseValue()))
        }
        catch (e: NoSuchElementException) {
            Single.error(e)
        }
    }


    class RequestValues(val players: List<PlayerWithPosition>, val position: FieldPosition, val lineupMode: Int): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}