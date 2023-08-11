package com.telen.easylineup.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.telen.easylineup.databinding.ItemPlayersGridBinding
import com.telen.easylineup.domain.model.Player

class PlayerGridCard(context: Context) : PlayerCard(context) {

    private val binding = ItemPlayersGridBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
    }

    override fun getCardRootView(): View {
        return binding.playerCardRootView
    }

    override fun bind(player: Player) {
        setName(binding.playerName, player.name)
        setShirtNumber(binding.playerShirtNumber, player.shirtNumber)
        setImage(binding.playerImage, player.image)
    }
}