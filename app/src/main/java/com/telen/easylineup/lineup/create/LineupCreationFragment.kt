package com.telen.easylineup.lineup.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.R
import com.telen.easylineup.domain.GetRoaster
import com.telen.easylineup.domain.STATUS_ALL
import com.telen.easylineup.lineup.list.LineupViewModel
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Tournament
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.OnActionButtonListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_create_lineup.view.*
import kotlinx.android.synthetic.main.fragment_lineup_creation.view.*
import timber.log.Timber

class LineupCreationFragment: Fragment() {

    private var saveDisposable: Disposable? = null
    private lateinit var lineupViewModel: LineupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lineupViewModel = ViewModelProviders.of(this).get(LineupViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_creation, container, false)

        lineupViewModel.getTournaments().subscribe({
            view.lineupCreationForm.setList(it)
        }, {
            Timber.e(it)
        })

        view.lineupCreationForm.setOnActionClickListener(object: OnActionButtonListener {
            override fun onRoasterChangeClicked() {
                val disposable = lineupViewModel.getRoaster().subscribe({ response ->
                    activity?.let { activity ->
                        val names = mutableListOf<CharSequence>()
//                        names.add("All")
                        names.addAll(response.players.map { it.player.name })

                        val checked = mutableListOf<Boolean>()
//                        when(response.status) {
//                            STATUS_ALL -> checked.add(true)
//                            STATUS_NONE -> checked.add(false)
//                        }
                        checked.addAll(response.players.map { it.status })

                        AlertDialog.Builder(activity)
                                .setMultiChoiceItems(names.toTypedArray(), checked.toBooleanArray()) { dialog, which, isChecked ->
                                    lineupViewModel.roasterPlayerStatusChanged(which, isChecked)
                                    updateRoasterSize(view.playerCount, response)
                                }
                                .setPositiveButton(android.R.string.ok, null)
                                .create()
                                .show()
                    }

                }, {
                    Timber.e(it)
                })
            }

            override fun onSaveClicked() {
                val tournament = view.lineupCreationForm.getSelectedTournament()
                saveLineup(tournament, view.lineupCreationForm.getLineupTitle())
            }

            override fun onCancelClicked() {
                findNavController().popBackStack()
            }
        })

        lineupViewModel.getRoaster().subscribe({
            updateRoasterSize(view.playerCount, it)
        }, {
            Timber.e(it)
        })

        return view
    }

    private fun updateRoasterSize(view: TextView, response: GetRoaster.ResponseValue) {
        when(response.status) {
            STATUS_ALL -> {
                view.text = getString(R.string.roaster_size_status_all)
            }
            else -> {
                val size = response.players.filter { it.status }.size
                view.text = resources.getQuantityString(R.plurals.roaster_size_status_selection, size, size)
            }
        }
    }

    private fun saveLineup(tournament: Tournament, lineupTitle: String) {
        Timber.d("chosen tournament = $tournament")
        Timber.d("chosen title = $lineupTitle")

        saveDisposable?.takeIf { !it.isDisposed }?.dispose()
        saveDisposable = lineupViewModel.createNewLineup(tournament, lineupTitle)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ lineupID ->
                    Timber.d("successfully inserted new lineup, new id: $lineupID")
                    val extras = Bundle()
                    extras.putLong(Constants.LINEUP_ID, lineupID)
                    extras.putString(Constants.LINEUP_TITLE, lineupTitle)
                    extras.putBoolean(Constants.EXTRA_EDITABLE, true)
                    findNavController().navigate(R.id.lineupFragment, extras, NavigationUtils().getOptionsWithPopDestination(R.id.navigation_lineups, false))
                }, { throwable ->
                    Timber.e(throwable)
                })
    }
}