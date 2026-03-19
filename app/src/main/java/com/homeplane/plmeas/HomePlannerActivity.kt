package com.homeplane.plmeas

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
import com.homeplane.plmeas.bfg.HomePlannerGlobalLayoutUtil
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication
import com.homeplane.plmeas.bfg.presentation.pushhandler.HomePlannerPushHandler
import com.homeplane.plmeas.bfg.homePlannerSetupSystemBars
import org.koin.android.ext.android.inject

class HomePlannerActivity : AppCompatActivity() {

    private val homePlannerPushHandler by inject<HomePlannerPushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homePlannerSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_home_planner)
        val homePlannerRootView = findViewById<View>(android.R.id.content)
        HomePlannerGlobalLayoutUtil().homePlannerAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(homePlannerRootView) { homePlannerView, homePlannerInsets ->
            val homePlannerSystemBars = homePlannerInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val homePlannerDisplayCutout = homePlannerInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val homePlannerIme = homePlannerInsets.getInsets(WindowInsetsCompat.Type.ime())


            val homePlannerTopPadding = maxOf(homePlannerSystemBars.top, homePlannerDisplayCutout.top)
            val homePlannerLeftPadding = maxOf(homePlannerSystemBars.left, homePlannerDisplayCutout.left)
            val homePlannerRightPadding = maxOf(homePlannerSystemBars.right, homePlannerDisplayCutout.right)
            window.setSoftInputMode(HomePlannerApplication.homePlannerInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "ADJUST PUN")
                val homePlannerBottomInset = maxOf(homePlannerSystemBars.bottom, homePlannerDisplayCutout.bottom)

                homePlannerView.setPadding(homePlannerLeftPadding, homePlannerTopPadding, homePlannerRightPadding, 0)

                homePlannerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = homePlannerBottomInset
                }
            } else {
                Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "ADJUST RESIZE")

                val homePlannerBottomInset = maxOf(homePlannerSystemBars.bottom, homePlannerDisplayCutout.bottom, homePlannerIme.bottom)

                homePlannerView.setPadding(homePlannerLeftPadding, homePlannerTopPadding, homePlannerRightPadding, 0)

                homePlannerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = homePlannerBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Activity onCreate()")
        homePlannerPushHandler.homePlannerHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            homePlannerSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        homePlannerSetupSystemBars()
    }
}