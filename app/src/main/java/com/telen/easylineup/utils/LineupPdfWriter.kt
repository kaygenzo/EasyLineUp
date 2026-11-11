/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.net.Uri
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType

/**
 * Everything a printable lineup card needs - plain data, no Android types, so
 * [LineupPdfWriter] stays the only place that knows how it's actually laid out on the page.
 *
 * @property lineupName shown next to "Opponent" - this app has no separate opponent/field
 * concept, so the lineup's own name is what a coach already uses to identify the game.
 * @property eventTimeInMillis the game's date, formatted on the card.
 * @property teamType drives which position short names/labels apply (baseball vs softball).
 * @property strategy drives the defense diagram's position layout.
 * @property battingOrder already sorted/computed - the same list [AttackScreen] renders.
 * @property defensePlayers starters and substitutes alike - the card separates them itself.
 */
data class LineupExportData(
    val lineupName: String,
    val eventTimeInMillis: Long,
    val teamType: TeamType,
    val strategy: TeamStrategy,
    val battingOrder: List<BatterState>,
    val defensePlayers: List<PlayerWithPosition>
)

/**
 * Renders a lineup as a printable PDF card (batting order + defense diagram + bench), behind
 * Android's own PDF/storage APIs so domain/ViewModel stay platform-agnostic.
 */
interface LineupPdfWriter {
    fun write(data: LineupExportData): Uri
}
