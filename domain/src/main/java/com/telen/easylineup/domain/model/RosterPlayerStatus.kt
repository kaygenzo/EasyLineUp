package com.telen.easylineup.domain.model

data class RosterPlayerStatus(val player: Player, var status: Boolean = true, val playerNumberOverlay: PlayerNumberOverlay?)