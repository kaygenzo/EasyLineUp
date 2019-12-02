package com.telen.easylineup.team

import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.repository.model.Player
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class TeamViewModel: ViewModel() {

    private val getTeamUseCase = GetTeam(App.database.teamDao(), App.prefs)

    fun getPlayers(): Single<List<Player>> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues())
                .map { it.team }
                .flatMap {
                    App.database.playerDao().getPlayersForTeamRx(it.id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                }
    }
}