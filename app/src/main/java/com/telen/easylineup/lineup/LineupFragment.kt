package com.telen.easylineup.lineup

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.lineup.attack.AttackFragment
import com.telen.easylineup.lineup.defense.DefenseFragmentEditable
import com.telen.easylineup.utils.NavigationUtils
import timber.log.Timber

class LineupFragmentFixed: LineupFragment("LineupFragmentFixed", R.layout.fragment_lineup_fixed, false) {

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_lineup_summary, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun getRootScreen(): View? {
        return view?.findViewById(R.id.lineupFixedRootView) ?: run {
            val index = getExportType()
            return pagerAdapter.getMapFragment()[index]
        }
    }

    override fun getExportType(): Int {
        return view?.findViewById<ViewPager2>(R.id.viewpager)?.currentItem ?: -1
    }

}

class LineupFragmentEditable: LineupFragment("LineupFragmentEditable", R.layout.fragment_lineup_edition, true) {
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_lineup_edition, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {

        val teamTypeDisposable = viewModel.getTeamType()
                .subscribe({
                    val teamType = TeamType.getTypeById(it)
                    val item = menu.findItem(R.id.action_lineup_mode)
                    when(teamType) {
                        TeamType.UNKNOWN -> item.isVisible = false
                        TeamType.BASEBALL -> {
                            menu.findItem(R.id.action_lineup_mode).setTitle(R.string.action_add_dh)
                        }
                        TeamType.SOFTBALL -> {
                            menu.findItem(R.id.action_lineup_mode).setTitle(R.string.action_add_dp_flex)
                        }
                    }
                }, {
                    Timber.e(it)
                })
        disposables.add(teamTypeDisposable)

        menu.findItem(R.id.action_lineup_mode).isChecked = viewModel.lineupMode == MODE_ENABLED

        super.onPrepareOptionsMenu(menu)
    }
}

abstract class LineupFragment(fragmentName: String, @LayoutRes private val layout: Int, private val isEditable: Boolean): BaseFragment(fragmentName) {

    companion object {
        const val REQUEST_WRITE_EXTERENAL_STORAGE_PERMISSION = 0

        fun getArguments(lineupID: Long, lineupTitle: String): Bundle {
            val extras = Bundle()
            extras.putLong(Constants.LINEUP_ID, lineupID)
            extras.putString(Constants.LINEUP_TITLE, lineupTitle)
            return extras
        }
    }

    lateinit var pagerAdapter: LineupPagerAdapter
    lateinit var viewModel: PlayersPositionViewModel

    open fun getRootScreen(): View? {
        return null
    }

    open fun getExportType(): Int {
        return -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(PlayersPositionViewModel::class.java)
        viewModel.lineupID = arguments?.getLong(Constants.LINEUP_ID, 0) ?: 0
        viewModel.lineupTitle = arguments?.getString(Constants.LINEUP_TITLE) ?: ""
        viewModel.editable = isEditable
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layout, container, false)

        initObserver()

