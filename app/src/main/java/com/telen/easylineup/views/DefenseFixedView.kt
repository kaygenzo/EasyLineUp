package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.data.PlayerFieldPosition
import kotlinx.android.synthetic.main.field_view.view.*
import kotlin.math.roundToInt

class DefenseFixedView: ConstraintLayout {

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_only, this)
    }

    private fun addPlayerOnField(view: PlayerFieldIcon, x: Float, y: Float) {
        val imageWidth = view.width
        val imageHeight = view.height

        val layoutParamCustom = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).run {
            leftMargin = x.roundToInt() - imageWidth / 2
            topMargin = y.roundToInt() - imageHeight / 2
            this
        }

        view.run {
            layoutParams = layoutParamCustom
            invalidate()
        }

        fieldFrameLayout.addView(view, layoutParamCustom)
    }

    fun setListPlayerInField(players: List<PlayerFieldPosition>) {
        players.forEach { playerFieldPosition ->
            var playerView = PlayerFieldIcon(context).run {
                setPlayerIcon(R.drawable.pikachu)
                setShirtNumber(-1)
                setPadding(20,20,20,20)
                this
            }
            addPlayerOnField(playerView, playerFieldPosition.x, playerFieldPosition.y)
        }
    }
}