/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Single

/**
 * Validates the team name, inserts or updates it, then marks it as the current team.
 */
class SaveTeam(
    private val dao: TeamRepository,
    private val checkTeam: CheckTeam,
    private val saveCurrentTeam: SaveCurrentTeam,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(team: Team): Single<Team> {
        return checkTeam(team)
            .andThen(Single.defer {
                if (team.type == TeamType.UNKNOWN.id) {
                    team.type = TeamType.BASEBALL.id
                }
                if (team.id == 0L) {
                    dao.insertTeam(team).map { id ->
                        team.id = id
                        team
                    }
                } else {
                    dao.updateTeam(team).andThen(Single.just(team))
                }
            })
            .flatMap { savedTeam ->
                saveCurrentTeam(savedTeam).andThen(Single.just(savedTeam))
            }
            .subscribeOn(schedulersProvider.io())
    }
}
