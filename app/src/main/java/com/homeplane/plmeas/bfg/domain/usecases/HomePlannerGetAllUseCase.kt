package com.homeplane.plmeas.bfg.domain.usecases

import android.util.Log
import com.homeplane.plmeas.bfg.data.repo.HomePlannerRepository
import com.homeplane.plmeas.bfg.data.utils.HomePlannerPushToken
import com.homeplane.plmeas.bfg.data.utils.HomePlannerSystemService
import com.homeplane.plmeas.bfg.domain.model.HomePlannerEntity
import com.homeplane.plmeas.bfg.domain.model.HomePlannerParam
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication

class HomePlannerGetAllUseCase(
    private val homePlannerRepository: HomePlannerRepository,
    private val homePlannerSystemService: HomePlannerSystemService,
    private val homePlannerPushToken: HomePlannerPushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : HomePlannerEntity?{
        val params = HomePlannerParam(
            homePlannerLocale = homePlannerSystemService.homePlannerGetLocale(),
            homePlannerPushToken = homePlannerPushToken.homePlannerGetToken(),
            homePlannerAfId = homePlannerSystemService.homePlannerGetAppsflyerId()
        )
        Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Params for request: $params")
        return homePlannerRepository.homePlannerGetClient(params, conversion)
    }



}