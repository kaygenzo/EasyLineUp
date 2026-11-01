/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.PlayerSideSelection

/** Unpacks a player's stored batting/pitching flag back into its left/right toggles. */
class DecodePlayerSideFlag {
    operator fun invoke(flag: Int): PlayerSideSelection {
        return PlayerSideSelection(
            left = (flag and PlayerSide.LEFT.flag) != 0,
            right = (flag and PlayerSide.RIGHT.flag) != 0
        )
    }
}
