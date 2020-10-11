package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.utils.ready
import com.telen.easylineup.utils.LoadingCallback
import kotlinx.android.synthetic.main.field_view.view.*
import timber.log.Timber
import kotlin.math.min
import kotlin.math.roundToInt

class DefenseFixedView: DefenseView {

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_only, this)
        fieldFrameLayout.ready {
            val size = fieldFrameLayout.run {
                val viewHeight = height
                val viewWidth = width
                min(viewHeight, viewWidth)
            }
            fieldFrameLayout.layoutParams.height = size
            fieldFrameLayout.layoutParams.width = size
        }
    }

    fun setListPlayerInField(players: List<PlayerWithPosition>) {
        setListPlayerInField(players, null)
    }

    fun setListPlayerInField(players: List<PlayerWithPosition>, loadingCallback: LoadingCallback?) {

        if(players.any { FieldPosition.isDefensePlayer(it.position) })
            loadingCallback?.onStartLoading()

        getContainerSize { containerSize ->

            cleanPlayerIcons()

            val iconSize = (containerSize * ICON_SIZE_SCALE).roundToInt()
            Timber.d("DefenseFixedView: iconSize=$iconSize containerWidth=${containerSize} containerHeight=${fieldFrameLayout.height}")

            players.filter { !FieldPosition.isSubstitute(it.position) }
                    .forEach { player ->
                        val position = FieldPosition.getFieldPosition(player.position)
                        position?.let {
                            val coordinatePercent = PointF(it.xPercent, it.yPercent)

                            val playerView = PlayerFieldIcon(context).run {
                                layoutParams = LayoutParams(iconSize, iconSize)
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
                            addPlayerOnFieldWithPercentage(containerSize, playerView, coordinatePercent.x, coordinatePercent.y, loadingCallback)
                        }
                    }
        }
    }

    fun setSmallPlayerPosition(players: List<FieldPosition>) {
        setSmallPlayerPosition(players, null)
    }
    fun setSmallPlayerPosition(positions: List<FieldPosition>, loadingCallback: LoadingCallback?) {

        if(positions.isNotEmpty())
            loadingCallback?.onStartLoading()

        getContainerSize { containerSize ->
            cleanPlayerIcons()
            positions.filter { FieldPosition.isDefensePlayer(it.position) }
                    .forEach { position ->
                        val iconView = SmallBaseballImageView(context).run {
                            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            setImageResource(R.drawable.baseball_ball_icon)
                            scaleType = ImageView.ScaleType.CENTER_INSIDE
                            this
                        }
                        addPlayerOnFieldWithPercentage(containerSize, iconView, position.xPercent, position.yPercent, loadingCallback)
                    }

        }
    }

    fun refresh() {
        refreshView(fieldFrameLayout)
    }

    private fun refreshView(rootView: View) {
        rootView.invalidate()
        if(rootView is ViewGroup) {
            for (i in 0 until rootView.childCount) {
                refreshView(rootView.getChildAt(i))
            }
        }
    }
}