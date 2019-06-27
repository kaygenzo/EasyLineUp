package com.telen.easylineup.lineup

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.utils.NavigationUtils
import kotlinx.android.synthetic.main.fragment_lineup.view.*

class LineupFragment: Fragment() {

    lateinit var pagerAdapter: LineupPagerAdapter
    lateinit var viewModel: PlayersPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup, container, false)

        viewModel = ViewModelProviders.of(activity as HomeActivity).get(PlayersPositionViewModel::class.java)
        viewModel.lineupID = arguments?.getLong(Constants.LINEUP_ID, 0) ?: 0
        viewModel.lineupTitle = arguments?.getString(Constants.LINEUP_TITLE) ?: ""
        viewModel.editable = arguments?.getBoolean(Constants.EXTRA_EDITABLE, false) ?: false

        activity?.let { activity ->
            pagerAdapter = LineupPagerAdapter(activity, childFragmentManager, viewModel.editable)
            view.viewpager.adapter = pagerAdapter
            view.lineupTabLayout.setupWithViewPager(view.viewpager)

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
//                val intent = Intent(this, LineupActivity::class.java)
//                intent.putExtra(Constants.EXTRA_EDITABLE, true)
//                intent.putExtra(Constants.LINEUP_ID, viewModel.lineupID)
//                intent.putExtra(Constants.LINEUP_TITLE, viewModel.lineupTitle)
//                startActivity(intent)
                val extras = Bundle()
                extras.putBoolean(Constants.EXTRA_EDITABLE, true)
                extras.putLong(Constants.LINEUP_ID, viewModel.lineupID ?: 0)
                extras.putString(Constants.LINEUP_TITLE, viewModel.lineupTitle)
                findNavController().navigate(R.id.lineupFragment, extras, NavigationUtils().getOptions())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}