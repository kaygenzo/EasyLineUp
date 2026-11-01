/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSortType

/** Orders a roster either alphabetically (case-insensitive) or by shirt number. */
class SortPlayers {
    operator fun invoke(players: List<Player>, sortType: PlayerSortType): List<Player> {
        return when (sortType) {
            PlayerSortType.ALPHA -> players.sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            )

            PlayerSortType.NUMERIC -> players.sortedBy { it.shirtNumber }
        }
    }
}
