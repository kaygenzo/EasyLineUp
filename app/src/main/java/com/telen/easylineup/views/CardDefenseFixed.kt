/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.telen.easylineup.databinding.CardDefenseFixedBinding
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy

class CardDefenseFixed : MaterialCardView {
    private val binding = CardDefenseFixedBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context?.let {
            setBackgroundColor(ContextCompat.getColor(it, android.R.color.transparent))
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun init(strategy: TeamStrategy) {
        binding.fieldAndPlayersRootView.initField(strategy)
    }

    fun setListPlayer(players: List<PlayerWithPosition>, lineupMode: Int) {
        binding.fieldAndPlayersRootView.setListPlayerInField(players, lineupMode)
    }
}
