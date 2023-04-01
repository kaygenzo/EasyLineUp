package com.telen.easylineup.lineup.defense

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentLineupDefenseEditableBinding
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignBothPlayersException
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.LineupFragment
import com.telen.easylineup.lineup.LineupViewModel
import com.telen.easylineup.lineup.LinkDpFlex
import com.telen.easylineup.lineup.ListAvailablePlayers
import com.telen.easylineup.lineup.defense.available.ListAvailablePlayersBottomSheet
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.views.DpFlexLinkView
import com.telen.easylineup.views.OnPlayerButtonCallback
import com.telen.easylineup.views.OnPlayerClickListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DefenseFragmentEditable : BaseFragment("DefenseFragmentEditable"), OnPlayerButtonCallback {

    lateinit var viewModel: LineupViewModel
    lateinit var availablePlayersBottomSheet: ListAvailablePlayersBottomSheet
    private var binder: FragmentLineupDefenseEditableBinding? = null

    companion object {
        const val PLAYERS_BOTTOM_SHEET_TAG = "available_players_bottom_sheet"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProviders.of(parentFragment as LineupFragment).get(LineupViewModel::class.java)
        availablePlayersBottomSheet = ListAvailablePlayersBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binder = FragmentLineupDefenseEditableBinding.inflate(inflater, container, false)
        this.binder = binder

        viewModel.getTeamStrategy()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binder.cardDefenseView.apply {
                    init(it)
                    setPlayerStateListener(this@DefenseFragmentEditable)
                }
                viewModel.observeDefensePlayers().observe(viewLifecycleOwner) { players ->
                    val lineupMode = viewModel.lineup?.mode ?: MODE_DISABLED
                    launch(viewModel.getTeamType().flatMap {
                        Completable.timer(100, TimeUnit.MILLISECONDS).andThen(Single.just(it))
                    }, { teamType ->
                        binder.cardDefenseView.setListPlayer(players, lineupMode, teamType)
                    }, {
                        Timber.e(it)
                    })
                }
            }, {
                Timber.e(it)
            })
        return binder.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder?.cardDefenseView?.clear()
        binder = null
    }

    override fun onPlayerButtonLongClicked(player: Player, position: FieldPosition) {
        activity?.run { getDialogDeletePosition(this, player).show() }
    }

    override fun onPlayerSentToTrash(player: Player, position: FieldPosition) {
        viewModel.onDeletePosition(player)
    }

    override fun onPlayerButtonClicked(position: FieldPosition) {
        viewModel.onPlayerClicked(position).subscribe({ event ->
            activity?.run {
                when (event) {
                    is LinkDpFlex -> onLinkDpWithFlex(this, event)
                    is ListAvailablePlayers -> onListAvailablePlayers(this, event, position)
                    else -> Timber.d("Nothing to do")
                }
            }
        }, {
            if (it is NeedAssignPitcherFirstException) {
                activity?.run {
                    FirebaseAnalyticsUtils.missingPitcher(this)
                    DialogFactory.getErrorDialog(
                        context = this,
                        title = R.string.error_need_assign_pitcher_first_title,
                        message = R.string.error_need_assign_pitcher_first_message
                    ).show()
                }
            } else {
                Timber.e(it)
            }
        }, {
            // if we arrive here, it means no players have been found
            activity?.run {
                DialogFactory.getSimpleDialog(this, R.string.players_list_empty).show()
            }
        })
    }

    override fun onPlayersSwitched(player1: PlayerWithPosition, player2: PlayerWithPosition) {
        Timber.d("Switch ${player1.playerName} with ${player2.playerName}")
        launch(viewModel.switchPlayersPosition(player1, player2), {}, {
            Timber.e("Cannot switch players: ${it.message}")
        })
    }

    override fun onPlayerReassigned(player: PlayerWithPosition, newPosition: FieldPosition) {
        Timber.d("${player.playerName} reassigned to $newPosition")
        launch(viewModel.switchPlayersPosition(player, newPosition), {}, {
            Timber.e("Cannot change player position: ${it.message}")
        })
    }

    private fun getDialogLinkDpAndFlex(
        context: Context,
        initialData: Pair<PlayerWithPosition?, PlayerWithPosition?>,
        dpLocked: Boolean,
        flexLocked: Boolean,
        teamType: Int,
        @StringRes title: Int
    ): Dialog {
        val dpFlexLinkView = DpFlexLinkView(context)
        dpFlexLinkView.setDpAndFlex(initialData.first?.toPlayer(), initialData.second?.toPlayer())
        dpFlexLinkView.setTeamType(teamType)

        dpFlexLinkView.setOnDpClickListener(dpLocked) {
            launch(viewModel.getPlayerSelectionForDp(), {
                dpFlexLinkView.setPlayerList(it.map { it.toPlayer() })
            }, {
                Timber.e(it)
            })
        }

        dpFlexLinkView.setOnFlexClickListener(flexLocked) {
            launch(viewModel.getPlayerSelectionForFlex(), {
                dpFlexLinkView.setPlayerList(it.map { it.toPlayer() })
            }, {
                Timber.e(it)
            })
        }

        val dialog = DialogFactory.getSimpleDialog(
            context = context,
            title = title,
            view = dpFlexLinkView,
            confirmClick = { dialog, _ ->
                val dp = dpFlexLinkView.getDp()
                val flex = dpFlexLinkView.getFlex()
                launch(viewModel.linkDpAndFlex(dp, flex), {
                    dialog.dismiss()
                }, {
                    if (it is NeedAssignBothPlayersException) {
                        Timber.w(it.message)
                        FirebaseAnalyticsUtils.missingDpFlex(context)
                        DialogFactory.getErrorDialog(
                            context = context,
                            title = R.string.error_need_assign_both_players_title,
                            message = R.string.error_need_assign_both_players_message
                        ).show()
                    } else {
                        Timber.e(it)
                    }
                })
            }
        )
        dialog.setCancelable(false)
        return dialog
    }

    private fun getDialogDeletePosition(
        context: Context,
        player: Player
    ): Dialog {
        return DialogFactory.getWarningTaskDialog(
            context = context,
            title = R.string.dialog_delete_position_title,
            message = R.string.dialog_delete_cannot_undo_message,
            task = Completable.create { emitter ->
                viewModel.onDeletePosition(player)
                emitter.onComplete()
            })
    }

    private fun onLinkDpWithFlex(context: Context, event: LinkDpFlex) {
        event.run {
            getDialogLinkDpAndFlex(context, initialData, dpLocked, flexLocked, teamType, title)
                .show()
        }
    }

    private fun onListAvailablePlayers(
        activity: FragmentActivity,
        event: ListAvailablePlayers,
        position: FieldPosition
    ) {
        event.players.run {
            val listener = object : OnPlayerClickListener {
                override fun onPlayerSelected(player: Player) {
                    viewModel.onPlayerSelected(player, event.position)
                    availablePlayersBottomSheet.dismiss()
                }
            }
            availablePlayersBottomSheet.setPlayers(this.map { it.toPlayer() }, position, listener)
        }
        if (!availablePlayersBottomSheet.isAdded) {
            availablePlayersBottomSheet.show(
                activity.supportFragmentManager,
                PLAYERS_BOTTOM_SHEET_TAG
            )
        }
    }
}