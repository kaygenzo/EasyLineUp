/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.errors.DomainErrors
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

private val availablePositions: List<FieldPosition> = FieldPosition.values()
    .filter { it.isDefensePlayer() }
    .groupBy { it.mask }
    .map { it.value.first() }

/** Replaces `PlayerEditFragment`. [playerId] is `0` for a new player, matching the old behavior. */
@Composable
fun PlayerEditRoute(navController: NavController, playerId: Long) {
    val viewModel = koinViewModel<PlayerViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    remember(playerId) { viewModel.playerId = playerId }

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var shirtNumberText by remember { mutableStateOf("") }
    var licenseNumberText by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var sex by remember { mutableStateOf(Sex.UNKNOWN) }
    var battingLeft by remember { mutableStateOf(false) }
    var battingRight by remember { mutableStateOf(false) }
    var pitchingLeft by remember { mutableStateOf(false) }
    var pitchingRight by remember { mutableStateOf(false) }
    var selectedPositions by remember { mutableStateOf<Set<FieldPosition>>(emptySet()) }
    var teamTypeId by remember { mutableStateOf(0) }
    var showDiscardSheet by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            ImagePickerUtils.persistImage(context.contentResolver, it)
            imageUri = it
        }
    }

    // This Fragment-era subscription (registerPlayerFormErrorResult() returns an RxJava
    // Subject) is relocated as-is into a DisposableEffect rather than rewritten - an accepted
    // interop boundary, not new RxJava usage.
    DisposableEffect(viewModel) {
        val disposable = viewModel.registerPlayerFormErrorResult().subscribe({ error ->
            nameError = null
            emailError = null
            phoneError = null
            when (error) {
                DomainErrors.Players.INVALID_PLAYER_NAME -> {
                    nameError = context.getString(R.string.player_creation_error_name_empty)
                    FirebaseAnalyticsUtils.emptyPlayerName(context)
                }
                DomainErrors.Players.INVALID_PLAYER_ID -> {
                    // case of a player creation
                    FirebaseAnalyticsUtils.emptyPlayerId(context)
                }
                DomainErrors.Players.INVALID_EMAIL_FORMAT -> {
                    emailError =
                        context.getString(R.string.player_creation_error_invalid_email_format)
                    FirebaseAnalyticsUtils.invalidPlayerEmail(context)
                }
                DomainErrors.Players.INVALID_PHONE_NUMBER_FORMAT -> {
                    phoneError =
                        context.getString(R.string.player_creation_error_invalid_phone_format)
                    FirebaseAnalyticsUtils.invalidPlayerPhoneNumber(context)
                }
                else -> Timber.e("Unknown error: $error")
            }
        }, { Timber.e(it) })
        onDispose { disposable.dispose() }
    }

    LaunchedEffect(viewModel) {
        viewModel.observePlayerName()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { name = it }
            .launchIn(this)

        viewModel.observePlayerLicenseNumber()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { licenseNumberText = if (it > 0) it.toString() else "" }
            .launchIn(this)

        viewModel.observePlayerShirtNumber()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { shirtNumberText = if (it > 0) it.toString() else "" }
            .launchIn(this)

        viewModel.observePlayerPosition()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { bitmask ->
                selectedPositions = availablePositions.filter { (bitmask and it.mask) != 0 }.toSet()
            }
            .launchIn(this)

        viewModel.observePlayerImage()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { imageUri = it?.let(Uri::parse) }
            .launchIn(this)

        viewModel.observePlayerPitchingSide()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                pitchingLeft = (it and PlayerSide.LEFT.flag) != 0
                pitchingRight = (it and PlayerSide.RIGHT.flag) != 0
            }
            .launchIn(this)

        viewModel.observePlayerBattingSide()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                battingLeft = (it and PlayerSide.LEFT.flag) != 0
                battingRight = (it and PlayerSide.RIGHT.flag) != 0
            }
            .launchIn(this)

        viewModel.observePlayerEmail()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { email = it }
            .launchIn(this)

        viewModel.observePlayerPhoneNumber()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { phone = it }
            .launchIn(this)

        viewModel.observePlayerSex()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { sex = Sex.getById(it) }
            .launchIn(this)

        viewModel.observeTeamType()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamTypeId = it }
            .launchIn(this)
    }

    // Mirrors the old onSaveInstanceState timing: snapshot the in-progress form into the
    // ViewModel (which survives rotation via the back stack entry's ViewModelStore) right
    // before this composable leaves composition, so it can be restored on recreation.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.run {
                savedName = name
                savedImage = imageUri?.toString()
                savedLicenseNumber = licenseNumberText.toLongOrNull()
                savedShirtNumber = shirtNumberText.toIntOrNull()
                savedPositions = selectedPositions.fold(0) { acc, position -> acc or position.mask }
                savedPitching = sideFlag(pitchingLeft, pitchingRight)
                savedBatting = sideFlag(battingLeft, battingRight)
                savedEmail = email
                savedPhoneNumber = phone
                savedSex = sex.id
            }
        }
    }

    fun showDiscardDialog(trigger: String) {
        FirebaseAnalyticsUtils.onClick(context, "click_player_edit_cancel_$trigger")
        showDiscardSheet = true
    }

    BackHandler { showDiscardDialog("back_pressed") }

    PlayerFormScreen(
        state = PlayerFormState(
            isEditing = viewModel.playerId > 0,
            name = name,
            nameError = nameError,
            shirtNumberText = shirtNumberText,
            licenseNumberText = licenseNumberText,
            imageUri = imageUri,
            email = email,
            emailError = emailError,
            phone = phone,
            phoneError = phoneError,
            sex = sex,
            battingLeft = battingLeft,
            battingRight = battingRight,
            pitchingLeft = pitchingLeft,
            pitchingRight = pitchingRight,
            selectedPositions = selectedPositions,
            availablePositions = availablePositions,
            teamType = TeamType.getTypeById(teamTypeId)
        ),
        onNameChanged = { name = it },
        onShirtNumberChanged = { shirtNumberText = it.filter(Char::isDigit) },
        onLicenseNumberChanged = { licenseNumberText = it.filter(Char::isDigit) },
        onAvatarClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_player_edit_image_pick")
            ImagePickerUtils.launchPicker(pickImage)
        },
        onAvatarRemoveClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_player_edit_image_remove")
            imageUri = null
        },
        onSexSelected = { sex = it },
        onBattingLeftToggled = { battingLeft = !battingLeft },
        onBattingRightToggled = { battingRight = !battingRight },
        onPitchingLeftToggled = { pitchingLeft = !pitchingLeft },
        onPitchingRightToggled = { pitchingRight = !pitchingRight },
        onPositionToggled = { position ->
            val newSelection = selectedPositions.toMutableSet()
            if (position in newSelection) {
                newSelection.remove(position)
            } else if (newSelection.size < 3) {
                newSelection.add(position)
            }
            selectedPositions = newSelection
        },
        onEmailChanged = { email = it },
        onPhoneChanged = { phone = it },
        onCloseClick = { showDiscardDialog("close_icon") },
        onSaveClick = {
            coroutineScope.launch {
                viewModel.savePlayer(
                    name = name,
                    shirtNumber = shirtNumberText.toIntOrNull(),
                    licenseNumber = licenseNumberText.toLongOrNull(),
                    image = imageUri?.toString(),
                    positions = selectedPositions.fold(0) { acc, position -> acc or position.mask },
                    pitching = sideFlag(pitchingLeft, pitchingRight),
                    batting = sideFlag(battingLeft, battingRight),
                    email = email,
                    phone = phone,
                    sex = sex.id
                )
                    .onSuccess { navController.navigateUp() }
                    .onFailure {
                        when (it) {
                            is NameEmptyException,
                            is InvalidEmailException,
                            is InvalidPhoneException -> Timber.w(it.message)
                            else -> Timber.e(it)
                        }
                    }
            }
        }
    )

    if (showDiscardSheet) {
        ConfirmSheet(
            title = stringResource(R.string.discard_change_title),
            message = stringResource(R.string.discard_change_message),
            confirmLabel = stringResource(R.string.generic_discard),
            cancelLabel = stringResource(R.string.discard_keep_editing),
            onDismiss = { showDiscardSheet = false },
            onConfirm = { navController.navigateUp() }
        )
    }
}

private fun sideFlag(left: Boolean, right: Boolean): Int {
    var flag = 0
    if (left) flag = flag or PlayerSide.LEFT.flag
    if (right) flag = flag or PlayerSide.RIGHT.flag
    return flag
}
