package com.telen.easylineup.newLineup

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.data.Lineup
import com.telen.easylineup.data.Tournament
import com.telen.easylineup.listLineup.LineupViewModel
import com.telen.easylineup.listLineup.TournamentViewModel
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.views.LineupCreationDialogView
import com.telen.easylineup.views.OnFormReadyListener
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
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

            val tournamentViewModel =  ViewModelProviders.of(activity as AppCompatActivity).get(TournamentViewModel::class.java)
            val view = LineupCreationDialogView(activity as AppCompatActivity)
            tournamentViewModel.tournaments.observe(this, Observer {
                view.setList(it)
            })

            builder.setTitle(R.string.new_lineup_dialog_title)
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

        val tournamentViewModel = ViewModelProviders.of(this).get(TournamentViewModel::class.java)
        val teamViewModel = ViewModelProviders.of(this).get(TeamViewModel::class.java)
        val lineupViewModel = ViewModelProviders.of(this).get(LineupViewModel::class.java)

        saveDisposable = tournamentViewModel.insertTournamentIfNotExists(tournament)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { tournamentID ->
                    Timber.d("successfully inserted tournament, new id: $tournamentID")
                    teamViewModel.getTeam().observe(activity as AppCompatActivity, Observer { team ->
                        val newLineup = Lineup(name = lineupTitle, teamId = team.id, tournamentId = tournamentID)
                        lineupViewModel.createNewLineup(newLineup)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ lineupID ->
                                    dialog.dismiss()
                                    Timber.d("successfully inserted new lineup, new id: $lineupID")
                                    val intent = Intent(activity, NewLineUpActivity::class.java)
                                    intent.putExtra(Constants.LINEUP_ID, lineupID)
                                    intent.putExtra(Constants.LINEUP_TITLE, lineupTitle)
                                    intent.putExtra(Constants.TEAM_ID, team.id)
                                    activity?.startActivityForResult(intent, REQUEST_CODE_NEW_LINEUP)
                                }, { throwable ->
                                    Timber.e(throwable)
                                })
                    })
                }
    }
}