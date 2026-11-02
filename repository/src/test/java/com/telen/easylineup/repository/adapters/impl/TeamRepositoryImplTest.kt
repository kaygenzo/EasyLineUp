/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.repository.dao.TeamDao
import com.telen.easylineup.repository.model.RoomTeam
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify

/**
 * Unit tests for [TeamRepositoryImpl]: verifies it maps domain<->Room correctly and delegates to
 * [TeamDao] - [TeamDao] itself is mocked, so this never touches a real database.
 */
@RunWith(MockitoJUnitRunner::class)
internal class TeamRepositoryImplTest {
    @Mock
    lateinit var teamDao: TeamDao

    private lateinit var repository: TeamRepositoryImpl

    @Before
    fun setup() {
        repository = TeamRepositoryImpl(teamDao)
    }

    @Test
    fun shouldInsertTeamMappedToRoom() = runTest {
        val team = Team(id = 0, name = "Panthers", type = 1, main = true)
        Mockito.`when`(teamDao.insertTeam(any())).thenReturn(42L)

        val result = repository.insertTeam(team)

        assertEquals(42L, result)
        verify(teamDao).insertTeam(argThat { name == "Panthers" && type == 1 && main })
    }

    @Test
    fun shouldDeleteTeamMappedToRoom() = runTest {
        val team = Team(id = 1L, name = "Panthers")

        repository.deleteTeam(team)

        verify(teamDao).deleteTeam(argThat { id == 1L && name == "Panthers" })
    }

    @Test
    fun shouldDeleteTeamsMappedToRoom() = runTest {
        val teams = listOf(Team(id = 1L, name = "A"), Team(id = 2L, name = "B"))

        repository.deleteTeams(teams)

        verify(teamDao).deleteTeams(argThat { size == 2 && map { it.id } == listOf(1L, 2L) })
    }

    @Test
    fun shouldUpdateTeamMappedToRoom() = runTest {
        val team = Team(id = 1L, name = "Panthers Renamed")

        repository.updateTeam(team)

        verify(teamDao).updateTeam(argThat { name == "Panthers Renamed" })
    }

    @Test
    fun shouldUpdateTeamsMappedToRoom() = runTest {
        val teams = listOf(Team(id = 1L, name = "A"), Team(id = 2L, name = "B"))

        repository.updateTeams(teams)

        verify(teamDao).updateTeams(argThat { size == 2 })
    }

    @Test
    fun shouldUpdateTeamsWithRowCountAndReturnCount() = runTest {
        val teams = listOf(Team(id = 1L, name = "A"))
        Mockito.`when`(teamDao.updateTeamsWithRowCount(any())).thenReturn(1)

        val result = repository.updateTeamsWithRowCount(teams)

        assertEquals(1, result)
        verify(teamDao).updateTeamsWithRowCount(argThat { size == 1 })
    }

    @Test
    fun shouldGetTeamByIdMappedToDomain() = runTest {
        val room = RoomTeam(id = 1L, name = "Panthers", type = 2, main = true, hash = "h1")
        Mockito.`when`(teamDao.getTeamById(1L)).thenReturn(room)

        val result = repository.getTeamById(1L)

        assertEquals(Team(id = 1L, name = "Panthers", type = 2, main = true, hash = "h1"), result)
    }

    @Test
    fun shouldGetTeamByHashMappedToDomain() = runTest {
        val room = RoomTeam(id = 1L, name = "Panthers", hash = "h1")
        Mockito.`when`(teamDao.getTeamByHash("h1")).thenReturn(room)

        val result = repository.getTeamByHash("h1")

        assertEquals("Panthers", result?.name)
    }

    @Test
    fun shouldReturnNullWhenTeamHashNotFound() = runTest {
        Mockito.`when`(teamDao.getTeamByHash("missing")).thenReturn(null)

        val result = repository.getTeamByHash("missing")

        assertNull(result)
    }

    @Test
    fun shouldObserveTeamsMappedToDomain() = runTest {
        val rooms = listOf(RoomTeam(id = 1L, name = "A"), RoomTeam(id = 2L, name = "B"))
        Mockito.`when`(teamDao.observeTeams()).thenReturn(flowOf(rooms))

        val result = repository.observeTeams().toList()

        assertEquals(1, result.size)
        assertEquals(listOf("A", "B"), result.first().map { it.name })
    }

    @Test
    fun shouldGetTeamsMappedToDomain() = runTest {
        val rooms = listOf(RoomTeam(id = 1L, name = "A"), RoomTeam(id = 2L, name = "B"))
        Mockito.`when`(teamDao.getTeams()).thenReturn(rooms)

        val result = repository.getTeams()

        assertEquals(listOf("A", "B"), result.map { it.name })
    }
}
