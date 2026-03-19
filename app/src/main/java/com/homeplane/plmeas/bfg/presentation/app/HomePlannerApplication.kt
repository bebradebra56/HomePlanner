package com.homeplane.plmeas.bfg.presentation.app

import android.app.Application
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.homeplane.plmeas.bfg.presentation.di.homePlannerModule
import com.homeplane.plmeas.data.AppDatabase
import com.homeplane.plmeas.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


sealed interface HomePlannerAppsFlyerState {
    data object HomePlannerDefault : HomePlannerAppsFlyerState
    data class HomePlannerSuccess(val homePlannerData: MutableMap<String, Any>?) :
        HomePlannerAppsFlyerState

    data object HomePlannerError : HomePlannerAppsFlyerState
}

interface HomePlannerAppsApi {
    @Headers("Content-Type: application/json")
    @GET(HOME_PLANNER_LIN)
    fun homePlannerGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}

private const val HOME_PLANNER_APP_DEV = "GLzsqryu52DBiTcQWRwqMG"
private const val HOME_PLANNER_LIN = "com.homeplane.plmeas"

class HomePlannerApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { AppRepository(database.appDao()) }

    private var homePlannerIsResumed = false
    ///////
    private var homePlannerConversionTimeoutJob: Job? = null
    private var homePlannerDeepLinkData: MutableMap<String, Any>? = null

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance()
        homePlannerSetDebufLogger(appsflyer)
        homePlannerMinTimeBetween(appsflyer)

        AppsFlyerLib.getInstance().subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(p0: DeepLinkResult) {
                when (p0.status) {
                    DeepLinkResult.Status.FOUND -> {
                        homePlannerExtractDeepMap(p0.deepLink)
                        Log.d(HOME_PLANNER_MAIN_TAG, "onDeepLinking found: ${p0.deepLink}")

                    }

                    DeepLinkResult.Status.NOT_FOUND -> {
                        Log.d(HOME_PLANNER_MAIN_TAG, "onDeepLinking not found: ${p0.deepLink}")
                    }

                    DeepLinkResult.Status.ERROR -> {
                        Log.d(HOME_PLANNER_MAIN_TAG, "onDeepLinking error: ${p0.error}")
                    }
                }
            }

        })


        appsflyer.init(
            HOME_PLANNER_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    //////////
                    homePlannerConversionTimeoutJob?.cancel()
                    Log.d(HOME_PLANNER_MAIN_TAG, "onConversionDataSuccess: $p0")

                    val afStatus = p0?.get("af_status")?.toString() ?: "null"
                    if (afStatus == "Organic") {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                delay(5000)
                                val api = homePlannerGetApi(
                                    "https://gcdsdk.appsflyer.com/install_data/v4.0/",
                                    null
                                )
                                val response = api.homePlannerGetClient(
                                    devkey = HOME_PLANNER_APP_DEV,
                                    deviceId = homePlannerGetAppsflyerId()
                                ).awaitResponse()

                                val resp = response.body()
                                Log.d(HOME_PLANNER_MAIN_TAG, "After 5s: $resp")
                                if (resp?.get("af_status") == "Organic" || resp?.get("af_status") == null) {
                                    homePlannerResume(
                                        HomePlannerAppsFlyerState.HomePlannerError
                                    )
                                } else {
                                    homePlannerResume(
                                        HomePlannerAppsFlyerState.HomePlannerSuccess(
                                            resp
                                        )
                                    )
                                }
                            } catch (d: Exception) {
                                Log.d(HOME_PLANNER_MAIN_TAG, "Error: ${d.message}")
                                homePlannerResume(HomePlannerAppsFlyerState.HomePlannerError)
                            }
                        }
                    } else {
                        homePlannerResume(
                            HomePlannerAppsFlyerState.HomePlannerSuccess(
                                p0
                            )
                        )
                    }
                }

                override fun onConversionDataFail(p0: String?) {
                    /////////
                    homePlannerConversionTimeoutJob?.cancel()
                    Log.d(HOME_PLANNER_MAIN_TAG, "onConversionDataFail: $p0")
                    homePlannerResume(HomePlannerAppsFlyerState.HomePlannerError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(HOME_PLANNER_MAIN_TAG, "onAppOpenAttribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(HOME_PLANNER_MAIN_TAG, "onAttributionFailure: $p0")
                }
            },
            this
        )

        appsflyer.start(this, HOME_PLANNER_APP_DEV, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(HOME_PLANNER_MAIN_TAG, "AppsFlyer started")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(HOME_PLANNER_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
            }
        })
        ///////////
        homePlannerStartConversionTimeout()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@HomePlannerApplication)
            modules(
                listOf(
                    homePlannerModule
                )
            )
        }
    }

    private fun homePlannerExtractDeepMap(dl: DeepLink) {
        val map = mutableMapOf<String, Any>()
        dl.deepLinkValue?.let { map["deep_link_value"] = it }
        dl.mediaSource?.let { map["media_source"] = it }
        dl.campaign?.let { map["campaign"] = it }
        dl.campaignId?.let { map["campaign_id"] = it }
        dl.afSub1?.let { map["af_sub1"] = it }
        dl.afSub2?.let { map["af_sub2"] = it }
        dl.afSub3?.let { map["af_sub3"] = it }
        dl.afSub4?.let { map["af_sub4"] = it }
        dl.afSub5?.let { map["af_sub5"] = it }
        dl.matchType?.let { map["match_type"] = it }
        dl.clickHttpReferrer?.let { map["click_http_referrer"] = it }
        dl.getStringValue("timestamp")?.let { map["timestamp"] = it }
        dl.isDeferred?.let { map["is_deferred"] = it }
        for (i in 1..10) {
            val key = "deep_link_sub$i"
            dl.getStringValue(key)?.let {
                if (!map.containsKey(key)) {
                    map[key] = it
                }
            }
        }
        Log.d(HOME_PLANNER_MAIN_TAG, "Extracted DeepLink data: $map")
        homePlannerDeepLinkData = map
    }
    /////////////////

    private fun homePlannerStartConversionTimeout() {
        homePlannerConversionTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(30000)
            if (!homePlannerIsResumed) {
                Log.d(HOME_PLANNER_MAIN_TAG, "TIMEOUT: No conversion data received in 30s")
                homePlannerResume(HomePlannerAppsFlyerState.HomePlannerError)
            }
        }
    }

    private fun homePlannerResume(state: HomePlannerAppsFlyerState) {
        ////////////
        homePlannerConversionTimeoutJob?.cancel()
        if (state is HomePlannerAppsFlyerState.HomePlannerSuccess) {
            val convData = state.homePlannerData ?: mutableMapOf()
            val deepData = homePlannerDeepLinkData ?: mutableMapOf()
            val merged = mutableMapOf<String, Any>().apply {
                putAll(convData)
                for ((key, value) in deepData) {
                    if (!containsKey(key)) {
                        put(key, value)
                    }
                }
            }
            if (!homePlannerIsResumed) {
                homePlannerIsResumed = true
                homePlannerConversionFlow.value =
                    HomePlannerAppsFlyerState.HomePlannerSuccess(merged)
            }
        } else {
            if (!homePlannerIsResumed) {
                homePlannerIsResumed = true
                homePlannerConversionFlow.value = state
            }
        }
    }

    private fun homePlannerGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""
        Log.d(HOME_PLANNER_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }

    private fun homePlannerSetDebufLogger(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun homePlannerMinTimeBetween(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }

    private fun homePlannerGetApi(url: String, client: OkHttpClient?): HomePlannerAppsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

    companion object {
        var homePlannerInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val homePlannerConversionFlow: MutableStateFlow<HomePlannerAppsFlyerState> = MutableStateFlow(
            HomePlannerAppsFlyerState.HomePlannerDefault
        )
        var HOME_PLANNER_FB_LI: String? = null
        const val HOME_PLANNER_MAIN_TAG = "HomePlannerMainTag"
    }
}