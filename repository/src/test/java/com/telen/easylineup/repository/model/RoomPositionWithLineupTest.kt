/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomPositionWithLineupTest {
    @Test
    fun shouldMapRoomPositionWithLineupToDomain() {
        val room = RoomPositionWithLineup(
            position = 4,
            x = 0.5f,
            y = 0.75f,
            order = 2,
            lineupName = "Lineup",
            tournamentName = "Tournament"
        )

        val domain = room.toDomain()

        assertEquals(room.position, domain.position)
        assertEquals(room.x, domain.x)
        assertEquals(room.y, domain.y)
        assertEquals(room.order, domain.order)
        assertEquals(room.lineupName, domain.lineupName)
        assertEquals(room.tournamentName, domain.tournamentName)
    }
}
