/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isAssigned
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.ports.DispatcherProvider
import kotlinx.coroutines.withContext

class GetListAvailablePlayersForSelection(
    private val getRoster: GetRoster,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(
        players: List<PlayerWithPosition>,
        position: FieldPosition?,
        lineup: Lineup
    ): Result<List<PlayerWithPosition>> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            val rosterPlayers = getRoster(lineup.id).getOrThrow().players
            val playersSelectedForLineup = rosterPlayers
                .filter { it.status }
                .map { it.player.id }

            var listAvailablePlayers = players
                // get only player no placed on a position except the substitutes, but only
                // if it is not to add in the container of substitutes
                .filter {
                    val setAsSubstitute = position == FieldPosition.SUBSTITUTE
                    !it.isAssigned() || (it.isSubstitute() && !setAsSubstitute)
                }
                // no player excluded from the lineup roster
                .filter { playersSelectedForLineup.contains(it.playerId) }

            position?.run {
                if (isDefensePlayer()) {
                    listAvailablePlayers = listAvailablePlayers
                        .sortedWith(getPlayerComparator(this))
                }
            }

            if (listAvailablePlayers.isEmpty()) {
                throw NoSuchElementException()
            }

            listAvailablePlayers
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
}
