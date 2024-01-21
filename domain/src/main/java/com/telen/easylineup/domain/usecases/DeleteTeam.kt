/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class DeleteTeam(private val dao: TeamRepository) :
    UseCase<DeleteTeam.RequestValues, DeleteTeam.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.team)
            .flatMap {
                val isMain = it.main
                dao.deleteTeam(it)
                    .andThen(Single.just(isMain))
                    .flatMapCompletable { mainTeam ->
                        if (mainTeam) {
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
                            // the main team was not the one we deleted, no need to designate
                            // another one
                            Completable.complete()
                        }
                    }
                    .andThen(Single.just(ResponseValue()))
            }
    }

    /**
     * @property team
     */
    class RequestValues(val team: Team) : UseCase.RequestValues
    class ResponseValue : UseCase.ResponseValue
}
