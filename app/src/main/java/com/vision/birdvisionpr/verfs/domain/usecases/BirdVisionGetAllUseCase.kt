package com.vision.birdvisionpr.verfs.domain.usecases

import android.util.Log
import com.vision.birdvisionpr.verfs.data.repo.BirdVisionRepository
import com.vision.birdvisionpr.verfs.data.utils.BirdVisionPushToken
import com.vision.birdvisionpr.verfs.data.utils.BirdVisionSystemService
import com.vision.birdvisionpr.verfs.domain.model.BirdVisionEntity
import com.vision.birdvisionpr.verfs.domain.model.BirdVisionParam
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication

class BirdVisionGetAllUseCase(
    private val birdVisionRepository: BirdVisionRepository,
    private val birdVisionSystemService: BirdVisionSystemService,
    private val birdVisionPushToken: BirdVisionPushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : BirdVisionEntity?{
        val params = BirdVisionParam(
            birdVisionLocale = birdVisionSystemService.birdVisionGetLocale(),
            birdVisionPushToken = birdVisionPushToken.birdVisionGetToken(),
            birdVisionAfId = birdVisionSystemService.birdVisionGetAppsflyerId()
        )
        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Params for request: $params")
        return birdVisionRepository.birdVisionGetClient(params, conversion)
    }



}