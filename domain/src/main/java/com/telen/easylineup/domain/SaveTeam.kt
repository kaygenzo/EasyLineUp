package com.telen.easylineup.domain

import android.text.TextUtils
import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Team
import io.reactivex.Single

class NameEmptyException: Exception()

class SaveTeam(val dao: TeamDao): UseCase<SaveTeam.RequestValues, SaveTeam.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.just(requestValues.team)
                .flatMap { team ->
                    if(!TextUtils.isEmpty(team.name.trim())) {
                        //TODO put all other main to false
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