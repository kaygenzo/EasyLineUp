/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.model.isSubstitute
import io.reactivex.rxjava3.core.Single

class GetListAvailablePlayersForSelection(private val getRoster: GetRoster) :
    UseCase<GetListAvailablePlayersForSelection.RequestValues,
GetListAvailablePlayersForSelection.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return getRoster.executeUseCase(GetRoster.RequestValues(requestValues.lineup.id))
            .map { it.summary.players }
            .map { rosterPlayers ->
                val players = requestValues.players
                val playersSelectedForLineup = rosterPlayers
                    .filter { it.status }
                    .map { it.player.id }

                var listAvailablePlayers = players
                    // get only player no placed on a position except the substitutes, but only
                    // if it is not to add in the container of substitutes
                    .filter {
                        val setAsSubstitute = requestValues.position == FieldPosition.SUBSTITUTE
                        !it.isAssigned() || (it.isSubstitute() && !setAsSubstitute)
                    }
                    // no player excluded from the lineup roster
                    .filter { playersSelectedForLineup.contains(it.playerId) }

                requestValues.position?.run {
                    if (isDefensePlayer()) {
                        listAvailablePlayers = listAvailablePlayers
                            .sortedWith(getPlayerComparator(this))
                    }
                }
                listAvailablePlayers
            }
            .flatMap { listAvailablePlayers ->
                if (listAvailablePlayers.isNotEmpty()) {
                    Single.just(ResponseValue(listAvailablePlayers))
                } else {
                    Single.error(NoSuchElementException())
                }
            }
    }

    private fun getPlayerComparator(position: FieldPosition): Comparator<PlayerWithPosition> {
        return Comparator { position1, position2 ->
            val firstHasPosition = position1.playerPositions and position.mask > 0
            val secondHasPosition = position2.playerPositions and position.mask > 0
            if (firstHasPosition && !secondHasPosition) {
                -1
            } else if (!firstHasPosition && secondHasPosition) {
                1
            } else {
                position1.playerName.compareTo(position2.playerName)
            }
        }
    }

    /**
     * @property players
     * @property position
     * @property lineup
     */
    class RequestValues(
        val players: List<PlayerWithPosition>,
        val position: FieldPosition?,
        val lineup: Lineup
    ) : UseCase.RequestValues

    /**
     * @property players
     */
    class ResponseValue(val players: List<PlayerWithPosition>) : UseCase.ResponseValue
}
