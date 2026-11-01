/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.SubstituteBattingSlots
import com.telen.easylineup.domain.model.isSubstitute

/**
 * Counts how many of a lineup's extra-hitter batting slots are still free, from the substitutes
 * already batting in it. Used both to show the budget on the "add substitute" entry point and
 * to decide whether the next picked substitute will bat.
 */
class GetSubstituteBattingSlots {
    operator fun invoke(
        lineupPlayers: List<PlayerWithPosition>,
        extraHitters: Int
    ): SubstituteBattingSlots {
        if (extraHitters > Constants.MAX_EXTRA_HITTERS_BEFORE_UNLIMITED) {
            return SubstituteBattingSlots.Unlimited
        }
        val batting = lineupPlayers.count {
            it.isSubstitute() && it.order in 1 until Constants.SUBSTITUTE_ORDER_VALUE
        }
        return SubstituteBattingSlots.Limited(
            remaining = (extraHitters - batting).coerceAtLeast(0),
            total = extraHitters
        )
    }
}
