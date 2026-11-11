/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Creating or editing a player: [playerId] is `0` for a new one. [PlayerViewModel.uiState]
 * drives the whole form, so there is no local Compose state to lose on rotation.
 */
@Composable
fun PlayerEditRoute(navController: NavController, playerId: Long) {
    val viewModel = koinViewModel<PlayerViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    remember(playerId) { viewModel.playerId = playerId }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showDiscardSheet by rememberSaveable { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            ImagePickerUtils.persistImage(context.contentResolver, it)
            viewModel.onImageSelected(it)
        }
    }

    fun showDiscardDialog(trigger: String) {
        FirebaseAnalyticsUtils.onClick(context, "click_player_edit_cancel_$trigger")
        showDiscardSheet = true
    }

    BackHandler { showDiscardDialog("back_pressed") }

    LaunchedEffect(viewModel) {
        viewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    PlayerEvent.Saved -> navController.navigateUp()
                    PlayerEvent.SaveRejected -> {
                        val failed = viewModel.uiState.value
                        when {
                            failed.nameError -> FirebaseAnalyticsUtils.emptyPlayerName(context)
                            failed.emailError ->
                                FirebaseAnalyticsUtils.invalidPlayerEmail(context)
                            failed.phoneError ->
                                FirebaseAnalyticsUtils.invalidPlayerPhoneNumber(context)
                        }
                    }
                    PlayerEvent.SaveFailed -> Toast.makeText(
                        context,
                        R.string.error_player_save_failed,
                        Toast.LENGTH_LONG
                    ).show()
                    // Deleting happens from the profile screen, not this form.
                    PlayerEvent.Deleted, is PlayerEvent.DeleteFailed -> Unit
                }
            }
    }

    PlayerFormScreen(
        state = PlayerFormState(
            isEditing = playerId > 0,
            name = state.name,
            nameError = if (state.nameError) {
                stringResource(R.string.player_creation_error_name_empty)
            } else {
                null
            },
            shirtNumberText = state.shirtNumberText,
            licenseNumberText = state.licenseNumberText,
            imageUri = state.imageUri,
            email = state.email,
            emailError = if (state.emailError) {
                stringResource(R.string.player_creation_error_invalid_email_format)
            } else {
                null
            },
            phone = state.phone,
            phoneError = if (state.phoneError) {
                stringResource(R.string.player_creation_error_invalid_phone_format)
            } else {
                null
            },
            sex = state.sex,
            battingLeft = state.battingLeft,
            battingRight = state.battingRight,
            pitchingLeft = state.pitchingLeft,
            pitchingRight = state.pitchingRight,
            selectedPositions = state.selectedPositions,
            availablePositions = FieldPosition.availablePositions,
            teamType = state.teamType
        ),
        onNameChanged = viewModel::onNameChanged,
        onShirtNumberChanged = viewModel::onShirtNumberChanged,
        onLicenseNumberChanged = viewModel::onLicenseNumberChanged,
        onAvatarClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_player_edit_image_pick")
            ImagePickerUtils.launchPicker(pickImage)
        },
        onAvatarRemoveClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_player_edit_image_remove")
            viewModel.onImageSelected(null)
        },
        onSexSelected = viewModel::onSexSelected,
        onBattingLeftToggled = viewModel::onBattingLeftToggled,
        onBattingRightToggled = viewModel::onBattingRightToggled,
        onPitchingLeftToggled = viewModel::onPitchingLeftToggled,
        onPitchingRightToggled = viewModel::onPitchingRightToggled,
        onPositionToggled = viewModel::onPositionToggled,
        onEmailChanged = viewModel::onEmailChanged,
        onPhoneChanged = viewModel::onPhoneChanged,
        onCloseClick = { showDiscardDialog("close_icon") },
        onSaveClick = viewModel::onSaveClick
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
