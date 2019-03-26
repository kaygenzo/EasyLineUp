package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import kotlinx.android.synthetic.main.card_line_up.view.*

class CardLineUp: CardView {
    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){ init(context)}

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.card_line_up, this)
    }

    fun setListPlayer(players: List<Player>) {
        fieldAndPlayersRootView.setListPlayerInContainer(players)
    }
}