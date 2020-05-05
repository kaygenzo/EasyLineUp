package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.TeamType
import kotlinx.android.synthetic.main.view_dp_flex_link.view.*

class DpFlexLinkView: ConstraintLayout {

    companion object {
        const val TYPE_NONE = 0
        const val TYPE_DP = 1
        const val TYPE_FLEX = 2
    }

    private var playerTypeChoice = TYPE_NONE
    private var dp: Player? = null
    private var flex: Player? = null
    private val iconeSize: Int

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_dp_flex_link, this)
        linkDpFlexPlayerList.visibility = View.GONE
        iconeSize = resources.getDimensionPixelSize(R.dimen.link_dp_flex_icon_size)
    }

    fun setTeamType(type: Int) {
        when(type) {
            TeamType.BASEBALL.id -> {
                player_dp_label.setText(R.string.field_position_dh)
                player_flex_label.setText(R.string.field_position_pitcher)
            }
            TeamType.SOFTBALL.id -> {
                player_dp_label.setText(R.string.field_position_dp)
                player_flex_label.setText(R.string.field_position_flex)
            }
        }
    }

    fun setOnDpClickListener(dpLocked: Boolean, listener: OnClickListener) {
        if(!dpLocked) {
            player_dp.alpha = 1f
            player_dp.setOnClickListener {
                player_dp.setBorderColor(Color.RED)
                player_flex.setBorderColor(Color.BLACK)
                playerTypeChoice = TYPE_DP
                listener.onClick(it)
            }
        }
        else {
            player_dp.alpha = 0.5f
        }
    }

    fun setOnFlexClickListener(flexLocked: Boolean, listener: OnClickListener) {
        if(!flexLocked) {
            player_flex.alpha = 1f
            player_flex.setOnClickListener {
                player_dp.setBorderColor(Color.BLACK)
                player_flex.setBorderColor(Color.RED)
                playerTypeChoice = TYPE_FLEX
                listener.onClick(it)
            }
        }
        else {
            player_flex.alpha = 0.5f
        }
    }

    fun getCurrentTypeSelected(): Int {
        return playerTypeChoice
    }

    fun setDpAndFlex(dp: Player?, flex: Player?) {
        dp?.let {
            setDpPlayer(it)
        }
        flex?.let {
            setFlexPlayer(it)
        }
    }

    private fun setDpPlayer(player: Player) {
        this.dp = player
        player_dp.setPlayerImage(player.image, player.name, iconeSize)
    }

    private fun setFlexPlayer(player: Player) {
        this.flex = player
        player_flex.setPlayerImage(player.image, player.name, iconeSize)
    }

    fun setPlayerList(players: List<Player>) {
        linkDpFlexPlayerList.visibility = View.VISIBLE
        linkDpFlexPlayerList.setPlayers(players)
        linkDpFlexPlayerList.setOnPlayerClickListener(object : OnPlayerClickListener {
            override fun onPlayerSelected(player: Player) {
                linkDpFlexPlayerList.visibility = View.GONE
                if(getCurrentTypeSelected() == TYPE_FLEX) {
                    flex = player
                    setFlexPlayer(player)
                }
                else {
                    dp = player
                    setDpPlayer(player)
                }
            }
        })
    }

    fun getDp(): Player? {
        return dp
    }

    fun getFlex(): Player? {
        return flex
    }
}