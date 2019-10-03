package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.utils.LoadingCallback
import kotlinx.android.synthetic.main.baseball_field_with_players.view.*
import kotlinx.android.synthetic.main.field_view.view.*
import kotlin.math.roundToInt

const val ICON_SIZE_SCALE = 0.12f

interface OnPlayerButtonCallback {
    fun onPlayerButtonClicked(players: List<Player>, position: FieldPosition, isNewPlayer: Boolean)
    fun onPlayerButtonLongClicked(position: FieldPosition)
}

class DefenseEditableView: ConstraintLayout {

    private lateinit var playerPositions: MutableMap<String, Pair<Player, FieldPosition?>>
    private var playerListener: OnPlayerButtonCallback? = null

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun setOnPlayerListener(playerButtonCallback: OnPlayerButtonCallback) {
        playerListener = playerButtonCallback
    }

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_with_players, this)
        playerPositions = mutableMapOf()
    }

    fun setListPlayer(players: Map<Player, FieldPosition?>, loadingCallback: LoadingCallback?) {
        playersContainer.removeAllViews()
        cleanPlayerIcons()

        val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()

        val emptyPositions = mutableListOf<FieldPosition>()
        emptyPositions.addAll(FieldPosition.values())

        players.forEach { entry ->

            val player = entry.key
            val playerTag: String = player.id.toString()
            var fieldPosition = entry.value

            playerPositions[playerTag] = Pair(player, fieldPosition)

            val playerView = PlayerFieldIcon(context).run {
                layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
                setPlayerImage(player.image, iconSize)
                setShirtNumber(player.shirtNumber)
                this
            }

            fieldPosition?.let { pos ->
                loadingCallback?.onStartLoading()
                emptyPositions.remove(pos)
                addPlayerOnFieldWithPercentage(playerView, pos.xPercent, pos.yPercent, loadingCallback)
                playerView.setOnClickListener { view ->
                    playerListener?.onPlayerButtonClicked(players.filter { it.value == null }.keys.toList(), pos, false)
                }
                playerView.setOnLongClickListener {
                    playerListener?.onPlayerButtonLongClicked(pos)
                    true
                }
            }
        }

        addEmptyPositionMarker(players, emptyPositions)
    }

    private fun addEmptyPositionMarker(players: Map<Player, FieldPosition?>, positionMarkers: MutableList<FieldPosition>) {
        val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()
        positionMarkers.forEach {position ->

            val positionView = AddPlayerButton(context).run {
                layoutParams = LayoutParams(iconSize, iconSize)
                setScaleType(ImageView.ScaleType.CENTER)
                setOnClickListener {
                    playerListener?.onPlayerButtonClicked(players.filter { it.value == null }.keys.toList(), position, true)
                }
                this
            }

            addPlayerOnFieldWithPercentage(positionView, position.xPercent, position.yPercent, null)
        }
    }

    private fun addPlayerOnFieldWithPercentage(view: View, x: Float, y: Float, loadingCallback: LoadingCallback?) {
        fieldFrameLayout.post {
            val layoutHeight = fieldFrameLayout.height
            val layoutWidth = fieldFrameLayout.width

            val positionX = ((x * layoutWidth) / 100f)
            val positionY = ((y * layoutHeight) / 100f)

            addPlayerOnFieldWithCoordinate(view, positionX, positionY, loadingCallback)
        }
    }

    private fun addPlayerOnFieldWithCoordinate(view: View, x: Float, y: Float, loadingCallback: LoadingCallback?) {

        if(playersContainer.findViewWithTag<PlayerFieldIcon>(view.tag)!=null)
            playersContainer.removeView(view)
        if(fieldFrameLayout.findViewWithTag<PlayerFieldIcon>(view.tag)!=null)
            fieldFrameLayout.removeView(view)

        view.visibility = View.INVISIBLE

        val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()

        view.post {
            val imageWidth = view.width.toFloat()
            val imageHeight = view.height.toFloat()

            checkBounds(x, y, imageWidth, imageHeight) {
                correctedX: Float, correctedY: Float ->

                val positionX = correctedX - imageWidth / 2
                val positionY = correctedY - imageHeight / 2

                val layoutParamCustom = FrameLayout.LayoutParams(iconSize, iconSize).run {
                    leftMargin = positionX.toInt()
                    topMargin = positionY.toInt()
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

    private fun cleanPlayerIcons() {
        if(fieldFrameLayout.childCount > 1) {
            for (i in fieldFrameLayout.childCount-1 downTo 0) {
                val view = fieldFrameLayout.getChildAt(i)
                if(view is PlayerFieldIcon || view is AddPlayerButton) {
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