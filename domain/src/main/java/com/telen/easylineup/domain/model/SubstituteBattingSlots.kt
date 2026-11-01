/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/** How many of a lineup's extra-hitter batting slots a new substitute could still fill. */
sealed class SubstituteBattingSlots {
    data object Unlimited : SubstituteBattingSlots()

    /**
     * @property remaining
     * @property total
     */
    data class Limited(val remaining: Int, val total: Int) : SubstituteBattingSlots()
}
