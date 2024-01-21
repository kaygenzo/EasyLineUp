/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.repository.PlayerRepository
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.domain.utils.ValidatorUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SavePlayerTests {
    val observer: TestObserver<SavePlayer.ResponseValue> = TestObserver()

    @Mock
    lateinit var playerDao: PlayerRepository
    lateinit var validatorUtils: ValidatorUtils
    lateinit var savePlayer: SavePlayer

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        savePlayer = SavePlayer(playerDao)
        val player = Player(
            id = 1L,
            teamId = 1,
            name = "Test",
            image = null,
            shirtNumber = 1,
            licenseNumber = 1L,
            positions = 1,
            pitching = 1,
            batting = 3,
            hash = "hash",
            email = "p1@test.com",
            phone = "001"
        )
        Mockito.`when`(playerDao.updatePlayer(any())).thenReturn(Completable.complete())
        Mockito.`when`(playerDao.insertPlayer(any())).thenReturn(Single.just(1))
        Mockito.`when`(playerDao.getPlayerByIdAsSingle(any())).thenReturn(Single.just(player))
        validatorUtils = ValidatorUtilsMock()
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsEmpty() {
        val request = SavePlayer.RequestValues(
            validatorUtils = validatorUtils,
            playerId = 1L,
            teamId = 1,
            name = "",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            imageUri = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        savePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsWhitespaces() {
        val request = SavePlayer.RequestValues(
            validatorUtils = validatorUtils,
            playerId = 1L,
            teamId = 1,
            name = "     ",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            imageUri = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        savePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldTriggerNameEmptyExceptionIfNameIsNull() {
        val request = SavePlayer.RequestValues(
            validatorUtils = validatorUtils,
            playerId = 1L,
            teamId = 1,
            name = null,
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            imageUri = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        savePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldInsertEventIfShirtNumberIsNull() {
        val request = SavePlayer.RequestValues(
            validatorUtils = validatorUtils,
            playerId = 0L,
            teamId = 1,
            name = "Test",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = null,
            imageUri = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        savePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerDao).insertPlayer(any())
    }

    @Test
    fun shouldInsertIfNewPlayer() {
        val request = SavePlayer.RequestValues(
            validatorUtils = validatorUtils,
            playerId = 0L,
            teamId = 1,
            name = "Test",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            imageUri = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        savePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerDao).insertPlayer(any())
        verify(playerDao, never()).updatePlayer(any())
    }

    @Test
    fun shouldUpdateIfKnownPlayer() {
        val request = SavePlayer.RequestValues(
            validatorUtils = validatorUtils,
            playerId = 1L,
            teamId = 1,
            name = "Test",
            positions = 1,
            licenseNumber = 1,
            shirtNumber = 1,
            imageUri = null,
            pitching = 0,
            batting = 0,
            email = "p1@test.com",
            phone = "001",
            sex = 0
        )
        savePlayer.executeUseCase(request).subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(playerDao).updatePlayer(any())
        verify(playerDao, never()).insertPlayer(any())
    }

    inner class ValidatorUtilsMock : ValidatorUtils() {
        override fun isEmailValid(email: String?): Boolean {
            return true
        }

        override fun isValidPhoneNumber(phone: String?): Boolean {
            return true
        }
    }
}
