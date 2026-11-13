/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.currentTeam
import com.telen.easylineup.domain.ports.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The current team, following every switch, edit or deletion: a screen built on it never shows
 * a team the user has left. Null while there is no team at all.
 */
class ObserveCurrentTeam(private val dao: TeamRepository) {
    operator fun invoke(): Flow<Team?> = dao.observeTeams()
        .map { teams -> teams.currentTeam() }
        .distinctUntilChanged()
}
