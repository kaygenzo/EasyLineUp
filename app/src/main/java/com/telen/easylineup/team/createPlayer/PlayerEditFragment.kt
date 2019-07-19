package com.telen.easylineup.team.createPlayer

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
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.views.PlayerFormListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_player_edit.view.*
import kotlinx.android.synthetic.main.view_create_player.*

class PlayerEditFragment: Fragment(), PlayerFormListener {

    private lateinit var viewModel: PlayerViewModel
    private var saveDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_edit, container, false)
        viewModel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)
        viewModel.playerID = arguments?.getLong(Constants.PLAYER_ID)
        viewModel.teamID = arguments?.getLong(Constants.TEAM_ID)
        viewModel.playerID?.let {
            view.editPlayerForm.disableSaveButton()

            val savedName = savedInstanceState?.getString(Constants.PLAYER_NAME)
            val savedShirtNumber = savedInstanceState?.getInt(Constants.PLAYER_SHIRT)
            val savedLicenseNumber = savedInstanceState?.getLong(Constants.PLAYER_LICENSE)
            val savedImage = savedInstanceState?.getString(Constants.PLAYER_IMAGE)

            if(it > 0) {
                viewModel.getPlayer(it).observe(this, Observer { player ->
                    player?.let {
                        view.editPlayerForm.enableSaveButton()
                        view.editPlayerForm.setName(savedName ?: player.name)
                        view.editPlayerForm.setShirtNumber(savedShirtNumber ?: player.shirtNumber)
                        view.editPlayerForm.setLicenseNumber(savedLicenseNumber ?: player.licenseNumber)

                        val imagePath = savedImage ?: player.image
                        imagePath?.let { imageUriString ->
                            view.editPlayerForm.setImage(Uri.parse(imageUriString))
                        }

                        view.editPlayerForm.setListener(this)
                    }
                })
            }
            else {
                view.editPlayerForm.enableSaveButton()
                view.editPlayerForm.setListener(this)
            }
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val name = view?.editPlayerForm?.getName()
        val image = view?.editPlayerForm?.getImageUri()
        val license = view?.editPlayerForm?.getLicenseNumber()
        val shirt = view?.editPlayerForm?.getShirtNumber()

        if(!TextUtils.isEmpty(name))
            outState.putString(Constants.PLAYER_NAME, name)

        license?.let {
            outState.putLong(Constants.PLAYER_LICENSE, it)
        }

        shirt?.let {
            outState.putInt(Constants.PLAYER_SHIRT, it)
        }

        image?.let {
            outState.putString(Constants.PLAYER_IMAGE, it.toString())
        }
    }

    override fun onSaveClicked(name: String, shirtNumber: Int, licenseNumber: Long, imageUri: Uri?) {
        val playerID: Long = viewModel.playerID ?: 0
        val teamID: Long = viewModel.teamID ?: 0
        val player = Player(id = playerID, teamId = teamID, name = name, shirtNumber = shirtNumber, licenseNumber = licenseNumber, image = imageUri?.toString())
        dispose(saveDisposable)
        saveDisposable = viewModel.savePlayer(player)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    findNavController().popBackStack(R.id.navigation_team, false)
                }
    }

    private fun dispose(disposable: Disposable?) {
        disposable?.let {
            if(!it.isDisposed)
                it.dispose()
        }
    }
}