/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomPlayerGamesCountTest {
    @Test
    fun shouldMapRoomPlayerGamesCountToDomain() {
        val room = RoomPlayerGamesCount(
            playerId = 1L,
            size = 12
        )

        val domain = room.toDomain()

        assertEquals(room.playerId, domain.playerId)
        assertEquals(room.size, domain.size)
    }
}
