package com.homeplane.plmeas.bfg

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication

class HomePlannerGlobalLayoutUtil {

    private var homePlannerMChildOfContent: View? = null
    private var homePlannerUsableHeightPrevious = 0

    fun homePlannerAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        homePlannerMChildOfContent = content.getChildAt(0)

        homePlannerMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val homePlannerUsableHeightNow = homePlannerComputeUsableHeight()
        if (homePlannerUsableHeightNow != homePlannerUsableHeightPrevious) {
            val homePlannerUsableHeightSansKeyboard = homePlannerMChildOfContent?.rootView?.height ?: 0
            val homePlannerHeightDifference = homePlannerUsableHeightSansKeyboard - homePlannerUsableHeightNow

            if (homePlannerHeightDifference > (homePlannerUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(HomePlannerApplication.homePlannerInputMode)
            } else {
                activity.window.setSoftInputMode(HomePlannerApplication.homePlannerInputMode)
            }
//            mChildOfContent?.requestLayout()
            homePlannerUsableHeightPrevious = homePlannerUsableHeightNow
        }
    }

    private fun homePlannerComputeUsableHeight(): Int {
        val r = Rect()
        homePlannerMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}