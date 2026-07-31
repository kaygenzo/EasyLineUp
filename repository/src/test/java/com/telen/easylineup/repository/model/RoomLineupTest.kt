/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import com.telen.easylineup.domain.model.Lineup
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomLineupTest {
    @Test
    fun shouldMapRoomLineupToDomain() {
        val room = RoomLineup(
            id = 1L,
            name = "Lineup",
            teamId = 2L,
            tournamentId = 3L,
            mode = 1,
            strategy = 2,
            extraHitters = 1,
            eventTimeInMillis = 1000L,
            createdTimeInMillis = 2000L,
            editedTimeInMillis = 3000L,
            roster = "1;2;3",
            hash = "hash-1"
        )

        val domain = room.toDomain()

        assertEquals(room.id, domain.id)
        assertEquals(room.name, domain.name)
        assertEquals(room.teamId, domain.teamId)
        assertEquals(room.tournamentId, domain.tournamentId)
        assertEquals(room.mode, domain.mode)
        assertEquals(room.strategy, domain.strategy)
        assertEquals(room.extraHitters, domain.extraHitters)
        assertEquals(room.eventTimeInMillis, domain.eventTimeInMillis)
        assertEquals(room.createdTimeInMillis, domain.createdTimeInMillis)
        assertEquals(room.editedTimeInMillis, domain.editedTimeInMillis)
        assertEquals(room.roster, domain.roster)
        assertEquals(room.hash, domain.hash)
    }

    @Test
    fun shouldMapDomainLineupToRoom() {
        val domain = Lineup(
            id = 1L,
            name = "Lineup",
            teamId = 2L,
            tournamentId = 3L,
            mode = 1,
            strategy = 2,
            extraHitters = 1,
            eventTimeInMillis = 1000L,
            createdTimeInMillis = 2000L,
            editedTimeInMillis = 3000L,
            roster = "1;2;3",
            hash = "hash-1"
        )

        val room = domain.toRoom()

        assertEquals(domain.id, room.id)
        assertEquals(domain.name, room.name)
        assertEquals(domain.teamId, room.teamId)
        assertEquals(domain.tournamentId, room.tournamentId)
        assertEquals(domain.mode, room.mode)
        assertEquals(domain.strategy, room.strategy)
        assertEquals(domain.extraHitters, room.extraHitters)
        assertEquals(domain.eventTimeInMillis, room.eventTimeInMillis)
        assertEquals(domain.createdTimeInMillis, room.createdTimeInMillis)
        assertEquals(domain.editedTimeInMillis, room.editedTimeInMillis)
        assertEquals(domain.roster, room.roster)
        assertEquals(domain.hash, room.hash)
    }
}
