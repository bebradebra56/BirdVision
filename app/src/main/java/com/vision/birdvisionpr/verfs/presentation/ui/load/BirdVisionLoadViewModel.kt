package com.vision.birdvisionpr.verfs.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vision.birdvisionpr.verfs.data.shar.BirdVisionSharedPreference
import com.vision.birdvisionpr.verfs.data.utils.BirdVisionSystemService
import com.vision.birdvisionpr.verfs.domain.usecases.BirdVisionGetAllUseCase
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionAppsFlyerState
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BirdVisionLoadViewModel(
    private val birdVisionGetAllUseCase: BirdVisionGetAllUseCase,
    private val birdVisionSharedPreference: BirdVisionSharedPreference,
    private val birdVisionSystemService: BirdVisionSystemService
) : ViewModel() {

    private val _birdVisionHomeScreenState: MutableStateFlow<BirdVisionHomeScreenState> =
        MutableStateFlow(BirdVisionHomeScreenState.BirdVisionLoading)
    val birdVisionHomeScreenState = _birdVisionHomeScreenState.asStateFlow()

    private var birdVisionGetApps = false


    init {
        viewModelScope.launch {
            when (birdVisionSharedPreference.birdVisionAppState) {
                0 -> {
                    if (birdVisionSystemService.birdVisionIsOnline()) {
                        BirdVisionApplication.birdVisionConversionFlow.collect {
                            when(it) {
                                BirdVisionAppsFlyerState.BirdVisionDefault -> {}
                                BirdVisionAppsFlyerState.BirdVisionError -> {
                                    birdVisionSharedPreference.birdVisionAppState = 2
                                    _birdVisionHomeScreenState.value =
                                        BirdVisionHomeScreenState.BirdVisionError
                                    birdVisionGetApps = true
                                }
                                is BirdVisionAppsFlyerState.BirdVisionSuccess -> {
                                    if (!birdVisionGetApps) {
                                        birdVisionGetData(it.birdVisionData)
                                        birdVisionGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _birdVisionHomeScreenState.value =
                            BirdVisionHomeScreenState.BirdVisionNotInternet
                    }
                }
                1 -> {
                    if (birdVisionSystemService.birdVisionIsOnline()) {
                        if (BirdVisionApplication.BIRD_VISION_FB_LI != null) {
                            _birdVisionHomeScreenState.value =
                                BirdVisionHomeScreenState.BirdVisionSuccess(
                                    BirdVisionApplication.BIRD_VISION_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > birdVisionSharedPreference.birdVisionExpired) {
                            Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Current time more then expired, repeat request")
                            BirdVisionApplication.birdVisionConversionFlow.collect {
                                when(it) {
                                    BirdVisionAppsFlyerState.BirdVisionDefault -> {}
                                    BirdVisionAppsFlyerState.BirdVisionError -> {
                                        _birdVisionHomeScreenState.value =
                                            BirdVisionHomeScreenState.BirdVisionSuccess(
                                                birdVisionSharedPreference.birdVisionSavedUrl
                                            )
                                        birdVisionGetApps = true
                                    }
                                    is BirdVisionAppsFlyerState.BirdVisionSuccess -> {
                                        if (!birdVisionGetApps) {
                                            birdVisionGetData(it.birdVisionData)
                                            birdVisionGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Current time less then expired, use saved url")
                            _birdVisionHomeScreenState.value =
                                BirdVisionHomeScreenState.BirdVisionSuccess(
                                    birdVisionSharedPreference.birdVisionSavedUrl
                                )
                        }
                    } else {
                        _birdVisionHomeScreenState.value =
                            BirdVisionHomeScreenState.BirdVisionNotInternet
                    }
                }
                2 -> {
                    _birdVisionHomeScreenState.value =
                        BirdVisionHomeScreenState.BirdVisionError
                }
            }
        }
    }


    private suspend fun birdVisionGetData(conversation: MutableMap<String, Any>?) {
        val birdVisionData = birdVisionGetAllUseCase.invoke(conversation)
        if (birdVisionSharedPreference.birdVisionAppState == 0) {
            if (birdVisionData == null) {
                birdVisionSharedPreference.birdVisionAppState = 2
                _birdVisionHomeScreenState.value =
                    BirdVisionHomeScreenState.BirdVisionError
            } else {
                birdVisionSharedPreference.birdVisionAppState = 1
                birdVisionSharedPreference.apply {
                    birdVisionExpired = birdVisionData.birdVisionExpires
                    birdVisionSavedUrl = birdVisionData.birdVisionUrl
                }
                _birdVisionHomeScreenState.value =
                    BirdVisionHomeScreenState.BirdVisionSuccess(birdVisionData.birdVisionUrl)
            }
        } else  {
            if (birdVisionData == null) {
                _birdVisionHomeScreenState.value =
                    BirdVisionHomeScreenState.BirdVisionSuccess(birdVisionSharedPreference.birdVisionSavedUrl)
            } else {
                birdVisionSharedPreference.apply {
                    birdVisionExpired = birdVisionData.birdVisionExpires
                    birdVisionSavedUrl = birdVisionData.birdVisionUrl
                }
                _birdVisionHomeScreenState.value =
                    BirdVisionHomeScreenState.BirdVisionSuccess(birdVisionData.birdVisionUrl)
            }
        }
    }


    sealed class BirdVisionHomeScreenState {
        data object BirdVisionLoading : BirdVisionHomeScreenState()
        data object BirdVisionError : BirdVisionHomeScreenState()
        data class BirdVisionSuccess(val data: String) : BirdVisionHomeScreenState()
        data object BirdVisionNotInternet: BirdVisionHomeScreenState()
    }
}