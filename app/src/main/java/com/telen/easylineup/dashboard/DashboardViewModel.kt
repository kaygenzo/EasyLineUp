package com.telen.easylineup.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.dashboard.models.*
import com.telen.easylineup.domain.GetMostUsedPlayer
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.domain.GetTeamSize
import com.telen.easylineup.repository.model.Team
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DashboardViewModel: ViewModel() {
    private val tilesLiveData = MutableLiveData<List<ITileData>>()
    private var loadDisposable: Disposable? = null
    private val getTeamUseCase = GetTeam(App.database.teamDao())
    private val getTeamSizeUseCase = GetTeamSize(App.database.playerDao())
    private val getMostUsedPlayerUseCase = GetMostUsedPlayer(App.database.playerFieldPositionsDao(), App.database.playerDao())

    fun registerTilesLiveData(): LiveData<List<ITileData>> {
        return tilesLiveData
    }

    fun registerTeamChange(): LiveData<List<Team>> {
        return App.database.teamDao().getTeams()
    }

    fun loadTiles() {
        loadDisposable?.takeIf { !it.isDisposed }?.dispose()
        loadDisposable = getTeam().flatMap {
            getShakeBeta()
                    .concatWith(getTeamSize(it))
                    .concatWith(getMostUsedPlayer(it))
                    .concatWith(getLastLineup(it))
                    .toList()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tilesLiveData.value = it
                }, {
                    Timber.e(it)
                })
    }

    private fun getTeam(): Single<Team> {
        return UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()).map { it.team }
    }

    private fun getShakeBeta(): Maybe<ITileData> {
        return Maybe.just(ShakeBetaData())
    }

    private fun getTeamSize(team: Team): Maybe<ITileData> {
        return UseCaseHandler.execute(getTeamSizeUseCase, GetTeamSize.RequestValues(team))
                .flatMapMaybe { Maybe.just(it.data) }
    }

    private fun getMostUsedPlayer(team: Team): Maybe<ITileData> {
        return UseCaseHandler.execute(getMostUsedPlayerUseCase, GetMostUsedPlayer.RequestValues(team))
                .flatMapMaybe {
                    it.data?.let { data ->
                        Maybe.just(data)
                    } ?: Maybe.empty<ITileData>()
                }
    }

    private fun getLastLineup(team: Team): Maybe<ITileData> {
//        return App.database.lineupDao().getLastLineup()
//                .flatMapMaybe { Maybe.just(it) }
//                .flatMap {
//                    val name = it.name
//                    App.database.lineupDao().getAllPlayersWithPositionsForLineupRx(it.id)
//                            .map { positions ->
//                                LastLineupData(name, positions)
//                            }
//                }
        return Maybe.empty()
    }
}