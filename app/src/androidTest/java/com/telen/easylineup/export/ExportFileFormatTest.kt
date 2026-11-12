/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.export

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.export.json.writeExportFile
import com.telen.easylineup.testing.SeedDatabaseRule
import com.telen.easylineup.testing.TestFixtures
import com.telen.easylineup.testing.inject
import com.telen.easylineup.utils.ImportFileReaderImpl
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The `.elu` file's key names, checked against the app as it ships. A round trip alone can't
 * catch R8 renaming the export classes' fields: the app would write and read back the same
 * wrong names, and only a file from another version or another phone would break. So this
 * reads a file written by an earlier version, and checks the keys of a file written now with a
 * JSON parser R8 never touches. Run it on the minified build too (see `minified` in
 * app/build.gradle), where it matters.
 */
@RunWith(AndroidJUnit4::class)
class ExportFileFormatTest {
    @get:Rule
    val seedRule = SeedDatabaseRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val exportData: ExportData by inject()

    @Test
    fun readsAFileWrittenByAnEarlierVersion() {
        val file = File(context.cacheDir, "earlier_version.elu").apply { writeText(EARLIER_FILE) }

        val export = ImportFileReaderImpl(context).read(Uri.fromFile(file).toString())

        val team = export.teams.single()
        assertEquals("Riverside Hawks", team.name)
        assertEquals("Jonas Alvarez", team.players.single().name)
        assertEquals(7, team.players.single().shirtNumber)
        val tournament = team.tournaments.single()
        assertEquals("Fall Classic", tournament.name)
        val lineup = tournament.lineups.single()
        assertEquals("Opening Day", lineup.name)
        assertEquals(listOf("player-1"), lineup.roster)
        val position = lineup.playerPositions.single()
        assertEquals("player-1", position.playerId)
        assertEquals(12.5f, position.x)
        assertEquals(23, lineup.playerNumberOverlays.single().number)
    }

    @Test
    fun writesTheFileFormatsKeyNames() {
        val export = runBlocking { exportData().getOrThrow() }
        val json = StringBuilder().also { writeExportFile(export, it) }.toString()

        val team = JSONObject(json).getJSONArray("teams").getJSONObject(0)
        assertKeys(team, "id", "name", "type", "main", "players", "tournaments")
        assertEquals(TestFixtures.TEAM_NAME, team.getString("name"))
        assertKeys(
            team.getJSONArray("players").getJSONObject(0),
            "id", "name", "shirtNumber", "licenseNumber", "positions", "sex"
        )
        val tournament = team.getJSONArray("tournaments").getJSONObject(0)
        assertKeys(tournament, "id", "name", "createdAt", "startTime", "endTime", "lineups")
        val lineup = tournament.getJSONArray("lineups").getJSONObject(0)
        assertKeys(
            lineup,
            "id", "name", "eventTime", "createdAt", "editedAt", "mode", "strategy",
            "extraHitters", "playerPositions", "playerNumberOverlays"
        )
        assertKeys(
            lineup.getJSONArray("playerPositions").getJSONObject(0),
            "id", "playerID", "position", "x", "y", "flags", "order"
        )
    }

    private fun assertKeys(json: JSONObject, vararg keys: String) {
        keys.forEach { assertTrue("missing \"$it\" in $json", json.has(it)) }
    }

    private companion object {
        val EARLIER_FILE = """
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
    }
}
