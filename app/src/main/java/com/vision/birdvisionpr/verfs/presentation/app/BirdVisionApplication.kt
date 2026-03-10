package com.vision.birdvisionpr.verfs.presentation.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.vision.birdvisionpr.data.db.AppDatabase
import com.vision.birdvisionpr.data.repository.AppRepository
import com.vision.birdvisionpr.verfs.presentation.di.birdVisionModule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


sealed interface BirdVisionAppsFlyerState {
    data object BirdVisionDefault : BirdVisionAppsFlyerState
    data class BirdVisionSuccess(val birdVisionData: MutableMap<String, Any>?) :
        BirdVisionAppsFlyerState

    data object BirdVisionError : BirdVisionAppsFlyerState
}


private const val BIRD_VISION_APP_DEV = "DUikUxAx2R5S2JKsTFjucg"
private const val BIRD_VISION_LIN = "com.vision.birdvisionpr"

class BirdVisionApplication : Application() {


    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: AppRepository by lazy { AppRepository(database) }

    val prefs: SharedPreferences by lazy {
        getSharedPreferences("bird_vision_prefs", Context.MODE_PRIVATE)
    }


    private val birdVisionKtorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
            requestTimeoutMillis = 30000
        }

    }


    private var birdVisionIsResumed = false
//    private var birdVisionConversionTimeoutJob: Job? = null
    private var birdVisionDeepLinkData: MutableMap<String, Any>? = null

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance()
        birdVisionSetDebufLogger(appsflyer)
        birdVisionMinTimeBetween(appsflyer)

        AppsFlyerLib.getInstance().subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(p0: DeepLinkResult) {
                when (p0.status) {
                    DeepLinkResult.Status.FOUND -> {
                        birdVisionExtractDeepMap(p0.deepLink)
                        Log.d(BIRD_VISION_MAIN_TAG, "onDeepLinking found: ${p0.deepLink}")

                    }

                    DeepLinkResult.Status.NOT_FOUND -> {
                        Log.d(BIRD_VISION_MAIN_TAG, "onDeepLinking not found: ${p0.deepLink}")
                    }

                    DeepLinkResult.Status.ERROR -> {
                        Log.d(BIRD_VISION_MAIN_TAG, "onDeepLinking error: ${p0.error}")
                    }
                }
            }

        })


        appsflyer.init(
            BIRD_VISION_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
//                    birdVisionConversionTimeoutJob?.cancel()
                    Log.d(BIRD_VISION_MAIN_TAG, "onConversionDataSuccess: $p0")

                    val afStatus = p0?.get("af_status")?.toString() ?: "null"
                    if (afStatus == "Organic") {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                delay(5000)
                                val response = birdVisionKtorClient.get("https://gcdsdk.appsflyer.com/install_data/v4.0/$BIRD_VISION_LIN") {
                                    parameter("devkey", BIRD_VISION_APP_DEV)
                                    parameter("device_id", birdVisionGetAppsflyerId())
                                }

                                val resp = response.body<MutableMap<String, JsonElement>?>()
                                val f = resp?.mapValues { (_, v) -> jsonElementToAny(v) }?.toMutableMap() ?: mutableMapOf()
                                Log.d(BIRD_VISION_MAIN_TAG, "After 5s: $resp")
                                if (resp?.get("af_status")?.jsonPrimitive?.content == "Organic" || resp?.get("af_status") == null) {
                                    birdVisionResume(
                                        BirdVisionAppsFlyerState.BirdVisionError
                                    )
                                } else {
                                    birdVisionResume(
                                        BirdVisionAppsFlyerState.BirdVisionSuccess(
                                            f
                                        )
                                    )
                                }
                            } catch (d: Exception) {
                                Log.d(BIRD_VISION_MAIN_TAG, "Error: ${d.message}")
                                birdVisionResume(BirdVisionAppsFlyerState.BirdVisionError)
                            }
                        }
                    } else {
                        birdVisionResume(BirdVisionAppsFlyerState.BirdVisionSuccess(p0))
                    }
                }

                override fun onConversionDataFail(p0: String?) {
//                    birdVisionConversionTimeoutJob?.cancel()
                    Log.d(BIRD_VISION_MAIN_TAG, "onConversionDataFail: $p0")
                    birdVisionResume(BirdVisionAppsFlyerState.BirdVisionError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(BIRD_VISION_MAIN_TAG, "onAppOpenAttribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(BIRD_VISION_MAIN_TAG, "onAttributionFailure: $p0")
                }
            },
            this
        )

        appsflyer.start(this, BIRD_VISION_APP_DEV, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(BIRD_VISION_MAIN_TAG, "AppsFlyer started")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(BIRD_VISION_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
            }
        })
//        birdVisionStartConversionTimeout()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BirdVisionApplication)
            modules(
                listOf(
                    birdVisionModule
                )
            )
        }
    }

    fun jsonElementToAny(element: JsonElement): Any {
        return when (element) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.booleanOrNull != null -> element.boolean
                    element.longOrNull != null -> element.long
                    element.doubleOrNull != null -> element.double
                    else -> element.content
                }
            }
            is JsonObject -> element.mapValues { (_, v) -> jsonElementToAny(v) }
            is JsonArray -> element.map { jsonElementToAny(it) }

        }
    }

    private fun birdVisionExtractDeepMap(dl: DeepLink) {
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
        Log.d(BIRD_VISION_MAIN_TAG, "Extracted DeepLink data: $map")
        birdVisionDeepLinkData = map
    }

//    private fun birdVisionStartConversionTimeout() {
//        birdVisionConversionTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
//            delay(30000)
//            if (!birdVisionIsResumed) {
//                Log.d(PLINK_ZEN_MAIN_TAG, "TIMEOUT: No conversion data received in 30s")
//                birdVisionResume(PlinkZenAppsFlyerState.PlinkZenError)
//            }
//        }
//    }

    private fun birdVisionResume(state: BirdVisionAppsFlyerState) {
//        birdVisionConversionTimeoutJob?.cancel()
        if (state is BirdVisionAppsFlyerState.BirdVisionSuccess) {
            val convData = state.birdVisionData ?: mutableMapOf()
            val deepData = birdVisionDeepLinkData ?: mutableMapOf()
            val merged = mutableMapOf<String, Any>().apply {
                putAll(convData)
                for ((key, value) in deepData) {
                    if (!containsKey(key)) {
                        put(key, value)
                    }
                }
            }
            if (!birdVisionIsResumed) {
                birdVisionIsResumed = true
                birdVisionConversionFlow.value =
                    BirdVisionAppsFlyerState.BirdVisionSuccess(merged)
            }
        } else {
            if (!birdVisionIsResumed) {
                birdVisionIsResumed = true
                birdVisionConversionFlow.value = state
            }
        }
    }

    private fun birdVisionGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""
        Log.d(BIRD_VISION_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }

    private fun birdVisionSetDebufLogger(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun birdVisionMinTimeBetween(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }

    companion object {

        const val PREF_COOP_AREA = "coop_area"
        const val PREF_BIRD_COUNT = "bird_count"
        const val PREF_LIGHT_HOURS = "light_hours"
        const val PREF_LAMP_TYPE = "lamp_type"

        var birdVisionInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val birdVisionConversionFlow: MutableStateFlow<BirdVisionAppsFlyerState> = MutableStateFlow(
            BirdVisionAppsFlyerState.BirdVisionDefault
        )
        var BIRD_VISION_FB_LI: String? = null
        const val BIRD_VISION_MAIN_TAG = "BirdVisionMainTag"
    }
}