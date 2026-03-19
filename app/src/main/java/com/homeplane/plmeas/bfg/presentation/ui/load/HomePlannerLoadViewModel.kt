package com.homeplane.plmeas.bfg.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeplane.plmeas.bfg.data.shar.HomePlannerSharedPreference
import com.homeplane.plmeas.bfg.data.utils.HomePlannerSystemService
import com.homeplane.plmeas.bfg.domain.usecases.HomePlannerGetAllUseCase
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerAppsFlyerState
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomePlannerLoadViewModel(
    private val homePlannerGetAllUseCase: HomePlannerGetAllUseCase,
    private val homePlannerSharedPreference: HomePlannerSharedPreference,
    private val homePlannerSystemService: HomePlannerSystemService
) : ViewModel() {

    private val _homePlannerHomeScreenState: MutableStateFlow<HomePlannerHomeScreenState> =
        MutableStateFlow(HomePlannerHomeScreenState.HomePlannerLoading)
    val homePlannerHomeScreenState = _homePlannerHomeScreenState.asStateFlow()

    private var homePlannerGetApps = false


    init {
        viewModelScope.launch {
            when (homePlannerSharedPreference.homePlannerAppState) {
                0 -> {
                    if (homePlannerSystemService.homePlannerIsOnline()) {
                        HomePlannerApplication.homePlannerConversionFlow.collect {
                            when(it) {
                                HomePlannerAppsFlyerState.HomePlannerDefault -> {}
                                HomePlannerAppsFlyerState.HomePlannerError -> {
                                    homePlannerSharedPreference.homePlannerAppState = 2
                                    _homePlannerHomeScreenState.value =
                                        HomePlannerHomeScreenState.HomePlannerError
                                    homePlannerGetApps = true
                                }
                                is HomePlannerAppsFlyerState.HomePlannerSuccess -> {
                                    if (!homePlannerGetApps) {
                                        homePlannerGetData(it.homePlannerData)
                                        homePlannerGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _homePlannerHomeScreenState.value =
                            HomePlannerHomeScreenState.HomePlannerNotInternet
                    }
                }
                1 -> {
                    if (homePlannerSystemService.homePlannerIsOnline()) {
                        if (HomePlannerApplication.HOME_PLANNER_FB_LI != null) {
                            _homePlannerHomeScreenState.value =
                                HomePlannerHomeScreenState.HomePlannerSuccess(
                                    HomePlannerApplication.HOME_PLANNER_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > homePlannerSharedPreference.homePlannerExpired) {
                            Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Current time more then expired, repeat request")
                            HomePlannerApplication.homePlannerConversionFlow.collect {
                                when(it) {
                                    HomePlannerAppsFlyerState.HomePlannerDefault -> {}
                                    HomePlannerAppsFlyerState.HomePlannerError -> {
                                        _homePlannerHomeScreenState.value =
                                            HomePlannerHomeScreenState.HomePlannerSuccess(
                                                homePlannerSharedPreference.homePlannerSavedUrl
                                            )
                                        homePlannerGetApps = true
                                    }
                                    is HomePlannerAppsFlyerState.HomePlannerSuccess -> {
                                        if (!homePlannerGetApps) {
                                            homePlannerGetData(it.homePlannerData)
                                            homePlannerGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(HomePlannerApplication.HOME_PLANNER_MAIN_TAG, "Current time less then expired, use saved url")
                            _homePlannerHomeScreenState.value =
                                HomePlannerHomeScreenState.HomePlannerSuccess(
                                    homePlannerSharedPreference.homePlannerSavedUrl
                                )
                        }
                    } else {
                        _homePlannerHomeScreenState.value =
                            HomePlannerHomeScreenState.HomePlannerNotInternet
                    }
                }
                2 -> {
                    _homePlannerHomeScreenState.value =
                        HomePlannerHomeScreenState.HomePlannerError
                }
            }
        }
    }


    private suspend fun homePlannerGetData(conversation: MutableMap<String, Any>?) {
        val homePlannerData = homePlannerGetAllUseCase.invoke(conversation)
        if (homePlannerSharedPreference.homePlannerAppState == 0) {
            if (homePlannerData == null) {
                homePlannerSharedPreference.homePlannerAppState = 2
                _homePlannerHomeScreenState.value =
                    HomePlannerHomeScreenState.HomePlannerError
            } else {
                homePlannerSharedPreference.homePlannerAppState = 1
                homePlannerSharedPreference.apply {
                    homePlannerExpired = homePlannerData.homePlannerExpires
                    homePlannerSavedUrl = homePlannerData.homePlannerUrl
                }
                _homePlannerHomeScreenState.value =
                    HomePlannerHomeScreenState.HomePlannerSuccess(homePlannerData.homePlannerUrl)
            }
        } else  {
            if (homePlannerData == null) {
                _homePlannerHomeScreenState.value =
                    HomePlannerHomeScreenState.HomePlannerSuccess(
                        homePlannerSharedPreference.homePlannerSavedUrl
                    )
            } else {
                homePlannerSharedPreference.apply {
                    homePlannerExpired = homePlannerData.homePlannerExpires
                    homePlannerSavedUrl = homePlannerData.homePlannerUrl
                }
                _homePlannerHomeScreenState.value =
                    HomePlannerHomeScreenState.HomePlannerSuccess(homePlannerData.homePlannerUrl)
            }
        }
    }


    sealed class HomePlannerHomeScreenState {
        data object HomePlannerLoading : HomePlannerHomeScreenState()
        data object HomePlannerError : HomePlannerHomeScreenState()
        data class HomePlannerSuccess(val data: String) : HomePlannerHomeScreenState()
        data object HomePlannerNotInternet: HomePlannerHomeScreenState()
    }
}