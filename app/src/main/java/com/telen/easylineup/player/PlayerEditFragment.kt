package com.telen.easylineup.player

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import com.telen.easylineup.views.PlayerFormListener
import kotlinx.android.synthetic.main.fragment_player_edit.view.*
import timber.log.Timber

class PlayerEditFragment: BaseFragment("PlayerEditFragment"), PlayerFormListener {

    private lateinit var viewModel: PlayerViewModel

    override fun onCancel() {
        cancel()
    }

    override fun onImagePickerRequested() {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_edit_image_pick")
        ImagePickerUtils.launchPicker(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancel()
            }
        })

        viewModel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)
        viewModel.playerID = arguments?.getLong(Constants.PLAYER_ID)

        val errorsDisposable = viewModel.registerFormErrorResult().subscribe({ error ->
            when(error) {
                DomainErrors.INVALID_PLAYER_NAME -> {
                    view?.editPlayerForm?.displayInvalidName()
                    FirebaseAnalyticsUtils.emptyPlayerName(activity)
                }
                DomainErrors.INVALID_PLAYER_ID -> {
                    //case of a player creation
                    FirebaseAnalyticsUtils.emptyPlayerID(activity)
                }
                DomainErrors.INVALID_EMAIL_FORMAT -> {
                    view?.editPlayerForm?.displayInvalidEmail()
                    FirebaseAnalyticsUtils.invalidPlayerEmail(activity)
                }
                DomainErrors.INVALID_PHONE_NUMBER_FORMAT -> {
                    view?.editPlayerForm?.displayInvalidPhoneNumber()
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

        val eventsDisposable = viewModel.registerEvent().subscribe({
            when(it) {
                SavePlayerSuccess -> {
                    findNavController().navigateUp()
                }
                else -> {

                }
            }
        }, {
            Timber.e(it)
        })
        disposables.add(eventsDisposable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_edit, container, false)

        view.editPlayerForm.apply {
            setListener(this@PlayerEditFragment)
        }

        viewModel.observePlayer().observe(viewLifecycleOwner, Observer { player ->
            //if it's a player edition
            val savedName = savedInstanceState?.getString(Constants.NAME)
            val savedShirtNumber = savedInstanceState?.getInt(Constants.PLAYER_SHIRT)
            val savedLicenseNumber = savedInstanceState?.getLong(Constants.PLAYER_LICENSE)
            val savedImage = savedInstanceState?.getString(Constants.IMAGE)
            val savedPositions = savedInstanceState?.getInt(Constants.PLAYER_POSITIONS)
            val savedPitching = savedInstanceState?.getInt(Constants.PLAYER_PITCHING_SIDE)
            val savedBatting = savedInstanceState?.getInt(Constants.PLAYER_BATTING_SIDE)
            val savedEmail = savedInstanceState?.getString(Constants.PLAYER_EMAIL)
            val savedPhoneNumber = savedInstanceState?.getString(Constants.PLAYER_PHONE_NUMBER)

            view.editPlayerForm.apply {
                setName(savedName ?: player.name)
                setShirtNumber(savedShirtNumber ?: player.shirtNumber)
                setLicenseNumber(savedLicenseNumber ?: player.licenseNumber)
                setPositionsFilter(savedPositions ?: player.positions)
                val imagePath = savedImage ?: player.image
                imagePath?.let { imageUriString ->
                    setImage(imageUriString)
                }
                setPitchingSide(PlayerSide.getSideByValue(savedPitching ?: player.pitching))
                setBattingSide(PlayerSide.getSideByValue(savedBatting ?: player.batting))
                setEmail(savedEmail ?: player.email)
                setPhone(savedPhoneNumber ?: player.phone)
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val name = view?.editPlayerForm?.getName()
        val image = view?.editPlayerForm?.getImageUri()
        val license = view?.editPlayerForm?.getLicenseNumber()
        val shirt = view?.editPlayerForm?.getShirtNumber()
        val positions = view?.editPlayerForm?.getPlayerPositions()
        val pitching = view?.editPlayerForm?.getPitchingSide()
        val batting = view?.editPlayerForm?.getBattingSide()
        val email = view?.editPlayerForm?.getEmail()
        val phoneNumber = view?.editPlayerForm?.getPhone()

        if(!TextUtils.isEmpty(name))
            outState.putString(Constants.NAME, name)

        license?.let {
            outState.putLong(Constants.PLAYER_LICENSE, it)
        }

        shirt?.let {
            outState.putInt(Constants.PLAYER_SHIRT, it)
        }

        positions?.let {
            outState.putInt(Constants.PLAYER_POSITIONS, it)
        }

        image?.let {
            outState.putString(Constants.IMAGE, it.toString())
        }

        pitching?.let {
            outState.putInt(Constants.PLAYER_PITCHING_SIDE, it.flag)
        }

        batting?.let {
            outState.putInt(Constants.PLAYER_BATTING_SIDE, it.flag)
        }

        email?.let {
            outState.putString(Constants.PLAYER_EMAIL, it)
        }

        phoneNumber?.let {
            outState.putString(Constants.PLAYER_PHONE_NUMBER, it)
        }
    }

    override fun onSaveClicked(name: String?, shirtNumber: Int?, licenseNumber: Long?,
                               imageUri: Uri?, positions: Int, pitching: Int, batting: Int, email: String?, phone: String?) {
        viewModel.savePlayer(name, shirtNumber, licenseNumber, imageUri, positions, pitching, batting, email, phone)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == Activity.RESULT_OK) {
            data?.let {
                val pickedImages: ArrayList<Image> = it.getParcelableArrayListExtra(Config.EXTRA_IMAGES)
                pickedImages.firstOrNull()?.let {image ->
                    view?.editPlayerForm?.onImageUriReceived(image)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun cancel() {
        FirebaseAnalyticsUtils.onClick(activity, "click_player_edit_cancel")
        activity?.run {
            DialogFactory.getWarningDialog(
                    context = this,
                    title = R.string.discard_title,
                    message = R.string.discard_message,
                    confirmText = R.string.generic_discard,
                    confirmClick = DialogInterface.OnClickListener { dialog, which ->
                        findNavController().navigateUp()
                        dialog.dismiss()
                    }
            ).show()
        }
    }
}