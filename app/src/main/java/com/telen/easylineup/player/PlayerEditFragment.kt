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
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.databinding.FragmentPlayerEditBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.usecases.exceptions.InvalidEmailException
import com.telen.easylineup.domain.usecases.exceptions.InvalidPhoneException
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.launch
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import com.telen.easylineup.views.PlayerFormListener
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class PlayerEditFragment : BaseFragment("PlayerEditFragment"), PlayerFormListener, MenuProvider {
    private val viewModel by viewModels<PlayerViewModel>()
    private var binding: FragmentPlayerEditBinding? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let { uri ->
            context?.contentResolver?.let { ImagePickerUtils.persistImage(it, uri) }
            binding?.editPlayerForm?.onImageUriReceived(uri)
        }
    }

    override fun onCancel() {
        showDiscardDialog("cancel")
    }

    override fun onImagePickerRequested() {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_edit_image_pick")
        activity?.let { ImagePickerUtils.launchPicker(pickImage) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.playerId = arguments?.getLong(Constants.PLAYER_ID) ?: 0L

        launch(viewModel.registerPlayerFormErrorResult(), { error ->
            when (error) {
                DomainErrors.Players.INVALID_PLAYER_NAME -> {
                    binding?.editPlayerForm?.displayInvalidName()
                    FirebaseAnalyticsUtils.emptyPlayerName(activity)
                }
                DomainErrors.Players.INVALID_PLAYER_ID -> {
                    // case of a player creation
                    FirebaseAnalyticsUtils.emptyPlayerId(activity)
                }
                DomainErrors.Players.INVALID_EMAIL_FORMAT -> {
                    binding?.editPlayerForm?.displayInvalidEmail()
                    FirebaseAnalyticsUtils.invalidPlayerEmail(activity)
                }
                DomainErrors.Players.INVALID_PHONE_NUMBER_FORMAT -> {
                    binding?.editPlayerForm?.displayInvalidPhoneNumber()
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
        val binding = FragmentPlayerEditBinding.inflate(inflater, container, false)
        this.binding = binding

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showDiscardDialog("back_pressed")
                }
            })

        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.editPlayerForm.setListener(this)

        viewModel.observePlayerName()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { binding.editPlayerForm.setName(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerLicenseNumber()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { binding.editPlayerForm.setLicenseNumber(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerShirtNumber()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { binding.editPlayerForm.setShirtNumber(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerPosition()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { binding.editPlayerForm.setPositionsFilter(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerImage()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                it?.let { imageUriString -> binding.editPlayerForm.setImage(Uri.parse(imageUriString)) }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerPitchingSide()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { binding.editPlayerForm.setPitchingSide(PlayerSide.getSideByValue(it)) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerBattingSide()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { binding.editPlayerForm.setBattingSide(PlayerSide.getSideByValue(it)) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerEmail()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { binding.editPlayerForm.setEmail(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerPhoneNumber()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { binding.editPlayerForm.setPhone(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observePlayerSex()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { binding.editPlayerForm.setSex(Sex.getById(it)) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding?.editPlayerForm?.run {
            viewModel.run {
                savedName = getName()
                savedImage = getImageUri()?.toString()
                savedLicenseNumber = getLicenseNumber()
                savedShirtNumber = getShirtNumber()
                savedPositions = getPlayerPositions()
                savedPitching = getPitchingSide()?.flag
                savedBatting = getBattingSide()?.flag
                savedEmail = getEmail()
                savedPhoneNumber = getPhone()
                savedSex = getSex()?.id
            }
        }
    }

    override fun onSaveClicked(
        name: String?,
        shirtNumber: Int?,
        licenseNumber: Long?,
        imageUri: Uri?,
        positions: Int,
        pitching: Int,
        batting: Int,
        email: String?,
        phone: String?,
        sex: Int
    ) {
        launch(viewModel.savePlayer(
            name,
            shirtNumber,
            licenseNumber,
            imageUri,
            positions,
            pitching,
            batting,
            email,
            phone,
            sex
        ), {
            findNavController().navigateUp()
        }, {
            when (it) {
                is NameEmptyException,
                is InvalidEmailException,
                is InvalidPhoneException -> Timber.w(it.message)
                else -> Timber.e(it)
            }
        })
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
