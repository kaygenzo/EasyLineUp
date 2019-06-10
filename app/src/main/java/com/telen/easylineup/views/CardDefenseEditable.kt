package com.telen.easylineup.views

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import kotlinx.android.synthetic.main.card_defense_editable.view.*

class CardDefenseEditable: CardView {
    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){ init(context)}

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.card_defense_editable, this)
    }

    fun setListPlayer(players: Map<Player, PointF?>) {
        fieldAndPlayersRootView.setListPlayer(players)
    }

    fun setPlayerStateListener(playerStateChanged: OnPlayerStateChanged) {
        fieldAndPlayersRootView.setOnPlayerListener(playerStateChanged)
    }

    fun setLineupName(name: String) {
        lineup_name.text = name
    }
}