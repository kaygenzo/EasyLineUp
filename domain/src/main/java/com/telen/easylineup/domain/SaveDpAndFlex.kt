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

            requestValues.players.firstOrNull { p -> p.position == FieldPosition.DP_DH.position }?.run {
                toUpdate.add(this.toPlayerFieldPosition().apply {
                    playerId = requestValues.dp.id
                    order = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(order))
                })
            } ?: run {
                val newPosition = PlayerFieldPosition(playerId = requestValues.dp.id, lineupId = lineupID, position = FieldPosition.DP_DH.position,
                        order = PlayerWithPosition.getNextAvailableOrder(requestValues.players))
                toInsert.add(newPosition)
            }

            //check if there are flex flags and reset them because there can be only one flex
            requestValues.players.filter {
                player -> player.flags and PlayerFieldPosition.FLAG_FLEX > 0 && player.playerID != requestValues.flex.id
            }.forEach {
                val playerFieldPosition = it.toPlayerFieldPosition()
                playerFieldPosition.flags = PlayerFieldPosition.FLAG_NONE
                playerFieldPosition.order = Constants.ORDER_PITCHER_WHEN_DH
                toUpdate.add(playerFieldPosition)
            }

            //just find the player field position corresponding to the flexFieldPosition in which we will update the flag,
            requestValues.players.firstOrNull { p -> p.playerID == requestValues.flex.id }?.run {
                toUpdate.add(this.toPlayerFieldPosition().apply {
                    flags = PlayerFieldPosition.FLAG_FLEX
                    order = Constants.ORDER_PITCHER_WHEN_DH
                })
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