package com.telen.easylineup.views

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.data.PlayerWithPosition
import kotlinx.android.synthetic.main.field_view.view.*
import timber.log.Timber
import kotlin.math.roundToInt

const val SMALL_IMAGE_SIZE = 30
const val PLAYER_ICON_TAG = "playerIconTag"

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
                Timber.d("x=$x y=$y positionX=$positionX positionY=$positionY")

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

    fun setListPlayerInField(players: List<PlayerWithPosition>) {
        cleanPlayerIcons()
        players.forEach { player ->
            var coordinatePercent = PointF(player.x, player.y)

            val playerView = PlayerFieldIcon(context).run {
                layoutParams = LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPlayerImage(player.image)
                setShirtNumber(player.shirtNumber)
                tag = PLAYER_ICON_TAG
                this
            }
            addPlayerOnField(playerView, coordinatePercent.x, coordinatePercent.y)
        }
    }

    fun setSmallPlayerPosition(players: List<PointF>) {
        cleanPlayerIcons()
        players.forEach { playerCoordinate ->
            var iconView = ImageView(context).run {
                layoutParams = LayoutParams(SMALL_IMAGE_SIZE,SMALL_IMAGE_SIZE)
                setImageResource(R.drawable.baseball_ball_icon)
                tag = PLAYER_ICON_TAG
                this
            }
            addPlayerOnField(iconView, playerCoordinate.x, playerCoordinate.y)
        }
    }

    fun setSmallPlayer(player: PointF) {
        var iconView = ImageView(context).run {
            layoutParams = LayoutParams(SMALL_IMAGE_SIZE,SMALL_IMAGE_SIZE)
            setImageResource(R.drawable.baseball_ball_icon)
            tag = PLAYER_ICON_TAG
            this
        }
        addPlayerOnField(iconView, player.x, player.y)
    }

    private fun cleanPlayerIcons() {
        if(fieldFrameLayout.childCount > 1) {
            for (i in fieldFrameLayout.childCount-1 downTo 0) {
                val view = fieldFrameLayout.getChildAt(i)
                view.tag?.takeIf { it == PLAYER_ICON_TAG }?.let {
                    fieldFrameLayout.removeView(fieldFrameLayout.getChildAt(i))
                }
            }
        }
    }
}