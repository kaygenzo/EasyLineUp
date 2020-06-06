package com.telen.easylineup.lineup

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.switchmaterial.SwitchMaterial
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
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.utils.NavigationUtils
import timber.log.Timber

class LineupFragmentFixed: LineupFragment(R.layout.fragment_lineup_fixed, false) {
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_lineup_summary, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}

class LineupFragmentEditable: LineupFragment(R.layout.fragment_lineup_edition, true)

abstract class LineupFragment(@LayoutRes private val layout: Int, private val isEditable: Boolean): BaseFragment(), CompoundButton.OnCheckedChangeListener {

    companion object {
        const val REQUEST_WRITE_EXTERENAL_STORAGE_PERMISSION = 0
    }

    lateinit var pagerAdapter: LineupPagerAdapter
    lateinit var viewModel: PlayersPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(this).get(PlayersPositionViewModel::class.java)
        viewModel.lineupID = arguments?.getLong(Constants.LINEUP_ID, 0) ?: 0
        viewModel.lineupTitle = arguments?.getString(Constants.LINEUP_TITLE) ?: ""
        viewModel.editable = isEditable

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layout, container, false)

        activity?.let { activity ->
            pagerAdapter = LineupPagerAdapter(this, viewModel.editable)
            view.findViewById<ViewPager2>(R.id.viewpager)?.let { pager ->
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

            viewModel.getLineupName().observe(viewLifecycleOwner, Observer {
                (activity as HomeActivity).supportActionBar?.title = it
            })

            viewModel.registerLineupChange().observe(viewLifecycleOwner,  Observer {
                //some layouts don't have this checkbox
                val switch = view.findViewById<SwitchMaterial>(R.id.changeModeCheckBox)
                switch?.let { view ->
                    view.apply {
                        setOnCheckedChangeListener(null)
                        when(it.mode) {
                            MODE_DISABLED -> {
                                isChecked = false
                            }
                            MODE_ENABLED -> {
                                isChecked = true
                            }
                        }
                        setOnCheckedChangeListener(this@LineupFragment)
                    }
                }
            })

            viewModel.registerLineupAndPositionsChanged().observe(viewLifecycleOwner, Observer {
                val size = it.filter { item -> item.position == FieldPosition.SUBSTITUTE.position
                        && item.fieldPositionID > 0 }.size
                val substituteIndication = view.findViewById<MaterialTextView>(R.id.substitutesIndication)
                substituteIndication?.text = resources.getQuantityString(R.plurals.lineups_substitutes_size, size, size)
            })

            viewModel.getDesignatedPlayerLabel(activity).observe(viewLifecycleOwner, Observer {
                val switch = view.findViewById<SwitchMaterial>(R.id.changeModeCheckBox)
                switch?.text = it
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_edit -> {
                val extras = Bundle()
                extras.putLong(Constants.LINEUP_ID, viewModel.lineupID ?: 0)
                extras.putString(Constants.LINEUP_TITLE, viewModel.lineupTitle)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun askUserConsentForDelete() {
        activity?.let {
            viewModel.getUserDeleteConsentDialog(it).show()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(BuildConfig.DEBUG)
            Toast.makeText(activity, "Mode is $isChecked", Toast.LENGTH_SHORT).show()
        viewModel.onLineupModeChanged(isChecked)
    }

    private fun exportLineupToExternalStorage() {
        activity?.let {
            val index = view?.findViewById<ViewPager2>(R.id.viewpager)?.currentItem ?: 0
            val currentView = pagerAdapter.getMapFragment()[index]
            val disposable = viewModel.exportLineupToExternalStorage(it, currentView, index)
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
}