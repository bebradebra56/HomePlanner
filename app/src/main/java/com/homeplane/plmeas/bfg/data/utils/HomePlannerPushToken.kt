package com.homeplane.plmeas.bfg.data.utils

import android.util.Log
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class HomePlannerPushToken {

    suspend fun homePlannerGetToken(
        homePlannerMaxAttempts: Int = 3,
        homePlannerDelayMs: Long = 1500
    ): String {

        repeat(homePlannerMaxAttempts - 1) {
            try {
                val homePlannerToken = FirebaseMessaging.getInstance().token.await()
                return homePlannerToken
            } catch (e: Exception) {
                Log.e(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Token error (attempt ${it + 1}): ${e.message}")
                delay(homePlannerDelayMs)
            }
        }

        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Token error final: ${e.message}")
            "null"
        }
    }


}