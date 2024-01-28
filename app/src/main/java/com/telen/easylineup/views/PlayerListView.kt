/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ItemPlayerSimpleBinding
import com.telen.easylineup.databinding.ViewBottomSheetPlayerListBinding
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.utils.getPositionShortNames
import timber.log.Timber

interface OnPlayerClickListener {
    fun onPlayerSelected(player: Player)
}

class PlayerListView : ConstraintLayout, OnPlayerClickListener {
    private val binding =
        ViewBottomSheetPlayerListBinding.inflate(LayoutInflater.from(context), this, true)
    private val listPlayers: MutableList<Player> = mutableListOf()
    private var playersListAdapter: PlayerListAdapter = PlayerListAdapter(listPlayers, this)
    private var listener: OnPlayerClickListener? = null

    init {
        val linearLayoutManager = LinearLayoutManager(context)
        val dividerItemDecoration = DividerItemDecoration(context, linearLayoutManager.orientation)
        binding.playerRecyclerView.apply {
            layoutManager = linearLayoutManager
            addItemDecoration(dividerItemDecoration)
            adapter = playersListAdapter
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onPlayerSelected(player: Player) {
        listener?.onPlayerSelected(player)
    }

    fun setPlayers(players: List<Player>, position: FieldPosition? = null) {
        listPlayers.clear()
        listPlayers.addAll(players)
        playersListAdapter.setFilter(position)
        playersListAdapter.notifyDataSetChanged()
    }

    fun setOnPlayerClickListener(listener: OnPlayerClickListener) {
        this.listener = listener
    }
}

/**
 * @property list
 */
class PlayerListAdapter(
    val list: List<Player>,
    private val playerListener: OnPlayerClickListener?
) : RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {
    private var filter: FieldPosition? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding =
            ItemPlayerSimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = list[position]
        with(holder.view) {
            val playerNameValue = player.name.trim()
            playerName.text = playerNameValue

            filter?.let {
                val isMatchingPosition = player.positions and it.mask > 0
                if (isMatchingPosition) {
                    filterPosition.visibility = View.VISIBLE
                    val positionShortDescription = getPositionShortNames(filterPosition.context, 0)
                    filterPosition.setText(positionShortDescription[it.ordinal])
                    filterPosition.setBackground(R.drawable.position_selected_background)
                    filterPosition.setTextColor(R.color.white)
                } else {
                    filterPosition.visibility = View.GONE
                }
            }

            holder.view.playerImage.apply {
                val size = resources.getDimensionPixelSize(R.dimen.teams_list_icon_size)
                setState(StateDefense.PLAYER)
                setPlayerImage(player.image, playerNameValue, size)
            }

            rootView.setOnClickListener {
                playerListener?.onPlayerSelected(player)
            }
        }
    }

    fun setFilter(filter: FieldPosition?) {
        this.filter = filter
    }

    /**
     * @property view
     */
    class PlayerViewHolder(val view: ItemPlayerSimpleBinding) : RecyclerView.ViewHolder(view.root)
}
