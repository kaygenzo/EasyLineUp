package com.telen.easylineup.views

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import com.telen.easylineup.utils.LoadingCallback
import kotlinx.android.synthetic.main.baseball_field_with_players.view.*
import kotlinx.android.synthetic.main.field_view.view.*
import timber.log.Timber
import kotlin.math.roundToInt
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.*
import kotlin.math.min

const val TAG_TRASH = "_trash"

interface OnPlayerButtonCallback {
    fun onPlayerButtonClicked(position: FieldPosition)
    fun onPlayerButtonLongClicked(player: Player, position: FieldPosition)
    fun onPlayerSentToTrash(player: Player, position: FieldPosition)
    fun onPlayersSwitched(player1: PlayerWithPosition, player2: PlayerWithPosition)
    fun onPlayerReassigned(player: PlayerWithPosition, newPosition: FieldPosition)
}

class DefenseEditableView: DefenseView {

    private var playerListener: OnPlayerButtonCallback? = null
    private val players: MutableCollection<PlayerWithPosition> = mutableListOf()

    private val positionMarkers: MutableMap<FieldPosition, MultipleStateDefenseIconButton> = mutableMapOf()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setOnPlayerListener(playerButtonCallback: OnPlayerButtonCallback) {
        playerListener = playerButtonCallback
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_with_players, this)

