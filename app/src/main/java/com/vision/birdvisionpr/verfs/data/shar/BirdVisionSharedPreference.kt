package com.vision.birdvisionpr.verfs.data.shar

import android.content.Context
import androidx.core.content.edit

class BirdVisionSharedPreference(context: Context) {
    private val birdVisionPrefs = context.getSharedPreferences("birdVisionSharedPrefsAb", Context.MODE_PRIVATE)

    var birdVisionSavedUrl: String
        get() = birdVisionPrefs.getString(BIRD_VISION_SAVED_URL, "") ?: ""
        set(value) = birdVisionPrefs.edit { putString(BIRD_VISION_SAVED_URL, value) }

    var birdVisionExpired : Long
        get() = birdVisionPrefs.getLong(BIRD_VISION_EXPIRED, 0L)
        set(value) = birdVisionPrefs.edit { putLong(BIRD_VISION_EXPIRED, value) }

    var birdVisionAppState: Int
        get() = birdVisionPrefs.getInt(BIRD_VISION_APPLICATION_STATE, 0)
        set(value) = birdVisionPrefs.edit { putInt(BIRD_VISION_APPLICATION_STATE, value) }

    var birdVisionNotificationRequest: Long
        get() = birdVisionPrefs.getLong(BIRD_VISION_NOTIFICAITON_REQUEST, 0L)
        set(value) = birdVisionPrefs.edit { putLong(BIRD_VISION_NOTIFICAITON_REQUEST, value) }

    var birdVisionNotificationState:Int
        get() = birdVisionPrefs.getInt(BIRD_VISION_NOTIFICATION_STATE, 0)
        set(value) = birdVisionPrefs.edit { putInt(BIRD_VISION_NOTIFICATION_STATE, value) }

    companion object {
        private const val BIRD_VISION_NOTIFICATION_STATE = "birdVisionNotificationState"
        private const val BIRD_VISION_SAVED_URL = "birdVisionSavedUrl"
        private const val BIRD_VISION_EXPIRED = "birdVisionExpired"
        private const val BIRD_VISION_APPLICATION_STATE = "birdVisionApplicationState"
        private const val BIRD_VISION_NOTIFICAITON_REQUEST = "birdVisionNotificationRequest"
    }
}