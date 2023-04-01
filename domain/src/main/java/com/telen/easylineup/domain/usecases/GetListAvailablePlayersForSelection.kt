package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.*
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
            .filter { playersSelectedForLineup?.contains(it.playerID) ?: true }

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
        return Comparator { p1, p2 ->
            val firstHasPosition = p1.playerPositions and position.mask > 0
            val secondHasPosition = p2.playerPositions and position.mask > 0
            if (firstHasPosition && !secondHasPosition) {
                -1
            } else if (!firstHasPosition && secondHasPosition) {
                1
            } else {
                p1.playerName.compareTo(p2.playerName)
            }
        }
    }

    class RequestValues(
        val players: List<PlayerWithPosition>,
        val position: FieldPosition?,
        val rosterPlayers: List<RosterPlayerStatus>?
    ) : UseCase.RequestValues

    class ResponseValue(val players: List<PlayerWithPosition>) : UseCase.ResponseValue
}