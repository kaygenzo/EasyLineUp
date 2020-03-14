package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Team
import io.reactivex.Completable
import io.reactivex.Single

class DeleteTeam(private val dao: TeamDao): UseCase<DeleteTeam.RequestValues, DeleteTeam.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.team)
                .flatMap {
                    val isMain = it.main
                    dao.deleteTeam(it)
                            .andThen(Single.just(isMain))
                            .flatMapCompletable { mainTeam ->
                                if(mainTeam) {
                                    //we have deleted the main team, let's choose another as main
                                    dao.getTeamsRx()
                                            .flatMapCompletable { teams ->
                                                if(teams.isEmpty()) {
                                                    Completable.error(NoSuchElementException())
                                                }
                                                else {
                                                    val newMain = teams.first()
                                                    newMain.main = true
                                                    dao.updateTeam(newMain)
                                                }
                                            }
                                }
                                else {
                                    // the main team was not the one we deleted, no need to designate another one
                                    Completable.complete()
                                }
                            }
                            .andThen(Single.just(ResponseValue()))
                }
    }

    class RequestValues(val team: Team): UseCase.RequestValues
    class ResponseValue: UseCase.ResponseValue
}