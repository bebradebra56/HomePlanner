package com.homeplane.plmeas.bfg.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication

class HomePlannerPushHandler {
    fun homePlannerHandlePush(extras: Bundle?) {
        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map = homePlannerBundleToMap(extras)
            Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Map from Push = $map")
            map?.let {
                if (map.containsKey("url")) {
                    HomePlannerApplication.HOME_PLANNER_FB_LI = map["url"]
                    Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Push data no!")
        }
    }

    private fun homePlannerBundleToMap(extras: Bundle): Map<String, String?>? {
        val map: MutableMap<String, String?> = HashMap()
        val ks = extras.keySet()
        val iterator: Iterator<String> = ks.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = extras.getString(key)
        }
        return map
    }

}