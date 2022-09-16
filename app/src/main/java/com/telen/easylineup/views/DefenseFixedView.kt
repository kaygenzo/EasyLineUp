package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.*
import kotlinx.android.synthetic.main.field_view.view.*
import kotlin.math.roundToInt

class DefenseFixedView: DefenseView {

    constructor(context: Context) : super(context) { init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_only, this)

        getContainerSize { containerSize ->
            fieldFrameLayout.layoutParams.height = containerSize.toInt()
            fieldFrameLayout.layoutParams.width = containerSize.toInt()
        }
    }

    fun setListPlayerInField(players: List<PlayerWithPosition>, lineupMode: Int) {

        cleanSexIndicators()

        val emptyPositions = mutableListOf<FieldPosition>()
        positionMarkers.keys.forEach { emptyPositions.add(it) }

        getContainerSize { containerSize ->

            val iconSize = (containerSize * ICON_SIZE_SCALE).roundToInt()

            //Timber.d("DefenseFixedView: iconSize=$iconSize containerWidth=${containerSize} containerHeight=${fieldFrameLayout.height}")

            //here we process the player positions only for those who are really set, and excluded substitutes
            players.filter { !FieldPosition.isSubstitute(it.position) }.let { filteredPlayers ->
                filteredPlayers.forEach { player ->
                    val position = FieldPosition.getFieldPositionById(player.position)
                    position?.let {
                        positionMarkers[position]?.apply {
                            emptyPositions.remove(it)
                            setState(StateDefense.PLAYER)
                            setSexIndicator(player.toPlayer(), position)

                            val needLink = player.flags and PlayerFieldPosition.FLAG_FLEX > 0
                                    || it == FieldPosition.DP_DH
                            if (lineupMode == MODE_ENABLED && needLink) {
                                setPlayerImage(player.image, player.playerName, iconSize, Color.RED, 3f)
                            } else {
                                setPlayerImage(player.image, player.playerName, iconSize)
                            }
                        }
                    }
                }
            }

            processNonDefensePositions(emptyPositions, lineupMode)
        }
    }

    private fun processNonDefensePositions(emptyPositions: MutableList<FieldPosition>, lineupMode: Int) {
        //Special case for DP/DH
        // if the lineup do not manage dp/dh mode, just remove the field position. Otherwise,
        // let's just take a look if the position has been set before
        emptyPositions.filter { it == FieldPosition.DP_DH }
                .mapNotNull { positionMarkers[it] }
                .forEach {
                    if(lineupMode == MODE_DISABLED) {
                        it.setState(StateDefense.NONE)
                    }
                    else {
                        it.setState(StateDefense.EMPTY)
                    }
                }
        emptyPositions.remove(FieldPosition.DP_DH)

        //now, there are still some empty positions, let's just hide it
        emptyPositions.mapNotNull { positionMarkers[it] }.forEach { it.setState(StateDefense.EMPTY) }
    }
}