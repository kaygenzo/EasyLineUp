package com.telen.easylineup.views

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.repository.model.MODE_DH
import com.telen.easylineup.repository.model.Player
import com.telen.easylineup.repository.model.PlayerWithPosition
import com.telen.easylineup.utils.LoadingCallback
import kotlinx.android.synthetic.main.baseball_field_with_players.view.*
import kotlinx.android.synthetic.main.field_view.view.*
import timber.log.Timber
import kotlin.math.roundToInt
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.DecelerateInterpolator
import com.telen.easylineup.R

const val TAG_TRASH = "_trash"

interface OnPlayerButtonCallback {
    fun onPlayerButtonClicked(position: FieldPosition, isNewPlayer: Boolean)
    fun onPlayerButtonLongClicked(player: Player, position: FieldPosition)
    fun onPlayerSentToTrash(player: Player, position: FieldPosition)
    fun onPlayersSwitched(player1: PlayerWithPosition, player2: PlayerWithPosition)
    fun onPlayerReassigned(player: PlayerWithPosition, newPosition: FieldPosition)
}

class DefenseEditableView: DefenseView {

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

                        setOnDragListener { v, event ->
                            when(event.action) {
                                DragEvent.ACTION_DRAG_STARTED,
                                DragEvent.ACTION_DRAG_ENDED,
                                DragEvent.ACTION_DRAG_EXITED,
                                DragEvent.ACTION_DRAG_ENTERED,
                                DragEvent.ACTION_DRAG_LOCATION -> true
                                DragEvent.ACTION_DROP -> {
                                    val id: ClipData.Item = event.clipData.getItemAt(0)
                                    val playerFound = players.firstOrNull { it.playerID.toString() == id.text.toString() }
                                    Timber.d( "action=ACTION_DROP switch ${playerFound?.playerName} with ${entry.playerName}")
                                    playerFound?.let {
                                        playerListener?.onPlayersSwitched(entry, it)
                                    }
                                    true
                                }
                                else -> false
                            }
                            true
                        }

