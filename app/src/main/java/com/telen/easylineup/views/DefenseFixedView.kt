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
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.data.MODE_DH
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerWithPosition
import com.telen.easylineup.utils.LoadingCallback
import kotlinx.android.synthetic.main.field_view.view.*
import kotlin.math.roundToInt

const val PLAYER_ICON_TAG = "playerIconTag"

class DefenseFixedView: ConstraintLayout {

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_only, this)
    }

    private fun addPlayerOnField(view: View, x: Float, y: Float, loadingCallback: LoadingCallback?) {
        val layoutHeight = fieldFrameLayout.height
        val layoutWidth = fieldFrameLayout.width

        val positionX = ((x * layoutWidth)/100f)
        val positionY = ((y * layoutHeight)/100f)

        view.visibility = View.INVISIBLE

        val iconSize = (layoutWidth * ICON_SIZE_SCALE).roundToInt()

        view.post {
            val imageWidth = view.width.toFloat()
            val imageHeight = view.height.toFloat()

            checkBounds(positionX, positionY, imageWidth, imageHeight) { correctedX: Float, correctedY: Float ->

//                Timber.d("imageWidth=$imageWidth imageHeight=$imageHeight layoutWidth=$layoutWidth layoutHeight=$layoutHeight")
//                Timber.d("x=$x y=$y positionX=$positionX positionY=$positionY")

                val newPosX = correctedX - imageWidth / 2
                val newPosY = correctedY - imageHeight / 2

                val layoutParamCustom = FrameLayout.LayoutParams(iconSize, iconSize).run {
                    leftMargin = newPosX.toInt()
                    topMargin = newPosY.toInt()
                    this
                }

                view.run {
                    layoutParams = layoutParamCustom
                    visibility = View.VISIBLE
                    invalidate()
                }

                loadingCallback?.onFinishLoading()
            }
        }

        fieldFrameLayout.addView(view)
    }

    fun setListPlayerInField(players: List<PlayerWithPosition>) {
        setListPlayerInField(players, null)
    }

    fun setListPlayerInField(players: List<PlayerWithPosition>, loadingCallback: LoadingCallback?) {

        if(players.filter { FieldPosition.isDefensePlayer(it.position) }.isNotEmpty())
            loadingCallback?.onStartLoading()

        fieldFrameLayout.post {
            cleanPlayerIcons()

            val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()

            players.filter { !FieldPosition.isSubstitute(it.position) }
                    .forEach { player ->
                        val position = FieldPosition.getFieldPosition(player.position)
                        position?.let {
                            val coordinatePercent = PointF(it.xPercent, it.yPercent)

                            val playerView = PlayerFieldIcon(context).run {
                                layoutParams = LayoutParams(iconSize, iconSize)
                                setPlayerImage(player.image, iconSize)
                                setShirtNumber(player.shirtNumber)
                                tag = PLAYER_ICON_TAG
                                this
                            }
                            addPlayerOnField(playerView, coordinatePercent.x, coordinatePercent.y, loadingCallback)
                        }
                    }
        }
    }

    fun setSmallPlayerPosition(players: List<FieldPosition>) {
        setSmallPlayerPosition(players, null)
    }
    fun setSmallPlayerPosition(positions: List<FieldPosition>, loadingCallback: LoadingCallback?) {

        if(positions.isNotEmpty())
            loadingCallback?.onStartLoading()

        fieldFrameLayout.post {
            cleanPlayerIcons()
            positions.filter { FieldPosition.isDefensePlayer(it.position) }
                    .forEach { position ->
                        val iconView = ImageView(context).run {
                            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            setImageResource(R.drawable.baseball_ball_icon)
                            scaleType = ImageView.ScaleType.CENTER_INSIDE
                            tag = PLAYER_ICON_TAG
                            this
                        }
                        addPlayerOnField(iconView, position.xPercent, position.yPercent, loadingCallback)
                    }

        }
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

    private fun checkBounds(x: Float, y: Float, imageWidth: Float, imageHeight: Float, callback: (x: Float,y: Float) -> Unit) {
        val containerWidth = fieldFrameLayout.width.toFloat()
        val containerHeight = fieldFrameLayout.height.toFloat()

        var positionX: Float = x
        var positionY: Float = y

        if(positionX + imageWidth/2 > containerWidth)
            positionX = containerWidth - imageWidth/2
        if(positionX - imageWidth/2 < 0)
            positionX = imageWidth/2

        if(positionY - imageHeight/2 < 0)
            positionY = imageHeight/2
        if(positionY + imageHeight/2 > containerHeight)
            positionY = containerHeight - imageHeight/2

        callback(positionX, positionY)
    }
}