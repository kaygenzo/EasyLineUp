package com.telen.easylineup.views

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.OnPositionListener
import com.telen.easylineup.PositionFieldChoiceDialog
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.utils.LoadingCallback
import kotlinx.android.synthetic.main.baseball_field_with_players.view.*
import kotlinx.android.synthetic.main.field_view.view.*
import timber.log.Timber
import kotlin.math.roundToInt

const val ICON_SIZE_SCALE = 0.12f

interface OnPlayerStateChanged {
    fun onPlayerUpdated(player: Player, point: PointF, position: FieldPosition, isNewObject: Boolean)
}

class DefenseEditableView: ConstraintLayout {

    private lateinit var playerPositions: MutableMap<String, Pair<Player, PointF?>>
    private var playerListener: OnPlayerStateChanged? = null

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun setOnPlayerListener(playerStateChanged: OnPlayerStateChanged) {
        playerListener = playerStateChanged
    }

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_with_players, this)
        playerPositions = mutableMapOf()

        fieldFrameLayout.setOnDragListener { v, event ->
            when(event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    Timber.d("action=ACTION_DRAG_STARTED")
                    true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    //Log.d("BaseballFieldView", "action=ACTION_DRAG_ENTERED")
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    //Log.d("BaseballFieldView", "action=ACTION_DRAG_LOCATION")
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    Timber.d( "action=ACTION_DRAG_EXITED")
                    true
                }
                DragEvent.ACTION_DROP -> {
                    Timber.d( "action=ACTION_DROP")
                    Timber.d( "Position: x=${event.x} y=${event.y}")

                    val item: ClipData.Item = event.clipData.getItemAt(0)
                    val tag = item.text.toString()
                    onUserDropObject(tag, event.x, event.y)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    Timber.d("action=ACTION_DRAG_ENDED")
                    true
                }
                else -> {
                    Timber.d( "action=UNKNOWN")
                    false
                }
            }
            true
        }
    }

    private fun onUserDropObject(tag: String, eventX: Float, eventY: Float) {
        val positionDialog = PositionFieldChoiceDialog(context, object : OnPositionListener {
            override fun onPositionChosen(fieldPosition: FieldPosition) {

                playerPositions[tag]?.let {

                    val player = it.first
                    var playerPoint = it.second
                    var isNewObject = false

                    if (playerPoint == null) {
                        playerPoint = PointF()
                        playerPositions[tag] = Pair(player, playerPoint)
                        isNewObject = true
                    }

                    val view = baseballFieldAndPlayersRoot.findViewWithTag<PlayerFieldIcon>(tag)

                    view?.let {
                        val imageWidth = view.width.toFloat()
                        val imageHeight = view.height.toFloat()

                        checkBounds(eventX, eventY, imageWidth, imageHeight) { x: Float, y: Float ->
                            playerPoint.x = (x / fieldFrameLayout.width) * 100
                            playerPoint.y = (y / fieldFrameLayout.height) * 100

                            playerListener?.onPlayerUpdated(player, playerPoint, fieldPosition, isNewObject)
                        }
                    }
                }
            }
        })
        positionDialog.show()
    }

    fun setListPlayer(players: Map<Player, PointF?>, loadingCallback: LoadingCallback?) {
        playersContainer.removeAllViews()
        cleanPlayerIcons()

        val columnCount = resources.getInteger(R.integer.lineup_edition_players_container_column_count)
        val rowCount = ((players.size) / columnCount)+1

        playersContainer.columnCount = columnCount
        playersContainer.rowCount = rowCount

        val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()

        players.forEach { entry ->

            val player = entry.key
            val playerTag: String = player.id.toString()
            val coordinatePercent: PointF? = entry.value

            playerPositions[playerTag] = Pair(player, coordinatePercent)

            val playerView = PlayerFieldIcon(context).run {
                layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
                setPlayerImage(player.image, iconSize)
                setShirtNumber(player.shirtNumber)

                //replace by an id which is unique
                tag = playerTag

                setOnLongClickListener { view ->
                    val item = ClipData.Item(playerTag as? CharSequence)
                    val dragData = ClipData(playerTag as? CharSequence, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                    val shadowBuilder = DragShadowBuilder(view)
                    view.startDrag(dragData, shadowBuilder, null, 0)
                }
                this
            }

            coordinatePercent?.let {
                loadingCallback?.onStartLoading()
                addPlayerOnFieldWithPercentage(playerView, it.x, it.y, loadingCallback)
            } ?: playersContainer.addView(playerView)
        }
    }

    private fun addPlayerOnFieldWithPercentage(view: PlayerFieldIcon, x: Float, y: Float, loadingCallback: LoadingCallback?) {
        fieldFrameLayout.post {
            val layoutHeight = fieldFrameLayout.height
            val layoutWidth = fieldFrameLayout.width

            val positionX = ((x * layoutWidth) / 100f)
            val positionY = ((y * layoutHeight) / 100f)

            addPlayerOnFieldWithCoordinate(view, positionX, positionY, loadingCallback)
        }
    }

    private fun addPlayerOnFieldWithCoordinate(view: PlayerFieldIcon, x: Float, y: Float, loadingCallback: LoadingCallback?) {

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
                if(view is PlayerFieldIcon) {
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