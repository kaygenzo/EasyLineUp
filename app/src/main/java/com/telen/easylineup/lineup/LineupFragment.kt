package com.telen.easylineup.lineup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentLineupBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.attack.AttackFragment
import com.telen.easylineup.lineup.defense.DefenseFragmentEditable
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber

class LineupFragmentFixed : LineupFragment("LineupFragmentFixed", R.menu.menu_lineup_summary, false)

class LineupFragmentEditable :
    LineupFragment("LineupFragmentEditable", R.menu.menu_lineup_edition, true)

abstract class LineupFragment(
    fragmentName: String,
    @MenuRes private val menuRes: Int,
    private val isEditable: Boolean
) : BaseFragment(fragmentName), MenuProvider {

    private val viewModel by viewModels<LineupViewModel>()
    lateinit var pagerAdapter: LineupPagerAdapter
    private var binder: FragmentLineupBinding? = null

    companion object {
        fun getArguments(lineupID: Long): Bundle {
            val extras = Bundle()
            extras.putLong(Constants.LINEUP_ID, lineupID)
            return extras
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.lineupID = arguments?.getLong(Constants.LINEUP_ID, 0) ?: 0
        viewModel.editable = isEditable
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binder = FragmentLineupBinding.inflate(inflater)
        this.binder = binder

        if (isEditable) {
            binder.substitutesIndication?.visibility = View.GONE
            binder.bottomChoice?.apply {
                visibility = View.VISIBLE
                saveClickListener = View.OnClickListener {
                    launch(viewModel.save(), {
                        Timber.d("Successfully saved")
                        goBack()
                    })
                }
                cancelClickListener = View.OnClickListener { goBack() }
            }

        } else {
            binder.substitutesIndication?.visibility = View.VISIBLE
            binder.bottomChoice?.visibility = View.GONE
        }

        pagerAdapter = LineupPagerAdapter(this, viewModel.editable)
        binder.viewpager?.let { pager ->
            childFragmentManager.fragments.filterIsInstance<DefenseFragmentEditable>().forEach {
                childFragmentManager.beginTransaction().remove(it).commit()
            }

            childFragmentManager.fragments.filterIsInstance<AttackFragment>().forEach {
                childFragmentManager.beginTransaction().remove(it).commit()
            }

            pager.isSaveEnabled = false
            pager.adapter = pagerAdapter
            binder.lineupTabLayout?.let {
                TabLayoutMediator(it, pager) { tab, position ->
                    tab.text = when (position) {
                        FRAGMENT_DEFENSE_INDEX -> getString(R.string.new_lineup_tab_field_defense)
                        FRAGMENT_ATTACK_INDEX -> getString(R.string.new_lineup_tab_field_attack)
                        else -> ""
                    }
                }.attach()
            }

            pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    activity?.let { viewModel.onScreenChanged(position) }
                }
            })
        }

        replaceFragmentIfNeeded(binder.fragmentDefenseEdition, R.id.fragment_defense_edition) {
            DefenseFragmentEditable()
        }

        replaceFragmentIfNeeded(binder.fragmentAttackEdition, R.id.fragment_attack_edition) {
            AttackFragment()
        }

        viewModel.observeLineupName().observe(viewLifecycleOwner) {
            (activity as AppCompatActivity).supportActionBar?.title = it
        }

        viewModel.observeDefensePlayers().observe(viewLifecycleOwner) {
            val size = it.filter { item -> item.isSubstitute() }.size
            binder.substitutesIndication?.text =
                resources.getQuantityString(R.plurals.lineups_substitutes_size, size, size)
        }

        return binder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder?.viewpager?.let { it.adapter = null }
        disposables.clear()
        binder = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(menuRes, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        launch(viewModel.getTeamType(), {
            val teamType = TeamType.getTypeById(it)
            menu.findItem(R.id.action_lineup_mode)?.apply {
                when (teamType) {
                    TeamType.UNKNOWN -> isVisible = false
                    TeamType.BASEBALL -> {
                        setTitle(R.string.action_add_dh)
                    }

                    TeamType.SOFTBALL -> {
                        setTitle(R.string.action_add_dp_flex)
                    }

                    else -> {}
                }
                isChecked = viewModel.lineup?.mode == MODE_ENABLED
            }
        })
        super.onPrepareMenu(menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_lineup_edit")
                val extras = Companion.getArguments(lineupID = viewModel.lineupID ?: 0)
                findNavController().navigate(
                    R.id.lineupFragmentEditable,
                    extras,
                    NavigationUtils().getOptions()
                )
                true
            }

            R.id.action_delete -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_lineup_delete")
                askUserConsentForDelete()
                true
            }

            R.id.action_share -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_lineup_share")
                exportLineupToExternalStorage()
                true
            }

            R.id.action_lineup_mode -> {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(activity, "Mode is ${item.isChecked}", Toast.LENGTH_SHORT).show()
                }
                FirebaseAnalyticsUtils.onClick(activity, "click_lineup_mode")
                viewModel.onLineupModeChanged(!item.isChecked)
                true
            }

            else -> false
        }
    }

    private fun askUserConsentForDelete() {
        activity?.let { ctx ->
            DialogFactory
                .getWarningTaskDialog(context = ctx,
                    title = R.string.dialog_delete_lineup_title,
                    message = R.string.dialog_delete_cannot_undo_message,
                    task = viewModel.deleteLineup()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete { goBack(showFixedView = false) }
                        .doOnError {
                            Toast.makeText(
                                ctx, R.string.error_when_deleting_lineup, Toast.LENGTH_LONG
                            ).show()
                        })
                .show()
        }
    }

    private fun exportLineupToExternalStorage() {
        activity?.let {
            val views = listOf(FRAGMENT_DEFENSE_INDEX, FRAGMENT_ATTACK_INDEX).associateWith {
                pagerAdapter.map[it]?.drawToBitmap()
            }
            launch(viewModel.exportLineupToExternalStorage(it, views), { intent ->
                startActivity(Intent.createChooser(intent, ""))
            })
        }
    }

    private inline fun <reified E : Fragment> replaceFragmentIfNeeded(
        root: View?,
        @IdRes fragmentId: Int,
        create: () -> E
    ) {
        root?.let {
            childFragmentManager.fragments.filterIsInstance<E>()
                .lastOrNull()?.let {
                    if (!it.isRemoving) {
                        childFragmentManager
                            .beginTransaction()
                            .replace(fragmentId, it)
                            .commit()
                    }
                } ?: run {
                childFragmentManager
                    .beginTransaction()
                    .replace(fragmentId, create())
                    .commit()
            }
        }
    }

    private fun goBack(showFixedView: Boolean = true) {
        val isShortcut = arguments?.getBoolean(Constants.EXTRA_IS_FROM_SHORTCUT) ?: false
        if (isShortcut) {
            findNavController().popBackStack(R.id.navigation_home, false)
        } else {
            val success =
                !showFixedView && findNavController().popBackStack(R.id.navigation_lineups, false)
            if (!success) {
                findNavController().popBackStack()
            }
        }
    }
}