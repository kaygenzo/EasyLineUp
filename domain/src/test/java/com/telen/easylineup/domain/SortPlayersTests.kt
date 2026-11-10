/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSortType
import com.telen.easylineup.domain.usecases.SortPlayers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class SortPlayersTests {
    private lateinit var sortPlayers: SortPlayers

    private fun player(name: String, shirtNumber: Int) =
        Player(teamId = 1L, name = name, shirtNumber = shirtNumber, licenseNumber = "0")

    @Before
    fun init() {
        sortPlayers = SortPlayers()
    }

    @Test
    fun shouldSortAlphabeticallyIgnoringCase() {
        val players = listOf(player("bob", 2), player("Alice", 1), player("charlie", 3))

        val result = sortPlayers(players, PlayerSortType.ALPHA)

        assertEquals(listOf("Alice", "bob", "charlie"), result.map { it.name })
    }

    @Test
    fun shouldSortByShirtNumber() {
        val players = listOf(player("bob", 9), player("Alice", 2), player("charlie", 5))

        val result = sortPlayers(players, PlayerSortType.NUMERIC)

        assertEquals(listOf(2, 5, 9), result.map { it.shirtNumber })
    }
}
