/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.Constants

/**
 * Past [Constants.MAX_EXTRA_HITTERS_BEFORE_UNLIMITED], a lineup's extra-hitter budget is
 * uncapped.
 */
class IsExtraHittersUnlimited {
    operator fun invoke(extraHitters: Int): Boolean {
        return extraHitters > Constants.MAX_EXTRA_HITTERS_BEFORE_UNLIMITED
    }
}
