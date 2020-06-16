package com.telen.easylineup.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.ready
import kotlinx.android.synthetic.main.item_card_team_type.view.*
import timber.log.Timber

class TeamCardView: ConstraintLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.item_card_team_type, this)
    }

    fun setTeamImage(image: String?, teamType: Int) {
        val uri = image?.let { Uri.parse(image) }
        when(teamType) {
            TeamType.BASEBALL.id -> {
                setTeamImage(uri, R.drawable.ic_unknown_team, R.drawable.ic_unknown_team)
            }
            TeamType.SOFTBALL.id -> {
                setTeamImage(uri, R.drawable.ic_unknown_team, R.drawable.ic_unknown_team)
            }
        }
    }

    fun setTeamName(name: String) {
        teamTypeTitle.text = name
    }

    fun setTeamType(teamType: Int) {
        when(teamType) {
            TeamType.BASEBALL.id -> {
                Picasso.get().load(R.drawable.pitcher_baseball_team).into(teamTypeRepresentation)

            }
            TeamType.SOFTBALL.id -> {
                Picasso.get().load(R.drawable.pitcher_softball_team).into(teamTypeRepresentation)
            }
        }
    }

    private fun setTeamImage(image: Uri?, @DrawableRes placeholderRes: Int, @DrawableRes errorRes: Int) {
        teamTypeImage.ready {
            try {
                val size = resources.getDimensionPixelSize(R.dimen.card_team_type_ball_image_diameter)
                val cradleRadius = resources.getDimension(R.dimen.card_team_type_ball_image_cradle_radius)
                Picasso.get().load(image)
                        .resize(size, size)
                        .centerCrop()
                        .transform(RoundedTransformationBuilder()
                                .borderColor(ContextCompat.getColor(context, R.color.team_type_bottom_view))
                                .borderWidthDp(cradleRadius)
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