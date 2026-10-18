/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.player

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.errors.DomainErrors
import com.telen.easylineup.launch
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

private val availablePositions: List<FieldPosition> = FieldPosition.values()
    .filter { it.isDefensePlayer() }
    .groupBy { it.mask }
    .map { it.value.first() }

class PlayerEditFragment : BaseFragment("PlayerEditFragment"), MenuProvider {
    private val viewModel by viewModel<PlayerViewModel>()

    private var name by mutableStateOf("")
    private var nameError by mutableStateOf<String?>(null)
    private var shirtNumberText by mutableStateOf("")
    private var licenseNumberText by mutableStateOf("")
    private var imageUri by mutableStateOf<Uri?>(null)
    private var email by mutableStateOf("")
    private var emailError by mutableStateOf<String?>(null)
    private var phone by mutableStateOf("")
    private var phoneError by mutableStateOf<String?>(null)
    private var sex by mutableStateOf(Sex.UNKNOWN)
    private var battingLeft by mutableStateOf(false)
    private var battingRight by mutableStateOf(false)
    private var pitchingLeft by mutableStateOf(false)
    private var pitchingRight by mutableStateOf(false)
    private var selectedPositions by mutableStateOf<Set<FieldPosition>>(emptySet())
    private var teamTypeId by mutableStateOf(0)

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let { uri ->
            context?.contentResolver?.let { resolver ->
                ImagePickerUtils.persistImage(resolver, uri)
            }
            imageUri = uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.playerId = arguments?.getLong(Constants.PLAYER_ID) ?: 0L

        launch(viewModel.registerPlayerFormErrorResult(), { error ->
            nameError = null
            emailError = null
            phoneError = null
            when (error) {
                DomainErrors.Players.INVALID_PLAYER_NAME -> {
                    nameError = getString(R.string.player_creation_error_name_empty)
                    FirebaseAnalyticsUtils.emptyPlayerName(activity)
                }
                DomainErrors.Players.INVALID_PLAYER_ID -> {
                    // case of a player creation
                    FirebaseAnalyticsUtils.emptyPlayerId(activity)
                }
                DomainErrors.Players.INVALID_EMAIL_FORMAT -> {
                    emailError = getString(R.string.player_creation_error_invalid_email_format)
                    FirebaseAnalyticsUtils.invalidPlayerEmail(activity)
                }
                DomainErrors.Players.INVALID_PHONE_NUMBER_FORMAT -> {
                    phoneError = getString(R.string.player_creation_error_invalid_phone_format)
                    FirebaseAnalyticsUtils.invalidPlayerPhoneNumber(activity)
                }
                else -> Timber.e("Unknown error: $error")
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showDiscardDialog("back_pressed")
                }
            })

        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
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
                        onAvatarClick = ::onImagePickerRequested,
                        onAvatarRemoveClick = {
                            FirebaseAnalyticsUtils.onClick(
                                activity,
                                "click_player_edit_image_remove"
                            )
                            imageUri = null
                        },
                        onSexSelected = { sex = it },
                        onBattingLeftToggled = { battingLeft = !battingLeft },
                        onBattingRightToggled = { battingRight = !battingRight },
                        onPitchingLeftToggled = { pitchingLeft = !pitchingLeft },
                        onPitchingRightToggled = { pitchingRight = !pitchingRight },
                        onPositionToggled = ::onPositionToggled,
                        onEmailChanged = { email = it },
                        onPhoneChanged = { phone = it },
                        onCloseClick = { showDiscardDialog("close_icon") },
                        onSaveClick = ::onSaveClicked
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observePlayerName()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { name = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerLicenseNumber()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { licenseNumberText = if (it > 0) it.toString() else "" }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerShirtNumber()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { shirtNumberText = if (it > 0) it.toString() else "" }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerPosition()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { bitmask ->
                selectedPositions = availablePositions.filter { (bitmask and it.mask) != 0 }.toSet()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerImage()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { imageUri = it?.let(Uri::parse) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerPitchingSide()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                pitchingLeft = (it and PlayerSide.LEFT.flag) != 0
                pitchingRight = (it and PlayerSide.RIGHT.flag) != 0
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerBattingSide()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                battingLeft = (it and PlayerSide.LEFT.flag) != 0
                battingRight = (it and PlayerSide.RIGHT.flag) != 0
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerEmail()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { email = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerPhoneNumber()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { phone = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerSex()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { sex = Sex.getById(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeTeamType()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamTypeId = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
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

    private fun onImagePickerRequested() {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_edit_image_pick")
        activity?.let { ImagePickerUtils.launchPicker(pickImage) }
    }

    private fun onPositionToggled(position: FieldPosition) {
        val newSelection = selectedPositions.toMutableSet()
        if (position in newSelection) {
            newSelection.remove(position)
        } else if (newSelection.size < 3) {
            newSelection.add(position)
        }
        selectedPositions = newSelection
    }

    private fun sideFlag(left: Boolean, right: Boolean): Int {
        var flag = 0
        if (left) flag = flag or PlayerSide.LEFT.flag
        if (right) flag = flag or PlayerSide.RIGHT.flag
        return flag
    }

    private fun onSaveClicked() {
        viewLifecycleOwner.lifecycleScope.launch {
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
                .onSuccess {
                    findNavController().navigateUp()
                }
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

    private fun showDiscardDialog(trigger: String) {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_edit_cancel_$trigger")
        DialogFactory.getDiscardDialog(requireContext()) { _, _ ->
            findNavController().navigateUp()
        }.show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        showDiscardDialog("navigation_up")
        return true
    }
}
