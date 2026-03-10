package com.vision.birdvisionpr.verfs.data.utils

import android.util.Log
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class BirdVisionPushToken {

    suspend fun birdVisionGetToken(
        birdVisionMaxAttempts: Int = 3,
        birdVisionDelayMs: Long = 1500
    ): String {

        repeat(birdVisionMaxAttempts - 1) {
            try {
                val birdVisionToken = FirebaseMessaging.getInstance().token.await()
                return birdVisionToken
            } catch (e: Exception) {
                Log.e(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Token error (attempt ${it + 1}): ${e.message}")
                delay(birdVisionDelayMs)
            }
        }

        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Token error final: ${e.message}")
            "null"
        }
    }


}