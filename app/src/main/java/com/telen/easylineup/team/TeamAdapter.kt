package com.telen.easylineup.team

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.data.Player

class TeamAdapter(private val players: List<Player>): RecyclerView.Adapter<TeamAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val playerImage = view.findViewById<ImageView>(R.id.playerImage)
        val playerName = view.findViewById<TextView>(R.id.playerName)
        val playerShirtNumber = view.findViewById<TextView>(R.id.playerShirtNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val viewItem = LayoutInflater.from(parent.context).inflate(R.layout.player_list_item, parent, false)
        return PlayerViewHolder(viewItem)
    }

    override fun getItemCount(): Int {
       return players.size
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        with(holder) {
            playerName.text = player.name
            playerShirtNumber.text = player.shirtNumber.toString()
        }
    }


}