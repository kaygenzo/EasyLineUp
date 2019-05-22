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
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerFieldPosition
import com.telen.easylineup.lineup.LineupActivity
import com.telen.easylineup.lineup.PlayersPositionViewModel
import com.telen.easylineup.views.OnPlayerStateChanged
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.card_defense_editable.view.*
import kotlinx.android.synthetic.main.fragment_lineup_defense_editable.view.*
import timber.log.Timber

class DefenseFragmentEditable: Fragment(), OnPlayerStateChanged {
    lateinit var viewModel: PlayersPositionViewModel
    private val playersPosition: MutableMap<Player, PlayerFieldPosition?> = mutableMapOf()
    private var loadingPositionsDisposable: Disposable? = null
    private var savingPositionDisposable: Disposable? = null

    override fun onPlayerUpdated(player: Player, point: PointF, position: FieldPosition, isNewObject: Boolean) {
        Timber.d("player=$player point=$point position=$position")

        viewModel.lineupID?.let {
            if(playersPosition[player]==null) {
                playersPosition[player] = PlayerFieldPosition(
                        playerId = player.id,
                        lineupId = it,
                        order = playersPosition.filter { it.value !=null }.count()+1)
            }
        }

        Timber.d("before playerFieldPosition=${playersPosition[player]}")

        playersPosition[player]?.apply {
            this.position = position.position
            x = point.x
            y = point.y

            savingPositionDisposable = viewModel.savePlayerFieldPosition(this)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({
                        Timber.d("after playerFieldPosition=$this")
                    }, {
                        Timber.e(it)
                    })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_defense_editable, container, false)

        viewModel = ViewModelProviders.of(activity as LineupActivity).get(PlayersPositionViewModel::class.java)
        view.lineup_name.text = viewModel.lineupTitle

        viewModel.teamID?.let {teamID ->
            viewModel.getPlayersForTeam(teamID).observe(this, Observer { players ->

                playersPosition.clear()

                val listOperations = mutableListOf<Maybe<PlayerFieldPosition>>()

                viewModel.lineupID?.let { lineupID ->
                    players.forEach {player ->
                        playersPosition[player] = null
                        listOperations.add(viewModel.getPlayerPositionFor(lineupID, player.id).doOnSuccess {
                            Timber.d("playerPosition=$it")
                            playersPosition[player] = it
                        })
                    }
                }

                loadingPositionsDisposable = Maybe.concat(listOperations)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, {
                            Timber.e(it)
                        }, {
                            Timber.d("onComplete")
                            view.cardDefenseView.setListPlayer(playersPosition)
                            view.cardDefenseView.setPlayerStateListener(this)
                        })
            })
        }

        return view
    }
}