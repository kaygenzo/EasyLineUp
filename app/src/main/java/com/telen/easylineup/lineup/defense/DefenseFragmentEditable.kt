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
import cn.pedant.SweetAlert.SweetAlertDialog
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.lineup.PlayersPositionViewModel
import com.telen.easylineup.views.OnPlayerButtonCallback
import com.telen.easylineup.views.OnPlayerClickListener
import com.telen.easylineup.views.PlayerListView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_lineup_defense_editable.view.*
import timber.log.Timber
import java.util.concurrent.TimeUnit


class DefenseFragmentEditable: Fragment(), OnPlayerButtonCallback {

    lateinit var viewModel: PlayersPositionViewModel
    private var savingPositionDisposable: Disposable? = null

    override fun onPlayerButtonLongClicked(position: FieldPosition) {
        activity?.let {
            val dialog = SweetAlertDialog(it, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(R.string.dialog_delete_position_title)
                    .setContentText(getString(R.string.dialog_delete_cannot_undo_message))
                    .setConfirmText(getString(android.R.string.yes))
                    .setCancelText(getString(android.R.string.cancel))
                    .setConfirmClickListener { sDialog ->
                        sDialog.setTitleText(R.string.dialog_delete_progress_message)
                                .showContentText(false)
                                .showCancelButton(false)
                                .changeAlertType(SweetAlertDialog.PROGRESS_TYPE)
                        savingPositionDisposable = viewModel.deletePosition(position)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({

                                    sDialog.setTitleText("")
                                            .hideConfirmButton()
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)

                                    Completable.timer(1000, TimeUnit.MILLISECONDS)
                                            .subscribe {
                                                sDialog.dismiss()
                                            }
                                }, { throwable ->
                                    Timber.e(throwable)
                                })
                    }
            dialog.show()
        }
    }

    override fun onPlayerButtonClicked(players: List<Player>, position: FieldPosition, isNewPlayer: Boolean) {
        activity?.let {
            val view = PlayerListView(it)
            view.setPlayers(players)

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
                    view.cardDefenseView.setLineupName(viewModel.lineupTitle ?: "")
                })
            }
        }

        return view
    }
}