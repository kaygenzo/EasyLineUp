/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition

/**
 * Toggles one position in a player's favorite-positions selection, enforcing the
 * [MAX_FAVORITE_POSITIONS] business limit on how many a player can have at once.
 */
class ToggleFavoritePosition {
    operator fun invoke(
        selected: Set<FieldPosition>,
        position: FieldPosition
    ): Set<FieldPosition> {
        val newSelection = selected.toMutableSet()
        if (position in newSelection) {
            newSelection.remove(position)
        } else if (newSelection.size < MAX_FAVORITE_POSITIONS) {
            newSelection.add(position)
        }
        return newSelection
    }

    companion object {
        const val MAX_FAVORITE_POSITIONS = 3
    }
}
