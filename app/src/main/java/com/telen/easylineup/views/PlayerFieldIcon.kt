package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.player_icon_field.view.*

class PlayerFieldIcon: LinearLayout {
    constructor(context: Context?) : super(context) { init(context) }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context) }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.player_icon_field, this)
    }

    fun setShirtNumber(shirtNumber: Int) {
        playerShirtNumber.text = shirtNumber.toString()
    }

    fun setPlayerIcon(drawableRes: Int) {
        playerImage.setImageResource(drawableRes)
    }
}