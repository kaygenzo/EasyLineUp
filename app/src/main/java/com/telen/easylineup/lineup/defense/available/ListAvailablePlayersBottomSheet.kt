package com.telen.easylineup.lineup.defense.available

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.telen.easylineup.databinding.FragmentDefenseAvailablePlayersBinding
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.views.OnPlayerClickListener

class ListAvailablePlayersBottomSheet(
    private var playerClickListener: OnPlayerClickListener? = null
) : BottomSheetDialogFragment() {

    private var binder: FragmentDefenseAvailablePlayersBinding? = null

    private val players = mutableListOf<Player>()
    private var position: FieldPosition? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDefenseAvailablePlayersBinding.inflate(inflater, container, false).apply {
            with(playerListView) {
                setOnPlayerClickListener(object : OnPlayerClickListener {
                    override fun onPlayerSelected(player: Player) {
                        playerClickListener?.onPlayerSelected(player)
                    }
                })
                setPlayers(players, position)
            }
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder = null
    }

    fun setPlayers(
        players: List<Player>,
        position: FieldPosition? = null,
        playerClickListener: OnPlayerClickListener
    ) {
        this.playerClickListener = playerClickListener
        this.players.apply {
            clear()
            addAll(players)
        }
        this.position = position
    }
}