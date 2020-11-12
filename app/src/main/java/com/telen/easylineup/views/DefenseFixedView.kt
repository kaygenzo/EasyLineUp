package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.view.LayoutInflater
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import kotlinx.android.synthetic.main.field_view.view.*
import timber.log.Timber
import kotlin.math.roundToInt

class DefenseFixedView: DefenseView {

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_only, this)

        getContainerSize { containerSize ->
            fieldFrameLayout.layoutParams.height = containerSize.toInt()
            fieldFrameLayout.layoutParams.width = containerSize.toInt()
        }
    }

    fun setListPlayerInField(players: List<PlayerWithPosition>) {

        getContainerSize { containerSize ->

            val iconSize = (containerSize * ICON_SIZE_SCALE).roundToInt()
            Timber.d("DefenseFixedView: iconSize=$iconSize containerWidth=${containerSize} containerHeight=${fieldFrameLayout.height}")

            players.filter { !FieldPosition.isSubstitute(it.position) }
                    .forEach { player ->
                        val position = FieldPosition.getFieldPosition(player.position)
                        position?.let {
                            val coordinatePercent = PointF(it.xPercent, it.yPercent)

                            val playerView = MultipleStateDefenseIconButton(context).run {
                                layoutParams = LayoutParams(iconSize, iconSize)
                                setState(StateDefense.PLAYER)
                                if(players.any { pos -> pos.position == FieldPosition.DP_DH.position }) {
                                    if(player.flags and PlayerFieldPosition.FLAG_FLEX > 0 || player.position == FieldPosition.DP_DH.position) {
                                        setPlayerImage(player.image, player.playerName, iconSize, Color.RED, 3f)
                                    }
                                    else {
                                        setPlayerImage(player.image, player.playerName, iconSize)
                                    }

                                }
                                else
                                    setPlayerImage(player.image, player.playerName, iconSize)
                                this
                            }
                            addPlayerOnFieldWithPercentage(containerSize, playerView, coordinatePercent.x, coordinatePercent.y)
                        }
                    }
        }
    }

    fun setSmallPlayerPosition(positions: List<FieldPosition>, lineupMode: Int) {
        getContainerSize { containerSize ->
            positions.filter { FieldPosition.isDefensePlayer(it.position) }
                    .forEach { position ->
                        positionMarkers[position]?.apply {
                            setState(StateDefense.PLAYER)
                            setPlayerImage(R.drawable.baseball_ball_icon)
                        }
                    }
            if(lineupMode == MODE_DISABLED) {
                positionMarkers[FieldPosition.DP_DH]?.apply {
                    setState(StateDefense.NONE)
                }
            }
        }
    }
}