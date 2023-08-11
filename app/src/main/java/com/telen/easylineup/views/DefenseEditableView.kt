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
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import com.telen.easylineup.R
import com.telen.easylineup.databinding.BaseballFieldWithPlayersBinding
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDefensePlayer
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.model.toPlayer
import timber.log.Timber
import kotlin.math.roundToInt

const val TAG_TRASH = "_trash"

interface OnPlayerButtonCallback {
    fun onPlayerButtonClicked(position: FieldPosition)
    fun onPlayerButtonLongClicked(player: Player, position: FieldPosition)
    fun onPlayerSentToTrash(player: Player, position: FieldPosition)
    fun onPlayersSwitched(player1: PlayerWithPosition, player2: PlayerWithPosition)
    fun onPlayerReassigned(player: PlayerWithPosition, newPosition: FieldPosition)
}

class DefenseEditableView : DefenseView {

    private val binding =
        BaseballFieldWithPlayersBinding.inflate(LayoutInflater.from(context), this, true)

    private var playerListener: OnPlayerButtonCallback? = null
    private val players: MutableCollection<PlayerWithPosition> = mutableListOf()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setOnPlayerListener(playerButtonCallback: OnPlayerButtonCallback) {
        playerListener = playerButtonCallback
    }

    init {
        getContainerSize { containerSize ->
            getContainerView().layoutParams.height = containerSize.toInt()
            getContainerView().layoutParams.width = containerSize.toInt()

            addTrashButton(containerSize)
        }
    }

    override fun getFieldCanvas(): ImageView {
        return binding.baseballFieldView.binding.imageCanvas
    }

    override fun getFieldImage(): ImageView {
        return binding.baseballFieldView.binding.baseballField
    }

    override fun getContainerView(): ViewGroup {
        return binding.baseballFieldView.binding.fieldFrameLayout
    }

