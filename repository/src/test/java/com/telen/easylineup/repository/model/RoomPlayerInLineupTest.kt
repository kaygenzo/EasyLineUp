/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomPlayerInLineupTest {
    @Test
    fun shouldMapRoomPlayerInLineupToDomain() {
        val room = RoomPlayerInLineup(
            lineupName = "Lineup",
            lineupId = 1L,
            position = 4,
            playerName = "Player",
            playerId = 2L
        )

        val domain = room.toDomain()

        assertEquals(room.lineupName, domain.lineupName)
        assertEquals(room.lineupId, domain.lineupId)
        assertEquals(room.position, domain.position)
        assertEquals(room.playerName, domain.playerName)
        assertEquals(room.playerId, domain.playerId)
    }

    @Test
    fun shouldMapRoomPlayerInLineupToDomainWhenPlayerFieldsAreNull() {
        val room = RoomPlayerInLineup(
            lineupName = "Lineup",
            lineupId = 1L,
            position = null,
            playerName = null,
            playerId = null
        )

        val domain = room.toDomain()

        assertEquals(room.lineupName, domain.lineupName)
        assertEquals(room.lineupId, domain.lineupId)
        assertEquals(null, domain.position)
        assertEquals(null, domain.playerName)
        assertEquals(null, domain.playerId)
    }
}
