/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import com.telen.easylineup.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomPlayerTest {
    @Test
    fun shouldMapRoomPlayerToDomain() {
        val room = RoomPlayer(
            id = 1L,
            teamId = 2L,
            name = "Player",
            shirtNumber = 7,
            licenseNumber = 12345L,
            image = "image.png",
            positions = 3,
            pitching = 1,
            batting = 2,
            email = "player@easylineup.com",
            phone = "+33612345678",
            sex = 1,
            hash = "hash-1"
        )

        val domain = room.toDomain()

        assertEquals(room.id, domain.id)
        assertEquals(room.teamId, domain.teamId)
        assertEquals(room.name, domain.name)
        assertEquals(room.shirtNumber, domain.shirtNumber)
        assertEquals(room.licenseNumber, domain.licenseNumber)
        assertEquals(room.image, domain.image)
        assertEquals(room.positions, domain.positions)
        assertEquals(room.pitching, domain.pitching)
        assertEquals(room.batting, domain.batting)
        assertEquals(room.email, domain.email)
        assertEquals(room.phone, domain.phone)
        assertEquals(room.sex, domain.sex)
        assertEquals(room.hash, domain.hash)
    }

    @Test
    fun shouldMapDomainPlayerToRoom() {
        val domain = Player(
            id = 1L,
            teamId = 2L,
            name = "Player",
            shirtNumber = 7,
            licenseNumber = 12345L,
            image = "image.png",
            positions = 3,
            pitching = 1,
            batting = 2,
            email = "player@easylineup.com",
            phone = "+33612345678",
            sex = 1,
            hash = "hash-1"
        )

        val room = domain.toRoom()

        assertEquals(domain.id, room.id)
        assertEquals(domain.teamId, room.teamId)
        assertEquals(domain.name, room.name)
        assertEquals(domain.shirtNumber, room.shirtNumber)
        assertEquals(domain.licenseNumber, room.licenseNumber)
        assertEquals(domain.image, room.image)
        assertEquals(domain.positions, room.positions)
        assertEquals(domain.pitching, room.pitching)
        assertEquals(domain.batting, room.batting)
        assertEquals(domain.email, room.email)
        assertEquals(domain.phone, room.phone)
        assertEquals(domain.sex, room.sex)
        assertEquals(domain.hash, room.hash)
    }
}
