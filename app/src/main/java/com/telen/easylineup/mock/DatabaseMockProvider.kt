package com.telen.easylineup.mock

import android.content.Context
import com.google.gson.JsonParser
import com.telen.easylineup.App
import com.telen.easylineup.data.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.util.*


class DatabaseMockProvider {

    fun createMockDatabase(context: Context): Completable {
         return Single.create<String> { emitter ->

            var json: String? = null
            try {
                val input = context.assets.open("database.json")
                val size = input.available()
                val buffer = ByteArray(size)
                input.read(buffer)
                input.close()
                json = String(buffer, Charsets.UTF_8)
                emitter.onSuccess(json)
            } catch (ex: Throwable) {
                ex.printStackTrace()
                emitter.onError(ex)
            }
        }
                 .flatMapCompletable { json ->
                     try {
                         val root = JsonParser().parse(json).asJsonObject
                         val teamJson = root.getAsJsonObject("team")
                         val playersJson = root.getAsJsonArray("players")
                         val tournamentsJson = root.getAsJsonArray("tournaments")
                         val lineupsJson = root.getAsJsonArray("lineups")
                         val positionsJson = root.getAsJsonArray("playerPositions")

                         val team = Team(teamJson.get("id").asLong, teamJson.get("name").asString, teamJson.get("image").asString, type = teamJson.get("type").asInt)

                         val playersList = mutableListOf<Player>()
                         for( i in 0 until playersJson.size()) {
                             val line = playersJson[i].asJsonObject
                             playersList.add(Player(line["id"].asLong, line["teamId"].asLong, line["name"].asString,
                                     line["shirtNumber"].asInt, line["licenseNumber"].asLong,
                                     if(line.has("image")) line["image"].asString else null))
                         }

                         val tournamentsList = mutableListOf<Tournament>()
                         for( i in 0 until tournamentsJson.size()) {
                             val line = tournamentsJson[i].asJsonObject
                             tournamentsList.add(Tournament(line["id"].asLong, line["name"].asString, line["createdAt"].asLong))
                         }

                         val lineupsList = mutableListOf<Lineup>()
                         for( i in 0 until lineupsJson.size()) {
                             val line = lineupsJson[i].asJsonObject
                             lineupsList.add(Lineup(line["id"].asLong, line["name"].asString, line["teamId"].asLong,
                                     line["tournamentId"].asLong, line["mode"].asInt, line["createdTimeInMillis"].asLong, line["editedTimeInMillis"].asLong))
                         }

                         val positionsList = mutableListOf<PlayerFieldPosition>()
                         for( i in 0 until positionsJson.size()) {
                             val line = positionsJson[i].asJsonObject
                             positionsList.add(PlayerFieldPosition(line["id"].asLong, line["playerId"].asLong, line["lineupId"].asLong,
                                     line["position"].asInt, line["x"].asFloat, line["y"].asFloat, line["order"].asInt))
                         }

                         insertTeam(team).andThen(insertPlayers(playersList))
                                 .andThen(insertTournaments(tournamentsList))
                                 .andThen(insertLineups(lineupsList))
                                 .andThen(insertPlayerFieldPositions(positionsList))

                     } catch (e: Exception) {
                         Completable.error(e)
                     }
                 }
    }

    private fun insertTeam(team:Team): Completable {
        return App.database.teamDao().insertTeam(team).ignoreElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun insertPlayers(list: List<Player>): Completable {
        return App.database.playerDao().insertPlayers(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun insertLineups(list: List<Lineup>): Completable {
        return App.database.lineupDao().insertLineup(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun insertPlayerFieldPositions(list: List<PlayerFieldPosition>): Completable {
        return App.database.lineupDao().insertPlayerFieldPosition(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun insertTournaments(list: List<Tournament>): Completable {
        return App.database.tournamentDao().insertTournaments(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}