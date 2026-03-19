package com.homeplane.plmeas.bfg.domain.model

import com.google.gson.annotations.SerializedName


data class HomePlannerEntity (
    @SerializedName("ok")
    val homePlannerOk: String,
    @SerializedName("url")
    val homePlannerUrl: String,
    @SerializedName("expires")
    val homePlannerExpires: Long,
)