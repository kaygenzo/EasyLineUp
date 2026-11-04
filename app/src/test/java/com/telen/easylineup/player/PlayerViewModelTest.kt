/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.PlayerSideSelection
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.DecodePlayerSideFlag
import com.telen.easylineup.domain.usecases.DeletePlayer
import com.telen.easylineup.domain.usecases.EncodePlayerSideFlag
import com.telen.easylineup.domain.usecases.GetPositionsSummaryForPlayer
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ObservePlayer
import com.telen.easylineup.domain.usecases.SavePlayer
import com.telen.easylineup.domain.usecases.ToggleFavoritePosition
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull

/**
 * Unit tests for [PlayerViewModel]: every constructor use case is mocked directly (each already
 * has its own domain-level test suite), so this only verifies the ViewModel's own orchestration.
 */
@RunWith(MockitoJUnitRunner::class)
internal class PlayerViewModelTest {
    @Mock
    lateinit var getTeamUseCase: GetTeam

    @Mock
    lateinit var observePlayer: ObservePlayer

    @Mock
    lateinit var savePlayerUseCase: SavePlayer

    @Mock
    lateinit var deletePlayerUseCase: DeletePlayer

    @Mock
    lateinit var getPlayerPositionsSummaryUseCase: GetPositionsSummaryForPlayer

    @Mock
    lateinit var encodePlayerSideFlagUseCase: EncodePlayerSideFlag

    @Mock
    lateinit var decodePlayerSideFlagUseCase: DecodePlayerSideFlag

    @Mock
    lateinit var toggleFavoritePositionUseCase: ToggleFavoritePosition

    lateinit var viewModel: PlayerViewModel

