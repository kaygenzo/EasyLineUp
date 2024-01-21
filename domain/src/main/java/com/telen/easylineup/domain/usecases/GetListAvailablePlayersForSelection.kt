/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.model.isSubstitute
import io.reactivex.rxjava3.core.Single

internal class GetListAvailablePlayersForSelection :
    UseCase<GetListAvailablePlayersForSelection.RequestValues,
GetListAvailablePlayersForSelection.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        val players = requestValues.players
        val playersSelectedForLineup = requestValues.rosterPlayers
            ?.filter { it.status }
            ?.map { it.player.id }

        var listAvailablePlayers = players
            // get only player no placed on a position except the substitutes, but only if it is
            // not to add in the container of substitutes
            .filter {
                val setAsSubstitute = requestValues.position == FieldPosition.SUBSTITUTE
                !it.isAssigned() || (it.isSubstitute() && !setAsSubstitute)
            }
            // no player excluded from the lineup roster
            .filter { playersSelectedForLineup?.contains(it.playerId) ?: true }

        requestValues.position?.run {
            if (isDefensePlayer()) {
                listAvailablePlayers = listAvailablePlayers
                    .sortedWith(getPlayerComparator(this))
            }
        }

        return if (listAvailablePlayers.isNotEmpty()) {
            Single.just(ResponseValue(listAvailablePlayers))
        } else {
            Single.error(NoSuchElementException())
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
     * @property rosterPlayers
     */
    class RequestValues(
        val players: List<PlayerWithPosition>,
        val position: FieldPosition?,
        val rosterPlayers: List<RosterPlayerStatus>?
    ) : UseCase.RequestValues

    /**
     * @property players
     */
    class ResponseValue(val players: List<PlayerWithPosition>) : UseCase.ResponseValue
}
