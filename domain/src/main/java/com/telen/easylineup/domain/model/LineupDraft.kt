/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * A lineup and its players as edited, before they are saved: what the editing use cases that
 * change both the lineup itself and its players return, as one new value.
 *
 * @property lineup
 * @property players
 */
data class LineupDraft(val lineup: Lineup, val players: List<PlayerWithPosition>)
