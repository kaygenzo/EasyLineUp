package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Player
import kotlinx.android.synthetic.main.item_players_list.view.*

class PlayerListCard(context: Context) : PlayerCard(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_players_list, this)
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun getCardRootView(): View {
        return playerCardRootView
    }

    override fun bind(player: Player) {
        setName(playerName, player.name)
        setShirtNumber(playerShirtNumber, player.shirtNumber)
        playerIcon.setState(StateDefense.PLAYER)
        playerIcon.setBorderColor(Color.BLACK)
        val size = resources.getDimensionPixelSize(R.dimen.teams_list_icon_size)
        playerIcon.setPlayerImage(player.image, player.name, size)
    }
}