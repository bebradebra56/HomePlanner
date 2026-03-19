package com.homeplane.plmeas.bfg.data.repo

import android.util.Log
import com.homeplane.plmeas.bfg.domain.model.HomePlannerEntity
import com.homeplane.plmeas.bfg.domain.model.HomePlannerParam
import com.homeplane.plmeas.bfg.presentation.app.HomePlannerApplication.Companion.HOME_PLANNER_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface HomePlannerApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun homePlannerGetClient(
        @Body jsonString: JsonObject,
    ): Call<HomePlannerEntity>
}


private const val HOME_PLANNER_MAIN = "https://homepllanner.com/"
class HomePlannerRepository {

    suspend fun homePlannerGetClient(
        homePlannerParam: HomePlannerParam,
        homePlannerConversion: MutableMap<String, Any>?
    ): HomePlannerEntity? {
        val gson = Gson()
        val api = homePlannerGetApi(HOME_PLANNER_MAIN, null)

        val homePlannerJsonObject = gson.toJsonTree(homePlannerParam).asJsonObject
        homePlannerConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            homePlannerJsonObject.add(key, element)
        }
        return try {
            val homePlannerRequest: Call<HomePlannerEntity> = api.homePlannerGetClient(
                jsonString = homePlannerJsonObject,
            )
            val homePlannerResult = homePlannerRequest.awaitResponse()
            Log.d(HOME_PLANNER_MAIN_TAG, "Retrofit: Result code: ${homePlannerResult.code()}")
            if (homePlannerResult.code() == 200) {
                Log.d(HOME_PLANNER_MAIN_TAG, "Retrofit: Get request success")
                Log.d(HOME_PLANNER_MAIN_TAG, "Retrofit: Code = ${homePlannerResult.code()}")
                Log.d(HOME_PLANNER_MAIN_TAG, "Retrofit: ${homePlannerResult.body()}")
                homePlannerResult.body()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            Log.d(HOME_PLANNER_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(HOME_PLANNER_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun homePlannerGetApi(url: String, client: OkHttpClient?) : HomePlannerApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
