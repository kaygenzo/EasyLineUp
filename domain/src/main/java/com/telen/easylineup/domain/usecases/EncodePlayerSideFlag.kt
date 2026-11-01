/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.PlayerSide

/** Packs the left/right side toggles a player's batting or pitching form exposes into one flag. */
class EncodePlayerSideFlag {
    operator fun invoke(left: Boolean, right: Boolean): Int {
        var flag = 0
        if (left) flag = flag or PlayerSide.LEFT.flag
        if (right) flag = flag or PlayerSide.RIGHT.flag
        return flag
    }
}
