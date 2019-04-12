package com.telen.easylineup.data

import androidx.arch.core.util.Function
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.telen.easylineup.App
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.function.Consumer

class DatabaseMockProvider {

    private fun getMockFieldPosition(): List<PlayerFieldPosition> {
        val players = getMockPlayers()
        val listResult = mutableListOf<PlayerFieldPosition>()
        var position = 1
        var x = 10f
        var y = 10f
        players.forEach { player ->
            if(position <= 9) {
                listResult.add(PlayerFieldPosition(position, player.id, 1, position, x, y))
                position++
                x+=10
                y+=10
            }
        }
        return listResult
    }

    private fun getMockTeam(): Team {
        val team = Team(1,"Seadogs", null, mutableListOf())
        return team
    }

    private fun getMockTournaments(): List<Tournament> {
        val tournaments: MutableList<Tournament> = mutableListOf()
        tournaments.add(Tournament(1,"Breal Tournament"))
        tournaments.add(Tournament(2,"Brest Tournament"))
        tournaments.add(Tournament(3,"Rennes Tournament"))
        return tournaments
    }

    private fun getMockPlayers() : List<Player> {
        var playersList = ArrayList<Player>()
        playersList.add(Player(9, 1,"Karim", 20, 1))
        playersList.add(Player(1, 1,"Valentin", 90, 2))
        playersList.add(Player(2, 1,"Antoine", 10, 3))
        playersList.add(Player(3, 1,"Vincent", 24, 4))
        playersList.add(Player(4, 1,"Angie", 1, 5))
        playersList.add(Player(5, 1,"Cyrille", 2, 6))
        playersList.add(Player(6 ,1,"JM", 3, 7))
        playersList.add(Player(7, 1,"Lya", 4, 8))
        playersList.add(Player(8, 1,"Fanny", 5, 9))
        return playersList
    }

    private fun getMockLineUps(): List<Lineup> {
        val list: MutableList<Lineup> = mutableListOf()
        val currentTimeInmillis = Calendar.getInstance().timeInMillis
        list.add(Lineup(1,"Seadogs vs Seadogs 1", getMockTeam().id, 1, currentTimeInmillis, currentTimeInmillis+100, mutableListOf()))
        list.add(Lineup(2,"Seadogs vs Seadogs 2", getMockTeam().id, 1, currentTimeInmillis, currentTimeInmillis+200,mutableListOf()))
        list.add(Lineup(3,"Seadogs vs Seadogs 3", getMockTeam().id, 1, currentTimeInmillis, currentTimeInmillis+300,mutableListOf()))
        list.add(Lineup(4,"Seadogs vs Seadogs 4", getMockTeam().id, 1, currentTimeInmillis, currentTimeInmillis+400,mutableListOf()))
        list.add(Lineup(5,"Seadogs vs Seadogs 5", getMockTeam().id, 1, currentTimeInmillis, currentTimeInmillis+500,mutableListOf()))
        list.add(Lineup(6,"Seadogs vs Seadogs 6", getMockTeam().id, 2, currentTimeInmillis, currentTimeInmillis+600,mutableListOf()))
        list.add(Lineup(7,"Seadogs vs Seadogs 7", getMockTeam().id, 3, currentTimeInmillis, currentTimeInmillis+700,mutableListOf()))
        list.add(Lineup(8,"Seadogs vs Seadogs 8", getMockTeam().id, 3, currentTimeInmillis, currentTimeInmillis+800,mutableListOf()))
        return list
    }

    fun insertTeam(): Completable {
        val team = getMockTeam()
        return App.database.teamDao().insertTeam(team)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun checkTeam(lifecycle: LifecycleOwner) {
        App.database.teamDao().getTeams().observe(lifecycle, Observer { teams ->
            teams.forEach { team ->
                Timber.d("team -> $team")
            }
        })
    }

    fun insertPlayers(): Completable {
        return App.database.playerDao().insertPlayers(getMockPlayers())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun checkPlayers(lifecycle: LifecycleOwner) {
        App.database.playerDao().getPlayers().observe(lifecycle, Observer {  players ->
            players.forEach { player ->
                Timber.d("player -> $player")
            }
        })
    }

    fun insertLineups(): Completable {
        return App.database.lineupDao().insertLineup(getMockLineUps())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun checkLineups(lifecycle: LifecycleOwner) {
        App.database.lineupDao().getAllLineup().observe(lifecycle, Observer {  lineups ->
            lineups.forEach { lineup ->
                Timber.d("lineup -> $lineup")
            }
        })
    }

    fun insertPlayerFieldPositions(): Completable {
        return App.database.lineupDao().insertPlayerFieldPosition(getMockFieldPosition())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun checkPlayerFieldPositions(lifecycle: LifecycleOwner) {
        App.database.lineupDao().getAllPlayerFieldPositions().observe(lifecycle, Observer {  playerFieldPositions ->
            playerFieldPositions.forEach { playerFieldPosition ->
                Timber.d("playerFieldPosition -> $playerFieldPosition")
            }
        })
    }

    fun insertTournaments(): Completable {
        return App.database.tournamentDao().insertTournaments(getMockTournaments())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun checkTournaments(lifecycle: LifecycleOwner) {
        App.database.tournamentDao().getTournaments().observe(lifecycle, Observer {  tournaments ->
            tournaments.forEach { tournament ->
                Timber.d("tournament -> $tournament")
            }
        })
    }
}