        getContainerSize { containerSize ->
            fieldFrameLayout.layoutParams.height = containerSize.toInt()
            fieldFrameLayout.layoutParams.width = containerSize.toInt()

            addTrashButton(containerSize)
        }
    }

    fun initField(positions: List<FieldPosition>) {

        positionMarkers.clear()
        cleanPlayerIcons()

        getContainerSize {
            val iconSize = (it * ICON_SIZE_SCALE).roundToInt()
            positions.forEach { position ->
                val view = MultipleStateDefenseIconButton(context).apply {
                    layoutParams = LayoutParams(iconSize, iconSize)
                    setState(StateDefense.LOADING)
                    setOnClickListener { view ->
                        playerListener?.onPlayerButtonClicked(position)
                    }
                }
                positionMarkers[position] = view
                addPlayerOnFieldWithPercentage(it, view, position.xPercent, position.yPercent, null)
            }
        }
    }

    fun setListPlayer(players: List<PlayerWithPosition>, lineupMode: Int, teamType: Int, loadingCallback: LoadingCallback?) {

        this.players.clear()
        this.players.addAll(players)

        getContainerSize { containerSize ->

            val iconSize = (containerSize * ICON_SIZE_SCALE).roundToInt()

            val emptyPositions = mutableListOf<FieldPosition>()
            emptyPositions.addAll(positionMarkers.keys)

            players.forEach { entry ->

                val player = entry.toPlayer()
                val playerTag: String = player.id.toString()
                val fieldPosition = FieldPosition.getFieldPosition(entry.position)

                fieldPosition?.let { pos ->

                    emptyPositions.remove(pos)

                    if (FieldPosition.isDefensePlayer(pos.position)) {

                        positionMarkers[pos]?.apply {

                            setState(StateDefense.PLAYER)

                            if (lineupMode == MODE_ENABLED) {
                                if (entry.flags and PlayerFieldPosition.FLAG_FLEX > 0 || entry.position == FieldPosition.DP_DH.position) {
                                    setPlayerImage(player.image, player.name, iconSize, Color.RED, 3f)
                                } else {
                                    setPlayerImage(player.image, player.name, iconSize)
                                }

                            } else {
                                setPlayerImage(player.image, player.name, iconSize)
                            }

                            setOnDragListener { v, event ->
                                when (event.action) {
                                    DragEvent.ACTION_DRAG_STARTED,
                                    DragEvent.ACTION_DRAG_ENDED -> true
                                    DragEvent.ACTION_DROP -> {
                                        val id: ClipData.Item = event.clipData.getItemAt(0)
                                        val playerFound = players.firstOrNull { it.playerID.toString() == id.text.toString() }
                                        Timber.d("action=ACTION_DROP switch ${playerFound?.playerName} with ${entry.playerName}")
                                        playerFound?.let {
                                            playerListener?.onPlayersSwitched(entry, it)
                                        }
                                        true
                                    }
                                    else -> false
                                }
                            }

                            setOnLongClickListener {
                                val playerID = ClipData.Item(playerTag)
                                val playerPosition = ClipData.Item(pos.name)
                                val dragData = ClipData(ClipDescription(playerTag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)), playerID)
                                dragData.addItem(playerPosition)
                                val shadowBuilder = PlayerDragShadowBuilder(this)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    startDragAndDrop(dragData, shadowBuilder, null, 0)
                                } else {
                                    startDrag(dragData, shadowBuilder, null, 0)
                                }
                                true
                            }
                        }
                    }
                }
            }

            addEmptyPositionMarker(emptyPositions)
            addSubstitutePlayers(containerSize)
            addDesignatedPlayerIfExists(lineupMode, teamType, iconSize)
        }
    }

    private fun addTrashButton(containerSize: Float) {
        val iconSize = (containerSize * ICON_SIZE_SCALE).roundToInt()

        val trashView = TrashFieldButton(context).run {
            layoutParams = LayoutParams(iconSize, iconSize)
            setScaleType(ImageView.ScaleType.CENTER)
            tag = TAG_TRASH

            setOnDragListener { v, event ->
                Timber.d("action=${event.action}")
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED,
                    DragEvent.ACTION_DRAG_ENDED -> true
                    DragEvent.ACTION_DRAG_EXITED -> {
                        val expand = ScaleAnimation(2f, 1f, 2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                        expand.duration = 100
                        expand.interpolator = DecelerateInterpolator()
                        expand.fillAfter = true
                        this.startAnimation(expand)
                        true
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        val btnAnim = ScaleAnimation(1f, 2f, 1f, 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                        btnAnim.duration = 100
                        btnAnim.interpolator = AccelerateInterpolator()
                        btnAnim.fillAfter = true
                        startAnimation(btnAnim)
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        clearAnimation()
                        val id: ClipData.Item = event.clipData.getItemAt(0)
                        val player = players.firstOrNull { it.playerID.toString() == id.text.toString() }
                        val position = event.clipData.getItemAt(1)
                        val fieldPosition = FieldPosition.valueOf(position.text.toString())
                        Timber.d("action=ACTION_DROP player=${player?.playerName} position=$fieldPosition")
                        player?.let {
                            playerListener?.onPlayerSentToTrash(it.toPlayer(), fieldPosition)
                        }
                        true
                    }
                    else -> false
                }
            }

            this
        }

        trashView.apply {
            addPlayerOnFieldWithPercentage(containerSize, this, 100f, 100f, null)
        }
    }

    private fun addEmptyPositionMarker(emptyPositions: MutableList<FieldPosition>) {
        emptyPositions.forEach { position ->
            positionMarkers[position]?.apply {

                setState(StateDefense.EMPTY)

                setOnDragListener { v, event ->
                    when (event.action) {
                        DragEvent.ACTION_DRAG_STARTED,
                        DragEvent.ACTION_DRAG_ENDED -> true
                        DragEvent.ACTION_DROP -> {
                            val id: ClipData.Item = event.clipData.getItemAt(0)
                            val playerFound = players.firstOrNull { it.playerID.toString() == id.text.toString() }
                            Timber.d("action=ACTION_DROP reassigned ${playerFound?.playerName} to ${position}")
                            playerFound?.let {
                                playerListener?.onPlayerReassigned(it, position)
                            }
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }

    private fun addSubstitutePlayers(containerSize: Float) {
        val iconSize = (containerSize * ICON_SIZE_SCALE).roundToInt()
        val columnCount = (substituteContainer.width - substituteContainer.paddingStart - substituteContainer.paddingEnd) / iconSize
        substituteContainer.columnCount = columnCount
        substituteContainer.removeAllViews()
        val addSubstituteView = ImageView(context).run {
            layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
            setPadding(10,10,10,10)
            setImageResource(R.drawable.ic_person_add_black_24dp)

            setOnClickListener {
                playerListener?.onPlayerButtonClicked(FieldPosition.SUBSTITUTE)
            }

            this
        }

        substituteContainer.addView(addSubstituteView)

        players.filter { FieldPosition.isSubstitute(it.position) && it.fieldPositionID > 0 }
                .forEach { entry ->
                    val playerView = MultipleStateDefenseIconButton(context).run {
                        layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
                        setState(StateDefense.PLAYER)
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

    private fun addDesignatedPlayerIfExists(lineupMode: Int, teamType: Int, iconSize: Int) {
        if (lineupMode == MODE_ENABLED) {
            players.filter { it.position == FieldPosition.DP_DH.position }.let { listPlayers ->
                val position = FieldPosition.DP_DH
                positionMarkers[position]?.apply {
                    try {
                        val player = listPlayers.first().toPlayer()
                        setState(StateDefense.PLAYER)
                        setPlayerImage(player.image, player.name, iconSize, Color.RED, 3f)
                        setOnLongClickListener {
                            val playerID = ClipData.Item(player.id.toString())
                            val playerPosition = ClipData.Item(position.name)
                            val dragData = ClipData(ClipDescription(player.id.toString(), arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)), playerID)
                            dragData.addItem(playerPosition)
                            val shadowBuilder = PlayerDragShadowBuilder(this)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                startDragAndDrop(dragData, shadowBuilder, null, 0)
                            } else {
                                startDrag(dragData, shadowBuilder, null, 0)
                            }
                            true
                        }

                        setOnDragListener { v, event ->
                            when (event.action) {
                                DragEvent.ACTION_DRAG_STARTED,
                                DragEvent.ACTION_DRAG_ENDED -> true
                                DragEvent.ACTION_DROP -> {
                                    val id: ClipData.Item = event.clipData.getItemAt(0)
                                    val playerFound = players.firstOrNull { it.playerID.toString() == id.text.toString() }
                                    Timber.d("action=ACTION_DROP switch ${playerFound?.playerName} with ${listPlayers.first().playerName}")
                                    playerFound?.let {
                                        playerListener?.onPlayersSwitched(listPlayers.first(), it)
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                    } catch (e: NoSuchElementException) {

                        setState(StateDefense.DP_DH)

                        when (teamType) {
                            TeamType.SOFTBALL.id -> {
                                setLabel(context.getString(R.string.field_position_dp))
                            }
                            else -> {
                                setLabel(context.getString(R.string.field_position_dh))
                            }
                        }

                        setOnDragListener { v, event ->
                            when(event.action) {
                                DragEvent.ACTION_DRAG_STARTED,
                                DragEvent.ACTION_DRAG_ENDED -> true
                                DragEvent.ACTION_DROP -> {
                                    val id: ClipData.Item = event.clipData.getItemAt(0)
                                    val playerFound = players.firstOrNull { it.playerID.toString() == id.text.toString() }
                                    Timber.d( "action=ACTION_DROP reassigned ${playerFound?.playerName} to ${FieldPosition.DP_DH}")
                                    playerFound?.let {
                                        playerListener?.onPlayerReassigned(it, FieldPosition.DP_DH)
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                    }
                }
            }
        } else {
            positionMarkers[FieldPosition.DP_DH]?.apply {
                setState(StateDefense.NONE)
            }
        }
    }
}

class PlayerDragShadowBuilder(view: View): View.DragShadowBuilder(view) {

    private var mScaleFactor: Point? = null

    override fun onProvideShadowMetrics(outShadowSize: Point?, outShadowTouchPoint: Point?) {

        // Sets the width of the shadow to half the width of the original View
        val width = view.width * 2

        // Sets the height of the shadow to half the height of the original View
        val height = view.height * 2

        // Sets the size parameter's width and height values. These get back to the system
        // through the size parameter.
        outShadowSize?.set(width, height)
        // Sets size parameter to member that will be used for scaling shadow image.
        mScaleFactor = outShadowSize

        // Sets the touch point's position to be in the middle of the drag shadow
        outShadowTouchPoint?.set(width / 2, height / 2)
    }

    override fun onDrawShadow(canvas: Canvas?) {
        // Draws the ColorDrawable in the Canvas passed in from the system.
        mScaleFactor?.let {
            canvas?.scale(it.x / view.width.toFloat(), it.y / view.height.toFloat())
            view.draw(canvas)
        } ?: super.onDrawShadow(canvas)
    }
}