        activity?.let { activity ->

            pagerAdapter = LineupPagerAdapter(this, viewModel.editable)
            view.findViewById<ViewPager2>(R.id.viewpager)?.let { pager ->

                childFragmentManager.fragments.filterIsInstance<DefenseFragmentEditable>().forEach {
                    childFragmentManager.beginTransaction().remove(it).commit()
                }

                childFragmentManager.fragments.filterIsInstance<AttackFragment>().forEach {
                    childFragmentManager.beginTransaction().remove(it).commit()
                }

                pager.isSaveEnabled = false
                pager.adapter = pagerAdapter
                val tabLayout = view.findViewById<TabLayout>(R.id.lineupTabLayout)
                TabLayoutMediator(tabLayout, pager) { tab, position ->
                    tab.text = when(position) {
                        FRAGMENT_DEFENSE_INDEX -> getString(R.string.new_lineup_tab_field_defense)
                        FRAGMENT_ATTACK_INDEX -> getString(R.string.new_lineup_tab_field_attack)
                        else -> ""
                    }
                }.attach()
            }

            view.findViewById<ConstraintLayout>(R.id.fragment_defense_edition)?.let {
                childFragmentManager.fragments.filterIsInstance<DefenseFragmentEditable>().lastOrNull()?.let {
                    if (!it.isRemoving) {
                        childFragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment_defense_edition, it)
                                .commit()
                    }
                } ?: run {
                    childFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_defense_edition, DefenseFragmentEditable())
                            .commit()
                }
            }

            view.findViewById<ConstraintLayout>(R.id.fragment_attack)?.let {
                childFragmentManager.fragments.filterIsInstance<AttackFragment>().lastOrNull()?.let {
                    if (!it.isRemoving) {
                        childFragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment_attack, it)
                                .commit()
                    }
                } ?: run {
                    childFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_attack, AttackFragment())
                            .commit()
                }
            }

            viewModel.getLineupName().observe(viewLifecycleOwner, Observer {
                (activity as HomeActivity).supportActionBar?.title = it
            })

            viewModel.registerLineupAndPositionsChanged().observe(viewLifecycleOwner, Observer {
                val size = it.filter { item -> item.position == FieldPosition.SUBSTITUTE.id
                        && item.fieldPositionID > 0 }.size
                val substituteIndication = view.findViewById<MaterialTextView>(R.id.substitutesIndication)
                substituteIndication?.text = resources.getQuantityString(R.plurals.lineups_substitutes_size, size, size)
            })
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
        view?.findViewById<ViewPager2>(R.id.viewpager)?.let { pager ->
            pager.adapter = null
        }
        disposables.clear()
    }

    private fun initObserver() {
        val disposable = viewModel.observeErrors().subscribe({
            when(it) {
                DomainErrors.DELETE_LINEUP_FAILED -> {
                    activity?.run {
                        Toast.makeText(this, R.string.error_when_deleting_lineup, Toast.LENGTH_LONG).show()
                    }
                }
                else -> {}
            }
        }, {
            Timber.e(it)
        })
        disposables.add(disposable)

        val eventDisposable = viewModel.eventHandler.subscribe({
            when(it) {
                DeleteLineupSuccess -> {
                    activity?.run {
                        this.runOnUiThread {
                            findNavController().popBackStack(R.id.navigation_lineups, false)
                        }
                    }
                }
                else -> {}
            }
        }, {
            Timber.e(it)
        })
        disposables.add(eventDisposable)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_edit -> {
                val extras = Companion.getArguments(viewModel.lineupID ?: 0,  viewModel.lineupTitle ?: "")
                findNavController().navigate(R.id.lineupFragmentEditable, extras, NavigationUtils().getOptions())
                true
            }
            R.id.action_delete -> {
                askUserConsentForDelete()
                true
            }
            R.id.action_share -> {
                exportLineupToExternalStorage()
                true
            }
            R.id.action_roster -> {
                showLineupRosterScreen()
                true
            }
            R.id.action_lineup_mode -> {
                if(BuildConfig.DEBUG)
                    Toast.makeText(activity, "Mode is ${item.isChecked}", Toast.LENGTH_SHORT).show()
                viewModel.onLineupModeChanged(!item.isChecked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun askUserConsentForDelete() {
        activity?.let {
            viewModel.getUserDeleteConsentDialog(it).show()
        }
    }

    private fun exportLineupToExternalStorage() {
        activity?.let {
            val disposable = viewModel.exportLineupToExternalStorage(it, getRootScreen(), getExportType())
                    .subscribe({ intent ->
                        startActivity(Intent.createChooser(intent, ""))
                    }, { error ->
                        if(error is InsufficientPermissions) {
                            requestPermissions(error.permissionsNeeded, REQUEST_WRITE_EXTERENAL_STORAGE_PERMISSION)
                        }
                        else {
                            Timber.e(error)
                        }
                    })
            disposables.add(disposable)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_WRITE_EXTERENAL_STORAGE_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    exportLineupToExternalStorage()
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun showLineupRosterScreen() {
        val bundle = Bundle()
        bundle.putLong(Constants.LINEUP_ID, viewModel.lineupID ?: 0)
        findNavController().navigate(R.id.lineupRosterFragment, bundle, NavigationUtils().getOptions())
    }
}