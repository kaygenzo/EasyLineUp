package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerTests {

    @Mock lateinit var playerDao: PlayerRepository
    lateinit var mSavePlayer: SavePlayer

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSavePlayer = SavePlayer(playerDao)
        val player = Player(id = 1L, teamId = 1, name = "Test", image = null, shirtNumber = 1, licenseNumber = 1L, positions = 1,
                pitching = 1, batting = 3, hash = "hash")
        Mockito.`when`(playerDao.updatePlayer(any())).thenReturn(Completable.complete())
        Mockito.`when`(playerDao.insertPlayer(any())).thenReturn(Single.just(1))
        Mockito.`when`(playerDao.getPlayerByIdAsSingle(any())).thenReturn(Single.just(player))
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() {
        val request = SavePlayer.RequestValues(playerID = 1L, teamID = 1, name = "", positions = 1, licenseNumber = 1, shirtNumber = 1, imageUri = null, pitching = 0, batting = 0)
        val observer = TestObserver<SavePlayer.ResponseValue>()
        mSavePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsWhitespaces() {
        val request = SavePlayer.RequestValues(playerID = 1L, teamID = 1, name = "     ", positions = 1, licenseNumber = 1, shirtNumber = 1, imageUri = null, pitching = 0, batting = 0)
        val observer = TestObserver<SavePlayer.ResponseValue>()
        mSavePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsNull() {
        val request = SavePlayer.RequestValues(playerID = 1L, teamID = 1, name = null, positions = 1, licenseNumber = 1, shirtNumber = 1, imageUri = null, pitching = 0, batting = 0)
        val observer = TestObserver<SavePlayer.ResponseValue>()
        mSavePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldInsertEventIfShirtNumberIsNull() {
        val request = SavePlayer.RequestValues(playerID = 0L, teamID = 1, name = "Test", positions = 1, licenseNumber = 1, shirtNumber = null, imageUri = null, pitching = 0, batting = 0)
        val observer = TestObserver<SavePlayer.ResponseValue>()
        mSavePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerDao).insertPlayer(any())
    }

    @Test
    fun shouldInsertIfNewPlayer() {
        val request = SavePlayer.RequestValues(playerID = 0L, teamID = 1, name = "Test", positions = 1, licenseNumber = 1, shirtNumber = 1, imageUri = null, pitching = 0, batting = 0)
        val observer = TestObserver<SavePlayer.ResponseValue>()
        mSavePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerDao).insertPlayer(any())
        verify(playerDao, never()).updatePlayer(any())
    }

    @Test
    fun shouldUpdateIfKnownPlayer() {
        val request = SavePlayer.RequestValues(playerID = 1L, teamID = 1, name = "Test", positions = 1, licenseNumber = 1, shirtNumber = 1, imageUri = null, pitching = 0, batting = 0)
        val observer = TestObserver<SavePlayer.ResponseValue>()
        mSavePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerDao).updatePlayer(any())
        verify(playerDao, never()).insertPlayer(any())
    }
}