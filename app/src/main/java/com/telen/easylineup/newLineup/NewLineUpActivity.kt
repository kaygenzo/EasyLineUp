package com.telen.easylineup.newLineup

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.utils.Constants
import kotlinx.android.synthetic.main.new_lineup_activity.*

class NewLineUpActivity: AppCompatActivity() {

    lateinit var pagerAdapter: LineUpPagerAdapter
    lateinit var playersPositionViewModel: PlayersPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_lineup_activity)

        playersPositionViewModel = ViewModelProviders.of(this).get(PlayersPositionViewModel::class.java)
        playersPositionViewModel.lineupID = intent.getLongExtra(Constants.LINEUP_ID, 0)
        playersPositionViewModel.teamID = intent.getLongExtra(Constants.TEAM_ID, 0)

        pagerAdapter = LineUpPagerAdapter(this, supportFragmentManager)
        viewpager.adapter = pagerAdapter
        newLineUpTabLayout.setupWithViewPager(viewpager)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}