package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.model.*
import io.reactivex.Single

internal class DeletePlayerFieldPosition(private val dao: PlayerFieldPositionRepository): UseCase<DeletePlayerFieldPosition.RequestValues, DeletePlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return try {
            val toDelete = mutableListOf<PlayerFieldPosition>()
            //substitutes have the same position, let's use player to get the good one
            val player = requestValues.players.first {
                it.position == requestValues.fieldPosition.id && it.playerID == requestValues.playerToDelete.id
            }

            if(requestValues.lineupMode == MODE_ENABLED &&
                    (requestValues.fieldPosition == FieldPosition.DP_DH || player.flags and PlayerFieldPosition.FLAG_FLEX > 0)) {
                toDelete.addAll(requestValues.players
                        .filter { it.position == FieldPosition.DP_DH.id || it.flags and PlayerFieldPosition.FLAG_FLEX > 0 }
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


    class RequestValues(val players: List<PlayerWithPosition>, val playerToDelete: Player, val fieldPosition: FieldPosition, val lineupMode: Int): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}