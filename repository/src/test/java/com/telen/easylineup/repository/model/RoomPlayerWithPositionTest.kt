/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomPlayerWithPositionTest {
    @Test
    fun shouldMapRoomPlayerWithPositionToDomain() {
        val room = RoomPlayerWithPosition(
            playerName = "Player",
            playerSex = 1,
            shirtNumber = 7,
            licenseNumber = 12345L,
            teamId = 1L,
            image = "image.png",
            position = 4,
            x = 0.5f,
            y = 0.75f,
            flags = 1,
            order = 2,
            fieldPositionId = 3L,
            playerId = 5L,
            lineupId = 6L,
            playerPositions = 7
        )

        val domain = room.toDomain()

        assertEquals(room.playerName, domain.playerName)
        assertEquals(room.playerSex, domain.playerSex)
        assertEquals(room.shirtNumber, domain.shirtNumber)
        assertEquals(room.licenseNumber, domain.licenseNumber)
        assertEquals(room.teamId, domain.teamId)
        assertEquals(room.image, domain.image)
        assertEquals(room.position, domain.position)
        assertEquals(room.x, domain.x)
        assertEquals(room.y, domain.y)
        assertEquals(room.flags, domain.flags)
        assertEquals(room.order, domain.order)
        assertEquals(room.fieldPositionId, domain.fieldPositionId)
        assertEquals(room.playerId, domain.playerId)
        assertEquals(room.lineupId, domain.lineupId)
        assertEquals(room.playerPositions, domain.playerPositions)
    }
}
