package com.telen.easylineup.domain

import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.model.TeamType
import io.reactivex.Single

class NameEmptyException: Exception()

class SaveTeam(val dao: TeamDao): UseCase<SaveTeam.RequestValues, SaveTeam.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.team)
                .flatMap { team ->
                    if("" != team.name.trim()) {
                        if(team.type == TeamType.UNKNOWN.id) {
                            team.type = TeamType.BASEBALL.id
                        }
                        if(team.id == 0L) {
                            dao.insertTeam(team).map { id ->
                                team.id = id
                                team
                            }
                        }
                        else {
                            dao.updateTeam(team).andThen(Single.just(team))
                        }
                    }
                    else
                        Single.error(NameEmptyException())
                }
                .flatMap {
                    Single.just(ResponseValue(it))
                }
    }

    class ResponseValue(val team: Team): UseCase.ResponseValue
    class RequestValues(val team: Team): UseCase.RequestValues
}