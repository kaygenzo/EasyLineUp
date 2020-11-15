package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import kotlinx.android.synthetic.main.card_defense_editable.view.*

class CardDefenseEditable: MaterialCardView {

    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){ init(context)}

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.card_defense_editable, this)
        context?.let {
            setBackgroundColor(ContextCompat.getColor(it, android.R.color.transparent))
        }
    }

    fun init(positions: List<FieldPosition>) {
        fieldAndPlayersRootView.initField(positions)
    }

    fun setListPlayer(players: List<PlayerWithPosition>, lineupMode: Int, teamType: Int) {
        fieldAndPlayersRootView.setListPlayer(players, lineupMode, teamType)
    }

    fun setPlayerStateListener(playerButtonCallback: OnPlayerButtonCallback) {
        fieldAndPlayersRootView.setOnPlayerListener(playerButtonCallback)
    }

    fun clear() {
        fieldAndPlayersRootView.clear()
    }
}