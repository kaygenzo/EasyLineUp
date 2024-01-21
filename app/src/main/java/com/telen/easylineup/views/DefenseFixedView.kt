/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.telen.easylineup.databinding.BaseballFieldOnlyBinding
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isFlex
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.model.toPlayer
import kotlin.math.roundToInt

class DefenseFixedView : DefenseView {
    private val binding = BaseballFieldOnlyBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        getContainerSize { containerSize ->
            getContainerView().layoutParams.height = containerSize.toInt()
            getContainerView().layoutParams.width = containerSize.toInt()
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun getFieldCanvas(): ImageView {
        return binding.baseballFieldView.binding.imageCanvas
    }

    override fun getFieldImage(): ImageView {
        return binding.baseballFieldView.binding.baseballField
    }

    override fun getContainerView(): ViewGroup {
        return binding.baseballFieldView.binding.fieldFrameLayout
    }

    fun setListPlayerInField(players: List<PlayerWithPosition>, lineupMode: Int) {
        cleanSexIndicators()

        val emptyPositions: MutableList<FieldPosition> = mutableListOf()
        positionMarkers.keys.forEach { emptyPositions.add(it) }

        getContainerSize { containerSize ->

            val iconSize = (containerSize * ICON_SIZE_SCALE).roundToInt()
            // here we process the player positions only for those who are really set, and excluded
            // substitutes
            players.filter { !it.isSubstitute() }.let { filteredPlayers ->
                filteredPlayers.forEach { player ->
                    val position = FieldPosition.getFieldPositionById(player.position)
                    position?.let {
                        positionMarkers[position]?.apply {
                            emptyPositions.remove(it)
                            setState(StateDefense.PLAYER)
                            setSexIndicator(player.toPlayer(), position)

                            val needLink = player.isFlex() || it == FieldPosition.DP_DH
                            if (lineupMode == MODE_ENABLED && needLink) {
                                setPlayerImage(
                                    player.image,
                                    player.playerName,
                                    iconSize,
                                    Color.RED,
                                    3f
                                )
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

    private fun processNonDefensePositions(
        emptyPositions: MutableList<FieldPosition>,
        lineupMode: Int
    ) {
        // Special case for DP/DH
        // if the lineup do not manage dp/dh mode, just remove the field position. Otherwise,
        // let's just take a look if the position has been set before
        emptyPositions.filter { it == FieldPosition.DP_DH }
            .mapNotNull { positionMarkers[it] }
            .forEach {
                if (lineupMode == MODE_DISABLED) {
                    it.setState(StateDefense.NONE)
                } else {
                    it.setState(StateDefense.EMPTY)
                }
            }
        emptyPositions.remove(FieldPosition.DP_DH)

        // now, there are still some empty positions, let's just hide it
        emptyPositions.mapNotNull { positionMarkers[it] }
            .forEach { it.setState(StateDefense.EMPTY) }
    }
}
