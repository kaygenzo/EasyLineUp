package com.telen.easylineup.lineup.edition

import com.telen.easylineup.domain.model.Player

interface RosterAdapterCallback {
    fun onNumberChanged(player: Player, number: Int)
    fun onPlayerSelectedChanged(player: Player, selected: Boolean)
}