package com.telen.easylineup.lineup.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.getkeepsafe.taptargetview.TapTargetView
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.TeamRosterSummary
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.lineup.list.LineupViewModel
import com.telen.easylineup.lineup.list.SaveSuccess
import com.telen.easylineup.utils.FeatureViewFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.LineupCreationFormView
import com.telen.easylineup.views.OnActionButtonListener
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.dialog_create_lineup.view.*
import kotlinx.android.synthetic.main.fragment_lineup_creation.view.*
import timber.log.Timber

class LineupCreationFragment: BaseFragment() {

    private lateinit var lineupViewModel: LineupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lineupViewModel = ViewModelProviders.of(this).get(LineupViewModel::class.java)

        val disposable = lineupViewModel.observeErrors().subscribe({
            when(it) {
                DomainErrors.INVALID_TOURNAMENT_NAME -> {
                    view?.lineupCreationForm?.setTournamentNameError(getString(R.string.lineup_creation_error_name_empty))
                    FirebaseAnalyticsUtils.emptyTournamentName(activity)
                }
                DomainErrors.INVALID_LINEUP_NAME -> {
                    view?.lineupCreationForm?.setLineupNameError(getString(R.string.lineup_creation_error_tournament_empty))
                    FirebaseAnalyticsUtils.emptyLineupName(activity)
                }
                else -> {}
            }
        }, {
           Timber.e(it)
        })
        disposables.add(disposable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_creation, container, false)

        val getTournamentsDisposable = lineupViewModel.getTournaments().subscribe({
            view.lineupCreationForm.setList(it)
        }, {
            Timber.e(it)
        })
        disposables.add(getTournamentsDisposable)

        view.lineupCreationForm.setOnActionClickListener(object: OnActionButtonListener {
            override fun onRosterChangeClicked() {
                showRosterDialog(view.lineupCreationForm)
            }

            override fun onSaveClicked(lineupName: String, tournament: Tournament) {
                lineupViewModel.saveLineup(tournament, lineupName)
            }

            override fun onCancelClicked() {
                findNavController().popBackStack()
            }
        })

        val getRosterDisposable = lineupViewModel.getRoster().subscribe({
            updateRosterSize(view.playerCount, it)
        }, {
            Timber.e(it)
        })
        disposables.add(getRosterDisposable)

        lineupViewModel.registerSaveResults().observe(viewLifecycleOwner, Observer {
            when(it) {
                is SaveSuccess -> {
                    Timber.d("successfully inserted new lineup, new id: ${it.lineupID}")
                    val extras = Bundle()
                    extras.putLong(Constants.LINEUP_ID, it.lineupID)
                    extras.putString(Constants.LINEUP_TITLE, it.lineupName)
                    extras.putBoolean(Constants.EXTRA_EDITABLE, true)
                    findNavController().navigate(R.id.lineupFragment, extras, NavigationUtils().getOptionsWithPopDestination(R.id.navigation_lineups, false))
                }
            }
        })

        return view
    }

    private fun showRosterDialog(formView: LineupCreationFormView) {
        val disposable = lineupViewModel.getRoster()
                .subscribe({ response ->
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
                                .setTitle(R.string.roster_list_player_dialog_title)
                                .setMultiChoiceItems(names.toTypedArray(), checked.toBooleanArray()) { dialog, which, isChecked ->
                                    lineupViewModel.rosterPlayerStatusChanged(which, isChecked)
                                    updateRosterSize(formView.playerCount, response)
                                }
                                .setPositiveButton(android.R.string.ok, null)
                                .create()
                                .show()
                    }

                }, {
                    Timber.e(it)
                })
        disposables.add(disposable)
    }

    override fun onResume() {
        super.onResume()
        activity?.let { activity ->
            val disposable = lineupViewModel.showNewRosterFeature(activity)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ show ->
                        if(show) {
                            view?.lineupCreationForm?.let { form ->
                                FeatureViewFactory.apply(form.rosterExpandableEdit,
                                        activity as AppCompatActivity,
                                        getString(R.string.feature_roster_title),
                                        getString(R.string.feature_roster_description),
                                        object : TapTargetView.Listener() {
                                            override fun onTargetClick(view: TapTargetView?) {
                                                showRosterDialog(form)
                                                view?.dismiss(true)
                                            }

                                            override fun onOuterCircleClick(view: TapTargetView?) {
                                                view?.dismiss(false)
                                            }
                                        })
                            }
                        }
                    }, {
                        Timber.e(it)
                    })
            disposables.add(disposable)
        }
    }

    private fun updateRosterSize(view: TextView, response: TeamRosterSummary) {
        when(response.status) {
            Constants.STATUS_ALL -> {
                view.text = getString(R.string.roster_size_status_all)
            }
            else -> {
                val size = response.players.filter { it.status }.size
                view.text = resources.getQuantityString(R.plurals.roster_size_status_selection, size, size)
            }
        }
    }
}