package com.telen.easylineup.domain

import com.telen.easylineup.repository.model.*
import io.reactivex.Single

class GetListAvailablePlayersForSelection: UseCase<GetListAvailablePlayersForSelection.RequestValues, GetListAvailablePlayersForSelection.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val rosterIDs = requestValues.rosterPlayers?.filter { it.status }?.map { it.player.id }
        var listAvailablePlayers = requestValues.players
                .filter { player ->
                    player.fieldPositionID <= 0 && rosterIDs?.contains(player.playerID) ?: true
                }
                .map { it.toPlayer() }

        requestValues.position?.run {
            if (FieldPosition.isDefensePlayer(this.position))
                listAvailablePlayers = listAvailablePlayers.sortedWith(getPlayerComparator(this))
        }

        return if(listAvailablePlayers.isNotEmpty())
            Single.just(ResponseValue(listAvailablePlayers))
        else
            Single.error(NoSuchElementException())
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

    class RequestValues(val players: List<PlayerWithPosition>, val position: FieldPosition?, val rosterPlayers: List<RosterPlayerStatus>?): UseCase.RequestValues
    class ResponseValue(val players: List<Player>): UseCase.ResponseValue
}