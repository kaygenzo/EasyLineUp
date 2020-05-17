package com.telen.easylineup.lineup

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.utils.NavigationUtils
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_lineup_edition.view.*
import kotlinx.android.synthetic.main.fragment_lineup_fixed.view.lineupTabLayout
import kotlinx.android.synthetic.main.fragment_lineup_fixed.view.viewpager
import timber.log.Timber

class LineupFragment: BaseFragment(), CompoundButton.OnCheckedChangeListener {

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
        viewModel.editable = arguments?.getBoolean(Constants.EXTRA_EDITABLE, false) ?: false

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(viewModel.getLayout(), container, false)

        activity?.run {
            pagerAdapter = LineupPagerAdapter(this, childFragmentManager, viewModel.editable)
            view.viewpager?.let { pager ->
                pager.adapter = pagerAdapter
                view.lineupTabLayout?.setupWithViewPager(view.viewpager)
            }

            viewModel.getLineupName().observe(viewLifecycleOwner, Observer {
                (this as HomeActivity).supportActionBar?.title = it
            })

            viewModel.registerLineupChange().observe(viewLifecycleOwner,  Observer {
                //some layouts don't have this checkbox
                view.changeModeCheckBox?.let { view ->
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

            viewModel.getDesignatedPlayerLabel(this).observe(viewLifecycleOwner, Observer {
                view.changeModeCheckBox?.text = it
            })

            viewModel.eventHandler.observe(viewLifecycleOwner, Observer {
                when(it) {
                    DeleteLineupSuccess -> {
                        this.runOnUiThread {
                            findNavController().popBackStack(R.id.navigation_lineups, false)
                        }
                    }
                    else -> {}
                }
            })
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!viewModel.editable)
            inflater.inflate(R.menu.menu_lineup_summary, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_edit -> {
                val extras = Bundle()
                extras.putBoolean(Constants.EXTRA_EDITABLE, true)
                extras.putLong(Constants.LINEUP_ID, viewModel.lineupID ?: 0)
                extras.putString(Constants.LINEUP_TITLE, viewModel.lineupTitle)
                findNavController().navigate(R.id.lineupFragment, extras, NavigationUtils().getOptions())
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
            val disposable = viewModel.exportLineupToExternalStorage(it, pagerAdapter.getMapFragment())
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