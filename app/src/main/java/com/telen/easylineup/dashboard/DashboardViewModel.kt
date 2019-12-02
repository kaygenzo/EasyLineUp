package com.telen.easylineup.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.telen.easylineup.App
import com.telen.easylineup.UseCaseHandler
import com.telen.easylineup.dashboard.models.*
import com.telen.easylineup.domain.GetTeam
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
    private val getTeamUseCase = GetTeam(App.database.teamDao(), App.prefs)

    fun registerTilesLiveData(): LiveData<List<ITileData>> {
        return tilesLiveData
    }

    fun loadTiles() {
        loadDisposable?.takeIf { !it.isDisposed }?.dispose()
        loadDisposable = getShakeBeta()
                .concatWith(getTeamSize())
                .concatWith(getMostUsedPlayer())
                .concatWith(getLastLineup())
                .toList()
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

    private fun getTeamSize(): Maybe<ITileData> {
        return getTeam()
                .flatMapMaybe { team ->
                    App.database.playerDao().getPlayersSingle().subscribeOn(Schedulers.io())
                            .flatMapMaybe { players -> Maybe.just(players) }
                            .map { TeamSizeData(it.size, teamImage = team.image) }
                }
    }

    private fun getMostUsedPlayer(): Maybe<ITileData> {
        return App.database.playerFieldPositionsDao().getMostUsedPlayers()
                .flatMapMaybe {
                    try {
                        val mostUsed = it.first()
                        App.database.playerDao().getPlayerByIdAsSingle(mostUsed.playerID)
                                .flatMapMaybe { Maybe.just(it) }
                                .map { player ->
                                    val tileData: ITileData = MostUsedPlayerData(player.image, player.name, player.shirtNumber, mostUsed.size)
                                    tileData
                                }
                    } catch (e: NoSuchElementException) {
                        Maybe.empty<ITileData>()
                    }
                }
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