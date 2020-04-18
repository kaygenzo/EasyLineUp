package com.telen.easylineup.player

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import com.telen.easylineup.views.PlayerFormListener
import com.telen.easylineup.views.PlayerFormView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_player_edit.view.*
import timber.log.Timber

class PlayerEditFragment: Fragment(), PlayerFormListener {

    private lateinit var viewModel: PlayerViewModel
    private var saveDisposable: Disposable? = null
    private var playerForm: PlayerFormView? = null

    override fun onCancel() {
        findNavController().popBackStack(R.id.navigation_team, false)
    }

    override fun onImagePickerRequested() {
        ImagePickerUtils.launchPicker(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_edit, container, false)
        viewModel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)
        viewModel.playerID = arguments?.getLong(Constants.PLAYER_ID)
        viewModel.playerID?.let { id ->

            playerForm = view.editPlayerForm

            view.editPlayerForm.disableSaveButton()

            val savedName = savedInstanceState?.getString(Constants.NAME)
            val savedShirtNumber = savedInstanceState?.getInt(Constants.PLAYER_SHIRT)
            val savedLicenseNumber = savedInstanceState?.getLong(Constants.PLAYER_LICENSE)
            val savedImage = savedInstanceState?.getString(Constants.IMAGE)
            val savedPositions = savedInstanceState?.getInt(Constants.PLAYER_POSITIONS)

            //if it's a player edition
            if(id > 0) {
                viewModel.getPlayer().subscribe({ player ->
                    view.editPlayerForm.enableSaveButton()
                    view.editPlayerForm.setName(savedName ?: player.name)
                    view.editPlayerForm.setShirtNumber(savedShirtNumber ?: player.shirtNumber)
                    view.editPlayerForm.setLicenseNumber(savedLicenseNumber ?: player.licenseNumber)
                    view.editPlayerForm.setPositionsFilter(savedPositions ?: player.positions)
                    val imagePath = savedImage ?: player.image
                    imagePath?.let { imageUriString ->
                        view.editPlayerForm.setImage(imageUriString)
                    }

                    view.editPlayerForm.setListener(this)
                }, {
                    Timber.e(it)
                })
            }
            //case of a player creation
            else {
                view.editPlayerForm.enableSaveButton()
                view.editPlayerForm.setListener(this)
            }
        }

        viewModel.registerFormErrorResult().observe(viewLifecycleOwner, Observer { error ->
            when(error) {
                FormErrorResult.INVALID_NAME -> {
                    view.editPlayerForm.displayInvalidName()
                    FirebaseAnalyticsUtils.emptyPlayerName(activity)
                }
                FormErrorResult.INVALID_LICENSE -> {
                    view.editPlayerForm.displayInvalidLicense()
                    FirebaseAnalyticsUtils.emptyPlayerLicense(activity)
                }
                FormErrorResult.INVALID_NUMBER -> {
                    view.editPlayerForm.displayInvalidNumber()
                    FirebaseAnalyticsUtils.emptyPlayerNumber(activity)
                }
            }
        })

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val name = view?.editPlayerForm?.getName()
        val image = view?.editPlayerForm?.getImageUri()
        val license = view?.editPlayerForm?.getLicenseNumber()
        val shirt = view?.editPlayerForm?.getShirtNumber()
        val positions = view?.editPlayerForm?.getPlayerPositions()

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
    }

    override fun onSaveClicked(name: String?, shirtNumber: Int?, licenseNumber: Long?, imageUri: Uri?, positions: Int) {
        dispose(saveDisposable)
        saveDisposable = viewModel.savePlayer(name, shirtNumber, licenseNumber, imageUri, positions)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    findNavController().navigateUp()
                }, {
                    Timber.e(it)
                })
    }

    private fun dispose(disposable: Disposable?) {
        disposable?.let {
            if(!it.isDisposed)
                it.dispose()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == Activity.RESULT_OK) {
            data?.let {
                val pickedImages: ArrayList<Image> = it.getParcelableArrayListExtra(Config.EXTRA_IMAGES)
                pickedImages.firstOrNull()?.let {image ->
                    playerForm?.onImageUriReceived(image)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}