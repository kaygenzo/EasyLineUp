package com.telen.easylineup.views

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.FieldPosition
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import com.telen.easylineup.data.PlayerFieldPosition
import com.telen.easylineup.newLineup.OnPositionListener
import com.telen.easylineup.newLineup.PositionFieldChoiceDialog
import kotlinx.android.synthetic.main.baseball_field_with_players.view.*
import kotlinx.android.synthetic.main.field_view.view.*
import timber.log.Timber

interface OnPlayerStateChanged {
    fun onPlayerUpdated(player: Player, point: PointF, position: FieldPosition)
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

                    val playerView: PlayerFieldIcon? = baseballFieldAndPlayersRoot.findViewWithTag(tag)
                    playerView?.let { view ->
                        addPlayerOnFieldWithCoordinate(view, eventX, eventY)
                    }
                    val positionXpercentage: Float = (eventX/fieldFrameLayout.width)*100
                    val positionYpercentage: Float = (eventY/fieldFrameLayout.height)*100

                    val player = it.first
                    var playerPoint = it.second

                    if(playerPoint == null) {
                        playerPoint = PointF(positionXpercentage,positionYpercentage)
                        playerPositions[tag] = Pair(player, playerPoint)
                    }

                    playerListener?.onPlayerUpdated(player, playerPoint, fieldPosition)
                }
            }
        })
        positionDialog.show()
    }

    fun setListPlayer(players: MutableMap<Player, PlayerFieldPosition?>) {
        playersContainer.removeAllViews()

        val columnCount = 6
        val rowCount = ((players.size) / columnCount)+1
        val totalCells = rowCount * columnCount

        playersContainer.columnCount = columnCount
        playersContainer.rowCount = rowCount

        players.forEach { entry ->

            val player = entry.key
            val positionOnField = entry.value

            val playerTag: String = player.id.toString()

            var coordinatePercent: PointF? = null
            positionOnField?.let {
                coordinatePercent = PointF(it.x, it.y)
            }

            playerPositions[playerTag] = Pair(player, coordinatePercent)

            var playerView = PlayerFieldIcon(context).run {

                clipToPadding = false

                setPlayerIcon(R.drawable.pikachu)
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

            playersContainer.addView(playerView)
        }
    }

    private fun addPlayerOnFieldWithPercentage(view: PlayerFieldIcon, x: Float, y: Float) {
        val layoutHeight = fieldFrameLayout.height
        val layoutWidth = fieldFrameLayout.width

        val positionX = ((x * layoutWidth)/100f)
        val positionY = ((y * layoutHeight)/100f)

        addPlayerOnFieldWithCoordinate(view, positionX, positionY)
    }

    private fun addPlayerOnFieldWithCoordinate(view: PlayerFieldIcon, x: Float, y: Float) {
        val imageWidth = view.width.toFloat()
        val imageHeight = view.height.toFloat()

        if(playersContainer.findViewWithTag<PlayerFieldIcon>(view.tag)!=null)
            playersContainer.removeView(view)
        if(fieldFrameLayout.findViewWithTag<PlayerFieldIcon>(view.tag)!=null)
            fieldFrameLayout.removeView(view)

        val fieldWidth = fieldFrameLayout.width.toFloat()
        val fieldHeight = fieldFrameLayout.height.toFloat()

        var positionX: Float = x
        var positionY: Float = y

        if(positionX + imageWidth/2 > fieldWidth)
            positionX = fieldWidth - imageWidth/2
        if(positionX - imageWidth/2 < 0)
            positionX = imageWidth/2

        if(positionY - imageHeight/2 < 0)
            positionY = imageHeight/2
        if(positionY + imageHeight/2 > fieldHeight)
            positionY = fieldHeight - imageHeight/2

        positionX -= imageWidth / 2
        positionY -= imageHeight / 2

        val layoutParamCustom = LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).run {
            leftMargin = positionX.toInt()
            topMargin = positionY.toInt()
            this
        }

        view.layoutParams = layoutParamCustom

        fieldFrameLayout.addView(view, layoutParamCustom)
    }
}