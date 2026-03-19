package com.homeplane.plmeas.bfg.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class HomePlannerDataStore : ViewModel(){
    val homePlannerViList: MutableList<HomePlannerVi> = mutableListOf()
    var homePlannerIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var homePlannerContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var homePlannerView: HomePlannerVi

}