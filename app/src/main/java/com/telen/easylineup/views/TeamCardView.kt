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
import com.telen.easylineup.databinding.ItemCardTeamTypeBinding
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.drawn
import timber.log.Timber

class TeamCardView : ConstraintLayout {

    private var behavior: BottomSheetBehavior<ConstraintLayout>? = null

    private val binding = ItemCardTeamTypeBinding.inflate(LayoutInflater.from(context), this, true)

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
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        binding.bottomSheet?.let {
            behavior = BottomSheetBehavior.from(binding.bottomSheet)
            behavior?.isFitToContents = false
            behavior?.setExpandedOffset(resources.getDimensionPixelOffset(R.dimen.card_team_type_ball_image_radius))
        }

        binding.teamTypeTitle.drawn {
            val imageRadius =
                resources.getDimensionPixelSize(R.dimen.card_team_type_ball_image_radius)
            val textSize = binding.teamTypeTitle.height
            val margin = resources.getDimensionPixelSize(R.dimen.default_margin)
            val peekHeight = imageRadius + textSize + margin
            Timber.d("peekHeight=${peekHeight}")
            behavior?.peekHeight = peekHeight
        }
    }

    fun setDragEnabled(enabled: Boolean) {
        if (!enabled) {
            behavior?.addBottomSheetCallback(blockedBottomSheetBehaviour)
        } else {
            behavior?.removeBottomSheetCallback(blockedBottomSheetBehaviour)
            binding.teamTypeImage.setOnClickListener {
                if (behavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
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
        binding.teamTypeImage.setImageDrawable(VectorDrawableCompat.create(resources, image, null))
    }

    fun setTeamName(name: String) {
        binding.teamTypeTitle.text = name
    }

    fun setTeamType(teamType: Int) {
        with(binding.teamTypeRepresentation) {
            drawn {
                when (teamType) {
                    TeamType.BASEBALL.id -> TeamType.BASEBALL.sportResId
                    TeamType.SOFTBALL.id -> TeamType.SOFTBALL.sportResId
                    TeamType.BASEBALL_5.id -> TeamType.BASEBALL_5.sportResId
                    else -> null
                }?.let {
                    Picasso.get().load(it)
                        .resize(width, height)
                        .centerCrop()
                        .into(binding.teamTypeRepresentation)
                }
            }
        }
    }

    private fun setTeamImage(
        request: RequestCreator,
        @DrawableRes placeholderRes: Int,
        @DrawableRes errorRes: Int
    ) {
        binding.teamTypeImage.drawn {
            try {
                val size =
                    resources.getDimensionPixelSize(R.dimen.card_team_type_ball_image_diameter)
                val cradleRadius =
                    resources.getDimension(R.dimen.card_team_type_ball_image_cradle_radius)
                request
                    .resize(size, size)
                    .centerCrop()
                    .transform(
                        RoundedTransformationBuilder()
                            .borderColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.colorPrimary
                                )
                            )
                            .borderWidth(cradleRadius)
                            .cornerRadiusDp(16f)
                            .oval(true)
                            .build()
                    )
                    .placeholder(placeholderRes)
                    .error(errorRes)
                    .into(binding.teamTypeImage, object : Callback {
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
            binding.teamPlayersItem.setIcon(it)
        }
        binding.teamPlayersItem.setDescription(message)
    }

    fun setLineupsSize(@DrawableRes icon: Int?, message: String) {
        icon?.let {
            binding.teamLineupsItem.setIcon(it)
        }
        binding.teamLineupsItem.setDescription(message)
    }

    fun setSexStats(@DrawableRes icon: Int?, message: String) {
        icon?.let {
            binding.teamSexStats.setIcon(it)
        }
        binding.teamSexStats.setDescription(message)
    }
}