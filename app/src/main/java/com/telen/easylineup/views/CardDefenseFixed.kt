package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerFieldPosition
import com.telen.easylineup.data.PlayerWithPosition
import kotlinx.android.synthetic.main.card_defense_fixed.view.*

class CardDefenseFixed: CardView {
    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){ init(context)}

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.card_defense_fixed, this)
    }

    fun setListPlayer(players: List<PlayerWithPosition>) {
        fieldAndPlayersRootView.setListPlayerInField(players)
    }

    fun setGenericIcons(players: List<PlayerWithPosition>) {
        fieldAndPlayersRootView.setSmallPlayerPosition(players)
    }

    fun setLineupName(name: String) {
        lineup_name.text = name
    }
}