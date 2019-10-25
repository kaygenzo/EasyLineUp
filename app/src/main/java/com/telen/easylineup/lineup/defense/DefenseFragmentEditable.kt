package com.telen.easylineup.lineup.defense

import android.graphics.PointF
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.lineup.PlayersPositionViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.views.OnPlayerButtonCallback
import com.telen.easylineup.views.OnPlayerClickListener
import com.telen.easylineup.views.PlayerListView
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_lineup_defense_editable.view.*
import timber.log.Timber


class DefenseFragmentEditable: Fragment(), OnPlayerButtonCallback {

    lateinit var viewModel: PlayersPositionViewModel
    private var savingPositionDisposable: Disposable? = null

    override fun onPlayerButtonLongClicked(player: Player, position: FieldPosition) {
        activity?.let {
            DialogFactory.getWarningDialog(it,
                    it.getString(R.string.dialog_delete_position_title),
                    it.getString(R.string.dialog_delete_cannot_undo_message),
                    viewModel.deletePosition(player, position).doOnSubscribe {disposable ->
                        savingPositionDisposable = disposable
                    })
                    .show()
        }
    }

    override fun onPlayerButtonLongClicked(position: FieldPosition) {
        activity?.let {
            DialogFactory.getWarningDialog(it,
                    it.getString(R.string.dialog_delete_position_title),
                    it.getString(R.string.dialog_delete_cannot_undo_message),
                    viewModel.deletePosition(position).doOnSubscribe {disposable ->
                        savingPositionDisposable = disposable
                    })
                    .show()
        }
    }

    override fun onPlayerButtonClicked(players: List<Player>, position: FieldPosition, isNewPlayer: Boolean) {
        activity?.let {
                val view = PlayerListView(it)
                view.setPlayers(players, position)

                val dialog = DialogPlus.newDialog(it)
                        .setContentHolder(ViewHolder(view))
                        .setGravity(Gravity.BOTTOM)
                        .setCancelable(true)
                        .create()

                view.setOnPlayerClickListener(object : OnPlayerClickListener {
                    override fun onPlayerSelected(player: Player) {
                        savingPositionDisposable = viewModel.savePlayerFieldPosition(player, PointF(position.xPercent, position.yPercent), position, isNewPlayer)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .subscribe({
                                    Timber.d("after playerFieldPosition=$this")
                                }, { error -> Timber.e(error) })
                        dialog.dismiss()
                    }
                })

                dialog.show()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_defense_editable, container, false)

        parentFragment?.let { parent ->
            viewModel = ViewModelProviders.of(parent).get(PlayersPositionViewModel::class.java)

            viewModel.lineupID?.let { lineupID ->
                viewModel.getTeamPlayerWithPositions(lineupID).observe(this, Observer {
                    view.cardDefenseView.setListPlayer(it)
                    view.cardDefenseView.setPlayerStateListener(this)
                })
            }
        }

        return view
    }
}