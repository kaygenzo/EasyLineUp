package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.telen.easylineup.R
import com.telen.easylineup.lineup.LineupStatusDefense
import com.telen.easylineup.utils.LoadingCallback
import kotlinx.android.synthetic.main.card_defense_editable.view.*

class CardDefenseEditable: CardView, LoadingCallback {

    override fun onStartLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun onFinishLoading() {
        progressBar.visibility = View.GONE
    }

    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){ init(context)}

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.card_defense_editable, this)
        context?.let {
            setBackgroundColor(ContextCompat.getColor(it, android.R.color.transparent))
        }
    }

    fun setListPlayer(lineupStatusDefense: LineupStatusDefense) {
        fieldAndPlayersRootView.setListPlayer(lineupStatusDefense, this)
    }

    fun setPlayerStateListener(playerButtonCallback: OnPlayerButtonCallback) {
        fieldAndPlayersRootView.setOnPlayerListener(playerButtonCallback)
    }

    fun setLineupName(name: String) {
        lineup_name.text = name
    }
}