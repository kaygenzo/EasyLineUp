package com.telen.easylineup.lineup.defense

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.lineup.*
import com.telen.easylineup.lineup.defense.available.ListAvailablePlayersBottomSheet
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.views.DpFlexLinkView
import com.telen.easylineup.views.OnPlayerButtonCallback
import com.telen.easylineup.views.OnPlayerClickListener
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_lineup_defense_editable.view.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DefenseFragmentEditable: BaseFragment("DefenseFragmentEditable"), OnPlayerButtonCallback {

    lateinit var viewModel: PlayersPositionViewModel
    lateinit var availablePlayersBottomSheet: ListAvailablePlayersBottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(parentFragment as LineupFragment).get(PlayersPositionViewModel::class.java)

        availablePlayersBottomSheet = ListAvailablePlayersBottomSheet()

        val disposable = viewModel.observeErrors().subscribe({
            when(it) {
                DomainErrors.LIST_AVAILABLE_PLAYERS_EMPTY -> {
                    activity?.let { activity ->
                        DialogFactory.getSimpleDialog(activity, R.string.players_list_empty).show()
                    }
                }
                DomainErrors.SAVE_PLAYER_FIELD_POSITION_FAILED ->  Timber.e(Exception("Save player field position failed"))
                DomainErrors.DELETE_PLAYER_FIELD_POSITION_FAILED -> Timber.e(Exception("Delete player field position failed"))
                DomainErrors.UPDATE_PLAYERS_WITH_LINEUP_MODE_FAILED -> Timber.e(Exception("Update players with lineup mode failed"))
                DomainErrors.SAVE_LINEUP_MODE_FAILED -> Timber.e(Exception("Save lineup mode failed"))
                DomainErrors.NEED_ASSIGN_PITCHER_FIRST -> {
                    context?.run {
                        FirebaseAnalyticsUtils.missingPitcher(this)
                        DialogFactory.getErrorDialog(
                                context = this,
                                title = R.string.error_need_assign_pitcher_first_title,
                                message = R.string.error_need_assign_pitcher_first_message).show()
                    }
                }
                DomainErrors.DP_OR_FLEX_NOT_ASSIGNED -> {
                    context?.run {
                        FirebaseAnalyticsUtils.missingDpFlex(this)
                        DialogFactory.getErrorDialog(
                                context = this,
                                title = R.string.error_need_assign_both_players_title,
                                message = R.string.error_need_assign_both_players_message).show()
                    }
                }
                else -> {
                    Timber.e("Not managed error")
                }
            }
        }, {
            Timber.e(it)
        })
        disposables.add(disposable)

        val eventsDisposable = viewModel.eventHandler.subscribe({ event ->
            when(event) {
                SavePlayerPositionSuccess -> Timber.d("Successfully saved player field position")
                DeletePlayerPositionSuccess -> Timber.d("Successfully deleted player field position")
                is GetAllAvailablePlayersSuccess -> activity?.run {
                    availablePlayersBottomSheet.setPlayers(event.players, event.position, object: OnPlayerClickListener {
                        override fun onPlayerSelected(player: Player) {
                            viewModel.onPlayerSelected(player, event.position)
                            availablePlayersBottomSheet.dismiss()
                        }
                    })
                    if(!availablePlayersBottomSheet.isAdded)
                        availablePlayersBottomSheet.show(supportFragmentManager, "available_players_bottom_sheet")
                }
                is NeedLinkDpFlex -> activity?.run {
                    getDialogLinkDpAndFlex(this, event.initialData, event.dpLocked, event.flexLocked, event.teamType, event.title).show()
                }
                else -> {}
            }
        }, {
            Timber.e(it)
        })

        disposables.add(eventsDisposable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_defense_editable, container, false)

        view.cardDefenseView.init(viewModel.strategy)

        viewModel.registerLineupAndPositionsChanged().observe(viewLifecycleOwner, Observer { players ->
            view.cardDefenseView.setPlayerStateListener(this)
            val disposable = viewModel.getTeamType().subscribe({
                val displayDisposable = Completable.timer(100, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            view.cardDefenseView.setListPlayer(players, viewModel.lineupMode, viewModel.teamType)
                        }, {

                        })
                disposables.add(displayDisposable)
            }, {
                Timber.e(it)
            })
            disposables.add(disposable)
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.cardDefenseView?.clear()
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
        disposables.add(disposable)
    }

    override fun onPlayerReassigned(player: PlayerWithPosition, newPosition: FieldPosition) {
        Timber.d("${player.playerName} reassigned to $newPosition")
        val disposable = viewModel.switchPlayersPosition(player, newPosition).subscribe({}, {
            Timber.e("Cannot change player position: ${it.message}")
        })
        disposables.add(disposable)
    }

    private fun getDialogLinkDpAndFlex(context: Context, initialData: Pair<Player?, Player?>,
                                       dpLocked: Boolean, flexLocked: Boolean, teamType: Int, @StringRes title: Int): Dialog {

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

        val dialog = DialogFactory.getSimpleDialog(
                context = context,
                title = title,
                view = dpFlexLinkView,
                confirmClick = DialogInterface.OnClickListener { dialog, which ->
                    val dp = dpFlexLinkView.getDp()
                    val flex = dpFlexLinkView.getFlex()
                    val disposable = viewModel.linkDpAndFlex(dp, flex)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                dialog.dismiss()
                            }, {
                                Timber.e(it)
                            })
                    disposables.add(disposable)
                }
        )
        dialog.setOnDismissListener {
            viewModel.linkPlayersInField.removeObservers(viewLifecycleOwner)
        }
        dialog.setCancelable(false)
        return dialog
    }

    private fun getDialogDeletePosition(context: Context, player: Player, position: FieldPosition): Dialog {
        return DialogFactory.getWarningTaskDialog(context = context,
                title = R.string.dialog_delete_position_title,
                message = R.string.dialog_delete_cannot_undo_message,
                task = Completable.create { emitter ->
                    viewModel.onDeletePosition(player, position)
                    emitter.onComplete()
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}