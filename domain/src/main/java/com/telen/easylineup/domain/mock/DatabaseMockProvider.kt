package com.telen.easylineup.domain.mock

import android.content.Context
import com.google.gson.JsonParser
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.domain.model.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject


class DatabaseMockProvider: KoinComponent {

    private val domain: ApplicationPort by inject()

    fun createMockDatabase(context: Context): Completable {
         return Single.create<String> { emitter ->

            var json: String?
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
                         val overlaysJson = root.getAsJsonArray("playerNumberOverlays")

                         val team = Team(teamJson.get("id").asLong, teamJson.get("name").asString, teamJson.get("image").asString, type = teamJson.get("type").asInt, main = teamJson.get("main").asBoolean)

                         val playersList = mutableListOf<Player>()
                         for( i in 0 until playersJson.size()) {
                             val line = playersJson[i].asJsonObject
                             playersList.add(Player(id = line["id"].asLong, teamId = line["teamId"].asLong, name = line["name"].asString,
                                     shirtNumber = line["shirtNumber"].asInt, licenseNumber = line["licenseNumber"].asLong,
                                     image = if (line.has("image")) line["image"].asString else null,
                                     email = if (line.has("email")) line["email"].asString else null,
                                     phone = if (line.has("phone")) line["phone"].asString else null
                             ))
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
                                     line["tournamentId"].asLong, line["mode"].asInt, line["eventTime"].asLong, line["createdTimeInMillis"].asLong, line["editedTimeInMillis"].asLong))
                         }

                         val positionsList = mutableListOf<PlayerFieldPosition>()
                         for( i in 0 until positionsJson.size()) {
                             val line = positionsJson[i].asJsonObject
                             positionsList.add(PlayerFieldPosition(line["id"].asLong, line["playerId"].asLong, line["lineupId"].asLong,
                                     line["position"].asInt, line["x"].asFloat, line["y"].asFloat, line["order"].asInt))
                         }

                         val overlaysList = mutableListOf<PlayerNumberOverlay>()
                         for( i in 0 until overlaysJson.size()) {
                             val line = overlaysJson[i].asJsonObject
                             overlaysList.add(PlayerNumberOverlay(line["id"].asLong, line["lineupId"].asLong, line["playerId"].asLong,
                                     line["number"].asInt))
                         }

                         insertTeam(team).andThen(insertPlayers(playersList))
                                 .andThen(insertTournaments(tournamentsList))
                                 .andThen(insertLineups(lineupsList))
                                 .andThen(insertPlayerFieldPositions(positionsList))
                                 .andThen(insertPlayerNumberOverlays(overlaysList))

                     } catch (e: Exception) {
                         Completable.error(e)
                     }
                 }
    }

    private fun insertTeam(team: Team): Completable {
        return domain.insertTeam(team).ignoreElement()
                .subscribeOn(Schedulers.io())
    }

    private fun insertPlayers(list: List<Player>): Completable {
        return domain.insertPlayers(list)
                .subscribeOn(Schedulers.io())
    }

    private fun insertLineups(list: List<Lineup>): Completable {
        return domain.insertLineups(list)
                .subscribeOn(Schedulers.io())
    }

    private fun insertPlayerFieldPositions(list: List<PlayerFieldPosition>): Completable {
        return domain.insertPlayerFieldPositions(list)
                .subscribeOn(Schedulers.io())
    }

    private fun insertPlayerNumberOverlays(list: List<PlayerNumberOverlay>): Completable {
        return domain.insertPlayerNumberOverlays(list)
                .subscribeOn(Schedulers.io())
    }

    private fun insertTournaments(list: List<Tournament>): Completable {
        return domain.insertTournaments(list)
                .subscribeOn(Schedulers.io())
    }
}