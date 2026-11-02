/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.GeocodingProvider
import com.telen.easylineup.domain.usecases.GetTournamentMapLink
import com.telen.easylineup.domain.usecases.exceptions.AddressNotFoundException
import com.telen.easylineup.domain.usecases.exceptions.TournamentMapNotFoundException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetTournamentMapLinkTests {
    @Mock lateinit var geocodingProvider: GeocodingProvider
    lateinit var getTournamentMapLink: GetTournamentMapLink
    lateinit var tournament: Tournament

    @Before
    fun init() {
        getTournamentMapLink = GetTournamentMapLink(geocodingProvider, testDispatcherProvider())
        tournament = Tournament(
            id = 1L, name = "champs", createdAt = 0L, startTime = 0L, endTime = 0L,
            address = "1 rue de Paris"
        )
    }

    @Test
    fun shouldTriggerAddressNotFoundExceptionWhenTournamentAddressIsNull() = runTest {
        tournament = tournament.copy(address = null)
        val result = getTournamentMapLink(tournament)
        assertTrue(result.exceptionOrNull() is AddressNotFoundException)
    }

    @Test
    fun shouldTriggerTournamentMapNotFoundExceptionWhenGeocodingReturnsNull() = runTest {
        Mockito.`when`(geocodingProvider.getLocationFromAddress("1 rue de Paris")).thenReturn(null)
        val result = getTournamentMapLink(tournament)
        assertTrue(result.exceptionOrNull() is TournamentMapNotFoundException)
    }

    @Test
    fun shouldReturnLocationWhenGeocodingSucceeds() = runTest {
        val location = GeoLocation(latitude = 48.85, longitude = 2.35)
        Mockito.`when`(geocodingProvider.getLocationFromAddress("1 rue de Paris"))
            .thenReturn(location)

        val result = getTournamentMapLink(tournament)

        assertTrue(result.isSuccess)
        assertEquals(location, result.getOrNull())
    }
}
