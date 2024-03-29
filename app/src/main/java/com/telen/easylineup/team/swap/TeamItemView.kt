/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.swap

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.telen.easylineup.R
import com.telen.easylineup.databinding.TeamItemViewBinding
import com.telen.easylineup.views.StateDefense

class TeamItemView : LinearLayout {
    private val binding = TeamItemViewBinding.inflate(LayoutInflater.from(context), this)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setTeamName(teamName: String) {
        binding.name.text = teamName
    }

    fun setImage(stringUri: String?, teamName: String) {
        val size = resources.getDimensionPixelSize(R.dimen.teams_list_icon_size)
        binding.teamIcon.setState(StateDefense.PLAYER)
        binding.teamIcon.setPlayerImage(stringUri, teamName, size)
    }
}
