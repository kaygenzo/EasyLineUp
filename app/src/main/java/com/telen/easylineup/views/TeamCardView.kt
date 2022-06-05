package com.telen.easylineup.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.drawn
import com.telen.easylineup.utils.ready
import kotlinx.android.synthetic.main.item_card_team_type.view.*
import timber.log.Timber

class TeamCardView: ConstraintLayout {

    private var behavior: BottomSheetBehavior<ConstraintLayout>? = null

    private val blockedBottomSheetBehaviour = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.item_card_team_type, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        bottomSheet?.let {
            behavior = BottomSheetBehavior.from(bottomSheet)
            behavior?.isFitToContents = false
            behavior?.setExpandedOffset(resources.getDimensionPixelOffset(R.dimen.card_team_type_ball_image_radius))
        }

        teamTypeTitle.drawn {
            val imageRadius = resources.getDimensionPixelSize(R.dimen.card_team_type_ball_image_radius)
            val textSize = teamTypeTitle.height
            val margin = resources.getDimensionPixelSize(R.dimen.default_margin)
            val peekHeight = imageRadius + textSize + margin
            Timber.d("peekHeight=${peekHeight}")
            behavior?.peekHeight = peekHeight
        }
    }

    fun setDragEnabled(enabled: Boolean) {
        if(!enabled) {
            behavior?.addBottomSheetCallback(blockedBottomSheetBehaviour)
        }
        else {
            behavior?.removeBottomSheetCallback(blockedBottomSheetBehaviour)
            teamTypeImage.setOnClickListener {
                if(behavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                else {
                    behavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    fun setDragState(state: Int) {
        behavior?.state = state
    }

    fun setTeamImage(image: Uri?) {
        val request = Picasso.get().load(image)
        setTeamImage(request, R.drawable.ic_unknown_team, R.drawable.ic_unknown_team)
    }

    fun setImage(@DrawableRes image: Int) {
        teamTypeImage.setImageDrawable(VectorDrawableCompat.create(resources, image, null))
    }

    fun setTeamName(name: String) {
        teamTypeTitle.text = name
    }

    fun setTeamType(teamType: Int) {
        when(teamType) {
            TeamType.BASEBALL.id -> {
                Picasso.get().load(TeamType.BASEBALL.sportResId).into(teamTypeRepresentation)
            }
            TeamType.SOFTBALL.id -> {
                Picasso.get().load(TeamType.SOFTBALL.sportResId).into(teamTypeRepresentation)
            }
            TeamType.BASEBALL_5.id -> {
                Picasso.get().load(TeamType.BASEBALL_5.sportResId).into(teamTypeRepresentation)
            }
        }
    }

    private fun setTeamImage(request: RequestCreator, @DrawableRes placeholderRes: Int, @DrawableRes errorRes: Int) {
        teamTypeImage.drawn {
            try {
                val size = resources.getDimensionPixelSize(R.dimen.card_team_type_ball_image_diameter)
                val cradleRadius = resources.getDimension(R.dimen.card_team_type_ball_image_cradle_radius)
                request
                        .resize(size, size)
                        .centerCrop()
                        .transform(RoundedTransformationBuilder()
                                .borderColor(ContextCompat.getColor(context, R.color.team_type_bottom_view))
                                .borderWidth(cradleRadius)
                                .cornerRadiusDp(16f)
                                .oval(true)
                                .build())
                        .placeholder(placeholderRes)
                        .error(errorRes)
                        .into(teamTypeImage, object : Callback {
                            override fun onSuccess() {
                                Timber.d("Successfully loaded image")
                            }

                            override fun onError(e: Exception?) {
                                Timber.e(e)
                            }

                        })
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
            }
        }
    }

    fun setPlayersSize(@DrawableRes icon: Int?, message: String) {
        icon?.let {
            teamPlayersItem.setIcon(it)
        }
        teamPlayersItem.setDescription(message)
    }

    fun setLineupsSize(@DrawableRes icon: Int?, message: String) {
        icon?.let {
            teamLineupsItem.setIcon(it)
        }
        teamLineupsItem.setDescription(message)
    }

}