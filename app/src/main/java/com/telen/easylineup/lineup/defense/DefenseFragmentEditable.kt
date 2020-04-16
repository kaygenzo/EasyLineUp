package com.telen.easylineup.lineup.defense

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.telen.easylineup.views.DpFlexLinkView
import com.telen.easylineup.views.OnPlayerButtonCallback
import com.telen.easylineup.views.OnPlayerClickListener
import com.telen.easylineup.views.PlayerListView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
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

        viewModel.errorHandler.observe(viewLifecycleOwner, Observer {
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
                ErrorCase.NEED_ASSIGN_PITCHER_FIRST -> {
                    context?.run { DialogFactory.getErrorDialog(this, getString(R.string.error_need_assign_pitcher_first)).show() }
                }
                else -> {}
            }
        })

        viewModel.eventHandler.observe(viewLifecycleOwner, Observer { event ->
            when(event) {
                SavePlayerPositionSuccess -> Timber.d("Successfully saved player field position")
                DeletePlayerPositionSuccess -> Timber.d("Successfully deleted player field position")
                is GetAllAvailablePlayersSuccess -> activity?.run {
                    getDialogListAvailablePlayers(this, event.players, event.position).show()
                }
                is NeedLinkDpFlex -> activity?.run {
                    getDialogLinkDpAndFlex(this, event.initialData, event.dpLocked, event.flexLocked, event.teamType).show()
                }
                else -> {}
            }
        })

        viewModel.registerLineupAndPositionsChanged().observe(viewLifecycleOwner, Observer {
            view.cardDefenseView.setListPlayer(it, viewModel.lineupMode)
            view.cardDefenseView.setPlayerStateListener(this)
        })

        return view
    }

    override fun onPlayerButtonLongClicked(player: Player, position: FieldPosition) {
        activity?.run {
            getDialogDeletePosition(this, player, position).show()
        }
    }

    override fun onPlayerSentToTrash(player: Player, position: FieldPosition) {
        viewModel.onDeletePosition(player, position)
    }

    override fun onPlayerButtonClicked(position: FieldPosition) {
        viewModel.onPlayerClicked(position)
    }

    override fun onPlayersSwitched(player1: PlayerWithPosition, player2: PlayerWithPosition) {
        Timber.d("Switch ${player1.playerName} with ${player2.playerName}")
        val disposable = viewModel.switchPlayersPosition(player1, player2).subscribe({}, {
            Timber.e("Cannot switch players: ${it.message}")
        })
    }

    override fun onPlayerReassigned(player: PlayerWithPosition, newPosition: FieldPosition) {
        Timber.d("${player.playerName} reassigned to $newPosition")
        val disposable = viewModel.switchPlayersPosition(player, newPosition).subscribe({}, {
            Timber.e("Cannot change player position: ${it.message}")
        })
    }

    private fun getDialogLinkDpAndFlex(context: Context, initialData: Pair<Player?, Player?>,
                                       dpLocked: Boolean, flexLocked: Boolean, teamType: Int): Dialog {

        val dpFlexLinkView = DpFlexLinkView(context)
        dpFlexLinkView.setDpAndFlex(initialData.first, initialData.second)
        dpFlexLinkView.setTeamType(teamType)
        dpFlexLinkView.setOnDpClickListener(dpLocked, View.OnClickListener {
            viewModel.getPlayerSelectionForDp()
        })

        dpFlexLinkView.setOnFlexClickListener(flexLocked, View.OnClickListener {
            viewModel.getPlayerSelectionForFlex()
        })

        viewModel.linkPlayersInField.observe(viewLifecycleOwner, Observer {
            it?.run { dpFlexLinkView.setPlayerList(this) }
        })

        return AlertDialog.Builder(context)
                .setView(dpFlexLinkView)
                .setTitle("TODO Link your DP and you Flex")
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    val dp = dpFlexLinkView.DP()
                    val flex = dpFlexLinkView.FLEX()
                    viewModel.linkDpAndFlex(dp, flex)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                dialog.dismiss()
                            }, {
                                Timber.e(it)
                            })
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setOnDismissListener {
                    viewModel.linkPlayersInField.removeObservers(viewLifecycleOwner)
                }
                .setCancelable(false)
                .create()
    }

    private fun getDialogListAvailablePlayers(context: Context, players: List<Player>, position: FieldPosition): DialogPlus {
        val playerListView = PlayerListView(context)
        playerListView.setPlayers(players, position)

        val dialog = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(playerListView))
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .create()

        playerListView.setOnPlayerClickListener(object : OnPlayerClickListener {
            override fun onPlayerSelected(player: Player) {
                viewModel.onPlayerSelected(player, position)
                dialog.dismiss()
            }
        })

        return dialog
    }

    private fun getDialogDeletePosition(context: Context, player: Player, position: FieldPosition): Dialog {
        return DialogFactory.getWarningDialog(context,
                context.getString(R.string.dialog_delete_position_title),
                context.getString(R.string.dialog_delete_cannot_undo_message),
                Completable.create { emitter ->
                    viewModel.onDeletePosition(player, position)
                    emitter.onComplete()
                })
    }
}