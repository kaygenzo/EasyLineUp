package com.telen.easylineup.team

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.views.PlayerCard

interface OnPlayerClickListener {
    fun onPlayerSelected(player: Player)
}

class TeamAdapter(private val context: Context, private val onPlayerClickListener: OnPlayerClickListener?, private var players: MutableList<Player> = mutableListOf()): RecyclerView.Adapter<TeamAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(val card: PlayerCard): RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val viewItem = PlayerCard(context)
        return PlayerViewHolder(viewItem)
    }

    fun setPlayers(newPlayers: List<Player>) {
        players.clear()
        players.addAll(newPlayers)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
       return players.size
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        with(holder) {
            card.setName(player.name)
            card.setShirtNumber(player.shirtNumber)
            card.setOnClickListener {
                onPlayerClickListener?.onPlayerSelected(player)
            }
            card.setImage(player.image)
        }
    }
}