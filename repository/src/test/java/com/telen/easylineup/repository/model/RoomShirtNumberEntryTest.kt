/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomShirtNumberEntryTest {
    @Test
    fun shouldMapRoomShirtNumberEntryToDomain() {
        val room = RoomShirtNumberEntry(
            number = 7,
            playerName = "Player",
            playerId = 1L,
            eventTime = 1000L,
            createdAt = 2000L,
            lineupId = 2L,
            lineupName = "Lineup"
        )

        val domain = room.toDomain()

        assertEquals(room.number, domain.number)
        assertEquals(room.playerName, domain.playerName)
        assertEquals(room.playerId, domain.playerId)
        assertEquals(room.eventTime, domain.eventTime)
        assertEquals(room.createdAt, domain.createdAt)
        assertEquals(room.lineupId, domain.lineupId)
        assertEquals(room.lineupName, domain.lineupName)
    }
}
