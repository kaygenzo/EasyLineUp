/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.FieldPosition

/** The field position a player has appeared at the most, or null when they haven't played any. */
class GetMostPlayedPosition {
    operator fun invoke(positionsData: Map<FieldPosition, Int>): FieldPosition? {
        val topEntry = positionsData.maxByOrNull { it.value } ?: return null
        return topEntry.key.takeIf { topEntry.value > 0 }
    }
}
