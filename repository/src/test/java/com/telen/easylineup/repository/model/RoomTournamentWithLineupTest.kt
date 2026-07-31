/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomTournamentWithLineupTest {
    @Test
    fun shouldMapRoomTournamentWithLineupToDomain() {
        val room = RoomTournamentWithLineup(
            tournamentId = 1L,
            tournamentName = "Tournament",
            tournamentCreatedAt = 1000L,
            tournamentStartTime = 2000L,
            tournamentEndTime = 3000L,
            tournamentAddress = "1 rue de Paris",
            fieldPositionId = 4L,
            lineupName = "Lineup",
            lineupId = 5L,
            lineupMode = 1,
            lineupStrategy = 2,
            lineupExtraHittersSize = 1,
            x = 0.5f,
            y = 0.75f,
            position = 6,
            teamId = 7L,
            lineupEventTime = 4000L,
            lineupCreatedTime = 5000L,
            roster = "1;2;3"
        )

        val domain = room.toDomain()

        assertEquals(room.tournamentId, domain.tournamentId)
        assertEquals(room.tournamentName, domain.tournamentName)
        assertEquals(room.tournamentCreatedAt, domain.tournamentCreatedAt)
        assertEquals(room.tournamentStartTime, domain.tournamentStartTime)
        assertEquals(room.tournamentEndTime, domain.tournamentEndTime)
        assertEquals(room.tournamentAddress, domain.tournamentAddress)
        assertEquals(room.fieldPositionId, domain.fieldPositionId)
        assertEquals(room.lineupName, domain.lineupName)
        assertEquals(room.lineupId, domain.lineupId)
        assertEquals(room.lineupMode, domain.lineupMode)
        assertEquals(room.lineupStrategy, domain.lineupStrategy)
        assertEquals(room.lineupExtraHittersSize, domain.lineupExtraHittersSize)
        assertEquals(room.x, domain.x)
        assertEquals(room.y, domain.y)
        assertEquals(room.position, domain.position)
        assertEquals(room.teamId, domain.teamId)
        assertEquals(room.lineupEventTime, domain.lineupEventTime)
        assertEquals(room.lineupCreatedTime, domain.lineupCreatedTime)
        assertEquals(room.roster, domain.roster)
    }
}
