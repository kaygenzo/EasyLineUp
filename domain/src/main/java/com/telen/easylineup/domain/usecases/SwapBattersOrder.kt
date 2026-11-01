/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.BatterState

/** Exchanges two batters' spots in the batting order, leaving everything else untouched. */
class SwapBattersOrder {
    operator fun invoke(from: BatterState, to: BatterState): Pair<BatterState, BatterState> {
        return from.copy(playerOrder = to.playerOrder) to to.copy(playerOrder = from.playerOrder)
    }
}
