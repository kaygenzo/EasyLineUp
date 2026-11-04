/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export.json

import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.export.LineupExport
import com.telen.easylineup.domain.model.export.PlayerExport
import com.telen.easylineup.domain.model.export.PlayerNumberOverlayExport
import com.telen.easylineup.domain.model.export.PlayerPositionExport
import com.telen.easylineup.domain.model.export.TeamExport
import com.telen.easylineup.domain.model.export.TournamentExport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers the `.elu` file format's only two guarantees: a round-trip through the real wire
 * format never loses/mutates data, and the wire format's own field names (the actual backward
 * compatibility contract for a file exported by an old app version) don't silently drift when
 * [ExportBase]/[TeamExport]/etc. (free to evolve with the domain's own needs) are refactored.
 * Nothing here was covered before - the whole `export.json` package had zero tests.
 */
class JsonExportBaseTests {
    private fun samplePlayer(id: String) = PlayerExport(
        id = id,
        name = "Jonas Alvarez",
        image = "content://photos/$id",
        shirtNumber = 7,
        licenseNumber = "LIC-$id",
        positions = 4,
        pitching = 1,
        batting = 2,
        email = "jonas@easylineup.com",
        phone = "0601020304",
        sex = 1
    )

    private fun samplePlayerWithNullableFieldsNull(id: String) = PlayerExport(
        id = id,
        name = "No Contact Info",
        image = null,
        shirtNumber = 99,
        licenseNumber = "LIC-$id",
        positions = 0,
        pitching = 0,
        batting = 0,
        email = null,
        phone = null,
        sex = 0
    )

    private fun sampleLineup(id: String, playerId: String) = LineupExport(
        id = id,
        name = "Opening Day",
        eventTime = 1_700_000_000_000L,
        createdAt = 1_699_000_000_000L,
        editedAt = 1_699_100_000_000L,
        mode = 1,
        strategy = 0,
        extraHitters = 1,
        roster = listOf(playerId),
        playerPositions = listOf(
            PlayerPositionExport(
                id = "pos-1",
                playerId = playerId,
                position = 3,
                x = 12.5f,
                y = 88.25f,
                flags = 2,
                order = 1
            )
        ),
        playerNumberOverlays = listOf(
            PlayerNumberOverlayExport(id = "overlay-1", playerId = playerId, number = 23)
        )
    )

    private fun sampleTournament(id: String, playerId: String) = TournamentExport(
        id = id,
        name = "Fall Classic",
        createdAt = 1_698_000_000_000L,
        startTime = 1_698_100_000_000L,
        endTime = 1_698_200_000_000L,
        address = "12 Diamond Way",
        lineups = listOf(sampleLineup("lineup-1", playerId))
    )

    private fun sampleExportBase(): ExportBase {
        val playerId = "player-1"
        return ExportBase(
            teams = listOf(
                TeamExport(
                    id = "team-1",
                    name = "Riverside Hawks",
                    image = "content://photos/team-1",
                    type = 0,
                    main = true,
                    createdAt = 1_697_000_000_000L,
                    players = listOf(
                        samplePlayer(playerId),
                        samplePlayerWithNullableFieldsNull("player-2")
                    ),
                    tournaments = listOf(sampleTournament("tournament-1", playerId))
                )
            )
        )
    }

    @Test
    fun roundTripsThroughTheRealWireFormatWithoutLosingData() {
        val original = sampleExportBase()

        val json = exportGson.toJson(original.toJson())
        val roundTripped = exportGson.fromJson(json, JsonExportBase::class.java).toDomain()

        assertEquals(original, roundTripped)
    }

    @Test
    fun roundTripPreservesNullOptionalFields() {
        val original = sampleExportBase()

        val json = exportGson.toJson(original.toJson())
        val roundTripped = exportGson.fromJson(json, JsonExportBase::class.java).toDomain()

        val playerWithNulls = roundTripped.teams.single().players.single { it.id == "player-2" }
        assertNull(playerWithNulls.image)
        assertNull(playerWithNulls.email)
        assertNull(playerWithNulls.phone)
    }

    /**
     * Hand-written JSON mimicking a file an old app version could have produced - locks the
     * actual on-disk key names (`@SerializedName`), not just the current Kotlin property names,
     * so renaming a property without updating its annotation would fail this test instead of
     * silently breaking every existing user's exported file.
     */
    @Test
    fun deserializesAFixedLegacyWireFormatIntoTheExpectedDomainShape() {
        val legacyJson = """
            {
              "teams": [
                {
                  "id": "team-1",
                  "name": "Riverside Hawks",
                  "image": null,
                  "type": 0,
                  "main": true,
                  "createdAt": 1697000000000,
                  "players": [
                    {
                      "id": "player-1",
                      "name": "Jonas Alvarez",
                      "image": null,
                      "shirtNumber": 7,
                      "licenseNumber": "LIC-player-1",
                      "positions": 4,
                      "pitching": 1,
                      "batting": 2,
                      "email": null,
                      "phone": null,
                      "sex": 1
                    }
                  ],
                  "tournaments": [
                    {
                      "id": "tournament-1",
                      "name": "Fall Classic",
                      "createdAt": 1698000000000,
                      "startTime": 1698100000000,
                      "endTime": 1698200000000,
                      "address": "12 Diamond Way",
                      "lineups": [
                        {
                          "id": "lineup-1",
                          "name": "Opening Day",
                          "eventTime": 1700000000000,
                          "createdAt": 1699000000000,
                          "editedAt": 1699100000000,
                          "mode": 1,
                          "strategy": 0,
                          "extraHitters": 1,
                          "roster": ["player-1"],
                          "playerPositions": [
                            {
                              "id": "pos-1",
                              "playerID": "player-1",
                              "position": 3,
                              "x": 12.5,
                              "y": 88.25,
                              "flags": 2,
                              "order": 1
                            }
                          ],
                          "playerNumberOverlays": [
                            {"id": "overlay-1", "playerID": "player-1", "number": 23}
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val domain = exportGson.fromJson(legacyJson, JsonExportBase::class.java).toDomain()

        val team = domain.teams.single()
        assertEquals("Riverside Hawks", team.name)
        val player = team.players.single()
        assertEquals("Jonas Alvarez", player.name)
        val tournament = team.tournaments.single()
        val lineup = tournament.lineups.single()
        assertEquals(listOf("player-1"), lineup.roster)
        assertEquals("player-1", lineup.playerPositions.single().playerId)
        assertEquals("player-1", lineup.playerNumberOverlays.single().playerId)
        assertEquals(23, lineup.playerNumberOverlays.single().number)
    }
}
