package com.telen.easylineup.lineup.defense

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.lineup.PlayersPositionViewModel
import com.telen.easylineup.views.OnPlayerStateChanged
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_lineup_defense_editable.view.*
import timber.log.Timber

class DefenseFragmentEditable: Fragment(), OnPlayerStateChanged {
    lateinit var viewModel: PlayersPositionViewModel
    private var savingPositionDisposable: Disposable? = null

    override fun onPlayerUpdated(player: Player, point: PointF, position: FieldPosition, isNewObject: Boolean) {
        Timber.d("player=$player point=$point position=$position")

        savingPositionDisposable = viewModel.savePlayerFieldPosition(player, point, position, isNewObject)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    Timber.d("after playerFieldPosition=$this")
                }, {
                    Timber.e(it)
                })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_defense_editable, container, false)

        viewModel = ViewModelProviders.of(activity as HomeActivity).get(PlayersPositionViewModel::class.java)

        viewModel.lineupID?.let { lineupID ->
            viewModel.getTeamPlayerWithPositions(lineupID).observe(this, Observer {
                view.cardDefenseView.setListPlayer(it)
                view.cardDefenseView.setPlayerStateListener(this)
                view.cardDefenseView.setLineupName(viewModel.lineupTitle ?: "")
            })
        }

        return view
    }
}