    fun setListPlayer(players: List<PlayerWithPosition>, lineupMode: Int, teamType: Int) {

        cleanSexIndicators()

        this.players.clear()
        this.players.addAll(players)

        getContainerSize { containerSize ->

            val iconSize = (containerSize * ICON_SIZE_SCALE).roundToInt()

            val emptyPositions = mutableListOf<FieldPosition>()
            emptyPositions.addAll(positionMarkers.keys)

            players.forEach { entry ->

                val player = entry.toPlayer()
                val playerTag: String = player.id.toString()
                val fieldPosition = FieldPosition.getFieldPositionById(entry.position)

                fieldPosition?.let { pos ->

                    emptyPositions.remove(pos)

                    if (entry.isDefensePlayer()) {

                        positionMarkers[pos]?.apply {

                            setState(StateDefense.PLAYER)

                            if (lineupMode == MODE_ENABLED) {
                                if (entry.isFlex() || entry.isDpDh()) {
                                    setPlayerImage(
                                        player.image,
                                        player.name,
                                        iconSize,
                                        Color.RED,
                                        3f
                                    )
                                } else {
                                    setPlayerImage(player.image, player.name, iconSize)
                                }

                            } else {
                                setPlayerImage(player.image, player.name, iconSize)
                            }

                            setSexIndicator(player, pos)

                            setOnDragListener { v, event ->
                                when (event.action) {
                                    DragEvent.ACTION_DRAG_STARTED,
                                    DragEvent.ACTION_DRAG_ENDED -> true

                                    DragEvent.ACTION_DROP -> {
                                        val id: ClipData.Item = event.clipData.getItemAt(0)
                                        val playerFound = players.firstOrNull {
                                            it.playerID.toString() == id.text.toString()
                                        }
                                        Timber.d("action=ACTION_DROP switch " +
                                                "${playerFound?.playerName} with " +
                                                entry.playerName
                                        )
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
                                val dragData = ClipData(
                                    ClipDescription(
                                        playerTag,
                                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                    ), playerID
                                )
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
                        val expand = ScaleAnimation(
                            2f,
                            1f,
                            2f,
                            1f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f
                        )
                        expand.duration = 100
                        expand.interpolator = DecelerateInterpolator()
                        expand.fillAfter = true
                        this.startAnimation(expand)
                        true
                    }

                    DragEvent.ACTION_DRAG_ENTERED -> {
                        val btnAnim = ScaleAnimation(
                            1f,
                            2f,
                            1f,
                            2f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f
                        )
                        btnAnim.duration = 100
                        btnAnim.interpolator = AccelerateInterpolator()
                        btnAnim.fillAfter = true
                        startAnimation(btnAnim)
                        true
                    }

                    DragEvent.ACTION_DROP -> {
                        clearAnimation()
                        val id: ClipData.Item = event.clipData.getItemAt(0)
                        val player =
                            players.firstOrNull { it.playerID.toString() == id.text.toString() }
                        val position = event.clipData.getItemAt(1)
                        val fieldPosition = FieldPosition.valueOf(position.text.toString())
                        Timber.d("action=ACTION_DROP player=${player?.playerName} " +
                                "position=$fieldPosition")
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
            addPlayerOnFieldWithPercentage(containerSize, this, 100f, 100f)
        }
    }

    private fun addEmptyPositionMarker(emptyPositions: MutableList<FieldPosition>) {
        emptyPositions.forEach { position ->
            positionMarkers[position]?.apply {

                setState(StateDefense.ADD_PLAYER)

                setOnDragListener { v, event ->
                    when (event.action) {
                        DragEvent.ACTION_DRAG_STARTED,
                        DragEvent.ACTION_DRAG_ENDED -> true

                        DragEvent.ACTION_DROP -> {
                            val id: ClipData.Item = event.clipData.getItemAt(0)
                            val playerFound =
                                players.firstOrNull { it.playerID.toString() == id.text.toString() }
                            Timber.d("action=ACTION_DROP reassigned ${playerFound?.playerName}" +
                                    " to $position")
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
        val columnCount =
            (binding.substituteContainer.width - binding.substituteContainer.paddingStart
                    - binding.substituteContainer.paddingEnd) / iconSize
        binding.substituteContainer.columnCount = columnCount
        binding.substituteContainer.removeAllViews()
        val addSubstituteView = ImageView(context).run {
            layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
            setPadding(10, 10, 10, 10)
            setImageResource(R.drawable.ic_person_add_black_24dp)

            setOnClickListener {
                playerListener?.onPlayerButtonClicked(FieldPosition.SUBSTITUTE)
            }

            this
        }

        binding.substituteContainer.addView(addSubstituteView)

        players.filter { it.isSubstitute() }
            .forEach { entry ->
                val playerView = MultipleStateDefenseIconButton(context).run {
                    layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
                    setState(StateDefense.PLAYER)
                    setPlayerImage(entry.image, entry.playerName, iconSize)

                    setOnLongClickListener {
                        playerListener?.onPlayerButtonLongClicked(
                            entry.toPlayer(),
                            FieldPosition.SUBSTITUTE
                        )
                        true
                    }

                    this
                }
                binding.substituteContainer.addView(playerView)
            }
    }

    private fun addDesignatedPlayerIfExists(lineupMode: Int, teamType: Int, iconSize: Int) {
        if (lineupMode == MODE_ENABLED) {
            players.filter { it.isDpDh() }.let { listPlayers ->
                val position = FieldPosition.DP_DH
                positionMarkers[position]?.apply {
                    try {
                        val player = listPlayers.first().toPlayer()
                        setState(StateDefense.PLAYER)
                        setPlayerImage(player.image, player.name, iconSize, Color.RED, 3f)
                        setSexIndicator(player, position)
                        setOnLongClickListener {
                            val playerID = ClipData.Item(player.id.toString())
                            val playerPosition = ClipData.Item(position.name)
                            val dragData = ClipData(
                                ClipDescription(
                                    player.id.toString(),
                                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                ), playerID
                            )
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
                                    val playerFound =
                                        players.firstOrNull { it.playerID.toString() == id.text.toString() }
                                    Timber.d("action=ACTION_DROP switch " +
                                            "${playerFound?.playerName} with " +
                                            listPlayers.first().playerName
                                    )
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
                            when (event.action) {
                                DragEvent.ACTION_DRAG_STARTED,
                                DragEvent.ACTION_DRAG_ENDED -> true

                                DragEvent.ACTION_DROP -> {
                                    val id: ClipData.Item = event.clipData.getItemAt(0)
                                    val playerFound =
                                        players.firstOrNull { it.playerID.toString() == id.text.toString() }
                                    Timber.d("action=ACTION_DROP reassigned " +
                                            "${playerFound?.playerName} to ${FieldPosition.DP_DH}")
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

    override fun onFieldPositionClicked(position: FieldPosition) {
        playerListener?.onPlayerButtonClicked(position)
    }
}

class PlayerDragShadowBuilder(view: View) : View.DragShadowBuilder(view) {

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