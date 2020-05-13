package com.telen.easylineup.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.tiles.ITileData
import com.telen.easylineup.domain.model.tiles.ShakeBetaData
import com.telen.easylineup.domain.application.ApplicationPort
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class DashboardViewModel: ViewModel(), KoinComponent {
    private val tilesLiveData = MutableLiveData<List<ITileData>>()
    private var loadDisposable: Disposable? = null

    private val domain: ApplicationPort by inject()

    fun registerTilesLiveData(): LiveData<List<ITileData>> {
        return tilesLiveData
    }

    fun registerTeamChange(): LiveData<List<Team>> {
        return domain.observeTeams()
    }

    fun loadTiles() {
        loadDisposable?.takeIf { !it.isDisposed }?.dispose()
        loadDisposable = getTeam().flatMap {
            getShakeBeta()
                    .concatWith(getTeamSize(it))
                    .concatWith(getMostUsedPlayer(it))
                    .concatWith(getLastLineup())
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
        return domain.getTeam()
    }

    private fun getShakeBeta(): Maybe<ITileData> {
        return Maybe.just(ShakeBetaData())
    }

    private fun getTeamSize(team: Team): Maybe<ITileData> {
        return domain.getTeamSize(team)
    }

    private fun getMostUsedPlayer(team: Team): Maybe<ITileData> {
        return domain.getMostUsedPlayer(team)
    }

    private fun getLastLineup(): Maybe<ITileData> {
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