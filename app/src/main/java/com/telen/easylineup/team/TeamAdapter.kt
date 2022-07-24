package com.telen.easylineup.team

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.views.PlayerCard

interface OnPlayerClickListener {
    fun onPlayerSelected(player: Player)
}

class TeamAdapter(private val onPlayerClickListener: OnPlayerClickListener?) :
    ListAdapter<Player, TeamAdapter.PlayerViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Player, newItem: Player) = oldItem == newItem
    }

    class PlayerViewHolder(val card: PlayerCard) : RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val viewItem = PlayerCard(parent.context)
        return PlayerViewHolder(viewItem)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = getItem(position)
        with(holder) {
            card.setName(player.name)
            card.setShirtNumber(player.shirtNumber)
            card.setOnClickListener { onPlayerClickListener?.onPlayerSelected(player) }
            card.setImage(player.image)
        }
    }
}