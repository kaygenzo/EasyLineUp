/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

import java.io.Serializable

/**
 * @property status
 * @property players
 */
data class TeamRosterSummary(
    val status: Int,
    val players: List<RosterPlayerStatus>
) : Serializable
