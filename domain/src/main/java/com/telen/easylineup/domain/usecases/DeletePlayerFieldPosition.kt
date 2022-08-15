package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.model.*
import io.reactivex.rxjava3.core.Single

internal class DeletePlayerFieldPosition(private val dao: PlayerFieldPositionRepository): UseCase<DeletePlayerFieldPosition.RequestValues, DeletePlayerFieldPosition.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return try {
            val toDelete = mutableListOf<PlayerWithPosition>()
            val toUpdate = mutableListOf<PlayerWithPosition>()

            //substitutes have the same position, let's use player to get the good one
            val player = requestValues.players.first {
                it.position == requestValues.fieldPosition.id && it.playerID == requestValues.playerToDelete.id
            }

            if(requestValues.lineupMode == MODE_ENABLED &&
                    (requestValues.fieldPosition == FieldPosition.DP_DH || player.flags and PlayerFieldPosition.FLAG_FLEX > 0)) {
                toDelete.addAll(requestValues.players
                        .filter { it.position == FieldPosition.DP_DH.id || it.flags and PlayerFieldPosition.FLAG_FLEX > 0 }
                        .map { it }
                )
            }
            else {
                toDelete.add(player)
            }

            //check if substitutes was a batter, update the next substitutes order to replace it if necessary
            if(requestValues.fieldPosition == FieldPosition.SUBSTITUTE && player.order < Constants.SUBSTITUTE_ORDER_VALUE) {
                val intermediateList = requestValues.players.subtract(toDelete).toList()
                val substitutes = intermediateList.filter { FieldPosition.isSubstitute(it.position) && it.order > 0 }
                var found = false
                substitutes.forEachIndexed { index, playerWithPosition ->
                    if(!found && index < requestValues.extraHitterSize && playerWithPosition.order == Constants.SUBSTITUTE_ORDER_VALUE) {
                        playerWithPosition.order = player.order
                        toUpdate.add(playerWithPosition)
                        found = true
                    }
                }
            }

            dao.deletePositions(toDelete.map { it.toPlayerFieldPosition() })
                    .andThen(dao.updatePlayerFieldPositions(toUpdate.map { it.toPlayerFieldPosition() }))
                    .andThen(Single.just(ResponseValue()))
        }
        catch (e: NoSuchElementException) {
            Single.error(e)
        }
    }


    class RequestValues(val players: List<PlayerWithPosition>, val playerToDelete: Player, val fieldPosition: FieldPosition,
                        val lineupMode: Int, val extraHitterSize: Int): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}