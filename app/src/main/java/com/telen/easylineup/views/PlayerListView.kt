package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import kotlinx.android.synthetic.main.view_bottom_sheet_player_list.view.*
import timber.log.Timber

interface OnPlayerClickListener {
    fun onPlayerSelected(player: Player)
}

class PlayerListView: ConstraintLayout, OnPlayerClickListener {

    override fun onPlayerSelected(player: Player) {
        listener?.onPlayerSelected(player)
    }

    private val listPlayers = mutableListOf<Player>()
    private lateinit var mAdapter: PlayerListAdapter
    private var listener: OnPlayerClickListener? = null

    constructor(context: Context) : super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_bottom_sheet_player_list, this)
        mAdapter = PlayerListAdapter(listPlayers, this)

        val linearLayoutManager = LinearLayoutManager(context)
        val dividerItemDecoration = DividerItemDecoration(context, linearLayoutManager.orientation)
        playerRecyclerView.apply {
            layoutManager = linearLayoutManager
            addItemDecoration(dividerItemDecoration)
            adapter = mAdapter
        }
    }

    fun setPlayers(players: List<Player>, position: FieldPosition? = null) {
        listPlayers.clear()
        listPlayers.addAll(players)
        mAdapter.setFilter(position)
        mAdapter.notifyDataSetChanged()
    }

    fun setOnPlayerClickListener(listener: OnPlayerClickListener) {
        this.listener = listener
    }
}

class PlayerListAdapter(val list: List<Player>, private val playerListener: OnPlayerClickListener?): RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder>() {

    private var filter: FieldPosition? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_simple, parent, false)
        return PlayerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = list[position]
        holder.name.text = player.name.trim()

        filter?.let {
            val isMatchingPosition = player.positions and it.mask > 0
            if(isMatchingPosition) {
                holder.position.visibility = View.VISIBLE
                val positionShortDescription = FieldPosition.getPositionShortNames(holder.position.context, 0)
                holder.position.setText(positionShortDescription[it.ordinal])
                holder.position.setBackground(R.drawable.position_selected_background)
                holder.position.setTextColor(R.color.white)
            }
            else {
                holder.position.visibility = View.GONE
            }
        }

        holder.image.post {
            // I put this test here because untilReady is too long to complete so the adapter inflate too late the image.
            // this cause the images to be at the wrong place in the recycler
            if(holder.image.width > 0 && holder.image.height > 0) {
                try {
                    Picasso.get().load(player.image)
                            .resize(holder.image.width, holder.image.height)
                            .centerCrop()
                            .placeholder(R.drawable.ic_unknown_field_player)
                            .error(R.drawable.ic_unknown_field_player)
                            .into(holder.image)
                } catch (e: IllegalArgumentException) {
                    Timber.e(e)
                }
            }
            else {
                Picasso.get().load(R.drawable.ic_unknown_field_player)
                        .error(R.drawable.ic_unknown_field_player)
                        .placeholder(R.drawable.ic_unknown_field_player)
                        .into(holder.image)
            }
        }

        holder.root.setOnClickListener {
            playerListener?.onPlayerSelected(player)
        }
    }

    fun setFilter(filter: FieldPosition?) {
        this.filter = filter
    }

    class PlayerViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val root = view.findViewById<ConstraintLayout>(R.id.rootView)
        val image = view.findViewById<ImageView>(R.id.playerImage)
        val name = view.findViewById<TextView>(R.id.playerName)
        val position = view.findViewById<PlayerPositionFilterView>(R.id.filterPosition)
    }
}