package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import kotlinx.android.synthetic.main.card_defense_fixed.view.*

class CardDefenseFixed: MaterialCardView {

    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){ init(context)}

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.card_defense_fixed, this)
        context?.let {
            setBackgroundColor(ContextCompat.getColor(it, android.R.color.transparent))
        }
    }

    fun init(strategy: TeamStrategy) {
        fieldAndPlayersRootView.initField(strategy)
    }

    fun setListPlayer(players: List<PlayerWithPosition>, lineupMode: Int) {
        fieldAndPlayersRootView.setListPlayerInField(players, lineupMode)
    }
}