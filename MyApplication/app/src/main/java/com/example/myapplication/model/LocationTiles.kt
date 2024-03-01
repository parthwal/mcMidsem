package com.example.assignmentonemc.model

import androidx.annotation.StringRes
import com.example.myapplication.R

data class LocationTiles(
    @StringRes
    val stationName: Int,
    @StringRes
    val stationType: Int,
    @StringRes
    val stopDistance: Int,
    @StringRes
    val distance: Int,
    )

var tileList = listOf(
    LocationTiles(R.string.n1, R.string.t1, R.string.s1, R.string.d1),
    LocationTiles(R.string.n2, R.string.t2, R.string.s2, R.string.d2),
    LocationTiles(R.string.n3, R.string.t3, R.string.s3, R.string.d3),
    LocationTiles(R.string.n4, R.string.t4, R.string.s4, R.string.d4),
    LocationTiles(R.string.n5, R.string.t5, R.string.s5, R.string.d5),
    LocationTiles(R.string.n6, R.string.t6, R.string.s6, R.string.d6),
    LocationTiles(R.string.n7, R.string.t7, R.string.s7, R.string.d7),
    LocationTiles(R.string.n8, R.string.t8, R.string.s8, R.string.d8),
    LocationTiles(R.string.n9, R.string.t9, R.string.s9, R.string.d9),
    LocationTiles(R.string.n10, R.string.t10, R.string.s10, R.string.d10)
)
