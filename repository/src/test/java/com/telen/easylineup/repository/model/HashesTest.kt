/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import com.telen.easylineup.domain.model.Team
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HashesTest {
    @Test
    fun aRowKeepsTheHashItAlreadyHas() {
        assertEquals("kept", Team(name = "Hawks", hash = "kept").toNewRoom().hash)
    }

    @Test
    fun aRowWithoutAHashGetsANewOneOnInsert() {
        listOf(null, "", "  ").forEach { missing ->
            val hash = Team(name = "Hawks", hash = missing).toNewRoom().hash

            // Throws if it isn't a UUID.
            UUID.fromString(hash)
        }
    }

    @Test
    fun twoInsertedRowsWithoutAHashGetDifferentOnes() {
        val team = Team(name = "Hawks", hash = null)

        assertNotEquals(team.toNewRoom().hash, team.toNewRoom().hash)
    }

    @Test
    fun anUpdateDoesNotMakeAHash() {
        assertNull(Team(name = "Hawks", hash = null).toRoom().hash)
    }
}
