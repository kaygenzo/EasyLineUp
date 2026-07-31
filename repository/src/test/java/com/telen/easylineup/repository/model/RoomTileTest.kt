/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import com.telen.easylineup.domain.model.DashboardTile
import com.telen.easylineup.domain.model.tiles.TileType
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RoomTileTest {
    @Test
    fun shouldMapRoomTileToDomain() {
        val room = RoomTile(
            id = 1L,
            position = 2,
            type = TileType.MOST_USED_PLAYER.type,
            enabled = false
        )

        val domain = room.toDomain()

        assertEquals(room.id, domain.id)
        assertEquals(room.position, domain.position)
        assertEquals(room.type, domain.type)
        assertEquals(room.enabled, domain.enabled)
    }

    @Test
    fun shouldMapDomainTileToRoom() {
        val domain = DashboardTile(
            id = 1L,
            position = 2,
            type = TileType.MOST_USED_PLAYER.type,
            enabled = false
        )

        val room = domain.toRoom()

        assertEquals(domain.id, room.id)
        assertEquals(domain.position, room.position)
        assertEquals(domain.type, room.type)
        assertEquals(domain.enabled, room.enabled)
    }
}
