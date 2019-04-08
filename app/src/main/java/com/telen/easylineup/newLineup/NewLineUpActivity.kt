package com.telen.easylineup.newLineup

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.new_lineup_activity.*

class NewLineUpActivity: AppCompatActivity() {

    lateinit var pagerAdapter: LineUpPagerAdapter
    lateinit var playersPositionViewModel: PlayersPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_lineup_activity)

        playersPositionViewModel = ViewModelProviders.of(this).get(PlayersPositionViewModel::class.java)

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