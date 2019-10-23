package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.header_player_attack.view.*

class PlayerBattingHeader: ConstraintLayout {

    constructor(context: Context) : super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.header_player_attack, this)
        reorderImage.visibility = View.INVISIBLE
        playerName.setText(R.string.header_batting_order_player_name)
        fieldPosition.setText(R.string.header_batting_order_player_position)
        shirtNumber.setText(R.string.header_batting_order_player_shirt)
    }

    fun setIsEditable(isEditable: Boolean) {
        reorderImage.visibility = if(isEditable) View.INVISIBLE else View.GONE
    }
}