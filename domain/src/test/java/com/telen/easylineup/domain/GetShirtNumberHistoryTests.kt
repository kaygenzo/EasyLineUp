package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.PlayerNumberOverlay
import com.telen.easylineup.domain.model.ShirtNumberEntry
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.GetShirtNumberHistory
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetShirtNumberHistoryTests {

    @Mock lateinit var playerRepo: PlayerRepository
    lateinit var mGetShirtNumberEntry: GetShirtNumberHistory

    lateinit var entry1: ShirtNumberEntry
    lateinit var entry2: ShirtNumberEntry
    lateinit var entry3: ShirtNumberEntry
    lateinit var entry4: ShirtNumberEntry
    lateinit var entry5: ShirtNumberEntry

    lateinit var overlay1: PlayerNumberOverlay
    lateinit var overlay2: PlayerNumberOverlay

    lateinit var shirtNumberOverlay1: ShirtNumberEntry
    lateinit var shirtNumberOverlay2: ShirtNumberEntry

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetShirtNumberEntry = GetShirtNumberHistory(playerRepo)

        entry1 = ShirtNumberEntry(1, "toto", 1L, 1L, 1L,1L, "lineup1")
        entry4 = ShirtNumberEntry(1, "tutu", 2L, 2L, 1L,1L, "lineup1")

        entry2 = ShirtNumberEntry(1, "toto", 1L, 4L, 2L,2L, "lineup2")
        entry3 = ShirtNumberEntry(1, "tata", 2L, 5L, 2L,2L, "lineup2")
        entry5 = ShirtNumberEntry(42, "titi", 3L, 6L, 2L,3L, "lineup3")
        overlay1 = PlayerNumberOverlay(1, 2L, 3L, 1, "hash")
        overlay2 = PlayerNumberOverlay(2, 3L, 3L, 1, "hash")

        shirtNumberOverlay1 = ShirtNumberEntry(1, "titi", 3L, 6L, 2L, 2L, "lineup2")
        shirtNumberOverlay2 = ShirtNumberEntry(1, "test", 3L, 7L, 2L, 3L, "lineup3")

        Mockito.`when`(playerRepo.getShirtNumberOverlay(1L, 1L)).thenReturn(Single.error(Exception()))
        Mockito.`when`(playerRepo.getShirtNumberOverlay(2L, 1L)).thenReturn(Single.error(Exception()))
        Mockito.`when`(playerRepo.getShirtNumberOverlay(1L, 2L)).thenReturn(Single.error(Exception()))
        Mockito.`when`(playerRepo.getShirtNumberOverlay(2L, 2L)).thenReturn(Single.error(Exception()))
        Mockito.`when`(playerRepo.getShirtNumberOverlay(3L, 3L)).thenReturn(Single.error(Exception()))

        Mockito.`when`(playerRepo.getShirtNumberFromNumberOverlays(1)).thenReturn(Single.just(listOf()))
    }

    @Test
    fun shouldGetAllShirtNumberFromPositions() {
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1)).thenReturn(Single.just(listOf(entry1, entry2, entry3, entry4)))

        val observer = TestObserver<GetShirtNumberHistory.ResponseValue>()
        mGetShirtNumberEntry.executeUseCase(GetShirtNumberHistory.RequestValues(1))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(listOf(entry3, entry2, entry4, entry1), observer.values().first().history)
    }

    @Test
    fun shouldGetAllShirtNumberFromPositionsAndOverlays() {
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(1)).thenReturn(Single.just(listOf(entry1, entry2, entry3, entry4)))

        Mockito.`when`(playerRepo.getShirtNumberFromNumberOverlays(1)).thenReturn(Single.just(listOf(
                shirtNumberOverlay1, shirtNumberOverlay2
        )))

        val observer = TestObserver<GetShirtNumberHistory.ResponseValue>()
        mGetShirtNumberEntry.executeUseCase(GetShirtNumberHistory.RequestValues(1))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        val result = listOf(shirtNumberOverlay2, shirtNumberOverlay1, entry3, entry2, entry4, entry1)
        Assert.assertEquals(result, observer.values().first().history)
    }

    @Test
    fun shouldNotGetPlayerNumberIfOverlayExists() {
        Mockito.`when`(playerRepo.getShirtNumberFromPlayers(42)).thenReturn(Single.just(listOf(entry5)))
        Mockito.`when`(playerRepo.getShirtNumberOverlay(3L, 3L)).thenReturn(Single.just(overlay2))
        Mockito.`when`(playerRepo.getShirtNumberFromNumberOverlays(42)).thenReturn(Single.just(listOf()))

        val observer = TestObserver<GetShirtNumberHistory.ResponseValue>()
        mGetShirtNumberEntry.executeUseCase(GetShirtNumberHistory.RequestValues(42))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        val result: List<ShirtNumberEntry> = listOf()
        Assert.assertEquals(result, observer.values().first().history)
    }
}