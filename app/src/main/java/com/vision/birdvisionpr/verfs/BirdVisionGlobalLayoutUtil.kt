package com.vision.birdvisionpr.verfs

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication

class BirdVisionGlobalLayoutUtil {

    private var birdVisionMChildOfContent: View? = null
    private var birdVisionUsableHeightPrevious = 0

    fun birdVisionAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        birdVisionMChildOfContent = content.getChildAt(0)

        birdVisionMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val birdVisionUsableHeightNow = birdVisionComputeUsableHeight()
        if (birdVisionUsableHeightNow != birdVisionUsableHeightPrevious) {
            val birdVisionUsableHeightSansKeyboard = birdVisionMChildOfContent?.rootView?.height ?: 0
            val birdVisionHeightDifference = birdVisionUsableHeightSansKeyboard - birdVisionUsableHeightNow

            if (birdVisionHeightDifference > (birdVisionUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(BirdVisionApplication.birdVisionInputMode)
            } else {
                activity.window.setSoftInputMode(BirdVisionApplication.birdVisionInputMode)
            }
//            mChildOfContent?.requestLayout()
            birdVisionUsableHeightPrevious = birdVisionUsableHeightNow
        }
    }

    private fun birdVisionComputeUsableHeight(): Int {
        val r = Rect()
        birdVisionMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}