/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomPlayerNumberOverlayTest {
    @Test
    fun shouldMapRoomPlayerNumberOverlayToDomain() {
        val room = RoomPlayerNumberOverlay(
            id = 1L,
            lineupId = 2L,
            playerId = 3L,
            number = 42,
            hash = "hash-1"
        )

        val domain = room.toDomain()

        assertEquals(room.id, domain.id)
        assertEquals(room.lineupId, domain.lineupId)
        assertEquals(room.playerId, domain.playerId)
        assertEquals(room.number, domain.number)
        assertEquals(room.hash, domain.hash)
    }

    @Test
    fun shouldMapDomainPlayerNumberOverlayToRoom() {
        val domain = PlayerNumberOverlay(
            id = 1L,
            lineupId = 2L,
            playerId = 3L,
            number = 42,
            hash = "hash-1"
        )

        val room = domain.toRoom()

        assertEquals(domain.id, room.id)
        assertEquals(domain.lineupId, room.lineupId)
        assertEquals(domain.playerId, room.playerId)
        assertEquals(domain.number, room.number)
        assertEquals(domain.hash, room.hash)
    }
}
