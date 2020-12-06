package com.telen.easylineup.domain.model

data class TournamentStatsUIConfig(val leftHeader: List<Pair<String, Int>>,
                                   val topHeader: List<Pair<String, Int>>,
                                   val mainTable: List<List<Pair<String, Int>>>,
                                   val columnToHighlight: List<Int>,
                                   val topLeftCell: List<String>?)