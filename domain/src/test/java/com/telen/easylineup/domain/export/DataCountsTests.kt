/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.export

import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.export.LineupExport
import com.telen.easylineup.domain.model.export.PlayerExport
import com.telen.easylineup.domain.model.export.TeamExport
import com.telen.easylineup.domain.model.export.TournamentExport
import com.telen.easylineup.domain.model.export.toDataCounts
import org.junit.Assert.assertEquals
import org.junit.Test

internal class DataCountsTests {

    private fun player(id: String) = PlayerExport(
        id = id,
        name = "p$id",
        image = null,
        shirtNumber = 0,
        licenseNumber = "0",
        positions = 0,
        pitching = 0,
        batting = 0,
        email = null,
        phone = null,
        sex = 0
    )

    private fun lineup(id: String) = LineupExport(
        id = id,
        name = "l$id",
        eventTime = 0L,
        createdAt = 0L,
        editedAt = 0L,
        mode = 0,
        strategy = 0,
        extraHitters = 0,
        roster = null,
        playerPositions = emptyList(),
        playerNumberOverlays = emptyList()
    )

    private fun tournament(id: String, lineups: List<LineupExport>) = TournamentExport(
        id = id,
        name = "t$id",
        createdAt = 0L,
        startTime = 0L,
        endTime = 0L,
        address = null,
        lineups = lineups
    )

    @Test
    fun shouldCountEverythingAcrossAllTeams() {
        val team1 = TeamExport(
            id = "1",
            name = "team1",
            image = null,
            type = 0,
            main = true,
            players = listOf(player("1"), player("2")),
            tournaments = listOf(
                tournament("1", listOf(lineup("1"), lineup("2"))),
                tournament("2", listOf(lineup("3")))
            )
        )
        val team2 = TeamExport(
            id = "2",
            name = "team2",
            image = null,
            type = 0,
            main = false,
            players = listOf(player("3")),
            tournaments = listOf(tournament("3", emptyList()))
        )

        val result = ExportBase(listOf(team1, team2)).toDataCounts()

        assertEquals(2, result.teams)
        assertEquals(3, result.players)
        assertEquals(3, result.lineups)
        assertEquals(3, result.tournaments)
    }

    @Test
    fun shouldReturnZeroCountsForAnEmptyExport() {
        val result = ExportBase(emptyList()).toDataCounts()

        assertEquals(0, result.teams)
        assertEquals(0, result.players)
        assertEquals(0, result.lineups)
        assertEquals(0, result.tournaments)
    }
}
