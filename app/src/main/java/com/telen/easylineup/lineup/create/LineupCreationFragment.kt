package com.telen.easylineup.lineup.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Tournament
import com.telen.easylineup.lineup.list.LineupViewModel
import com.telen.easylineup.lineup.list.TournamentViewModel
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.views.OnActionButtonListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_lineup_creation.view.*
import timber.log.Timber

class LineupCreationFragment: Fragment() {

    private var saveDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_creation, container, false)

        val tournamentViewModel =  ViewModelProviders.of(this).get(TournamentViewModel::class.java)
        tournamentViewModel.getTournaments().observe(this, Observer { tournaments ->
            view.lineupCreationForm.setList(tournaments)
        })

        view.lineupCreationForm.setOnActionClickListener(object: OnActionButtonListener {
            override fun onSaveClicked() {
                val tournament = view.lineupCreationForm.getSelectedTournament()
                saveLineup(tournament, view.lineupCreationForm.getLineupTitle())
            }

            override fun onCancelClicked() {
                findNavController().popBackStack()
            }
        })

        return view
    }

    private fun saveLineup(tournament: Tournament, lineupTitle: String) {
        Timber.d("chosen tournament = $tournament")
        Timber.d("chosen title = $lineupTitle")

        val lineupViewModel = ViewModelProviders.of(this).get(LineupViewModel::class.java)

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