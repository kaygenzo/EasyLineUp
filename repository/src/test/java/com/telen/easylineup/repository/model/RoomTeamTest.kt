/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import com.telen.easylineup.domain.model.Team
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomTeamTest {
    @Test
    fun shouldMapRoomTeamToDomain() {
        val room = RoomTeam(
            id = 1L,
            name = "Panthers",
            image = "image.png",
            type = 2,
            main = true,
            hash = "hash-1"
        )

        val domain = room.toDomain()

        assertEquals(room.id, domain.id)
        assertEquals(room.name, domain.name)
        assertEquals(room.image, domain.image)
        assertEquals(room.type, domain.type)
        assertEquals(room.main, domain.main)
        assertEquals(room.hash, domain.hash)
    }

    @Test
    fun shouldMapDomainTeamToRoom() {
        val domain = Team(
            id = 1L,
            name = "Panthers",
            image = "image.png",
            type = 2,
            main = true,
            hash = "hash-1"
        )

        val room = domain.toRoom()

        assertEquals(domain.id, room.id)
        assertEquals(domain.name, room.name)
        assertEquals(domain.image, room.image)
        assertEquals(domain.type, room.type)
        assertEquals(domain.main, room.main)
        assertEquals(domain.hash, room.hash)
    }
}
