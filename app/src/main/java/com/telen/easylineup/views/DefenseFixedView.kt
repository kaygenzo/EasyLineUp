package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
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

    private fun addPlayerOnField(view: View, x: Float, y: Float) {
        val imageWidth = view.width
        val imageHeight = view.height

        val layoutHeight = fieldFrameLayout.height
        val layoutWidth = fieldFrameLayout.width

        val positionX = ((x * layoutWidth)/100f).roundToInt()
        val positionY = ((y * layoutHeight)/100f).roundToInt()

        val layoutParamCustom = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).run {
            leftMargin = positionX - imageWidth / 2
            topMargin = positionY - imageHeight / 2
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

    fun setSmallPlayerPosition(players: List<PlayerFieldPosition>) {
        players.forEach { playerFieldPosition ->
            var iconView = ImageView(context).run {
                setImageResource(R.drawable.baseball_ball_icon)
                this
            }
            addPlayerOnField(iconView, playerFieldPosition.x, playerFieldPosition.y)
        }
    }
}