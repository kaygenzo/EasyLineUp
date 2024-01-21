/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ViewDpFlexLinkBinding
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.TeamType

class DpFlexLinkView : ConstraintLayout {
    private val binding = ViewDpFlexLinkBinding.inflate(LayoutInflater.from(context), this, true)
    private var playerTypeChoice = TYPE_NONE
    private var dp: Player? = null
    private var flex: Player? = null
    private val iconeSize: Int

    init {
        binding.linkDpFlexPlayerList.visibility = View.GONE
        iconeSize = resources.getDimensionPixelSize(R.dimen.link_dp_flex_icon_size)
        binding.playerDp.setState(StateDefense.PLAYER)
        binding.playerFlex.setState(StateDefense.PLAYER)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setTeamType(type: Int) {
        when (type) {
            TeamType.BASEBALL.id -> {
                binding.playerDpLabel.setText(R.string.field_position_dh)
                binding.playerFlexLabel.setText(R.string.field_position_pitcher)
            }

            TeamType.SOFTBALL.id -> {
                binding.playerDpLabel.setText(R.string.field_position_dp)
                binding.playerFlexLabel.setText(R.string.field_position_flex)
            }
        }
    }

    fun setOnDpClickListener(dpLocked: Boolean, listener: OnClickListener) {
        if (!dpLocked) {
            binding.playerDp.alpha = 1f
            binding.playerDp.setOnClickListener {
                binding.playerDp.setBorderColor(Color.RED)
                binding.playerFlex.setBorderColor(Color.BLACK)
                playerTypeChoice = TYPE_DP
                listener.onClick(it)
            }
        } else {
            binding.playerDp.alpha = 0.5f
        }
    }

    fun setOnFlexClickListener(flexLocked: Boolean, listener: OnClickListener) {
        if (!flexLocked) {
            binding.playerFlex.alpha = 1f
            binding.playerFlex.setOnClickListener {
                binding.playerDp.setBorderColor(Color.BLACK)
                binding.playerFlex.setBorderColor(Color.RED)
                playerTypeChoice = TYPE_FLEX
                listener.onClick(it)
            }
        } else {
            binding.playerFlex.alpha = 0.5f
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
        binding.playerDp.setPlayerImage(player.image, player.name, iconeSize)
    }

    private fun setFlexPlayer(player: Player) {
        this.flex = player
        binding.playerFlex.setPlayerImage(player.image, player.name, iconeSize)
    }

    fun setPlayerList(players: List<Player>) {
        binding.linkDpFlexPlayerList.visibility = View.VISIBLE
        binding.linkDpFlexPlayerList.setPlayers(players)
        binding.linkDpFlexPlayerList.setOnPlayerClickListener(object : OnPlayerClickListener {
            override fun onPlayerSelected(player: Player) {
                binding.linkDpFlexPlayerList.visibility = View.GONE
                if (getCurrentTypeSelected() == TYPE_FLEX) {
                    flex = player
                    setFlexPlayer(player)
                } else {
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

    companion object {
        const val TYPE_NONE = 0
        const val TYPE_DP = 1
        const val TYPE_FLEX = 2
    }
}
