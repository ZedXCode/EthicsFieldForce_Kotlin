package ethicstechno.com.fieldforce.ui.fragments.bottomnavigation

import AnimationType
import addFragment
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentTripBinding
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.trip.GetVisitFromPlaceListResponse
import ethicstechno.com.fieldforce.models.trip.TripSubmitResponse
import ethicstechno.com.fieldforce.models.trip.VehicleTypeListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.dashboard.DashboardFragment
import ethicstechno.com.fieldforce.utils.AlbumUtility
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DistanceCalculatorUtils
import ethicstechno.com.fieldforce.utils.IS_MOCK_LOCATION
import ethicstechno.com.fieldforce.utils.IS_TRIP_START
import ethicstechno.com.fieldforce.utils.ImageUtils
import ethicstechno.com.fieldforce.utils.PermissionUtil
import ethicstechno.com.fieldforce.utils.VEHICLE_TYPE_NA
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.Executors


class TripFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentTripBinding
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    var currentAddress = ""
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var totalDistance = 0F
    var vehicleTypeList: ArrayList<VehicleTypeListResponse> = arrayListOf()
    var visitFromPlaceList: ArrayList<GetVisitFromPlaceListResponse> = arrayListOf()
    var selectedVehicleType: VehicleTypeListResponse? = null
    var visitFromPlaceData: GetVisitFromPlaceListResponse? = null
    var meterReadingImageFile: File? = null
    var base64String: String = ""
    var isTripIdZero = false
    var fromLat = 0.0
    var fromLng = 0.0
    private val REQUEST_PERMISSION_CODE = 123
    var tvHeader: TextView? = null


    private val locationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (fusedLocationClient == null) {
                    return@registerForActivityResult
                }
                fetchLocation(fusedLocationClient!!) // Call the method to fetch the location again or perform any other necessary tasks.
            } else {
                CommonMethods.showToastMessage(
                    mActivity,
                    mActivity.getString(R.string.enable_location)
                )
                locationEnableDialog()
                // Location settings resolution failed or was canceled.
                // Handle the failure or cancellation accordingly.
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trip, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

    }

    private fun initView() {
        binding.toolbar.imgMenu.visibility = View.VISIBLE
        binding.toolbar.imgBack.visibility = View.GONE
        binding.toolbar.tvHeader.text = getString(R.string.trip)
        binding.toolbar.imgMenu.setOnClickListener(this)
        binding.cardStartImage.setOnClickListener(this)
        binding.tvStartTrip.setOnClickListener(this)
        binding.tvEndTrip.setOnClickListener(this)
        binding.imgStartImageFile.setOnClickListener(this)
        binding.imgEndImageFile.setOnClickListener(this)
        binding.imgRefresh.setOnClickListener(this)
        binding.tvLocation.setTextIsSelectable(true)

        if (loginData.todayClockInDone && loginData.todayClockOutDone) {
            binding.tvStartTrip.setBackgroundResource(R.drawable.button_background_disable)
            binding.tvEndTrip.setBackgroundResource(R.drawable.button_background_disable)
            binding.tvStartTrip.isEnabled = false
            binding.tvEndTrip.isEnabled = false
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.alert),
                getString(R.string.your_today_attendance_done_try_tommorrow),
                object : PositiveButtonListener {
                    override fun okClickListener() {
                        mActivity.checkBottomNavigationItem(2)
                        mActivity.addFragment(
                            DashboardFragment(),
                            false,
                            true,
                            AnimationType.fadeInfadeOut
                        )
                    }
                },
                isCancelVisibility = false
            )
        } else if (loginData.todayClockInDone) {
            binding.tvStartTrip.setBackgroundResource(R.drawable.button_background_primary)
            binding.tvEndTrip.setBackgroundResource(R.drawable.button_background_primary)
            binding.tvStartTrip.isEnabled = true
            binding.tvEndTrip.isEnabled = true
            callVehicleTypeList()
        } else {
            binding.tvStartTrip.setBackgroundResource(R.drawable.button_background_disable)
            binding.tvEndTrip.setBackgroundResource(R.drawable.button_background_disable)
            binding.tvStartTrip.isEnabled = false
            binding.tvEndTrip.isEnabled = false
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.alert),
                getString(R.string.punch_in_trip_operation),
                object : PositiveButtonListener {
                    override fun okClickListener() {
                        mActivity.checkBottomNavigationItem(0)
                        mActivity.addFragment(
                            AttendanceFragment(),
                            addToBackStack = false,
                            ignoreIfCurrent = true,
                            animationType = AnimationType.fadeInfadeOut
                        )
                    }

                },
                isCancelVisibility = false
            )
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("TAG", "onRequestPermissionsResult: " + REQUEST_PERMISSION_CODE)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            // Check if all permissions were granted
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                openAlbum()
                Toast.makeText(mActivity, "PERMISSION IS GRANTERD", Toast.LENGTH_SHORT).show()
                // All permissions granted, you can proceed with your logic
            } else {
                Toast.makeText(mActivity, "PERMISSION IS NOT GRANTED !", Toast.LENGTH_SHORT).show()
                // At least one permission was denied, handle accordingly
                // You may inform the user about the importance of the permissions
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            mActivity.bottomVisible()
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgMenu ->
                mActivity.openDrawer()
            R.id.imgStartImageFile -> {
                if (selectedVehicleType?.vehicleTypeId == VEHICLE_TYPE_NA) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.no_image_required)
                    )
                    return
                }
                askCameraGalleryPermission()
            }
            R.id.imgEndImageFile -> {
                if (selectedVehicleType?.vehicleTypeId == VEHICLE_TYPE_NA) {
                    CommonMethods.showToastMessage(mActivity, getString(R.string.no_image_required))
                    return
                }
                askCameraGalleryPermission()
            }
            R.id.tvStartTrip -> {
                val locationManager =
                    mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (currentLatitude == 0.0 || currentLongitude == 0.0) {
                        CommonMethods.showToastMessage(
                            mActivity,
                            getString(R.string.please_wait_fetching_location)
                        )
                    } else {
                        startTripValidation()
                    }
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.enable_location)
                    )
                    locationEnableDialog()
                }
            }
            R.id.tvEndTrip -> {
                val locationManager =
                    mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (currentLatitude == 0.0 || currentLongitude == 0.0) {
                        CommonMethods.showToastMessage(
                            mActivity,
                            getString(R.string.please_wait_fetching_location)
                        )
                    } else {
                        endTripValidation()
                    }
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.enable_location)
                    )
                    locationEnableDialog()
                }
            }
            R.id.imgRefresh -> {
                val locationManager =
                    mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    fusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(mActivity)
                    fetchLocation(fusedLocationClient!!)
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.enable_location)
                    )
                    locationEnableDialog()
                }
            }

        }
    }

    private fun endTripValidation() {
        if (selectedVehicleType?.vehicleTypeId != VEHICLE_TYPE_NA && binding.etEndMeterReading.text.toString()
                .trim().isEmpty()
        ) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.end_meter_validation_msg)
            )
            return
        }

        if (selectedVehicleType?.vehicleTypeId != VEHICLE_TYPE_NA && binding.etStartMeterReading.text.toString()
                .toInt() > binding.etEndMeterReading.text.toString().toInt()
        ) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_valid_meter_reading))
            return
        }
        if (selectedVehicleType?.vehicleTypeId != VEHICLE_TYPE_NA && base64String.isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.end_meter_reading_upload_validation_msg)
            )
            return
        }
        callEndTripApi()
    }

    private fun startTripValidation() {
        if (binding.tvLocation.text.toString().isEmpty()) {
            CommonMethods.showAlertDialog(
                mActivity, getString(R.string.location_error_title), getString(
                    R.string.location_ads_error_msg
                ), object : PositiveButtonListener {
                    override fun okClickListener() {
                        Toast.makeText(
                            mActivity,
                            getString(R.string.enable_location),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                isCancelVisibility = false
            )
            return
        }
        if (selectedVehicleType?.vehicleTypeId != VEHICLE_TYPE_NA && binding.etStartMeterReading.text.toString()
                .trim().isEmpty()
        ) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.start_meter_validation_msg)
            )
            return
        }
        if (selectedVehicleType?.vehicleTypeId != VEHICLE_TYPE_NA && base64String.isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.start_meter_reading_upload_validation_msg)
            )
            return
        }

        callStartTripApi()
    }

    private fun askLocationPermission() {
        val arrListOfPermission = arrayListOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
            val locationManager =
                mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(mActivity)
                fetchLocation(fusedLocationClient!!)
            } else {
                locationEnableDialog()
            }
        }
    }

    private fun askCameraGalleryPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                openAlbum()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                openAlbum()
            }
        } else {
            val arrListOffPermission = arrayListOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOffPermission) {
                openAlbum()
            }
        }


        /*val arrListOfPermission = arrayListOf<String>(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
        PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
            openAlbum()
        }*/

    }

    private fun openAlbum() {
        AlbumUtility(mActivity, true).openAlbumAndHandleCameraSelection(
            onImageSelected = {
                meterReadingImageFile = it

                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    // This code runs on a background thread
                    val modifiedImageFile: File = CommonMethods.addDateAndTimeToFile(
                        it,
                        CommonMethods.createImageFile(mActivity)!!
                    )

                    if (modifiedImageFile != null) {

                        // Simulate a time-consuming task
                        Thread.sleep(1000)

                        // Update the UI on the main thread using runOnUiThread
                        handler.post {
                            if (visitFromPlaceData?.tripId == 0) {
                                ImageUtils().loadImageFile(
                                    mActivity,
                                    modifiedImageFile,
                                    binding.imgStartImageFile
                                )
                            } else {
                                ImageUtils().loadImageFile(
                                    mActivity,
                                    modifiedImageFile,
                                    binding.imgEndImageFile
                                )
                            }
                            /*Glide.with(mActivity)
                                .load(modifiedImageFile)
                                .into(binding.imgStartImageFile)*/
                            base64String =
                                CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                    .toString()
                            binding.imgStartImageFile.scaleType = ImageView.ScaleType.CENTER_CROP
                            binding.imgEndImageFile.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    }
                }
            },
            onError = {
                CommonMethods.showToastMessage(mActivity, it)
            }
        )

    }

    private fun locationEnableDialog() {
        try {
            if (!appDao.getLoginData().todayClockInDone) {
                binding.tvLocation.text = getString(R.string.fetching_location)
            }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 30 * 1000
            locationRequest.fastestInterval = 5 * 1000
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)

            val result: Task<LocationSettingsResponse> =
                LocationServices.getSettingsClient(mActivity)
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
            Log.e("TAG", "locationEnableDialog: " + e.printStackTrace())
        }
    }


    private fun callVehicleTypeList() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val reportListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getVehicleTypeList()

        reportListCall?.enqueue(object : Callback<List<VehicleTypeListResponse>> {
            override fun onResponse(
                call: Call<List<VehicleTypeListResponse>>,
                response: Response<List<VehicleTypeListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            vehicleTypeList.clear()
                            vehicleTypeList.addAll(it)
                            setupVehicleTypeSpinner()
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null
                    )
                }
                callGetVisitFromPlaceList()
            }

            override fun onFailure(call: Call<List<VehicleTypeListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
                callGetVisitFromPlaceList()
            }
        })

    }

    private fun callGetVisitFromPlaceList(isFromEndTrip: Boolean = false) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val fromPlaceReq = JsonObject()
        fromPlaceReq.addProperty("UserId", loginData.userId)
            fromPlaceReq.addProperty("ParameterString", "")

        val fromPlaceCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getVisitFromPlace(fromPlaceReq)

        fromPlaceCall?.enqueue(object : Callback<List<GetVisitFromPlaceListResponse>> {
            override fun onResponse(
                call: Call<List<GetVisitFromPlaceListResponse>>,
                response: Response<List<GetVisitFromPlaceListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            visitFromPlaceList.clear()
                            visitFromPlaceList.addAll(it)
                            visitFromPlaceData = visitFromPlaceList[0]
                            if (visitFromPlaceData?.tripId == 0) {
                                binding.toolbar.tvHeader.text = getString(R.string.start_trip)
                                binding.tvEndTrip.setBackgroundResource(R.drawable.button_background_disable)
                                binding.tvStartTrip.setBackgroundResource(R.drawable.button_background_primary)
                                binding.tvEndTrip.isEnabled = false
                                binding.tvStartTrip.isEnabled = true
                                binding.spVehicleType.isEnabled = true
                                isTripIdZero = true
                                askLocationPermission()
                                if (isFromEndTrip) {
                                    if(mActivity != null && isAdded) {
                                        mActivity.addFragment(
                                            TripFragment(), false,
                                            ignoreIfCurrent = false,
                                            animationType = AnimationType.fadeInfadeOut
                                        )
                                    }
                                }
                            } else {
                                isTripIdZero = false
                                setupDataForEndTrip(visitFromPlaceData!!)

                            }
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<GetVisitFromPlaceListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })

    }

    private fun setupDataForEndTrip(visitFromPlaceData: GetVisitFromPlaceListResponse) {
        try {
            binding.llEndTrip.visibility = View.VISIBLE
            binding.tvStartTrip.isEnabled = false
            binding.etStartMeterReading.isEnabled = false
            binding.tvEndTrip.isEnabled = true
            binding.spVehicleType.isEnabled = false
            binding.imgStartImageFile.isClickable = false
            binding.tvStartTrip.setBackgroundResource(R.drawable.button_background_disable)
            binding.tvEndTrip.setBackgroundResource(R.drawable.button_background_primary)
            binding.toolbar.tvHeader.text = getString(R.string.end_trip)
            binding.tvLocation.text = visitFromPlaceData.fromPlace
            fromLat = visitFromPlaceData.fromPlaceLatitude
            fromLng = visitFromPlaceData.fromPlaceLongitude
            val appRegistrationData = appDao.getAppRegistration()
            ImageUtils().loadImageUrl(
                mActivity,
                appRegistrationData?.apiHostingServer + visitFromPlaceData.tripStartMeterReadingPhoto,
                binding.imgStartImageFile
            )
            binding.imgStartImageFile.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.imgEndImageFile.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.etStartMeterReading.setText(visitFromPlaceData.tripStartMeterReading.toString())
            val vehicleItem = VehicleTypeListResponse(
                visitFromPlaceData.tripVehicleTypeId,
                visitFromPlaceData.tripVehicleTypeName,
                0,
                0,
                0
            )
            val position =
                vehicleTypeList.indexOfFirst { it.vehicleTypeId == vehicleItem.vehicleTypeId }
            if (position != -1) {
                binding.spVehicleType.setSelection(position)
            }
            askLocationPermission()
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }

    }

    private fun callStartTripApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()
        val startMeterReading = binding.etStartMeterReading.text.toString().ifEmpty { "0" }

        val startTripReq = JsonObject()
        startTripReq.addProperty("TripId", 0)
        startTripReq.addProperty("UserId", loginData.userId)
        startTripReq.addProperty("VehicleTypeId", selectedVehicleType?.vehicleTypeId)
        startTripReq.addProperty("FromPlace", currentAddress)
        startTripReq.addProperty("FromPlaceLatitude", currentLatitude)
        startTripReq.addProperty("FromPlaceLongitude", currentLongitude)
        startTripReq.addProperty(
            "StartMeterReading", startMeterReading.toDouble()
        )
        startTripReq.addProperty("StartMeterReadingPhoto", base64String)
        startTripReq.addProperty("ToPlace", "")
        startTripReq.addProperty("ToPlaceLatitude", 0.0)
        startTripReq.addProperty("ToPlaceLongitude", 0.0)
        startTripReq.addProperty("TotalKM", 0)
        startTripReq.addProperty("ActualKM", 0)
        startTripReq.addProperty("CreateBy", loginData.userId)
        startTripReq.addProperty("IsActive", true)
        startTripReq.addProperty("CommandId", 0)
        startTripReq.addProperty("Remarks", "start trip")
        startTripReq.addProperty("ReturnMessage", "dadad")

        Log.e("TAG", "callStartTripApi: " + startTripReq.toString())
        val startTripCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.startTripApi(startTripReq)

        startTripCall?.enqueue(object : Callback<TripSubmitResponse> {
            override fun onResponse(
                call: Call<TripSubmitResponse>,
                response: Response<TripSubmitResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            AppPreference.saveBooleanPreference(mActivity, IS_TRIP_START, true)
                            if (mActivity != null && isAdded) {
                                CommonMethods.showAlertDialog(
                                    mActivity,
                                    getString(R.string.start_trip),
                                    it.returnMessage,//getString(R.string.start_trip_success_msg),
                                    object : PositiveButtonListener {
                                        override fun okClickListener() {
                                            mActivity.checkBottomNavigationItem(2)
                                            mActivity.addFragment(
                                                DashboardFragment(),
                                                false,
                                                true,
                                                AnimationType.fadeInfadeOut
                                            )
                                        }
                                    },
                                    isCancelVisibility = false
                                )
                            }
                        } else {
                            CommonMethods.showAlertDialog(
                                mActivity,
                                getString(R.string.start_trip),
                                it.returnMessage, null
                            )
                        }
                    }
                } else {
                    Log.e("TAG", "onResponse: "+response.message()+", "+response.body() )
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null
                    )
                }

            }

            override fun onFailure(call: Call<TripSubmitResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })

    }

    private fun callEndTripApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()
        val endTripReq = JsonObject()
        val endMeterReading = if(binding.etEndMeterReading.text.toString().trim().isEmpty()) 0 else binding.etEndMeterReading.text.toString().trim().toDouble()

        endTripReq.addProperty("TripId", visitFromPlaceData?.tripId)
        endTripReq.addProperty("UserId", loginData.userId)
        endTripReq.addProperty("EndTripLatitude", currentLatitude)
        endTripReq.addProperty("EndTripLongitude", currentLongitude)
        endTripReq.addProperty("EndTripLocation", currentAddress)
        endTripReq.addProperty("EndMeterReading", endMeterReading)
        endTripReq.addProperty("ActualKM", 0)
        endTripReq.addProperty("TotalTripKm", totalDistance)
        endTripReq.addProperty("Remarks", binding.etRemarks.text.trim().toString())
        endTripReq.addProperty("EndMeterReadingPhoto", base64String)
        endTripReq.addProperty("IsActive", true)
        endTripReq.addProperty("UpdateBy", loginData.userId)
        endTripReq.addProperty("IsTripCompleted", true.toString())

        val endTripCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.endTripApi(endTripReq)

        endTripCall?.enqueue(object : Callback<TripSubmitResponse> {
            override fun onResponse(
                call: Call<TripSubmitResponse>,
                response: Response<TripSubmitResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            AppPreference.saveBooleanPreference(mActivity, IS_TRIP_START, false)
                            if(mActivity != null && isAdded) {
                                CommonMethods.showAlertDialog(
                                    mActivity,
                                    getString(R.string.end_trip),
                                    it.returnMessage,//getString(R.string.end_trip_success_msg),
                                    object : PositiveButtonListener {
                                        override fun okClickListener() {
                                            //callGetVisitFromPlaceList(isFromEndTrip = true)
                                            mActivity.checkBottomNavigationItem(2)
                                            mActivity.addFragment(
                                                DashboardFragment(),
                                                false,
                                                true,
                                                AnimationType.fadeInfadeOut
                                            )
                                        }
                                    },
                                    isCancelVisibility = false
                                )
                            }
                        } else {
                            CommonMethods.showAlertDialog(
                                mActivity,
                                getString(R.string.end_trip),
                                it.returnMessage, null
                            )
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<TripSubmitResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })

    }


    private fun setupVehicleTypeSpinner() {
        val adapter = VehicleTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            vehicleTypeList
        )
        binding.spVehicleType.adapter = adapter
        if (isTripIdZero) {
            binding.spVehicleType.setSelection(0)
            selectedVehicleType = vehicleTypeList[0]
        }
    }

    inner class VehicleTypeAdapter(
        context: Context,
        spinnerLayout: Int,
        private var vehicleTypeResponseList: ArrayList<VehicleTypeListResponse>,
    ) : ArrayAdapter<VehicleTypeListResponse>(context, spinnerLayout, vehicleTypeResponseList) {
        private val mContext: Context = context

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val VehicleTypeData = vehicleTypeResponseList[position]
            name.text = VehicleTypeData.vehicleTypeName
            selectedVehicleType = VehicleTypeData
            if (selectedVehicleType?.vehicleTypeId == 13) {
                binding.etStartMeterReading.isEnabled = false
                binding.etEndMeterReading.isEnabled = false
                binding.imgStartImageFile.isEnabled = false
                binding.imgEndImageFile.isEnabled = false
            } else {
                binding.etStartMeterReading.isEnabled = isTripIdZero
                binding.etEndMeterReading.isEnabled = true
                binding.imgStartImageFile.isEnabled = isTripIdZero
                binding.imgEndImageFile.isEnabled = true
            }
            return listItem
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val vehicleTypeData = vehicleTypeResponseList[position]
            name.text = vehicleTypeData.vehicleTypeName
            if (selectedVehicleType?.vehicleTypeId == 13) {
                binding.etStartMeterReading.isEnabled = false
                binding.etEndMeterReading.isEnabled = false
                binding.imgStartImageFile.isEnabled = false
                binding.imgEndImageFile.isEnabled = false
            } else {
                binding.etStartMeterReading.isEnabled = isTripIdZero
                binding.etEndMeterReading.isEnabled = true
                binding.imgStartImageFile.isEnabled = isTripIdZero
                binding.imgEndImageFile.isEnabled = true
            }
            return listItem
        }
    }

    private fun fetchLocation(fusedLocationClient: FusedLocationProviderClient) {
        try {
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

                    if (AppPreference.getBooleanPreference(mActivity, IS_MOCK_LOCATION)) {
                        CommonMethods.showAlertDialog(
                            mActivity,
                            getString(R.string.location_error_title),
                            getString(R.string.mock_location_msg),
                            okListener = object : PositiveButtonListener {
                                override fun okClickListener() {
                                    mActivity.finish()
                                }
                            },
                            isCancelVisibility = false
                        )
                    }

                    currentAddress = CommonMethods.getAddressFromLocation(
                        mActivity,
                        currentLatitude,
                        currentLongitude
                    ) ?: ""

                    if (visitFromPlaceData?.tripId == 0) {
                        binding.tvLocation.text = currentAddress
                    } else {
                        val distanceCalculatorUtils = DistanceCalculatorUtils()
                        distanceCalculatorUtils.getDistance(
                            visitFromPlaceData?.fromPlaceLatitude!!,
                            visitFromPlaceData?.fromPlaceLongitude!!,
                            currentLatitude,
                            currentLongitude,
                            appDao.getLoginData().googleApiKey!!,
                            object : DistanceCalculatorUtils.DistanceCallback {
                                override fun onDistanceCalculated(distance: Float) {
                                    totalDistance = distance
                                    Log.e("TAG", "onDistanceCalculated: $totalDistance")
                                }
                            }
                        )
                    }


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

}