    private fun player(id: Long) = Player(
        id = id,
        teamId = 1L,
        name = "Jonas",
        shirtNumber = 7,
        licenseNumber = 100L,
        batting = PlayerSide.LEFT.flag,
        pitching = PlayerSide.RIGHT.flag,
        email = "jonas@easylineup.com",
        phone = "0601020304",
        sex = Sex.MALE.id
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        runBlocking {
            // lenient: init{} always fetches the team, but not every test cares about the result
            Mockito.lenient().`when`(getTeamUseCase())
                .thenReturn(Result.success(Team(id = 1L, name = "Panthers", type = 1, main = true)))
        }
        Mockito.lenient().`when`(decodePlayerSideFlagUseCase(PlayerSide.LEFT.flag))
            .thenReturn(PlayerSideSelection(left = true, right = false))
        Mockito.lenient().`when`(decodePlayerSideFlagUseCase(PlayerSide.RIGHT.flag))
            .thenReturn(PlayerSideSelection(left = false, right = true))

        viewModel = PlayerViewModel(
            getTeamUseCase,
            observePlayer,
            savePlayerUseCase,
            deletePlayerUseCase,
            getPlayerPositionsSummaryUseCase,
            encodePlayerSideFlagUseCase,
            decodePlayerSideFlagUseCase,
            toggleFavoritePositionUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldStopLoadingImmediatelyForANewPlayer() = runTest {
        viewModel.playerId = 0L
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun shouldPopulateUiStateWhenAnExistingPlayerLoads() = runTest {
        val existing = player(5L)
        Mockito.`when`(observePlayer(5L)).thenReturn(flowOf(existing))
        runBlocking {
            Mockito.`when`(getPlayerPositionsSummaryUseCase(5L))
                .thenReturn(Result.success(mapOf(FieldPosition.SHORT_STOP to 3)))
        }

        viewModel.playerId = 5L
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Jonas", state.name)
        assertEquals("jonas@easylineup.com", state.email)
        assertEquals("0601020304", state.phone)
        assertTrue(state.battingLeft)
        assertFalse(state.battingRight)
        assertFalse(state.pitchingLeft)
        assertTrue(state.pitchingRight)
        assertEquals(PlayerSide.LEFT, state.battingSide)
        assertEquals(PlayerSide.RIGHT, state.pitchingSide)
        assertEquals(mapOf(FieldPosition.SHORT_STOP to 3), state.positionsData)
    }

    @Test
    fun shouldDelegateBattingLeftToggleToEncodeUseCase() {
        Mockito.`when`(encodePlayerSideFlagUseCase(true, false)).thenReturn(PlayerSide.LEFT.flag)

        viewModel.onBattingLeftToggled()

        val state = viewModel.uiState.value
        assertTrue(state.battingLeft)
        assertEquals(PlayerSide.LEFT, state.battingSide)
    }

    @Test
    fun shouldDelegatePitchingRightToggleToEncodeUseCase() {
        Mockito.`when`(encodePlayerSideFlagUseCase(false, true)).thenReturn(PlayerSide.RIGHT.flag)

        viewModel.onPitchingRightToggled()

        val state = viewModel.uiState.value
        assertTrue(state.pitchingRight)
        assertEquals(PlayerSide.RIGHT, state.pitchingSide)
    }

    // onBattingRightToggled/onPitchingLeftToggled are hand-written near-duplicates of
    // onBattingLeftToggled/onPitchingRightToggled above (each reads its OWN field's current
    // value to flip and the OTHER field's current value to pass through unchanged) - a
    // copy-paste that swapped which side reads which field would pass the two tests above
    // silently without these.
    @Test
    fun shouldDelegateBattingRightToggleToEncodeUseCase() {
        Mockito.`when`(encodePlayerSideFlagUseCase(false, true)).thenReturn(PlayerSide.RIGHT.flag)

        viewModel.onBattingRightToggled()

        val state = viewModel.uiState.value
        assertFalse(state.battingLeft)
        assertTrue(state.battingRight)
        assertEquals(PlayerSide.RIGHT, state.battingSide)
    }

    @Test
    fun shouldDelegatePitchingLeftToggleToEncodeUseCase() {
        Mockito.`when`(encodePlayerSideFlagUseCase(true, false)).thenReturn(PlayerSide.LEFT.flag)

        viewModel.onPitchingLeftToggled()

        val state = viewModel.uiState.value
        assertTrue(state.pitchingLeft)
        assertFalse(state.pitchingRight)
        assertEquals(PlayerSide.LEFT, state.pitchingSide)
    }

    @Test
    fun shouldDelegatePositionToggleToUseCase() {
        val expected = setOf(FieldPosition.PITCHER)
        Mockito.`when`(toggleFavoritePositionUseCase(emptySet(), FieldPosition.PITCHER))
            .thenReturn(expected)

        viewModel.onPositionToggled(FieldPosition.PITCHER)

        assertEquals(expected, viewModel.uiState.value.selectedPositions)
    }

    @Test
    fun shouldFilterNonDigitsFromShirtNumberInput() {
        viewModel.onShirtNumberChanged("1a2b3")

        assertEquals("123", viewModel.uiState.value.shirtNumberText)
    }

    @Test
    fun shouldSucceedOnSaveClickWhenValidationPasses() = runTest {
        runBlocking {
            Mockito.`when`(
                savePlayerUseCase(
                    Mockito.anyLong(),
                    Mockito.anyString(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    Mockito.anyInt(),
                    Mockito.anyInt(),
                    Mockito.anyInt(),
                    any(),
                    any(),
                    Mockito.anyInt()
                )
            ).thenReturn(Result.success(Unit))
        }

        val result = viewModel.onSaveClick()

        assertTrue(result)
        val state = viewModel.uiState.value
        assertFalse(state.nameError)
        assertFalse(state.emailError)
        assertFalse(state.phoneError)
    }

    @Test
    fun shouldSetNameErrorWhenSaveFailsWithNameEmpty() = runTest {
        stubSaveFailure(NameEmptyException())

        val result = viewModel.onSaveClick()

        assertFalse(result)
        assertTrue(viewModel.uiState.value.nameError)
    }

    @Test
    fun shouldSetEmailErrorWhenSaveFailsWithInvalidEmail() = runTest {
        stubSaveFailure(InvalidEmailException())

        val result = viewModel.onSaveClick()

        assertFalse(result)
        assertTrue(viewModel.uiState.value.emailError)
    }

    @Test
    fun shouldSetPhoneErrorWhenSaveFailsWithInvalidPhone() = runTest {
        stubSaveFailure(InvalidPhoneException())

        val result = viewModel.onSaveClick()

        assertFalse(result)
        assertTrue(viewModel.uiState.value.phoneError)
    }

    @Test
    fun shouldReturnSuccessResultWhenDeletePlayerSucceeds() = runTest {
        runBlocking { Mockito.`when`(deletePlayerUseCase(0L)).thenReturn(Result.success(Unit)) }

        val result = viewModel.deletePlayer()

        assertTrue(result.isSuccess)
    }

    @Test
    fun shouldReturnFailureResultWhenDeletePlayerFails() = runTest {
        val exception = Exception("db error")
        runBlocking {
            Mockito.`when`(deletePlayerUseCase(0L)).thenReturn(Result.failure(exception))
        }

        val result = viewModel.deletePlayer()

        assertTrue(result.isFailure)
    }

    private fun stubSaveFailure(throwable: Throwable) = runBlocking {
        Mockito.`when`(
            savePlayerUseCase(
                Mockito.anyLong(),
                Mockito.anyString(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                Mockito.anyInt(),
                Mockito.anyInt(),
                Mockito.anyInt(),
                any(),
                any(),
                Mockito.anyInt()
            )
        ).thenReturn(Result.failure(throwable))
    }
}
