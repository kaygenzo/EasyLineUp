package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.LineupDao
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.PlayerWithPosition
import io.reactivex.Single

class GetListAvailablePlayersForSelection(val lineupDao: LineupDao): UseCase<GetListAvailablePlayersForSelection.RequestValues, GetListAvailablePlayersForSelection.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return requestValues.lineupID?.let {
            lineupDao.getLineupByIdSingle(it).flatMap {
                val roasterIDs = requestValues.roasterPlayers?.filter { it.status }?.map { it.player.id }
                var listAvailablePlayers = requestValues.players
                        .filter { player ->
                            player.fieldPositionID <= 0 && roasterIDs?.contains(player.playerID) ?: true
                        }
                        .map { it.toPlayer() }

                if (FieldPosition.isDefensePlayer(requestValues.position.position))
                    listAvailablePlayers = listAvailablePlayers.sortedWith(getPlayerComparator(requestValues.position))

                if(listAvailablePlayers.isNotEmpty())
                    Single.just(ResponseValue(listAvailablePlayers))
                else
                    Single.error(NoSuchElementException())
            }
        } ?: throw IllegalArgumentException()
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

    class RequestValues(val lineupID: Long?, val players: List<PlayerWithPosition>, val position: FieldPosition, val roasterPlayers: List<RoasterPlayerStatus>?): UseCase.RequestValues
    class ResponseValue(val players: List<Player>): UseCase.ResponseValue
}