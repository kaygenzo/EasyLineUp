package com.telen.easylineup.tournaments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telen.easylineup.R
import com.telen.easylineup.databinding.DialogCreateTournamentBinding
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.views.TournamentFormView

@FunctionalInterface
fun interface OnTournamentCreated {
    fun onNewTournament(tournament: Tournament)
}

class CreateTournamentDialog(private val callback: OnTournamentCreated) : DialogFragment(),
    TournamentFormView.TournamentFormCallback {

    private val tournament =
        Tournament(name = "", createdAt = System.currentTimeMillis(), startTime = 0L, endTime = 0L)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val binding = DialogCreateTournamentBinding.inflate(layoutInflater, null, false)

            binding.form.apply {
                fragmentManager = this@CreateTournamentDialog.childFragmentManager
                callback = this@CreateTournamentDialog
            }

            return MaterialAlertDialogBuilder(it)
                .setCancelable(false)
                .setView(binding.root)
                .setTitle(R.string.form_tournament_creation_title)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    callback.onNewTournament(tournament)
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStartTimeChanged(timeInMillis: Long) {
        tournament.startTime = timeInMillis
    }

    override fun onEndTimeChanged(timeInMillis: Long) {
        tournament.endTime = timeInMillis
    }

    override fun onNameChanged(name: String) {
        tournament.name = name
    }

    override fun onAddressChanged(address: String) {
        tournament.name = address
    }

}