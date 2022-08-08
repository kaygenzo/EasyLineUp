package com.telen.easylineup.player

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentPlayerEditBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import com.telen.easylineup.views.PlayerFormListener
import timber.log.Timber

class PlayerEditFragment : BaseFragment("PlayerEditFragment"), PlayerFormListener {

    private val viewModel by viewModels<PlayerViewModel>()
    private var binding: FragmentPlayerEditBinding? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { imageUri ->
                    binding?.editPlayerForm?.onImageUriReceived(imageUri)
                }
            }
        }

    override fun onCancel() {
        onCancelForm()
    }

    override fun onImagePickerRequested() {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_edit_image_pick")
        activity?.let { ImagePickerUtils.launchPicker(it, view, pickImage) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCancelForm()
            }
        })
        viewModel.playerID = arguments?.getLong(Constants.PLAYER_ID) ?: 0L

        val errorsDisposable = viewModel.registerPlayerFormErrorResult().subscribe({ error ->
            when (error) {
                DomainErrors.Players.INVALID_PLAYER_NAME -> {
                    binding?.editPlayerForm?.displayInvalidName()
                    FirebaseAnalyticsUtils.emptyPlayerName(activity)
                }
                DomainErrors.Players.INVALID_PLAYER_ID -> {
                    // case of a player creation
                    FirebaseAnalyticsUtils.emptyPlayerID(activity)
                }
                DomainErrors.Players.INVALID_EMAIL_FORMAT -> {
                    binding?.editPlayerForm?.displayInvalidEmail()
                    FirebaseAnalyticsUtils.invalidPlayerEmail(activity)
                }
                DomainErrors.Players.INVALID_PHONE_NUMBER_FORMAT -> {
                    binding?.editPlayerForm?.displayInvalidPhoneNumber()
                    FirebaseAnalyticsUtils.invalidPlayerPhoneNumber(activity)
                }
                else -> {
                    Timber.e("Unknown error: $error")
                }
            }
        }, {
            Timber.e(it)
        })
        disposables.add(errorsDisposable)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPlayerEditBinding.inflate(inflater, container, false)
        this.binding = binding

        binding.editPlayerForm.setListener(this)

        viewModel.observePlayerName().observe(viewLifecycleOwner) {
            binding.editPlayerForm.setName(it)
        }

        viewModel.observePlayerLicenseNumber().observe(viewLifecycleOwner) {
            binding.editPlayerForm.setLicenseNumber(it)
        }

        viewModel.observePlayerShirtNumber().observe(viewLifecycleOwner) {
            binding.editPlayerForm.setShirtNumber(it)
        }

        viewModel.observePlayerPosition().observe(viewLifecycleOwner) {
            binding.editPlayerForm.setPositionsFilter(it)
        }

        viewModel.observePlayerImage().observe(viewLifecycleOwner) {
            it?.let { imageUriString -> binding.editPlayerForm.setImage(Uri.parse(imageUriString)) }
        }

        viewModel.observePlayerPitchingSide().observe(viewLifecycleOwner) {
            binding.editPlayerForm.setPitchingSide(PlayerSide.getSideByValue(it))
        }

        viewModel.observePlayerBattingSide().observe(viewLifecycleOwner) {
            binding.editPlayerForm.setBattingSide(PlayerSide.getSideByValue(it))
        }

        viewModel.observePlayerEmail().observe(viewLifecycleOwner) {
            binding.editPlayerForm.setEmail(it)
        }

        viewModel.observePlayerPhoneNumber().observe(viewLifecycleOwner) {
            binding.editPlayerForm.setPhone(it)
        }

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
        phone: String?
    ) {
        val saveDisposable = viewModel.savePlayer(
            name,
            shirtNumber,
            licenseNumber,
            imageUri,
            positions,
            pitching,
            batting,
            email,
            phone
        ).subscribe({
            findNavController().navigateUp()
        }, {
            Timber.e(it)
        })
        disposables.add(saveDisposable)
    }

    fun onCancelForm() {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_edit_cancel")
        activity?.run {
            DialogFactory.getWarningDialog(
                context = this,
                title = R.string.discard_title,
                message = R.string.discard_message,
                confirmText = R.string.generic_discard,
                confirmClick = { dialog, _ ->
                    findNavController().navigateUp()
                    dialog.dismiss()
                }
            ).show()
        }
    }
}