package com.telen.easylineup.recentActivity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.telen.easylineup.R
import com.telen.easylineup.newLineup.NewLineUpActivity
import kotlinx.android.synthetic.main.recent_activity_fragment.view.*

class RecentActivityFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.recent_activity_fragment, container, false)
        view.fab.setOnClickListener {
            val intent = Intent(activity, NewLineUpActivity::class.java)
            startActivity(intent)
        }
        return view
    }
}