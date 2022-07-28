package com.telen.easylineup.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Player
import kotlinx.android.synthetic.main.item_players_grid.view.*

class PlayerGridCard(context: Context) : PlayerCard(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_players_grid, this)
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
    }

    override fun getCardRootView(): View {
        return playerCardRootView
    }

    override fun bind(player: Player) {
        setName(playerName, player.name)
        setShirtNumber(playerShirtNumber, player.shirtNumber)
        setImage(playerImage, player.image)
    }
}