package com.homeplane.plmeas.bfg.data.shar

import android.content.Context
import androidx.core.content.edit

class HomePlannerSharedPreference(context: Context) {
    private val homePlannerPrefs = context.getSharedPreferences("homePlannerSharedPrefsAb", Context.MODE_PRIVATE)

    var homePlannerSavedUrl: String
        get() = homePlannerPrefs.getString(HOME_PLANNER_SAVED_URL, "") ?: ""
        set(value) = homePlannerPrefs.edit { putString(HOME_PLANNER_SAVED_URL, value) }

    var homePlannerExpired : Long
        get() = homePlannerPrefs.getLong(HOME_PLANNER_EXPIRED, 0L)
        set(value) = homePlannerPrefs.edit { putLong(HOME_PLANNER_EXPIRED, value) }

    var homePlannerAppState: Int
        get() = homePlannerPrefs.getInt(HOME_PLANNER_APPLICATION_STATE, 0)
        set(value) = homePlannerPrefs.edit { putInt(HOME_PLANNER_APPLICATION_STATE, value) }

    var homePlannerNotificationRequest: Long
        get() = homePlannerPrefs.getLong(HOME_PLANNER_NOTIFICAITON_REQUEST, 0L)
        set(value) = homePlannerPrefs.edit { putLong(HOME_PLANNER_NOTIFICAITON_REQUEST, value) }


    var homePlannerNotificationState:Int
        get() = homePlannerPrefs.getInt(HOME_PLANNER_NOTIFICATION_STATE, 0)
        set(value) = homePlannerPrefs.edit { putInt(HOME_PLANNER_NOTIFICATION_STATE, value) }

    companion object {
        private const val HOME_PLANNER_NOTIFICATION_STATE = "homePlannerNotificationState"
        private const val HOME_PLANNER_SAVED_URL = "homePlannerSavedUrl"
        private const val HOME_PLANNER_EXPIRED = "homePlannerExpired"
        private const val HOME_PLANNER_APPLICATION_STATE = "homePlannerApplicationState"
        private const val HOME_PLANNER_NOTIFICAITON_REQUEST = "homePlannerNotificationRequest"
    }
}