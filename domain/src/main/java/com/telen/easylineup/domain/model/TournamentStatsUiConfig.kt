/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.model

/**
 * @property leftHeader
 * @property topHeader
 * @property mainTable
 * @property columnToHighlight
 * @property topLeftCell
 */
data class TournamentStatsUiConfig(
    val leftHeader: List<Pair<String, Int>>,
    val topHeader: List<Pair<String, Int>>,
    val mainTable: List<List<Pair<String, Int>>>,
    val columnToHighlight: List<Int>,
    val topLeftCell: List<String>?
)