                        this
                    }

                    addPlayerOnFieldWithPercentage(playerView, pos.xPercent, pos.yPercent, loadingCallback)

                    playerView.setOnClickListener { view ->
                        playerListener?.onPlayerButtonClicked(pos, false)
                    }

                    playerView.setOnLongClickListener {
                        val playerID = ClipData.Item(playerTag)
                        val playerPosition = ClipData.Item(pos.name)
                        val dragData = ClipData(ClipDescription(playerTag, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)), playerID)
                        dragData.addItem(playerPosition)
                        val shadowBuilder = DragShadowBuilder(playerView)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            playerView.startDragAndDrop(dragData, shadowBuilder, null, 0)
                        } else {
                            playerView.startDrag(dragData, shadowBuilder, null, 0)
                        }
                    }
                }
            }
        }

        addEmptyPositionMarker(players, emptyPositions)
        addSubstitutePlayers(players)
        addDesignatedPlayerIfExists(players, lineupMode)
        addTrashButton(players)
    }

    private fun addTrashButton(players: List<PlayerWithPosition>) {
        val iconSize = (fieldFrameLayout.width * ICON_SIZE_SCALE).roundToInt()

        val trashView = TrashFieldButton(context).run {
            layoutParams = LayoutParams(iconSize, iconSize)
            setScaleType(ImageView.ScaleType.CENTER)
            tag = TAG_TRASH

            setOnDragListener { v, event ->
                Timber.d( "action=${event.action}")
                when(event.action) {
                    DragEvent.ACTION_DRAG_STARTED,
                    DragEvent.ACTION_DRAG_ENDED,
                    DragEvent.ACTION_DRAG_LOCATION -> true
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
                        Timber.d( "action=ACTION_DROP player=${player?.playerName} position=$fieldPosition")
                        player?.let {
                            playerListener?.onPlayerSentToTrash(it.toPlayer(), fieldPosition)
                        }
                        true
                    }
                    else -> false
                }
                true
            }

            this
        }

        trashView.apply {
            addPlayerOnFieldWithPercentage(this, 100f, 100f, null)
        }
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

                setOnDragListener { v, event ->
                    when(event.action) {
                        DragEvent.ACTION_DRAG_STARTED,
                        DragEvent.ACTION_DRAG_ENDED,
                        DragEvent.ACTION_DRAG_EXITED,
                        DragEvent.ACTION_DRAG_ENTERED,
                        DragEvent.ACTION_DRAG_LOCATION -> true
                        DragEvent.ACTION_DROP -> {
                            val id: ClipData.Item = event.clipData.getItemAt(0)
                            val playerFound = players.firstOrNull { it.playerID.toString() == id.text.toString() }
                            Timber.d( "action=ACTION_DROP reassigned ${playerFound?.playerName} to ${position}")
                            playerFound?.let {
                                playerListener?.onPlayerReassigned(it, position)
                            }
                            true
                        }
                        else -> false
                    }
                    true
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
            players.filter { it.position == FieldPosition.DH.position }.let { listPlayers ->
                var view: View?
                try {
                    val player = listPlayers.first().toPlayer()
                    val position = FieldPosition.DH
                    val playerView = PlayerFieldIcon(context).run {
                        layoutParams = FrameLayout.LayoutParams(iconSize, iconSize)
                        setPlayerImage(player.image, player.name, iconSize, Color.RED, 3f)

                        setOnLongClickListener {
                            val playerID = ClipData.Item(player.id.toString())
                            val playerPosition = ClipData.Item(position.name)
                            val dragData = ClipData(ClipDescription(player.id.toString(), arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)), playerID)
                            dragData.addItem(playerPosition)
                            val shadowBuilder = DragShadowBuilder(this)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                startDragAndDrop(dragData, shadowBuilder, null, 0)
                            } else {
                                startDrag(dragData, shadowBuilder, null, 0)
                            }
                        }

                        setOnDragListener { v, event ->
                            when(event.action) {
                                DragEvent.ACTION_DRAG_STARTED,
                                DragEvent.ACTION_DRAG_ENDED,
                                DragEvent.ACTION_DRAG_EXITED,
                                DragEvent.ACTION_DRAG_ENTERED,
                                DragEvent.ACTION_DRAG_LOCATION -> true
                                DragEvent.ACTION_DROP -> {
                                    val id: ClipData.Item = event.clipData.getItemAt(0)
                                    val playerFound = players.firstOrNull { it.playerID.toString() == id.text.toString() }
                                    Timber.d( "action=ACTION_DROP switch ${playerFound?.playerName} with ${listPlayers.first().playerName}")
                                    playerFound?.let {
                                        playerListener?.onPlayersSwitched(listPlayers.first(), it)
                                    }
                                    true
                                }
                                else -> false
                            }
                            true
                        }

                        this
                    }

                    view = playerView
                } catch (e: NoSuchElementException) {
                    val positionView = AddDesignatedPlayerButton(context).run {
                        layoutParams = LayoutParams(iconSize, iconSize)

                        setOnDragListener { v, event ->
                            when(event.action) {
                                DragEvent.ACTION_DRAG_STARTED,
                                DragEvent.ACTION_DRAG_ENDED,
                                DragEvent.ACTION_DRAG_EXITED,
                                DragEvent.ACTION_DRAG_ENTERED,
                                DragEvent.ACTION_DRAG_LOCATION -> true
                                DragEvent.ACTION_DROP -> {
                                    val id: ClipData.Item = event.clipData.getItemAt(0)
                                    val playerFound = players.firstOrNull { it.playerID.toString() == id.text.toString() }
                                    Timber.d( "action=ACTION_DROP reassigned ${playerFound?.playerName} to ${FieldPosition.DH}")
                                    playerFound?.let {
                                        playerListener?.onPlayerReassigned(it, FieldPosition.DH)
                                    }
                                    true
                                }
                                else -> false
                            }
                            true
                        }

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
}