package com.telen.easylineup.lineup

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.utils.Constants
import kotlinx.android.synthetic.main.activity_lineup.*

class LineupActivity: AppCompatActivity() {

    lateinit var pagerAdapter: LineupPagerAdapter
    lateinit var playersPositionViewModel: PlayersPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lineup)

        val editable = intent.getBooleanExtra(Constants.EXTRA_EDITABLE, false)

        playersPositionViewModel = ViewModelProviders.of(this).get(PlayersPositionViewModel::class.java)
        playersPositionViewModel.lineupID = intent.getLongExtra(Constants.LINEUP_ID, 0)
        playersPositionViewModel.teamID = intent.getLongExtra(Constants.TEAM_ID, 0)
        playersPositionViewModel.lineupTitle = intent.getStringExtra(Constants.LINEUP_TITLE)

        pagerAdapter = LineupPagerAdapter(this, supportFragmentManager, editable)
        viewpager.adapter = pagerAdapter
        lineupTabLayout.setupWithViewPager(viewpager)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }
}