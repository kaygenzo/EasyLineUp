/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.GeocodingPort
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.exceptions.AddressNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.MapApiKeyNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.TournamentMapNotFoundException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetTournamentMapLinkTests {
    @Mock lateinit var geocodingPort: GeocodingPort
    lateinit var getTournamentMapLink: GetTournamentMapLink
    lateinit var tournament: Tournament

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        getTournamentMapLink = GetTournamentMapLink(geocodingPort, testDispatcherProvider())
        tournament = Tournament(
            id = 1L, name = "champs", createdAt = 0L, startTime = 0L, endTime = 0L,
            address = "1 rue de Paris"
        )
    }

    @Test
    fun shouldTriggerMapApiKeyNotFoundExceptionWhenApiKeyIsNull() = runTest {
        val result = getTournamentMapLink(tournament, null, 100, 100)
        assertTrue(result.exceptionOrNull() is MapApiKeyNotFoundException)
    }

    @Test
    fun shouldTriggerMapApiKeyNotFoundExceptionWhenApiKeyIsEmpty() = runTest {
        val result = getTournamentMapLink(tournament, "", 100, 100)
        assertTrue(result.exceptionOrNull() is MapApiKeyNotFoundException)
    }

    @Test
    fun shouldTriggerAddressNotFoundExceptionWhenTournamentAddressIsNull() = runTest {
        tournament.address = null
        val result = getTournamentMapLink(tournament, "apiKey", 100, 100)
        assertTrue(result.exceptionOrNull() is AddressNotFoundException)
    }

    @Test
    fun shouldTriggerTournamentMapNotFoundExceptionWhenGeocodingReturnsNull() = runTest {
        Mockito.`when`(geocodingPort.getLocationFromAddress("1 rue de Paris")).thenReturn(null)
        val result = getTournamentMapLink(tournament, "apiKey", 100, 100)
        assertTrue(result.exceptionOrNull() is TournamentMapNotFoundException)
    }

    @Test
    fun shouldBuildMapInfoWhenGeocodingSucceeds() = runTest {
        val location = GeoLocation(latitude = 48.85, longitude = 2.35)
        Mockito.`when`(geocodingPort.getLocationFromAddress("1 rue de Paris")).thenReturn(location)

        val result = getTournamentMapLink(tournament, "apiKey", 300, 200)

        assertTrue(result.isSuccess)
        val mapInfo = result.getOrNull()
        assertEquals(location, mapInfo?.location)
        assertTrue(mapInfo?.url!!.contains("2.35,48.85"))
        assertTrue(mapInfo.url!!.contains("300x200"))
        assertTrue(mapInfo.url!!.contains("apikey=apiKey"))
    }
}
