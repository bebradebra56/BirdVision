package com.vision.birdvisionpr

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.vision.birdvisionpr.verfs.BirdVisionGlobalLayoutUtil
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication
import com.vision.birdvisionpr.verfs.presentation.pushhandler.BirdVisionPushHandler
import com.vision.birdvisionpr.verfs.birdVisionSetupSystemBars
import org.koin.android.ext.android.inject

class BirdVisionActivity : AppCompatActivity() {

    private val birdVisionPushHandler by inject<BirdVisionPushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        birdVisionSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_bird_vision)

        val birdVisionRootView = findViewById<View>(android.R.id.content)
        BirdVisionGlobalLayoutUtil().birdVisionAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(birdVisionRootView) { birdVisionView, birdVisionInsets ->
            val birdVisionSystemBars = birdVisionInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val birdVisionDisplayCutout = birdVisionInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val birdVisionIme = birdVisionInsets.getInsets(WindowInsetsCompat.Type.ime())


            val birdVisionTopPadding = maxOf(birdVisionSystemBars.top, birdVisionDisplayCutout.top)
            val birdVisionLeftPadding = maxOf(birdVisionSystemBars.left, birdVisionDisplayCutout.left)
            val birdVisionRightPadding = maxOf(birdVisionSystemBars.right, birdVisionDisplayCutout.right)
            window.setSoftInputMode(BirdVisionApplication.birdVisionInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "ADJUST PUN")
                val birdVisionBottomInset = maxOf(birdVisionSystemBars.bottom, birdVisionDisplayCutout.bottom)

                birdVisionView.setPadding(birdVisionLeftPadding, birdVisionTopPadding, birdVisionRightPadding, 0)

                birdVisionView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = birdVisionBottomInset
                }
            } else {
                Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "ADJUST RESIZE")

                val birdVisionBottomInset = maxOf(birdVisionSystemBars.bottom, birdVisionDisplayCutout.bottom, birdVisionIme.bottom)

                birdVisionView.setPadding(birdVisionLeftPadding, birdVisionTopPadding, birdVisionRightPadding, 0)

                birdVisionView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = birdVisionBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(BirdVisionApplication.BIRD_VISION_MAIN_TAG, "Activity onCreate()")
        birdVisionPushHandler.birdVisionHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            birdVisionSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        birdVisionSetupSystemBars()
    }
}