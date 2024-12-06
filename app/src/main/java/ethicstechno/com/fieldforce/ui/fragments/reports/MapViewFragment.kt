package ethicstechno.com.fieldforce.ui.fragments.reports

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.google.maps.internal.PolylineEncoding
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentMapViewBinding
import ethicstechno.com.fieldforce.models.attendance.UserLocationListResponse
import ethicstechno.com.fieldforce.models.reports.TripListByAttendanceIdResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors


class MapViewFragment : HomeBaseFragment(), View.OnClickListener, OnMapReadyCallback {

    lateinit var binding: FragmentMapViewBinding
    var mGoogleMap: GoogleMap? = null
    var tripListByAttendanceIdList: ArrayList<TripListByAttendanceIdResponse> = arrayListOf()
    var userLocationList: ArrayList<UserLocationListResponse> = arrayListOf()
    var attendanceId = 0
    var tripId = 0
    var partyLatitude = 0.0
    var partyLongitude = 0.0
    var isFromPartyDealer = false
    var isFromAttendance = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    var punchOutLatitude = 0.0
    var punchOutLongitude = 0.0
    var punchOutTitle = ""
    var punchOutDetails = ""
    var currentAddress = ""
    private var markerTitle = ""
    private var markerDetails = ""


    private val locationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (fusedLocationClient == null) {
                    return@registerForActivityResult
                }
                fetchLocation(fusedLocationClient!!) // Call the method to fetch the location again or perform any other necessary tasks.
            } else {
                CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.enable_location))
                locationEnableDialog()
                // Location settings resolution failed or was canceled.
                // Handle the failure or cancellation accordingly.
            }
        }

    companion object {

        fun newInstance(
            attendanceId: Int,
            tripId: Int,
            isFromPartyDealer: Boolean,
            latitude: Double,
            longitude: Double,
            markerTitle: String,
            markerDetails: String,
            punchOutLatitude: Double,
            punchOutLongitude: Double,
            punchOutTitle: String,
            punchOutDetails: String,
            isFromAttendance: Boolean
        ): MapViewFragment {
            val args = Bundle()
            args.putInt(ARG_PARAM1, attendanceId)
            args.putInt(ARG_PARAM2, tripId)
            args.putBoolean(ARG_PARAM3, isFromPartyDealer)
            args.putDouble(ARG_PARAM4, latitude)
            args.putDouble(ARG_PARAM5, longitude)
            args.putString(ARG_PARAM6, markerTitle)
            args.putString(ARG_PARAM7, markerDetails)
            args.putDouble(ARG_PARAM8, punchOutLatitude)
            args.putDouble(ARG_PARAM9, punchOutLongitude)
            args.putString(ARG_PARAM10, punchOutTitle)
            args.putString(ARG_PARAM11, punchOutDetails)
            args.putBoolean(ARG_PARAM12, isFromAttendance)
            val fragment = MapViewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map_view, container, false)
        binding.googleMapView.onCreate(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity.bottomHide()
        arguments?.let {
            attendanceId = it.getInt(ARG_PARAM1)
            tripId = it.getInt(ARG_PARAM2)
            isFromPartyDealer = it.getBoolean(ARG_PARAM3, false)
            partyLatitude = it.getDouble(ARG_PARAM4, 0.0)
            partyLongitude = it.getDouble(ARG_PARAM5, 0.0)
            markerTitle = it.getString(ARG_PARAM6, "")
            markerDetails = it.getString(ARG_PARAM7, "")
            punchOutLatitude = it.getDouble(ARG_PARAM8, 0.0)
            punchOutLongitude = it.getDouble(ARG_PARAM9, 0.0)
            punchOutTitle = it.getString(ARG_PARAM10, "")
            punchOutDetails = it.getString(ARG_PARAM11, "")
            isFromAttendance = it.getBoolean(ARG_PARAM12, false)
        }
        initView()

    }


    private fun initView() {
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.text = mActivity.getString(R.string.map_view)
        binding.toolbar.imgShare.setOnClickListener(this)
        binding.toolbar.imgBack.setOnClickListener(this)
        askLocationPermission()
    }

    private fun askLocationPermission() {
        val arrListOfPermission = arrayListOf<String>(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
            val locationManager =
                mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(mActivity)
                fetchLocation(fusedLocationClient!!)
            }else {
                locationEnableDialog()
            }
        }
    }

    private fun locationEnableDialog() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 30 * 1000
            locationRequest.fastestInterval = 5 * 1000
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)

            val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(mActivity)
                .checkLocationSettings(builder.build())
            result.addOnCompleteListener { task ->
                try {
                    val response: LocationSettingsResponse? =
                        task.getResult(ApiException::class.java)
                    // All location settings are satisfied. The client can initialize location requests here.
                    fetchLocation(fusedLocationClient!!)
                    //Toast.makeText(activity, AppConstants.mLatitude + ", " + AppConstants.mLongitude, Toast.LENGTH_SHORT).show()
                } catch (exception: ApiException) {
                    FirebaseCrashlytics.getInstance().recordException(exception)
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            val resolvable: ResolvableApiException =
                                exception as ResolvableApiException
                            val intentSenderRequest =
                                IntentSenderRequest.Builder(resolvable.resolution).build()
                            locationSettingsLauncher.launch(intentSenderRequest)
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            // settings, so we won't show the dialog.
                        }
                    }
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("TAG", "locationEnableDialog: "+e.printStackTrace() )
        }
    }

    private fun fetchLocation(fusedLocationClient: FusedLocationProviderClient) {
        try {
            CommonMethods.showLoading(mActivity)
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 5000
            locationRequest.fastestInterval = 2000
            val locationCallback: LocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult == null) {
                        // Handle the case where the location is null
                        return
                    }
                    val location = locationResult.locations[0]
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    val fromLocation = Location("")
                    fromLocation.latitude = loginData.hqLatitude
                    fromLocation.longitude = loginData.hqLongitude
                    val toLocation = Location("")
                    toLocation.latitude = currentLatitude
                    toLocation.longitude = currentLongitude
                    currentAddress = CommonMethods.getAddressFromLocation(
                        mActivity,
                        currentLatitude,
                        currentLongitude
                    ) ?: ""
                    loadMapView()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AppPreference.saveBooleanPreference(
                            mActivity,
                            IS_MOCK_LOCATION,
                            location.isMock
                        )
                    } else {
                        AppPreference.saveBooleanPreference(
                            mActivity,
                            IS_MOCK_LOCATION,
                            location.isFromMockProvider
                        )
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
        }
    }


    private fun loadMapView() {
        try {
            binding.googleMapView.getMapAsync { googleMap ->
                CommonMethods.hideLoading()
                mGoogleMap = googleMap
                if (mGoogleMap != null) {
                    mGoogleMap?.uiSettings?.isMapToolbarEnabled = false
                    mGoogleMap?.clear()
                    val startLatLng = LatLng(partyLatitude, partyLongitude)

                    val endLatLng = if (isFromPartyDealer) LatLng(
                        currentLatitude,
                        currentLongitude
                    ) else LatLng(punchOutLatitude, punchOutLongitude)

                    if (isFromPartyDealer || isFromAttendance) {
                        partyDealerMarkerLoad(mGoogleMap!!, startLatLng, endLatLng)
                    }

                    if (!isFromPartyDealer && !isFromAttendance) {
                        callTripListByAttendanceIdApi()
                    }
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
            CommonMethods.showAlertDialog(mActivity, mActivity.getString(R.string.map_error), e.message, null, isCancelVisibility = false)
            //PubFun.writeLog("[GpsTrackingView] loadMapView()****Error****" + e.message)
        }
    }

    private fun partyDealerMarkerLoad(
        mGoogleMap: GoogleMap?,
        startLatLng: LatLng,
        endLatLng: LatLng
    ) {
        val partyMarkerVec = CommonMethods.BitmapFromVector(
            mActivity,
            if (isFromAttendance) R.drawable.ic_trip_start else R.drawable.ic_party_marker
        )
        val currentMarkerVec = CommonMethods.BitmapFromVector(
            mActivity,
            if (isFromAttendance) R.drawable.ic_trip_end else R.drawable.ic_current_marker
        )

        val partyMarker = MarkerOptions()
            .position(startLatLng)
            .title(markerTitle)
            .snippet(markerDetails)
            .icon(partyMarkerVec)
        mGoogleMap?.addMarker(partyMarker)

        val currentMarker = MarkerOptions()
            .position(endLatLng)
            .title(if (isFromAttendance) punchOutTitle else "Me")
            .snippet(if (isFromAttendance) punchOutDetails else currentAddress)
            .icon(currentMarkerVec)
        mGoogleMap?.addMarker(currentMarker)


        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLng(startLatLng))
        mGoogleMap?.animateCamera(CameraUpdateFactory.zoomTo(12f))
        val customInfoWindowAdapter = CustomInfoWindowAdapter(mActivity)
        mGoogleMap?.setInfoWindowAdapter(customInfoWindowAdapter)
        if(isFromAttendance){
            callUserLocationListApi()
        }else {
            doPathOperations(startLatLng, endLatLng)
        }
    }

    private fun doPathOperations(startPoint: LatLng, endPoint: LatLng) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val directionsResult = DirectionsApiClient.getDirections(
                appDao.getLoginData().googleApiKey!!,
                "${startPoint.latitude},${startPoint.longitude}",
                "${endPoint.latitude},${endPoint.longitude}"
            )

            handler.post {
                directionsResult?.routes?.firstOrNull()?.overviewPolyline?.let { overviewPolyline ->
                    val decodedPath =
                        PolylineEncoding.decode(overviewPolyline.encodedPath)
                    val randomHexColor = CommonMethods.getRandomHexColor()

                    val randomColor = Color.parseColor(randomHexColor)

                    val path = PolylineOptions().apply {
                        addAll(decodedPath.map {
                            LatLng(
                                it.lat,
                                it.lng
                            )
                        }) // Convert to LatLng and add to PolylineOptions
                        color(randomColor)
                        width(10f)
                    }

                    //val animationDuration = 5000 // 5 seconds
                    val polyLineThis = mGoogleMap?.addPolyline(path)
                    CommonMethods.animatePolyline(polyLineThis!!)
                }
            }
        }

    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }
            R.id.imgShare -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    openSharableOption()
                }else{
                    val arrayPermissions = arrayListOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    PermissionUtil(mActivity).requestPermissions(arrayPermissions){
                        openSharableOption()
                    }
                }
            }
        }
    }

    private fun openSharableOption() {

    }

    private fun callUserLocationListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                mActivity.getString(R.string.network_error),
                mActivity.getString(R.string.network_error_msg),
                null,
                isCancelVisibility = false
            )
            return
        }

        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val userLocationListReq = JsonObject()
        userLocationListReq.addProperty("AttendanceId", attendanceId)
        val userLocationListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.userLocationList(userLocationListReq)

        userLocationListCall?.enqueue(object : Callback<List<UserLocationListResponse>> {
            override fun onResponse(
                call: Call<List<UserLocationListResponse>>,
                response: Response<List<UserLocationListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            userLocationList = arrayListOf()
                            userLocationList.addAll(it)
                            if(userLocationList.size > 0){
                                val currentMarkerVec = CommonMethods.BitmapFromVector(
                                    mActivity,
                                    R.drawable.ic_current_marker
                                )
                                for(i in userLocationList) {
                                    val currentMarker = MarkerOptions()
                                        .position(LatLng(i.latitude, i.longitude))
                                        .title("Intermediate Location, (${CommonMethods.formatDateTime(i.createDateTime)})")
                                        .snippet(i.location)
                                        .icon(currentMarkerVec)
                                    mGoogleMap?.addMarker(currentMarker)
                                }
                            }
                            //setupTripAttendanceData(userLocationList)
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                mActivity.getString(R.string.something_went_wrong)
                            )
                        }
                    }
                }else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        mActivity.getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<UserLocationListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }

        })

    }

    private fun callTripListByAttendanceIdApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                mActivity.getString(R.string.network_error),
                mActivity.getString(R.string.network_error_msg),
                null,
                isCancelVisibility = false
            )
            return
        }

        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val tripListReq = JsonObject()
        tripListReq.addProperty("AttendanceId", attendanceId)
        Log.e("TAG", "callTripListByAttendanceIdApi: " + attendanceId)
        val tripListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.getTripListByAttendanceId(tripListReq)

        tripListCall?.enqueue(object : Callback<List<TripListByAttendanceIdResponse>> {
            override fun onResponse(
                call: Call<List<TripListByAttendanceIdResponse>>,
                response: Response<List<TripListByAttendanceIdResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            tripListByAttendanceIdList = arrayListOf()
                            tripListByAttendanceIdList.addAll(it)
                            setupTripAttendanceData(tripListByAttendanceIdList)
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                mActivity.getString(R.string.something_went_wrong)
                            )
                        }
                    }
                }else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        mActivity.getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<TripListByAttendanceIdResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }

        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            mGoogleMap = googleMap
            if (mGoogleMap == null) return
            val latLng = LatLng(partyLatitude, partyLongitude) //set INDIA Lat Lng at Initial Leval
            mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 4f))
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            CommonMethods.showAlertDialog(
                mActivity,
                mActivity.getString(R.string.map_error),
                e.message.toString(),
                null,
                isCancelVisibility = false
            )
            //PubFun.writeLog("[GpsTrackingView] onMapReady()****Error****" + e.message)
        }
    }

    fun setupTripAttendanceData(list: ArrayList<TripListByAttendanceIdResponse>) {
        for (i in list.indices) {
            val startLocation = LatLng(list[i].fromPlaceLatitude, list[i].fromPlaceLongitude)
            val endLocation = LatLng(list[i].endTripLatitude, list[i].endTripLongitude)
            tripSummeryMarker(startLocation, endLocation, list[i], i)
            //setStartAndEndMarkersAndDrawPolyline(startLocation, endLocation)
        }
    }

    private fun tripSummeryMarker(
        startLocation: LatLng,
        endLocation: LatLng,
        tripData: TripListByAttendanceIdResponse,
        position: Int
    ) {
        val partyMarkerVec = CommonMethods.BitmapFromVector(mActivity, R.drawable.ic_party_marker)
        val currentMarkerVec = CommonMethods.BitmapFromVector(mActivity, R.drawable.ic_party_marker)

        val currentMarker = MarkerOptions()
            .position(LatLng(startLocation.latitude, startLocation.longitude))
            .title("Trip ${position+1} Source")
            .snippet(tripData.fromPlace)
            .icon(currentMarkerVec)
        mGoogleMap?.addMarker(currentMarker)

        val partyMarker = MarkerOptions()
            .position(LatLng(endLocation.latitude, endLocation.longitude))
            .title("Trip ${position+1} Destination")
            .snippet(tripData.endTripLocation)
            .icon(partyMarkerVec)
        mGoogleMap?.addMarker(partyMarker)

        val startPoint = LatLng(endLocation.latitude, endLocation.longitude)
        val endPoint = LatLng(startLocation.latitude, startLocation.longitude)

        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLng(startPoint))
        mGoogleMap?.animateCamera(CameraUpdateFactory.zoomTo(12f))
        val customInfoWindowAdapter = CustomInfoWindowAdapter(mActivity)
        mGoogleMap?.setInfoWindowAdapter(customInfoWindowAdapter)
        doPathOperations(startPoint, endPoint)
    }


    override fun onResume() {
        super.onResume()
        if (binding.googleMapView == null) return
        binding.googleMapView.onResume()
        mActivity.bottomHide()
    }

    override fun onPause() {
        super.onPause()
        if (binding.googleMapView == null) return
        binding.googleMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding.googleMapView == null) return
        binding.googleMapView.onDestroy()
    }


    class CustomInfoWindowAdapter(val mContext: Context) : GoogleMap.InfoWindowAdapter {
        override fun getInfoWindow(marker: Marker): View? {
            return null // Return null to use the default info window
        }

        override fun getInfoContents(marker: Marker): View {
            val view = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window, null)
            val titleTextView: TextView = view.findViewById(R.id.markerTitle)
            val messageTextView: TextView = view.findViewById(R.id.markerMessage)

            // Set the multi-line title and message
            titleTextView.text = "${marker.title}"
            messageTextView.text = "${marker.snippet}"

            return view
        }
    }


}