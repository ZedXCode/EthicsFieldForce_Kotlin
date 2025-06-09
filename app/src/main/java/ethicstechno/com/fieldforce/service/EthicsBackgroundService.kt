package ethicstechno.com.fieldforce.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.db.AppDatabase
import ethicstechno.com.fieldforce.db.dao.AppDao
import ethicstechno.com.fieldforce.models.attendance.PunchInResponse
import ethicstechno.com.fieldforce.models.attendance.UserLastSyncResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.trip.TripSubmitResponse
import ethicstechno.com.fieldforce.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EthicsBackgroundService : Service() {

    var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var mLocationCallBack: LocationCallback
    lateinit var mLocationRequest: LocationRequest
    lateinit var mLocationSettingRequest: LocationSettingsRequest
    var apiFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val ACTION_LOCATION_BROADCAST: String =
        EthicsBackgroundService::class.java.name + "LocationBroadcast"
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    var executorService: ExecutorService? = null
    lateinit var appDao: AppDao
    lateinit var mContext: Context


    override fun onCreate() {
        super.onCreate()
        mContext = this
        val appDatabase = AppDatabase.getDatabase(mContext)
        appDao = appDatabase.appDao()
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                startMyOwnBackground()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(1, Notification(), FOREGROUND_SERVICE_TYPE_LOCATION)
                } else {
                    startForeground(1, Notification())
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnBackground() {

        val NOTIFICATION_CHANNEL_ID = "ethics_techno.fieldfoce.notification"
        val CHANNEL_NAME = "ethics_backgrund_service"
        val notificationChanel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChanel.lightColor = Color.BLUE
        notificationChanel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        val manager =
            NotificationManagerCompat.from(this)//(getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(notificationChanel)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setSmallIcon(ethicstechno.com.fieldforce.R.drawable.ethics_notification_icon)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(2, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(2, notification)
        }
        //startForeground(2, notification)
        //Log.e("ETHICSSERVICE", ":: startMyOwnBackground: MY OWN FOREGROUND STARTED ::")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        this.mLocationRequest = LocationRequest()
        this.mLocationRequest.interval = 10000.toLong()
        this.mLocationRequest.fastestInterval = 5000.toLong()
        this.mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val mBuilder = LocationSettingsRequest.Builder()
        mBuilder.addLocationRequest(this.mLocationRequest)

        this.mLocationSettingRequest = mBuilder.build()

        this.mLocationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult) // why? this. is. retarded. Android.
                val currentLocation = locationResult.lastLocation

                if (currentLocation != null) {
                    when (currentLocation.provider) {
                        LocationManager.GPS_PROVIDER -> {
                            AppPreference.saveStringPreference(
                                mContext,
                                KEY_LAST_LOCATION_BY,
                                "1"
                            )
                        }
                        LocationManager.NETWORK_PROVIDER -> {
                            AppPreference.saveStringPreference(
                                mContext,
                                KEY_LAST_LOCATION_BY,
                                "2"
                            )
                        }
                        LocationManager.PASSIVE_PROVIDER -> {
                            AppPreference.saveStringPreference(
                                mContext,
                                KEY_LAST_LOCATION_BY,
                                "3"
                            )
                        }
                        else -> {
                            AppPreference.saveStringPreference(
                                mContext,
                                KEY_LAST_LOCATION_BY,
                                "0"
                            )
                        }
                    }
                    AppPreference.saveStringPreference(
                        mContext,
                        KEY_LAST_LOCATION_LATITUDE,
                        currentLocation.latitude.toString()
                    )
                    AppPreference.saveStringPreference(
                        mContext,
                        KEY_LAST_LOCATION_LONGITUDE,
                        currentLocation.longitude.toString()
                    )
                    AppPreference.saveStringPreference(
                        mContext,
                        KEY_LOCATION_ACCURACY,
                        currentLocation.accuracy.toString()
                    )

                    val currentDateandTime: String = apiFormat.format(Date())
                    AppPreference.saveStringPreference(
                        mContext,
                        KEY_LAST_LOCATION_UPDATE_DATE,
                        currentDateandTime
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AppPreference.saveBooleanPreference(
                            mContext,
                            IS_MOCK_LOCATION,
                            currentLocation.isMock
                        )
                    } else {
                        AppPreference.saveBooleanPreference(
                            mContext,
                            IS_MOCK_LOCATION,
                            currentLocation.isFromMockProvider
                        )
                    }
                } else {
                    AppPreference.saveStringPreference(
                        mContext,
                        KEY_LAST_LOCATION_LATITUDE,
                        "0.01"
                    )
                    AppPreference.saveStringPreference(
                        mContext,
                        KEY_LAST_LOCATION_LONGITUDE,
                        "0.01"
                    )
                    AppPreference.saveStringPreference(
                        mContext,
                        KEY_LOCATION_ACCURACY,
                        "0.01"
                    )
                }
                sendMessageToUI()
            }

        }
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AppPreference.saveStringPreference(
                mContext,
                KEY_LAST_LOCATION_LATITUDE,
                "0.01"
            )
            AppPreference.saveStringPreference(
                mContext,
                KEY_LAST_LOCATION_LONGITUDE,
                "0.01"
            )
            sendMessageToUI()
        } else {
            if (this.mFusedLocationProviderClient == null) {
                this.mFusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(mContext)
            } else {
                this.mFusedLocationProviderClient?.removeLocationUpdates(this.mLocationCallBack)
            }
            this.mFusedLocationProviderClient?.requestLocationUpdates(
                this.mLocationRequest,
                this.mLocationCallBack,
                Looper.myLooper()
            )
            startTimer()
        }
        return START_STICKY
    }

    private fun startTimer() {
        stopTimer()
        try {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    executorService = Executors.newSingleThreadExecutor()
                    executorService?.execute {

                        val lat: String =
                            AppPreference.getStringPreference(mContext, KEY_LAST_LOCATION_LATITUDE)
                        val lng: String =
                            AppPreference.getStringPreference(mContext, KEY_LAST_LOCATION_LONGITUDE)

                        if (lat.isNotEmpty() && lng.isNotEmpty()) {
                            CommonMethods.saveLocationData(lat.toDouble(), lng.toDouble(), mContext)
                        }
                        if (ConnectionUtil.isInternetAvailable(mContext)) {
                            val userLastSyncLocationReq = JsonObject()
                            if(::appDao.isInitialized) {
                                userLastSyncLocationReq.addProperty(
                                    "UserId",
                                    appDao.getLoginData().userId
                                )
                            }

                            val userLastSyncLocationCall = WebApiClient.getInstance(mContext)
                                .webApi_without(appDao.getAppRegistration().apiHostingServer)
                                ?.getUserLastSyncLocation(userLastSyncLocationReq)

                            userLastSyncLocationCall?.enqueue(object :
                                Callback<List<UserLastSyncResponse>> {
                                override fun onResponse(
                                    call: Call<List<UserLastSyncResponse>>,
                                    response: Response<List<UserLastSyncResponse>>
                                ) {
                                    if (response != null && response.code() == 200 && response.body() != null) {
                                        response.body()?.let { it ->
                                            if (it.isNotEmpty()) {

                                                if (it[0].isStopBackgroundService) {
                                                    val mServiceIntent = Intent(
                                                        mContext,
                                                        EthicsBackgroundService::class.java
                                                    )
                                                    if (isMyServiceRunning(EthicsBackgroundService::class.java)) {
                                                        stopService(mServiceIntent)
                                                    }
                                                } else {
                                                    val attendanceIdFromApi =
                                                        appDao.getLoginData().attendanceId
                                                    val tripIdFromApi = it[0].tripId
                                                    val tripTimerInterval =
                                                        it[0].tripLocationInterval
                                                    val attendanceTimerInterval =
                                                        it[0].attendanceLocationInterval

                                                    val isSaveIntervalWiseLocation =
                                                        appDao.getLoginData().saveIntervalWiseLocation

                                                    val currentDateTimeTrip =
                                                        Calendar.getInstance().time
                                                    // Parse the given time
                                                    val sdfTrip = SimpleDateFormat(
                                                        DATE_FORMAT_FOR_SERVICE,
                                                        Locale.getDefault()
                                                    )
                                                    val givenDateTimeTrip =
                                                        sdfTrip.parse(it[0].lastTripLocationDateTime)
                                                    // Calculate the difference in milliseconds
                                                    val differenceInMillisTrip =
                                                        currentDateTimeTrip.time - givenDateTimeTrip!!.time
                                                    // Convert milliseconds to minutes
                                                    val differenceInMinutesTrip =
                                                        differenceInMillisTrip / (60 * 1000)
                                                    Log.e("ETHICSSERVICE", "onResponse: INTERVAL IS : " + tripTimerInterval + "DIFFRENCEC IS :: " + differenceInMinutesTrip)

                                                    if (tripIdFromApi > 0 && differenceInMinutesTrip >= tripTimerInterval) {
                                                        Log.e("ETHICSSERVICE", "TRIP SUCCESS")
                                                        if (ConnectionUtil.isInternetAvailable(
                                                                mContext
                                                            )
                                                        ) {
                                                            val lastLat =
                                                                AppPreference.getStringPreference(
                                                                    mContext,
                                                                    KEY_LAST_LOCATION_LATITUDE,
                                                                    "0.0"
                                                                )
                                                                    .toDouble()
                                                            val lastLng =
                                                                AppPreference.getStringPreference(
                                                                    mContext,
                                                                    KEY_LAST_LOCATION_LONGITUDE,
                                                                    "0.0"
                                                                )
                                                                    .toDouble()

                                                            var totalDistance = 0.0
                                                            val distanceCalculatorUtils = DistanceCalculatorUtils()
                                                            distanceCalculatorUtils.getDistance(
                                                                it[0]?.lastSyncLatitude!!,
                                                                it[0]?.lastSyncLongitude!!,
                                                                lastLat,
                                                                lastLng,
                                                                appDao.getLoginData().googleApiKey!!,
                                                                object : DistanceCalculatorUtils.DistanceCallback {
                                                                    override fun onDistanceCalculated(distance: Float) {
                                                                        totalDistance = distance.toDouble()
                                                                        Log.e("TAG", "onDistanceCalculated: $totalDistance")
                                                                    }
                                                                }
                                                            )

                                                            val apiReq = JsonObject()
                                                            apiReq.addProperty(
                                                                "TripId",
                                                                tripIdFromApi
                                                            )
                                                            apiReq.addProperty(
                                                                "UserId",
                                                                appDao.getLoginData().userId
                                                            )
                                                            apiReq.addProperty("Latitude", lastLat)
                                                            apiReq.addProperty("Longitude", lastLng)
                                                            apiReq.addProperty(
                                                                "ReturnMessage",
                                                                "12345"
                                                            )
                                                            apiReq.addProperty(
                                                                "Location",
                                                                if (isSaveIntervalWiseLocation) CommonMethods.getAddressFromLocation(
                                                                    mContext,
                                                                    lastLat,
                                                                    lastLng
                                                                ) else ""
                                                            )
                                                            apiReq.addProperty("ReferenceTableName", it[0].referenceTableName)
                                                            apiReq.addProperty("ReferenceId", it[0].referenceId)
                                                            apiReq.addProperty("MapKM", totalDistance)

                                                            Log.e("TAG", "onResponse: api called TRIP LOCATION:: "+it[0].referenceTableName)

                                                            //Log.e("ETHICSSERVICE", "BACKGORUND API REQ: $apiReq")

                                                            val userLocation =
                                                                WebApiClient.getInstance(mContext)
                                                                    .webApi_without(appDao.getAppRegistration().apiHostingServer)
                                                                    ?.sendTripLocation(apiReq)
                                                            userLocation?.enqueue(object :
                                                                Callback<CommonSuccessResponse> {
                                                                override fun onResponse(
                                                                    call: Call<CommonSuccessResponse>,
                                                                    response: Response<CommonSuccessResponse>
                                                                ) {
                                                                    if (response.code() == 200) {
                                                                        response.body()?.let {
                                                                            //Log.e("ETHICSSERVICE", "onResponse: " + it.returnMessage)
                                                                        }
                                                                    }
                                                                }

                                                                override fun onFailure(
                                                                    call: Call<CommonSuccessResponse>,
                                                                    t: Throwable
                                                                ) {
                                                                }

                                                            })
                                                        }
                                                    }

                                                    val currentDateTime =
                                                        Calendar.getInstance().time
                                                    // Parse the given time
                                                    val sdf = SimpleDateFormat(
                                                        DATE_FORMAT_FOR_SERVICE,
                                                        Locale.getDefault()
                                                    )
                                                    val givenDateTime =
                                                        sdf.parse(it[0].lastAttendanceLocationDateTime)
                                                    // Calculate the difference in milliseconds
                                                    val differenceInMillis =
                                                        currentDateTime.time - givenDateTime!!.time
                                                    // Convert milliseconds to minutes
                                                    val differenceInMinutes =
                                                        differenceInMillis / (60 * 1000)

                                                    Log.e("ETHICSSERVICE", "onResponse: INTERVAL IS : " + attendanceTimerInterval + "DIFFRENCEC IS :: " + differenceInMinutes)
                                                    if (appDao.getLoginData().todayClockInDone && !appDao.getLoginData().todayClockOutDone && differenceInMinutes >= attendanceTimerInterval) {
                                                        Log.e("ETHICSSERVICE", "onResponse: ATTENDANCE SUCESS", )
                                                        if (ConnectionUtil.isInternetAvailable(
                                                                mContext
                                                            )
                                                        ) {
                                                            val lastLat =
                                                                AppPreference.getStringPreference(
                                                                    mContext,
                                                                    KEY_LAST_LOCATION_LATITUDE
                                                                )
                                                                    .toDouble()
                                                            val lastLng =
                                                                AppPreference.getStringPreference(
                                                                    mContext,
                                                                    KEY_LAST_LOCATION_LONGITUDE
                                                                )
                                                                    .toDouble()
                                                            var totalDistance = 0.0
                                                            val distanceCalculatorUtils = DistanceCalculatorUtils()
                                                            distanceCalculatorUtils.getDistance(
                                                                it[0]?.lastSyncLatitude!!,
                                                                it[0]?.lastSyncLongitude!!,
                                                                lastLat,
                                                                lastLng,
                                                                appDao.getLoginData().googleApiKey!!,
                                                                object : DistanceCalculatorUtils.DistanceCallback {
                                                                    override fun onDistanceCalculated(distance: Float) {
                                                                        totalDistance = distance.toDouble()
                                                                        Log.e("TAG", "onDistanceCalculated: $totalDistance")
                                                                    }
                                                                }
                                                            )
                                                            val apiReq = JsonObject()
                                                            apiReq.addProperty(
                                                                "AttendanceId",
                                                                attendanceIdFromApi
                                                            )
                                                            apiReq.addProperty(
                                                                "UserId",
                                                                appDao.getLoginData().userId
                                                            )
                                                            apiReq.addProperty("Latitude", lastLat)
                                                            apiReq.addProperty("Longitude", lastLng)
                                                            apiReq.addProperty(
                                                                "ReturnMessage",
                                                                "12345"
                                                            )
                                                            apiReq.addProperty(
                                                                "Location",
                                                                if (isSaveIntervalWiseLocation) CommonMethods.getAddressFromLocation(
                                                                    mContext,
                                                                    lastLat,
                                                                    lastLng
                                                                ) else ""
                                                            )
                                                            apiReq.addProperty("ReferenceTableName", it[0].referenceTableName)
                                                            apiReq.addProperty("ReferenceId", it[0].referenceId)
                                                            apiReq.addProperty("MapKM", totalDistance)
                                                            Log.e("TAG", "onResponse: api called USER LOCATION:: "+it[0].referenceTableName)

                                                            //Log.e("ETHICSSERVICE", "BACKGORUND API REQ: $apiReq")

                                                            val userLocation =
                                                                WebApiClient.getInstance(mContext)
                                                                    .webApi_without(appDao.getAppRegistration().apiHostingServer)
                                                                    ?.sendUserLocation(apiReq)
                                                            userLocation?.enqueue(object :
                                                                Callback<CommonSuccessResponse> {
                                                                override fun onResponse(
                                                                    call: Call<CommonSuccessResponse>,
                                                                    response: Response<CommonSuccessResponse>
                                                                ) {
                                                                    if (response.code() == 200) {
                                                                        response.body()?.let {
                                                                            //Log.e("ETHICSSERVICE", "onResponse: " + it.returnMessage)
                                                                        }
                                                                    }
                                                                }

                                                                override fun onFailure(
                                                                    call: Call<CommonSuccessResponse>,
                                                                    t: Throwable
                                                                ) {
                                                                }

                                                            })
                                                        }
                                                    }

                                                    if(tripIdFromApi > 0 && it[0].autoPunchOutFlag){
                                                        val lastLat =
                                                        AppPreference.getStringPreference(
                                                            mContext,
                                                            KEY_LAST_LOCATION_LATITUDE,
                                                            "0.0"
                                                        )
                                                            .toDouble()
                                                        val lastLng =
                                                            AppPreference.getStringPreference(
                                                                mContext,
                                                                KEY_LAST_LOCATION_LONGITUDE,
                                                                "0.0"
                                                            )
                                                                .toDouble()
                                                        callEndTripApi(it[0], mContext, lastLat, lastLng)
                                                    }
                                                }
                                            }
                                        }
                                    }


                                }

                                override fun onFailure(
                                    call: Call<List<UserLastSyncResponse>>,
                                    t: Throwable
                                ) {
                                    //Log.e("ETHICSSERVICE", "onFailure: " + t.message)
                                }
                            })

                        }
                    }
                }
            }
            timer!!.schedule(
                timerTask,
                30000,
                30000
            )
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            //Log.e("TAG", "startTimer:" + e.message.toString())
        }
    }

    private fun callEndTripApi(
        userLastSyncResponse: UserLastSyncResponse,
        mContext: Context,
        lastLat: Double,
        lastLng: Double
    ) {
        if (!ConnectionUtil.isInternetAvailable(mContext)) {
            CommonMethods.showToastMessage(mContext, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mContext)
        val currentAddress = CommonMethods.getAddressFromLocation(
            mContext,
            lastLat,
            lastLng
        ) ?: ""
        var totalDistance = 0.0
        val distanceCalculatorUtils = DistanceCalculatorUtils()
        distanceCalculatorUtils.getDistance(
            userLastSyncResponse?.lastSyncLatitude!!,
            userLastSyncResponse?.lastSyncLongitude!!,
            lastLat,
            lastLng,
            appDao.getLoginData().googleApiKey!!,
            object : DistanceCalculatorUtils.DistanceCallback {
                override fun onDistanceCalculated(distance: Float) {
                    totalDistance = distance.toDouble()
                    Log.e("TAG", "onDistanceCalculated: $totalDistance")
                }
            }
        )

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()
        val endTripReq = JsonObject()
        val endMeterReading = 0

        endTripReq.addProperty("TripId", userLastSyncResponse.tripId)
        endTripReq.addProperty("UserId", loginData.userId)
        endTripReq.addProperty("EndTripLatitude", lastLat)
        endTripReq.addProperty("EndTripLongitude", lastLng)
        endTripReq.addProperty("EndTripLocation", currentAddress)
        endTripReq.addProperty("EndMeterReading", endMeterReading)
        endTripReq.addProperty("ActualKM", 0)
        endTripReq.addProperty("TotalTripKm", totalDistance)
        endTripReq.addProperty("Remarks", "Auto Trip End")
        endTripReq.addProperty("EndMeterReadingPhoto", "")
        endTripReq.addProperty("IsActive", true)
        endTripReq.addProperty("UpdateBy", loginData.userId)
        endTripReq.addProperty("IsTripCompleted", true.toString())

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callEndTripApi()$ :: API REQUEST = " + endTripReq.toString())

        val endTripCall = WebApiClient.getInstance(mContext)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.endTripApi(endTripReq)

        endTripCall?.enqueue(object : Callback<TripSubmitResponse> {
            override fun onResponse(
                call: Call<TripSubmitResponse>,
                response: Response<TripSubmitResponse>
            ) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callEndTripApi()$ :: API RESPONSE = " + Gson().toJson(response.body()))

                if (response.code() == 200) {
                    response.body()?.let {
                        if (it.success) {
                            AppPreference.saveBooleanPreference(mContext, IS_TRIP_START, false)
                            callPunchOutApi(userLastSyncResponse, lastLat, lastLng, currentAddress)
                        } else {
                            /*CommonMethods.showAlertDialog(
                                mActivity,
                                getString(R.string.end_trip),
                                it.returnMessage, null
                            )*/
                        }
                    }
                } else {
                    /*CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null
                    )*/
                }
            }

            override fun onFailure(call: Call<TripSubmitResponse>, t: Throwable) {
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callEndTripApi()$ :: onFailure = " + t.message.toString())
                //CommonMethods.hideLoading()
                /*if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }*/
            }
        })
    }

    private fun callPunchOutApi(
        userLastSyncResponse: UserLastSyncResponse,
        lastLat: Double,
        lastLng: Double,
        currentAddress: String
    ) {
        if (!ConnectionUtil.isInternetAvailable(mContext)) {
            CommonMethods.showAlertDialog(
                mContext,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null
            )
            return
        }
        //CommonMethods.showLoading(m)

        val appRegistrationData = appDao.getAppRegistration()
        val punchOutReq = JsonObject()
        punchOutReq.addProperty("AttendanceId", userLastSyncResponse.attendanceId)
        punchOutReq.addProperty("UserId", userLastSyncResponse.userId)
        punchOutReq.addProperty("PunchOutLatitude", lastLat)
        punchOutReq.addProperty("PunchOutLongitude", lastLng)
        punchOutReq.addProperty("Remarks", "Auto Punch Out")
        punchOutReq.addProperty("PunchOutLocation", currentAddress)

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callPunchOutApi()$ :: API REQUEST = " + punchOutReq.toString())

        val punchOutCall = WebApiClient.getInstance(mContext)
            .webApi_without(appRegistrationData.apiHostingServer)?.punchOutApi(punchOutReq)

        punchOutCall?.enqueue(object : Callback<PunchInResponse> {
            override fun onResponse(
                call: Call<PunchInResponse>,
                response: Response<PunchInResponse>
            ) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callPunchOutApi()$ :: API RESPONSE = " + Gson().toJson(response.body()))
                if (response.code() == 200) {
                    response.body()?.let {
                        if (!it.success) {
                            /*CommonMethods.showAlertDialog(
                                mContext,
                                it.returnMessage,//getString(R.string.punch_out_unsuccessful),
                                it.returnMessage,
                                null
                            )*/
                            return
                        }

                        appDao.updateAttendanceId(0, userLastSyncResponse.userId)
                        appDao.updatePunchOutFlag(true, userLastSyncResponse.userId)
                        /*CommonMethods.showAlertDialog(
                            mContext,
                            it.returnMessage,//getString(R.string.punch_out_success),
                            getString(R.string.punch_out_success_msg),
                            okListener = object : PositiveButtonListener {
                                override fun okClickListener() {

                                }
                            },
                            isCancelVisibility = false
                        )*/
                        val mServiceIntent = Intent(mContext, EthicsBackgroundService::class.java)
                        if (isMyServiceRunning(EthicsBackgroundService::class.java)) {
                            mContext.stopService(mServiceIntent)
                        }
                    }
                } else {
                    /*CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null
                    )*/
                }
            }

            override fun onFailure(call: Call<PunchInResponse>, t: Throwable) {
                Log.e("TAG", "onFailure: " + t.message)
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callPunchOutApi()$ :: onFailure = " +t.message.toString())
                /*if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }*/
                //CommonMethods.hideLoading()
            }
        })
    }


    /*override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        stopLocationUpdates()
        if (AppPreference.getBooleanPreference(
                this,
                IS_LOGIN
            ) && appDao.getLoginData().attendanceId != 0
        ) {
            stopTimer()
            stopLocationUpdates()
            val broadcastIntent = Intent()
            broadcastIntent.action = "restartservice"
            broadcastIntent.setClass(this, GPSRestart::class.java)
            this.sendBroadcast(broadcastIntent)
        } else {
            stopTimer()
            stopLocationUpdates()
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        stopLocationUpdates()

        if (::appDao.isInitialized) { // Check if appDao is initialized before using it
            val loginData = appDao.getLoginData()
            if (AppPreference.getBooleanPreference(this, IS_LOGIN) && loginData.attendanceId != 0) {
                val broadcastIntent = Intent().apply {
                    action = "restartservice"
                    setClass(this@EthicsBackgroundService, GPSRestart::class.java)
                }
                sendBroadcast(broadcastIntent)
            }
        }

        stopTimer()
        stopLocationUpdates()
    }


    private fun stopLocationUpdates() {
        if (this.mFusedLocationProviderClient != null) {
            this.mFusedLocationProviderClient?.removeLocationUpdates(this.mLocationCallBack)
        }
    }

    private fun stopTimer() {
        if (timer == null || timerTask == null) {
            return
        }
        try {
            timerTask!!.cancel()
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            ex.printStackTrace()
        }
        try {
            timer!!.cancel()
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().recordException(ex)
            ex.printStackTrace()
        }
    }

    private fun sendMessageToUI() {
        val intent = Intent(ACTION_LOCATION_BROADCAST)
        intent.putExtra(
            EXTRA_LATITUDE,
            AppPreference.getStringPreference(mContext, KEY_LAST_LOCATION_LATITUDE)
        )
        intent.putExtra(
            EXTRA_LONGITUDE,
            AppPreference.getStringPreference(mContext, KEY_LAST_LOCATION_LONGITUDE)
        )
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}