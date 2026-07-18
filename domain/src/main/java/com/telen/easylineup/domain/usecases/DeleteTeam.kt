/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Completable

class DeleteTeam(
    private val dao: TeamRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(team: Team): Completable {
        val isMain = team.main
        return dao.deleteTeam(team)
            .andThen(
                if (isMain) {
                    // we have deleted the main team, let's choose another as main
                    dao.getTeamsRx()
                        .flatMapCompletable { teams ->
                            if (teams.isEmpty()) {
                                Completable.error(NoSuchElementException())
                            } else {
                                val newMain = teams.first().apply {
                                    main = true
                                }
                                dao.updateTeam(newMain)
                            }
                        }
                } else {
                    // the main team was not the one we deleted, no need to designate another one
                    Completable.complete()
                }
            )
            .subscribeOn(schedulersProvider.io())
    }
}
