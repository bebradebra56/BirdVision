package com.vision.birdvisionpr.verfs.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class BirdVisionDataStore : ViewModel(){
    val birdVisionViList: MutableList<BirdVisionVi> = mutableListOf()
    var birdVisionIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var birdVisionContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var birdVisionView: BirdVisionVi

}