package com.telen.easylineup.team.createPlayer

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.views.PlayerFormListener
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.player_edit_activity.*

class PlayerEditActivity: AppCompatActivity(), PlayerFormListener {

    private lateinit var viewModel: PlayerViewModel
    private var saveDisposable: Disposable? = null

    override fun onSaveClicked(name: String, shirtNumber: Int, licenseNumber: Long, imageUri: Uri?) {
        val playerID: Long = viewModel.playerID ?: 0
        val teamID: Long = viewModel.teamID ?: 0
        val player = Player(id = playerID, teamId = teamID, name = name, shirtNumber = shirtNumber, licenseNumber = licenseNumber, image = imageUri?.toString())
        dispose(saveDisposable)
        saveDisposable = viewModel.savePlayer(player)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    finish()
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_edit_activity)
        viewModel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)
        viewModel.playerID = intent?.extras?.getLong(Constants.PLAYER_ID)
        viewModel.teamID = intent?.extras?.getLong(Constants.TEAM_ID)
        viewModel.playerID?.let {
            viewModel.getPlayer(it).observe(this, Observer { player ->
                editPlayerForm.setName(player.name)
                editPlayerForm.setShirtNumber(player.shirtNumber)
                editPlayerForm.setLicenseNumber(player.licenseNumber)
                player.image?.let { imageUriString ->
                    editPlayerForm.setImage(Uri.parse(imageUriString))
                }
                editPlayerForm.setListener(this)
            })
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun dispose(disposable: Disposable?) {
        disposable?.let {
            if(!disposable.isDisposed)
                disposable.dispose()
        }
    }
}