package com.telen.easylineup.domain

import com.telen.easylineup.FieldPosition
import com.telen.easylineup.UseCase
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerWithPosition

class GetListAvailablePlayersForSelection: UseCase<GetListAvailablePlayersForSelection.RequestValues, GetListAvailablePlayersForSelection.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let { req ->
            val listAvailablePlayers = req.players
                    .filter { it.fieldPositionID <= 0 }
                    .map { it.toPlayer() }

            if (FieldPosition.isDefensePlayer(req.position.position))
                listAvailablePlayers.sortedWith(getPlayerComparator(req.position))

            if(listAvailablePlayers.isNotEmpty())
                mUseCaseCallback?.onSuccess(ResponseValue(listAvailablePlayers))
            else
                mUseCaseCallback?.onError()
        }
    }

    private fun getPlayerComparator(position: FieldPosition): Comparator<Player> {
        return Comparator { p1, p2 ->
            val firstHasPosition = p1.positions and position.mask > 0
            val secondHasPosition = p2.positions and position.mask > 0
            if(firstHasPosition && !secondHasPosition)
                -1
            else if(!firstHasPosition && secondHasPosition)
                1
            else
                p1.name.compareTo(p2.name)
        }
    }

    class RequestValues(val players: List<PlayerWithPosition>, val position: FieldPosition): UseCase.RequestValues
    class ResponseValue(val players: List<Player>): UseCase.ResponseValue
}