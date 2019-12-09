package com.telen.easylineup.domain

import android.content.SharedPreferences
import com.telen.easylineup.repository.model.Team
import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.data.TournamentDao
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Tournament
import io.reactivex.Single

class GetTournaments(val dao: TournamentDao): UseCase<GetTournaments.RequestValues, GetTournaments.ResponseValue>() {

    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTournaments().map { ResponseValue(it) }
    }

    class ResponseValue(val tournaments: List<Tournament>): UseCase.ResponseValue
    class RequestValues: UseCase.RequestValues
}