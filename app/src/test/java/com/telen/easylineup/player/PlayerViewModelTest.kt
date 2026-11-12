/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.PlayerSideSelection
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
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
        licenseNumber = "100",
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
            0L,
            SavedStateHandle(),
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
        viewModel = buildViewModel(0L)
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

        viewModel = buildViewModel(5L)
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

    // Unlike the shirt number, a license number is free text: federations issue
    // alphanumeric ones.
    @Test
    fun shouldKeepLettersInLicenseNumberInput() {
        viewModel.onLicenseNumberChanged("FR-22841A")

        assertEquals("FR-22841A", viewModel.uiState.value.licenseNumberText)
    }

    @Test
    fun shouldUpdateNameAndClearNameError() {
        viewModel.onNameChanged("Jonas")

        val state = viewModel.uiState.value
        assertEquals("Jonas", state.name)
        assertFalse(state.nameError)
    }

    @Test
    fun shouldUpdateImageUri() {
        val uri = Mockito.mock(Uri::class.java)

        viewModel.onImageSelected(uri)

        assertEquals(uri, viewModel.uiState.value.imageUri)
    }

    @Test
    fun shouldClearImageUriWhenSelectedIsNull() {
        viewModel.onImageSelected(Mockito.mock(Uri::class.java))

        viewModel.onImageSelected(null)

        assertEquals(null, viewModel.uiState.value.imageUri)
    }

    @Test
    fun shouldUpdateSex() {
        viewModel.onSexSelected(Sex.FEMALE)

        assertEquals(Sex.FEMALE, viewModel.uiState.value.sex)
    }

    @Test
    fun shouldUpdateEmailAndClearEmailError() {
        viewModel.onEmailChanged("jonas@easylineup.com")

        val state = viewModel.uiState.value
        assertEquals("jonas@easylineup.com", state.email)
        assertFalse(state.emailError)
    }

    @Test
    fun shouldUpdatePhoneAndClearPhoneError() {
        viewModel.onPhoneChanged("0601020304")

        val state = viewModel.uiState.value
        assertEquals("0601020304", state.phone)
        assertFalse(state.phoneError)
    }

    @Test
    fun shouldUpdateStrategy() {
        viewModel.onStrategySelected(TeamStrategy.B5_DEFAULT)

        assertEquals(TeamStrategy.B5_DEFAULT, viewModel.uiState.value.strategy)
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

        val events = collectEvents(viewModel.events)

        viewModel.onSaveClick()

        assertEquals(listOf(PlayerEvent.Saved), events)
        val state = viewModel.uiState.value
        assertFalse(state.nameError)
        assertFalse(state.emailError)
        assertFalse(state.phoneError)
    }

    @Test
    fun shouldSetNameErrorWhenSaveFailsWithNameEmpty() = runTest {
        stubSaveFailure(NameEmptyException())

        val events = collectEvents(viewModel.events)

        viewModel.onSaveClick()

        assertEquals(listOf(PlayerEvent.SaveRejected), events)
        assertTrue(viewModel.uiState.value.nameError)
    }

    @Test
    fun shouldSetEmailErrorWhenSaveFailsWithInvalidEmail() = runTest {
        stubSaveFailure(InvalidEmailException())

        val events = collectEvents(viewModel.events)

        viewModel.onSaveClick()

        assertEquals(listOf(PlayerEvent.SaveRejected), events)
        assertTrue(viewModel.uiState.value.emailError)
    }

    @Test
    fun shouldSetPhoneErrorWhenSaveFailsWithInvalidPhone() = runTest {
        stubSaveFailure(InvalidPhoneException())

        val events = collectEvents(viewModel.events)

        viewModel.onSaveClick()

        assertEquals(listOf(PlayerEvent.SaveRejected), events)
        assertTrue(viewModel.uiState.value.phoneError)
    }

    @Test
    fun shouldReportTheDeleteWhenDeletePlayerSucceeds() = runTest {
        runBlocking { Mockito.`when`(deletePlayerUseCase(0L)).thenReturn(Result.success(Unit)) }

        val events = collectEvents(viewModel.events)

        viewModel.onDeleteConfirmed()

        assertEquals(listOf(PlayerEvent.Deleted), events)
    }

    @Test
    fun shouldReportTheErrorWhenDeletePlayerFails() = runTest {
        val exception = Exception("db error")
        runBlocking {
            Mockito.`when`(deletePlayerUseCase(0L)).thenReturn(Result.failure(exception))
        }

        val events = collectEvents(viewModel.events)

        viewModel.onDeleteConfirmed()

        assertEquals(listOf(PlayerEvent.DeleteFailed("db error")), events)
    }

    /** Starts collecting [flow] on the test scheduler, for the rest of the test. */
    private fun <T> TestScope.collectEvents(flow: Flow<T>): List<T> {
        val events = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.toList(events) }
        return events
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

    private fun buildViewModel(playerId: Long = 0L) = PlayerViewModel(
        playerId,
        SavedStateHandle(),
        getTeamUseCase,
        observePlayer,
        savePlayerUseCase,
        deletePlayerUseCase,
        getPlayerPositionsSummaryUseCase,
        encodePlayerSideFlagUseCase,
        decodePlayerSideFlagUseCase,
        toggleFavoritePositionUseCase
    )

    @Test
    fun shouldResolveTheStrategyFromTheTeamType() = runTest {
        runBlocking {
            Mockito.`when`(getTeamUseCase()).thenReturn(
                Result.success(Team(id = 1L, name = "B5", type = TeamType.BASEBALL_5.id))
            )
        }

        val state = buildViewModel().uiState.value

        assertEquals(TeamType.BASEBALL_5, state.teamType)
        assertEquals(TeamStrategy.B5_DEFAULT, state.strategy)
    }

    @Test
    fun shouldKeepDefaultsWhenTheTeamCannotBeRead() = runTest {
        runBlocking {
            Mockito.`when`(getTeamUseCase()).thenReturn(Result.failure(Exception("boom")))
        }

        assertEquals(TeamStrategy.STANDARD, buildViewModel().uiState.value.strategy)
    }

    @Test
    fun shouldShowEmptyFieldsForUnsetNumbersAndContacts() = runTest {
        val existing = player(5L).copy(
            shirtNumber = 0,
            licenseNumber = "",
            email = null,
            phone = null
        )
        Mockito.`when`(observePlayer(5L)).thenReturn(flowOf(existing))
        runBlocking {
            Mockito.`when`(getPlayerPositionsSummaryUseCase(5L))
                .thenReturn(Result.success(emptyMap()))
        }

        viewModel = buildViewModel(5L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.shirtNumberText)
        assertEquals("", state.licenseNumberText)
        assertEquals("", state.email)
        assertEquals("", state.phone)
    }

    @Test
    fun shouldExposeNumbersAsTextAndAsValues() = runTest {
        Mockito.`when`(observePlayer(5L)).thenReturn(flowOf(player(5L)))
        runBlocking {
            Mockito.`when`(getPlayerPositionsSummaryUseCase(5L))
                .thenReturn(Result.success(emptyMap()))
        }

        viewModel = buildViewModel(5L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("7", state.shirtNumberText)
        assertEquals(7, state.shirtNumber)
        assertEquals("100", state.licenseNumberText)
    }

    @Test
    fun shouldReadUnsetNumbersAsZero() {
        val state = viewModel.uiState.value

        assertEquals(0, state.shirtNumber)
        assertEquals("", state.licenseNumberText)
    }

    @Test
    fun shouldKeepThePlayerWhenThePositionsSummaryFails() = runTest {
        Mockito.`when`(observePlayer(5L)).thenReturn(flowOf(player(5L)))
        runBlocking {
            Mockito.`when`(getPlayerPositionsSummaryUseCase(5L))
                .thenReturn(Result.failure(Exception("boom")))
        }

        viewModel = buildViewModel(5L)
        advanceUntilIdle()

        assertEquals("Jonas", viewModel.uiState.value.name)
        assertTrue(viewModel.uiState.value.positionsData.isEmpty())
    }

    @Test
    fun shouldTurnEverySideBackOffOnASecondToggle() {
        viewModel.onBattingLeftToggled()
        viewModel.onBattingRightToggled()
        viewModel.onPitchingLeftToggled()
        viewModel.onPitchingRightToggled()
        viewModel.onBattingLeftToggled()
        viewModel.onBattingRightToggled()
        viewModel.onPitchingLeftToggled()
        viewModel.onPitchingRightToggled()

        val state = viewModel.uiState.value
        assertFalse(state.battingLeft)
        assertFalse(state.battingRight)
        assertFalse(state.pitchingLeft)
        assertFalse(state.pitchingRight)
    }

    @Test
    fun shouldReportASaveFailureNoFieldCanPointAt() = runTest {
        stubSaveFailure(IllegalStateException("db error"))

        val events = collectEvents(viewModel.events)

        viewModel.onSaveClick()

        assertEquals(listOf(PlayerEvent.SaveFailed), events)
        val state = viewModel.uiState.value
        assertFalse(state.nameError)
        assertFalse(state.emailError)
        assertFalse(state.phoneError)
    }

    private fun viewModelWith(handle: SavedStateHandle, playerId: Long) = PlayerViewModel(
        playerId,
        handle,
        getTeamUseCase,
        observePlayer,
        savePlayerUseCase,
        deletePlayerUseCase,
        getPlayerPositionsSummaryUseCase,
        encodePlayerSideFlagUseCase,
        decodePlayerSideFlagUseCase,
        toggleFavoritePositionUseCase
    )

    @Test
    fun shouldRestoreTypedInputOverTheDatabaseAfterTheProcessWasKilled() = runTest {
        val handle = SavedStateHandle()
        val dbUpdates = MutableSharedFlow<Player>(replay = 1)
        Mockito.`when`(observePlayer(5L)).thenReturn(dbUpdates)
        runBlocking {
            Mockito.`when`(getPlayerPositionsSummaryUseCase(5L))
                .thenReturn(Result.success(emptyMap()))
        }
        dbUpdates.emit(player(5L))
        val beforeKill = viewModelWith(handle, 5L)
        beforeKill.onNameChanged("Typed name")
        beforeKill.onEmailChanged("typed@easylineup.com")

        val restored = viewModelWith(handle, 5L)
        advanceUntilIdle()

        assertEquals("Typed name", restored.uiState.value.name)
        assertEquals("typed@easylineup.com", restored.uiState.value.email)
        assertFalse(restored.uiState.value.isLoading)
    }

    @Test
    fun shouldFollowTheDatabaseAgainAfterTheRestoredInput() = runTest {
        val handle = SavedStateHandle()
        val dbUpdates = MutableSharedFlow<Player>(replay = 1)
        Mockito.`when`(observePlayer(5L)).thenReturn(dbUpdates)
        runBlocking {
            Mockito.`when`(getPlayerPositionsSummaryUseCase(5L))
                .thenReturn(Result.success(emptyMap()))
        }
        dbUpdates.emit(player(5L))
        viewModelWith(handle, 5L).apply {
            onNameChanged("Typed name")
        }
        val restored = viewModelWith(handle, 5L)

        dbUpdates.emit(player(5L).copy(name = "Saved elsewhere"))
        advanceUntilIdle()

        assertEquals("Saved elsewhere", restored.uiState.value.name)
    }

    @Test
    fun shouldRestoreANewPlayersForm() = runTest {
        val handle = SavedStateHandle()
        Mockito.`when`(toggleFavoritePositionUseCase(any(), any()))
            .thenReturn(setOf(FieldPosition.PITCHER))
        viewModelWith(handle, 0L).apply {
            onNameChanged("Rookie")
            onShirtNumberChanged("42")
            onPositionToggled(FieldPosition.PITCHER)
        }

        val restored = viewModelWith(handle, 0L)

        assertEquals("Rookie", restored.uiState.value.name)
        assertEquals("42", restored.uiState.value.shirtNumberText)
        assertEquals(setOf(FieldPosition.PITCHER), restored.uiState.value.selectedPositions)
    }
}
