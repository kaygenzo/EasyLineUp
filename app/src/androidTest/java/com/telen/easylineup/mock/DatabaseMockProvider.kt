/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.mock

import android.content.Context
import com.google.gson.JsonParser
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.usecases.InsertLineups
import com.telen.easylineup.domain.usecases.InsertPlayerFieldPositions
import com.telen.easylineup.domain.usecases.InsertPlayerNumberOverlays
import com.telen.easylineup.domain.usecases.InsertPlayers
import com.telen.easylineup.domain.usecases.InsertTeam
import com.telen.easylineup.domain.usecases.InsertTournaments

class DatabaseMockProvider(
    private val insertTeamUseCase: InsertTeam,
    private val insertPlayersUseCase: InsertPlayers,
    private val insertPlayerNumberOverlaysUseCase: InsertPlayerNumberOverlays,
    private val insertLineupsUseCase: InsertLineups,
    private val insertPlayerFieldPositionsUseCase: InsertPlayerFieldPositions,
    private val insertTournamentsUseCase: InsertTournaments
) {

    suspend fun createMockDatabase(context: Context) {
        val json: String
        try {
            val input = context.assets.open("database.json")
            val size = input.available()
            val buffer = ByteArray(size)
            input.read(buffer)
            input.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            throw ex
        }

        run {
                try {
                    val root = JsonParser().parse(json).asJsonObject
                    val teamJson = root.getAsJsonObject("team")
                    val playersJson = root.getAsJsonArray("players")
                    val tournamentsJson = root.getAsJsonArray("tournaments")
                    val lineupsJson = root.getAsJsonArray("lineups")
                    val positionsJson = root.getAsJsonArray("playerPositions")
                    val overlaysJson = root.getAsJsonArray("playerNumberOverlays")

                    val team = Team(
                        teamJson.get("id").asLong,
                        teamJson.get("name").asString,
                        teamJson.get("image").asString,
                        type = teamJson.get("type").asInt,
                        main = teamJson.get("main").asBoolean
                    )

                    val playersList: MutableList<Player> = mutableListOf()
                    for (i in 0 until playersJson.size()) {
                        val line = playersJson[i].asJsonObject
                        playersList.add(
                            Player(
                                id = line["id"].asLong,
                                teamId = line["teamId"].asLong,
                                name = line["name"].asString,
                                shirtNumber = line["shirtNumber"].asInt,
                                licenseNumber = line["licenseNumber"].asLong,
                                image = if (line.has("image")) line["image"].asString else null,
                                email = if (line.has("email")) line["email"].asString else null,
                                phone = if (line.has("phone")) line["phone"].asString else null
                            )
                        )
                    }

                    val tournamentsList: MutableList<Tournament> = mutableListOf()
                    for (i in 0 until tournamentsJson.size()) {
                        val line = tournamentsJson[i].asJsonObject
                        tournamentsList.add(
                            Tournament(
                                line["id"].asLong,
                                line["name"].asString,
                                line["createdAt"].asLong,
                                line["startTime"].asLong,
                                line["endTime"].asLong,
                                try {
                                    line["address"].asString } catch (e: Exception) {
                                    null
                                }
                            )
                        )
                    }

                    val lineupsList: MutableList<Lineup> = mutableListOf()
                    for (i in 0 until lineupsJson.size()) {
                        val line = lineupsJson[i].asJsonObject
                        lineupsList.add(
                            Lineup(
                                line["id"].asLong,
                                line["name"].asString,
                                line["teamId"].asLong,
                                line["tournamentId"].asLong,
                                line["mode"].asInt,
                                line["strategy"].asInt,
                                line["extraHitters"].asInt,
                                line["eventTime"].asLong,
                                line["createdTimeInMillis"].asLong,
                                line["editedTimeInMillis"].asLong
                            )
                        )
                    }

                    val positionsList: MutableList<PlayerFieldPosition> = mutableListOf()
                    for (i in 0 until positionsJson.size()) {
                        val line = positionsJson[i].asJsonObject
                        positionsList.add(
                            PlayerFieldPosition(
                                line["id"].asLong,
                                line["playerId"].asLong,
                                line["lineupId"].asLong,
                                line["position"].asInt,
                                line["x"].asFloat,
                                line["y"].asFloat,
                                line["order"].asInt
                            )
                        )
                    }

                    val overlaysList: MutableList<PlayerNumberOverlay> = mutableListOf()
                    for (i in 0 until overlaysJson.size()) {
                        val line = overlaysJson[i].asJsonObject
                        overlaysList.add(
                            PlayerNumberOverlay(
                                line["id"].asLong, line["lineupId"].asLong, line["playerId"].asLong,
                                line["number"].asInt
                            )
                        )
                    }

                    insertTeam(team)
                    insertPlayers(playersList)
                    insertTournaments(tournamentsList)
                    insertLineups(lineupsList)
                    insertPlayerFieldPositions(positionsList)
                    insertPlayerNumberOverlays(overlaysList)
                } catch (e: Exception) {
                    throw e
                }
        }
    }

    private suspend fun insertTeam(team: Team) {
        insertTeamUseCase(team).getOrThrow()
    }

    private suspend fun insertPlayers(list: List<Player>) {
        insertPlayersUseCase(list).getOrThrow()
    }

    private suspend fun insertLineups(list: List<Lineup>) {
        insertLineupsUseCase(list).getOrThrow()
    }

    private suspend fun insertPlayerFieldPositions(list: List<PlayerFieldPosition>) {
        insertPlayerFieldPositionsUseCase(list).getOrThrow()
    }

    private suspend fun insertPlayerNumberOverlays(list: List<PlayerNumberOverlay>) {
        insertPlayerNumberOverlaysUseCase(list).getOrThrow()
    }

    private suspend fun insertTournaments(list: List<Tournament>) {
        insertTournamentsUseCase(list).getOrThrow()
    }
}
