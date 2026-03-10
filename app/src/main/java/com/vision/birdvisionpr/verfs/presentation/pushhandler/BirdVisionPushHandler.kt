package com.vision.birdvisionpr.verfs.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication

class BirdVisionPushHandler {
    fun birdVisionHandlePush(extras: Bundle?) {
        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map: MutableMap<String, String?> = HashMap()
            val ks = extras.keySet()
            val iterator: Iterator<String> = ks.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                map[key] = extras.getString(key)
            }
            Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Map from Push = $map")
            map.let {
                if (map.containsKey("url")) {
                    BirdVisionApplication.BIRD_VISION_FB_LI = map["url"]
                    Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Push data no!")
        }
    }

}