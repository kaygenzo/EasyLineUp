package com.telen.easylineup.team.createPlayer

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.data.Player
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.views.PlayerFormDialogView
import com.telen.easylineup.views.PlayerFormListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CreationPlayerDialog:  DialogFragment(), PlayerFormListener {

    override fun onSaveClicked(name: String, shirtNumber: Int, licenseNumber: Long, imageUri: Uri?) {
        val viewModel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)
        arguments?.getLong(Constants.TEAM_ID)?.let {
            val player = Player(teamId = it, name = name, shirtNumber = shirtNumber, licenseNumber = licenseNumber, image = imageUri?.toString())
            viewModel.savePlayer(player)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        dismiss()
                    }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = PlayerFormDialogView(it)
            view.setListener(this)
            val builder = AlertDialog.Builder(it).setView(view)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}