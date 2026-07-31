/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import com.telen.easylineup.domain.model.Tournament
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomTournamentTest {
    @Test
    fun shouldMapRoomTournamentToDomain() {
        val room = RoomTournament(
            id = 1L,
            name = "Tournament",
            createdAt = 1000L,
            startTime = 2000L,
            endTime = 3000L,
            address = "1 rue de Paris",
            hash = "hash-1"
        )

        val domain = room.toDomain()

        assertEquals(room.id, domain.id)
        assertEquals(room.name, domain.name)
        assertEquals(room.createdAt, domain.createdAt)
        assertEquals(room.startTime, domain.startTime)
        assertEquals(room.endTime, domain.endTime)
        assertEquals(room.address, domain.address)
        assertEquals(room.hash, domain.hash)
    }

    @Test
    fun shouldMapDomainTournamentToRoom() {
        val domain = Tournament(
            id = 1L,
            name = "Tournament",
            createdAt = 1000L,
            startTime = 2000L,
            endTime = 3000L,
            address = "1 rue de Paris",
            hash = "hash-1"
        )

        val room = domain.toRoom()

        assertEquals(domain.id, room.id)
        assertEquals(domain.name, room.name)
        assertEquals(domain.createdAt, room.createdAt)
        assertEquals(domain.startTime, room.startTime)
        assertEquals(domain.endTime, room.endTime)
        assertEquals(domain.address, room.address)
        assertEquals(domain.hash, room.hash)
    }
}
