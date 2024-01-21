/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ItemPlayersListBinding
import com.telen.easylineup.domain.model.Player

class PlayerListCard(context: Context) : PlayerCard(context) {
    private val binding = ItemPlayersListBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun getCardRootView(): View {
        return binding.playerCardRootView
    }

    override fun bind(player: Player) {
        setName(binding.playerName, player.name)
        setShirtNumber(binding.playerShirtNumber, player.shirtNumber)
        binding.playerIcon.setState(StateDefense.PLAYER)
        binding.playerIcon.setBorderColor(Color.BLACK)
        val size = resources.getDimensionPixelSize(R.dimen.teams_list_icon_size)
        binding.playerIcon.setPlayerImage(player.image, player.name, size)
    }
}
