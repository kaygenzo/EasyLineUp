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
import io.reactivex.rxjava3.observers.TestObserver
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
        getTournamentMapLink = GetTournamentMapLink(geocodingPort, testSchedulersProvider())
        tournament = Tournament(
            id = 1L, name = "champs", createdAt = 0L, startTime = 0L, endTime = 0L,
            address = "1 rue de Paris"
        )
    }

    @Test
    fun shouldTriggerMapApiKeyNotFoundExceptionWhenApiKeyIsNull() {
        val observer = TestObserver<com.telen.easylineup.domain.model.MapInfo>()
        getTournamentMapLink(tournament, null, 100, 100).subscribe(observer)
        observer.await()
        observer.assertError(MapApiKeyNotFoundException::class.java)
    }

    @Test
    fun shouldTriggerMapApiKeyNotFoundExceptionWhenApiKeyIsEmpty() {
        val observer = TestObserver<com.telen.easylineup.domain.model.MapInfo>()
        getTournamentMapLink(tournament, "", 100, 100).subscribe(observer)
        observer.await()
        observer.assertError(MapApiKeyNotFoundException::class.java)
    }

    @Test
    fun shouldTriggerAddressNotFoundExceptionWhenTournamentAddressIsNull() {
        tournament.address = null
        val observer = TestObserver<com.telen.easylineup.domain.model.MapInfo>()
        getTournamentMapLink(tournament, "apiKey", 100, 100).subscribe(observer)
        observer.await()
        observer.assertError(AddressNotFoundException::class.java)
    }

    @Test
    fun shouldTriggerTournamentMapNotFoundExceptionWhenGeocodingReturnsNull() {
        Mockito.`when`(geocodingPort.getLocationFromAddress("1 rue de Paris")).thenReturn(null)
        val observer = TestObserver<com.telen.easylineup.domain.model.MapInfo>()
        getTournamentMapLink(tournament, "apiKey", 100, 100).subscribe(observer)
        observer.await()
        observer.assertError(TournamentMapNotFoundException::class.java)
    }

    @Test
    fun shouldBuildMapInfoWhenGeocodingSucceeds() {
        val location = GeoLocation(latitude = 48.85, longitude = 2.35)
        Mockito.`when`(geocodingPort.getLocationFromAddress("1 rue de Paris")).thenReturn(location)

        val observer = TestObserver<com.telen.easylineup.domain.model.MapInfo>()
        getTournamentMapLink(tournament, "apiKey", 300, 200).subscribe(observer)
        observer.await()

        observer.assertComplete()
        val result = observer.values().first()
        assertEquals(location, result.location)
        assertTrue(result.url!!.contains("2.35,48.85"))
        assertTrue(result.url!!.contains("300x200"))
        assertTrue(result.url!!.contains("apikey=apiKey"))
    }
}
