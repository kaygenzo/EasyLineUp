/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property status
 * @property players
 */
data class TeamRosterSummary(var status: Int, val players: List<RosterPlayerStatus>)
