package com.telen.easylineup.lineup

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.utils.NavigationUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_lineup_fixed.view.*

class LineupFragment: Fragment() {

    lateinit var pagerAdapter: LineupPagerAdapter
    lateinit var viewModel: PlayersPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(this).get(PlayersPositionViewModel::class.java)
        viewModel.lineupID = arguments?.getLong(Constants.LINEUP_ID, 0) ?: 0
        viewModel.lineupTitle = arguments?.getString(Constants.LINEUP_TITLE) ?: ""
        viewModel.editable = arguments?.getBoolean(Constants.EXTRA_EDITABLE, false) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = when(viewModel.editable) {
            true -> inflater.inflate(R.layout.fragment_lineup_edition, container, false)
            false -> inflater.inflate(R.layout.fragment_lineup_fixed, container, false)
        }

        activity?.let { activity ->
            pagerAdapter = LineupPagerAdapter(activity, childFragmentManager, viewModel.editable)
            view.viewpager?.let { pager ->
                pager.adapter = pagerAdapter
                view.lineupTabLayout?.let { tabLayout ->
                    tabLayout.setupWithViewPager(view.viewpager)
                }
            }

            if (viewModel.editable) {
                (activity as HomeActivity).supportActionBar?.title = getString(R.string.title_lineup_edition)
            }
            else {
                (activity as HomeActivity).supportActionBar?.title = ""
            }
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!viewModel.editable)
            inflater.inflate(R.menu.lineup_edit_menu, menu)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun askUserConsentForDelete() {
        AlertDialog.Builder(activity as AppCompatActivity)
                .setCancelable(true)
                .setTitle(R.string.dialog_delete_lineup_title)
                .setMessage(R.string.dialog_delete_cannot_undo_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.deleteLineup()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                findNavController().popBackStack(R.id.navigation_lineups, false)
                            }, {
                                Toast.makeText(activity, "Something wrong happened: ${it.message}", Toast.LENGTH_LONG).show()
                            })
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
    }
}