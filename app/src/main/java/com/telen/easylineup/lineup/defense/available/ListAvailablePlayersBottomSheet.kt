package com.telen.easylineup.lineup.defense.available

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.views.OnPlayerClickListener
import kotlinx.android.synthetic.main.fragment_defense_available_players.view.*

class ListAvailablePlayersBottomSheet(private var playerClickListener: OnPlayerClickListener? = null): BottomSheetDialogFragment() {

    private val players = mutableListOf<Player>()
    private var position: FieldPosition? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_defense_available_players, container, false)
        view.playerListView.apply {
            setOnPlayerClickListener(object : OnPlayerClickListener {
                override fun onPlayerSelected(player: Player) {
                    playerClickListener?.onPlayerSelected(player)
                }
            })
            setPlayers(players, position)
        }
        return view
    }

    fun setPlayers(players: List<Player>, position: FieldPosition? = null, playerClickListener: OnPlayerClickListener) {
        this.playerClickListener = playerClickListener
        this.players.apply {
            clear()
            addAll(players)
        }
        this.position = position
    }
}