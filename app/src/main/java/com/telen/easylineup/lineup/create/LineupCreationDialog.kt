package com.telen.easylineup.lineup.create

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.R
import com.telen.easylineup.data.Tournament
import com.telen.easylineup.lineup.list.LineupViewModel
import com.telen.easylineup.lineup.list.CategorizedLineupsViewModel
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.LineupCreationDialogView
import com.telen.easylineup.views.OnFormReadyListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class LineupCreationDialog: DialogFragment() {

    companion object {
        const val REQUEST_CODE_NEW_LINEUP = 0
    }

    private var saveDisposable: Disposable? = null
    private lateinit var dialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val tournamentViewModel =  ViewModelProviders.of(this).get(CategorizedLineupsViewModel::class.java)
            val view = LineupCreationDialogView(activity as AppCompatActivity)
            tournamentViewModel.getTournaments().observe(this, Observer { tournaments ->
                view.setList(tournaments)
            })

            builder.setTitle(R.string.dialog_create_lineup_title)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setOnDismissListener {
                        saveDisposable?.dispose()
                    }
            this.dialog = builder.create()
            dialog.setOnShowListener { dialogInterface ->
                val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positive.isEnabled = false
                positive.setOnClickListener {
                    val tournament = view.getSelectedTournament()
                    saveLineup(tournament, view.getLineupTitle())
                }

                view.setOnReadyStateListener(object: OnFormReadyListener {
                    override fun onFormStateChanged(isReady: Boolean) {
                        positive.isEnabled = isReady
                    }
                })
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun saveLineup(tournament: Tournament, lineupTitle: String) {
        Timber.d("chosen tournament = $tournament")
        Timber.d("chosen title = $lineupTitle")

        val lineupViewModel = ViewModelProviders.of(this).get(LineupViewModel::class.java)

        saveDisposable?.takeIf { !it.isDisposed }?.let { it.dispose() }
        saveDisposable = lineupViewModel.createNewLineup(tournament, lineupTitle)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ lineupID ->
                    dialog.dismiss()
                    Timber.d("successfully inserted new lineup, new id: $lineupID")
//                    activity?.startActivityForResult(intent, REQUEST_CODE_NEW_LINEUP)
                    val extras = Bundle()
                    extras.putLong(Constants.LINEUP_ID, lineupID)
                    extras.putString(Constants.LINEUP_TITLE, lineupTitle)
                    extras.putBoolean(Constants.EXTRA_EDITABLE, true)
                    findNavController().navigate(R.id.lineupFragment, extras, NavigationUtils().getOptions())
                }, { throwable ->
                    Timber.e(throwable)
                })
    }
}