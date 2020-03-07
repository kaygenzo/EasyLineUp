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
import com.telen.easylineup.R
import com.telen.easylineup.lineup.*
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.PlayerWithPosition
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.views.OnPlayerButtonCallback
import com.telen.easylineup.views.OnPlayerClickListener
import com.telen.easylineup.views.PlayerListView
import io.reactivex.Completable
import kotlinx.android.synthetic.main.fragment_lineup_defense_editable.view.*
import timber.log.Timber


class DefenseFragmentEditable: Fragment(), OnPlayerButtonCallback {

    lateinit var viewModel: PlayersPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(parentFragment as LineupFragment).get(PlayersPositionViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_defense_editable, container, false)

        viewModel.errorHandler.observe(this, Observer {
            when(it) {
                ErrorCase.LIST_AVAILABLE_PLAYERS_EMPTY -> {
                    activity?.let { activity ->
                        DialogFactory.getSimpleDialog(activity, activity.getString(R.string.players_list_empty)).show()
                    }
                }
                ErrorCase.SAVE_PLAYER_FIELD_POSITION_FAILED ->  Timber.e(Exception("Save player field position failed"))
                ErrorCase.DELETE_PLAYER_FIELD_POSITION_FAILED -> Timber.e(Exception("Delete player field position failed"))
                ErrorCase.UPDATE_PLAYERS_WITH_LINEUP_MODE_FAILED -> Timber.e(Exception("Update players with lineup mode failed"))
                ErrorCase.SAVE_LINEUP_MODE_FAILED -> Timber.e(Exception("Save lineup mode failed"))
                else -> {}
            }
        })

        viewModel.eventHandler.observe(this, Observer { event ->
            when(event) {
                SavePlayerPositionSuccess -> Timber.d("Successfully saved player field position")
                DeletePlayerPositionSuccess -> Timber.d("Successfully deleted player field position")
                is GetAllAvailablePlayersSuccess -> {
                    activity?.let { activity ->
                        val playerListView = PlayerListView(activity)
                        playerListView.setPlayers(event.players, event.position)

                        val dialog = DialogPlus.newDialog(activity)
                                .setContentHolder(ViewHolder(playerListView))
                                .setGravity(Gravity.BOTTOM)
                                .setCancelable(true)
                                .create()

                        playerListView.setOnPlayerClickListener(object : OnPlayerClickListener {
                            override fun onPlayerSelected(player: Player) {
                                viewModel.savePlayerFieldPosition(player, PointF(event.position.xPercent, event.position.yPercent), event.position, event.isNewPlayer)
                                dialog.dismiss()
                            }
                        })

                        dialog.show()
                    }
                }
                else -> {}
            }
        })

        viewModel.registerLineupAndPositionsChanged().observe(this, Observer {
            view.cardDefenseView.setListPlayer(it, viewModel.lineupMode)
            view.cardDefenseView.setPlayerStateListener(this)
        })

        return view
    }

    override fun onPlayerButtonLongClicked(player: Player, position: FieldPosition) {
        activity?.let {
            DialogFactory.getWarningDialog(it,
                    it.getString(R.string.dialog_delete_position_title),
                    it.getString(R.string.dialog_delete_cannot_undo_message),
                    Completable.create { emitter ->
                        viewModel.deletePosition(player, position)
                        emitter.onComplete()
                    })
                    .show()
        }
    }

    override fun onPlayerSentToTrash(player: Player, position: FieldPosition) {
        viewModel.deletePosition(player, position)
    }

    override fun onPlayerButtonClicked(position: FieldPosition, isNewPlayer: Boolean) {
        viewModel.getAllAvailablePlayers(position, isNewPlayer)
    }

    override fun onPlayersSwitched(player1: PlayerWithPosition, player2: PlayerWithPosition) {
        Timber.d("Switch ${player1.playerName} with ${player2.playerName}")
        val disposable = viewModel.switchPlayersPosition(player1, player2).subscribe({}, {
            Timber.e("Cannot switch players: ${it.message}")
        })
    }

    override fun onPlayerReassigned(player: PlayerWithPosition, newPosition: FieldPosition) {
        Timber.d("${player.playerName} reassigned to ${newPosition}")
        val disposable = viewModel.changePlayerPosition(player, newPosition).subscribe({}, {
            Timber.e("Cannot change player position: ${it.message}")
        })
    }
}