package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.utils.LoadingCallback
import kotlinx.android.synthetic.main.baseball_field_with_players.view.*
import kotlinx.android.synthetic.main.field_view.view.*
import kotlin.math.roundToInt
import android.view.animation.AnimationUtils
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.MODE_DH
import com.telen.easylineup.repository.model.PlayerWithPosition


const val ICON_SIZE_SCALE = 0.12f

interface OnPlayerButtonCallback {
    fun onPlayerButtonClicked(position: FieldPosition, isNewPlayer: Boolean)
    fun onPlayerButtonLongClicked(player: Player, position: FieldPosition)
}

class DefenseEditableView: ConstraintLayout {

    private var playerListener: OnPlayerButtonCallback? = null

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun setOnPlayerListener(playerButtonCallback: OnPlayerButtonCallback) {
        playerListener = playerButtonCallback
    }

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_with_players, this)
    }

    fun setListPlayer(players: List<PlayerWithPosition>, lineupMode: Int, loadingCallback: LoadingCallback?) {
        cleanPlayerIcons()

        if(fieldFrameLayout==null || fieldFrameLayout.width <= 0)
            return

        val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()

        val emptyPositions = mutableListOf<FieldPosition>()
        emptyPositions.addAll(FieldPosition.values().filter { FieldPosition.isDefensePlayer(it.position) })

        val playerPositions: MutableMap<String, Pair<Player, FieldPosition?>> = mutableMapOf()

        players.forEach { entry ->

            val player = entry.toPlayer()
            val playerTag: String = player.id.toString()
            val fieldPosition = FieldPosition.getFieldPosition(entry.position)

            playerPositions[playerTag] = Pair(player, fieldPosition)

            fieldPosition?.let { pos ->

                emptyPositions.remove(pos)

                if(FieldPosition.isDefensePlayer(pos.position)) {

                    loadingCallback?.onStartLoading()

                    val playerView = PlayerFieldIcon(context).run {
                        layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
                        if(lineupMode == MODE_DH) {
                            when(pos) {
                                FieldPosition.DH, FieldPosition.PITCHER -> {
                                    setPlayerImage(player.image, player.name, iconSize, Color.RED, 3f)
                                }
                                else -> {
                                    setPlayerImage(player.image, player.name, iconSize)
                                }
                            }
                        }
                        else {
                            setPlayerImage(player.image, player.name, iconSize)
                        }
                        this
                    }

                    addPlayerOnFieldWithPercentage(playerView, pos.xPercent, pos.yPercent, loadingCallback)
                    playerView.setOnClickListener { view ->
                        playerListener?.onPlayerButtonClicked(pos, false)
                    }
                    playerView.setOnLongClickListener {
                        playerListener?.onPlayerButtonLongClicked(player, pos)
                        true
                    }
                }
            }
        }

        addEmptyPositionMarker(players, emptyPositions)
        addSubstitutePlayers(players)
        addDesignatedPlayerIfExists(players, lineupMode)
    }

    private fun addEmptyPositionMarker(players: List<PlayerWithPosition>, positionMarkers: MutableList<FieldPosition>) {
        val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()
        positionMarkers.forEach {position ->

            val positionView = AddPlayerButton(context).run {
                layoutParams = LayoutParams(iconSize, iconSize)
                setScaleType(ImageView.ScaleType.CENTER)
                setOnClickListener {
                    playerListener?.onPlayerButtonClicked(position, true)
                }
                this
            }

            addPlayerOnFieldWithPercentage(positionView, position.xPercent, position.yPercent, null)
        }
    }

    private fun addSubstitutePlayers(players: List<PlayerWithPosition>) {
        val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()
        val columnCount = (substituteContainer.width - substituteContainer.paddingStart - substituteContainer.paddingEnd) / iconSize
        substituteContainer.columnCount = columnCount
        substituteContainer.removeAllViews()
        val addSubstituteView = ImageView(context).run {
            layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
            setPadding(10,10,10,10)
            setImageResource(R.drawable.ic_person_add_black_24dp)

            setOnClickListener {
                playerListener?.onPlayerButtonClicked(FieldPosition.SUBSTITUTE, true)
            }

            this
        }

        substituteContainer.addView(addSubstituteView)

        players.filter { FieldPosition.isSubstitute(it.position) && it.fieldPositionID > 0 }
                .forEach { entry ->
                    val playerView = PlayerFieldIcon(context).run {
                        layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
                        setPlayerImage(entry.image, entry.playerName, iconSize)

                        setOnLongClickListener {
                            playerListener?.onPlayerButtonLongClicked(entry.toPlayer(), FieldPosition.SUBSTITUTE)
                            true
                        }

                        this
                    }
                    substituteContainer.addView(playerView)
                }
    }

    private fun addDesignatedPlayerIfExists(players: List<PlayerWithPosition>, lineupMode: Int) {
         if(lineupMode == MODE_DH) {
            val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()
            players.filter { it.position == FieldPosition.DH.position }?.let {
                var view: View?
                try {
                    val player = it.first().toPlayer()
                    val position = FieldPosition.DH
                    val playerView = PlayerFieldIcon(context).run {
                        layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
                        setPlayerImage(player.image, player.name, iconSize, Color.RED, 3f)

                        setOnLongClickListener {
                            playerListener?.onPlayerButtonLongClicked(player, position)
                            true
                        }

                        this
                    }

                    view = playerView
                } catch (e: NoSuchElementException) {
                    val positionView = AddDesignatedPlayerButton(context).run {
                        layoutParams = LayoutParams(iconSize, iconSize)
                        this
                    }

                    view = positionView
                }

                view?.apply {
                    setOnClickListener {
                        playerListener?.onPlayerButtonClicked(FieldPosition.DH, view is AddDesignatedPlayerButton)
                    }

                    addPlayerOnFieldWithPercentage(this, FieldPosition.DH.xPercent, FieldPosition.DH.yPercent, null)
                }

            }
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

                if(view is AddPlayerButton) {
                    val shake = AnimationUtils.loadAnimation(context, R.anim.shake_effect)
                    view.animation = shake
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
                if(view is PlayerFieldIcon || view is AddPlayerButton || view is AddDesignatedPlayerButton) {
                    view.clearAnimation()
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