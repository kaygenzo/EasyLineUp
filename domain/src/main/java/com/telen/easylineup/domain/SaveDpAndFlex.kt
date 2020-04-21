package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.PlayerFieldPositionsDao
import com.telen.easylineup.repository.model.*
import io.reactivex.Completable
import io.reactivex.Single

class SaveDpAndFlex(private val playerFieldPositionDao: PlayerFieldPositionsDao): UseCase<SaveDpAndFlex.RequestValues, SaveDpAndFlex.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { lineupID ->
            val toUpdate = mutableListOf<PlayerFieldPosition>()
            val toInsert = mutableListOf<PlayerFieldPosition>()

            // just find the player field position corresponding to the flexFieldPosition in which
            // we will update the flag, and by the way, free the batting order
            requestValues.players.firstOrNull { p -> p.playerID == requestValues.flex.id }?.run {
                flags = PlayerFieldPosition.FLAG_FLEX
                order = Constants.ORDER_PITCHER_WHEN_DH
                toUpdate.add(this.toPlayerFieldPosition())
            }

            //check if there are flex flags and reset them because there can be only one flex
            requestValues.players.filter {
                player -> player.flags and PlayerFieldPosition.FLAG_FLEX > 0 && player.playerID != requestValues.flex.id
            }.forEach {
                it.flags = PlayerFieldPosition.FLAG_NONE
                it.order = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(it.order))
                toUpdate.add(it.toPlayerFieldPosition())
            }

            requestValues.players.firstOrNull { p -> p.position == FieldPosition.DP_DH.position }?.run {
                order = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(order))
                toUpdate.add(this.toPlayerFieldPosition().apply {
                    playerId = requestValues.dp.id
                })
            } ?: run {
                val newPosition = PlayerFieldPosition(playerId = requestValues.dp.id, lineupId = lineupID, position = FieldPosition.DP_DH.position,
                        order = PlayerWithPosition.getNextAvailableOrder(requestValues.players))
                toInsert.add(newPosition)
            }

            return playerFieldPositionDao.updatePlayerFieldPositions(toUpdate)
                    .andThen(playerFieldPositionDao.insertPlayerFieldPositions(toInsert))
                    .andThen(Single.just(ResponseValue()))

        } ?: Single.error(Exception("Lineup id is null!"))
    }

    class RequestValues(val lineupID: Long?,
                        val dp: Player,
                        val flex: Player,
                        val players: List<PlayerWithPosition>
    ): UseCase.RequestValues
    inner class ResponseValue: UseCase.ResponseValue
}