package com.telen.easylineup.team.createTeam.teamType

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.databinding.FragmentTeamTypeBinding
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.team.createTeam.SetupViewModel
import timber.log.Timber

class TeamTypeFragment : BaseFragment("TeamTypeFragment") {

    private val viewModel by activityViewModels<SetupViewModel>()
    private val mCards = mutableListOf<TeamTypeCardItem>()
    private var binder: FragmentTeamTypeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCards.addAll(viewModel.getTeamTypeCardItems())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binder = FragmentTeamTypeBinding.inflate(inflater, container, false)
        this.binder = binder

        val viewPager = binder.teamTypeViewPager.apply {
            adapter = CardPagerAdapter(mCards)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    Timber.d("onPageSelected position=$position")
                    viewModel.setTeamType(position)
                }
            })
            setPageTransformer(ZoomOutPageTransformer())
        }

        viewModel.observeTeamType().observe(viewLifecycleOwner) {
            TeamType.getTypeById(it).let { type -> viewPager.currentItem = type.position }
        }

        return binder.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder = null
    }
}