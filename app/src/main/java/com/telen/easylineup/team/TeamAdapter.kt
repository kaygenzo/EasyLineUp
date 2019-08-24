package com.telen.easylineup.team

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.views.PlayerCard

interface OnPlayerClickListener {
    fun onPlayerSelected(player: Player)
}

class TeamAdapter(private val context: Context, private val players: List<Player>, val onPlayerClickListener: OnPlayerClickListener?): RecyclerView.Adapter<TeamAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(val card: PlayerCard): RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val viewItem = PlayerCard(context)
        return PlayerViewHolder(viewItem)
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