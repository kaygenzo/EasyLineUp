package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.telen.easylineup.databinding.CardDefenseEditableBinding
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy

class CardDefenseEditable : MaterialCardView {

    private val binding =
        CardDefenseEditableBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        context?.let {
            setBackgroundColor(ContextCompat.getColor(it, android.R.color.transparent))
        }
    }

    fun init(strategy: TeamStrategy) {
        binding.fieldAndPlayersRootView.initField(strategy)
    }

    fun setListPlayer(players: List<PlayerWithPosition>, lineupMode: Int, teamType: Int) {
        binding.fieldAndPlayersRootView.setListPlayer(players, lineupMode, teamType)
    }

    fun setPlayerStateListener(playerButtonCallback: OnPlayerButtonCallback) {
        binding.fieldAndPlayersRootView.setOnPlayerListener(playerButtonCallback)
    }

    fun clear() {
        binding.fieldAndPlayersRootView.clear()
    }
}