package com.telen.easylineup.lineup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.utils.Constants
import kotlinx.android.synthetic.main.activity_lineup.*

class LineupActivity: AppCompatActivity() {

    lateinit var pagerAdapter: LineupPagerAdapter
    lateinit var viewModel: PlayersPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lineup)

        viewModel = ViewModelProviders.of(this).get(PlayersPositionViewModel::class.java)
        viewModel.lineupID = intent.getLongExtra(Constants.LINEUP_ID, 0)
        viewModel.lineupTitle = intent.getStringExtra(Constants.LINEUP_TITLE)
        viewModel.editable = intent.getBooleanExtra(Constants.EXTRA_EDITABLE, false)

        pagerAdapter = LineupPagerAdapter(this, supportFragmentManager, viewModel.editable)
        viewpager.adapter = pagerAdapter
        lineupTabLayout.setupWithViewPager(viewpager)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (viewModel.editable) {
            supportActionBar?.title = getString(R.string.title_lineup_edition)
        }
        else {
            supportActionBar?.title = ""
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!viewModel.editable)
            menuInflater.inflate(R.menu.lineup_edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_edit -> {
                val intent = Intent(this, LineupActivity::class.java)
                intent.putExtra(Constants.EXTRA_EDITABLE, true)
                intent.putExtra(Constants.LINEUP_ID, viewModel.lineupID)
                intent.putExtra(Constants.LINEUP_TITLE, viewModel.lineupTitle)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}