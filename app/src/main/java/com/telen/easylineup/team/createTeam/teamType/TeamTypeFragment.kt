package com.telen.easylineup.team.createTeam.teamType

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.team.createTeam.SetupViewModel
import kotlinx.android.synthetic.main.fragment_team_type.view.*
import timber.log.Timber


class TeamTypeFragment: BaseFragment() {

    private lateinit var viewModel: SetupViewModel
    private lateinit var mViewPager: ViewPager2
    private val mCards = mutableListOf<TeamTypeCardItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            viewModel = ViewModelProviders.of(it)[SetupViewModel::class.java]
        }

        TeamType.values().forEach { type ->
            when(type) {
                TeamType.BASEBALL -> {
                    mCards.add(TeamTypeCardItem(TeamType.BASEBALL.id, R.string.title_baseball, R.drawable.image_baseball_ball_with_stroke,
                            R.drawable.image_baseball_ball, R.drawable.pitcher_baseball_team))
                }
                TeamType.SOFTBALL -> {
                    mCards.add(TeamTypeCardItem(TeamType.SOFTBALL.id,R.string.title_softball, R.drawable.image_softball_ball_with_stroke,
                            R.drawable.image_softball_ball, R.drawable.pitcher_softball_team))
                }
                else -> {

                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Constants.EXTRA_TEAM_TYPE, viewModel.team.type)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_team_type, container, false)

        mViewPager = view.teamTypeViewPager
        val mCardAdapter = CardPagerAdapter(mCards)

        mViewPager.adapter = mCardAdapter
        mViewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Timber.d("onPageSelected position=$position")
                viewModel.setTeamType(position)
            }
        })

        mViewPager.setPageTransformer(ZoomOutPageTransformer())

        savedInstanceState?.getInt(Constants.EXTRA_TEAM_TYPE)?.run {
            viewModel.team.type = this
        }

        val disposable = viewModel.getTeam().subscribe({
            TeamType.getTypeById(it.type).let { type ->
                mViewPager.currentItem = type.position
            }
        }, {
            Timber.e(it)
        })
        disposables.add(disposable)

        return view
    }
}