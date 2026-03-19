package com.homeplane.plmeas.bfg.domain.model

import com.google.gson.annotations.SerializedName


private const val HOME_PLANNER_A = "com.homeplane.plmeas"
private const val HOME_PLANNER_B = "homeplanner-e3838"
data class HomePlannerParam (
    @SerializedName("af_id")
    val homePlannerAfId: String,
    @SerializedName("bundle_id")
    val homePlannerBundleId: String = HOME_PLANNER_A,
    @SerializedName("os")
    val homePlannerOs: String = "Android",
    @SerializedName("store_id")
    val homePlannerStoreId: String = HOME_PLANNER_A,
    @SerializedName("locale")
    val homePlannerLocale: String,
    @SerializedName("push_token")
    val homePlannerPushToken: String,
    @SerializedName("firebase_project_id")
    val homePlannerFirebaseProjectId: String = HOME_PLANNER_B,

    )