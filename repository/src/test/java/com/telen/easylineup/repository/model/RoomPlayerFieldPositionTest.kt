/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import com.telen.easylineup.domain.model.PlayerFieldPosition
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomPlayerFieldPositionTest {
    @Test
    fun shouldMapRoomPlayerFieldPositionToDomain() {
        val room = RoomPlayerFieldPosition(
            id = 1L,
            playerId = 2L,
            lineupId = 3L,
            position = 4,
            x = 0.5f,
            y = 0.75f,
            order = 1,
            flags = 1,
            hash = "hash-1"
        )

        val domain = room.toDomain()

        assertEquals(room.id, domain.id)
        assertEquals(room.playerId, domain.playerId)
        assertEquals(room.lineupId, domain.lineupId)
        assertEquals(room.position, domain.position)
        assertEquals(room.x, domain.x)
        assertEquals(room.y, domain.y)
        assertEquals(room.order, domain.order)
        assertEquals(room.flags, domain.flags)
        assertEquals(room.hash, domain.hash)
    }

    @Test
    fun shouldMapDomainPlayerFieldPositionToRoom() {
        val domain = PlayerFieldPosition(
            id = 1L,
            playerId = 2L,
            lineupId = 3L,
            position = 4,
            x = 0.5f,
            y = 0.75f,
            order = 1,
            flags = 1,
            hash = "hash-1"
        )

        val room = domain.toRoom()

        assertEquals(domain.id, room.id)
        assertEquals(domain.playerId, room.playerId)
        assertEquals(domain.lineupId, room.lineupId)
        assertEquals(domain.position, room.position)
        assertEquals(domain.x, room.x)
        assertEquals(domain.y, room.y)
        assertEquals(domain.order, room.order)
        assertEquals(domain.flags, room.flags)
        assertEquals(domain.hash, room.hash)
    }
}
