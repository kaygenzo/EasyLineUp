package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.repository.PlayerFieldPositionRepository
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import io.reactivex.Single



internal class SaveDpAndFlex(private val playerFieldPositionDao: PlayerFieldPositionRepository): UseCase<SaveDpAndFlex.RequestValues, SaveDpAndFlex.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let { lineupID ->
            val toUpdate = mutableListOf<PlayerWithPosition>()
            val toInsert = mutableListOf<PlayerFieldPosition>()

            val dp = requestValues.dp ?: return Single.error(NeedAssignBothPlayersException())
            val flex = requestValues.flex ?: return Single.error(NeedAssignBothPlayersException())

            // just find the player field position corresponding to the flexFieldPosition in which
            // we will update the flag, and by the way, free the batting order
            requestValues.players.firstOrNull { p -> p.playerID == flex.id }?.run {
                flags = PlayerFieldPosition.FLAG_FLEX
                order = Constants.ORDER_PITCHER_WHEN_DH
                if(!toUpdate.contains(this))
                    toUpdate.add(this)
            }

            //check if there are flex flags and reset them because there can be only one flex
            requestValues.players.filter {
                player -> player.flags and PlayerFieldPosition.FLAG_FLEX > 0 && player.playerID != flex.id
            }.forEach {
                it.flags = PlayerFieldPosition.FLAG_NONE
                it.order = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(it.order))
                if(!toUpdate.contains(it))
                    toUpdate.add(it)
            }

            requestValues.players.firstOrNull { p -> p.position == FieldPosition.DP_DH.position }?.run {
                order = PlayerWithPosition.getNextAvailableOrder(requestValues.players, listOf(order))
                playerID = dp.id
                if(!toUpdate.contains(this))
                    toUpdate.add(this)
            } ?: run {
                val newPosition = PlayerFieldPosition(playerId = dp.id, lineupId = lineupID, position = FieldPosition.DP_DH.position,
                        order = PlayerWithPosition.getNextAvailableOrder(requestValues.players))
                toInsert.add(newPosition)
            }

            return playerFieldPositionDao.updatePlayerFieldPositions(toUpdate.map { it.toPlayerFieldPosition() })
                    .andThen(playerFieldPositionDao.insertPlayerFieldPositions(toInsert))
                    .andThen(Single.just(ResponseValue()))

        } ?: Single.error(Exception("Lineup id is null!"))
    }

    class RequestValues(val lineupID: Long?,
                        val dp: Player?,
                        val flex: Player?,
                        val players: List<PlayerWithPosition>
    ): UseCase.RequestValues
    inner class ResponseValue: UseCase.ResponseValue
}