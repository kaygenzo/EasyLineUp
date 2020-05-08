package com.telen.easylineup.team.createTeam.teamType

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.team.createTeam.SetupViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_team_type.view.*
import timber.log.Timber


class TeamTypeFragment: BaseFragment(), ViewPager.OnPageChangeListener {

    private lateinit var viewModel: SetupViewModel
    private lateinit var mViewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            viewModel = ViewModelProviders.of(it)[SetupViewModel::class.java]
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_team_type, container, false)

        mViewPager = view.teamTypeViewPager
        val mCardAdapter = CardPagerAdapter()

        TeamType.values().forEach { type ->
            when(type) {
                TeamType.BASEBALL -> {
                    mCardAdapter.addCardItem(TeamTypeCardItem(R.string.title_baseball, R.drawable.image_baseball_ball))
                }
                TeamType.SOFTBALL -> {
                    mCardAdapter.addCardItem(TeamTypeCardItem(R.string.title_softball, R.drawable.image_softball_ball))
                }
                else -> {

                }
            }
        }

        mViewPager.adapter = mCardAdapter
        mViewPager.offscreenPageLimit = 3

        mViewPager.addOnPageChangeListener(this)

        val shadowTransformer = ShadowTransformer(mViewPager, mCardAdapter)
        mViewPager.addOnPageChangeListener(shadowTransformer)

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

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        Timber.d("onPageSelected position=$position")
        viewModel.setTeamType(position)
    }
}