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
import timber.log.Timber
import kotlin.math.roundToInt

const val SMALL_IMAGE_SIZE = 30

class DefenseFixedView: ConstraintLayout {

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_only, this)
    }

    private fun addPlayerOnField(view: View, x: Float, y: Float) {
        fieldFrameLayout.post {
            val layoutHeight = fieldFrameLayout.height
            val layoutWidth = fieldFrameLayout.width

            val positionX = ((x * layoutWidth)/100f).roundToInt()
            val positionY = ((y * layoutHeight)/100f).roundToInt()

            view.post {
                val imageWidth = view.width
                val imageHeight = view.height

                Timber.d("imageWidth=$imageWidth imageHeight=$imageHeight layoutWidth=$layoutWidth layoutHeight=$layoutHeight")

                val layoutParamCustom = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).run {
                    leftMargin = positionX - imageWidth / 2
                    topMargin = positionY - imageHeight / 2
                    this
                }

                view.run {
                    layoutParams = layoutParamCustom
                    invalidate()
                }
            }

            fieldFrameLayout.addView(view)
        }
    }

    fun setListPlayerInField(players: List<PlayerFieldPosition>) {
        players.forEach { playerFieldPosition ->
            val playerView = PlayerFieldIcon(context).run {
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
                layoutParams = LayoutParams(SMALL_IMAGE_SIZE,SMALL_IMAGE_SIZE)
                setImageResource(R.drawable.baseball_ball_icon)
                this
            }
            addPlayerOnField(iconView, playerFieldPosition.x, playerFieldPosition.y)
        }
    }
}