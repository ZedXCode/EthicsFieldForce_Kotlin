package ethicstechno.com.fieldforce.ui.fragments.moreoption.visit

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
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
import android.widget.ImageView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAddVisitBinding
import ethicstechno.com.fieldforce.listener.DatePickerListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.VisitListResponse
import ethicstechno.com.fieldforce.models.reports.VisitReportListResponse
import ethicstechno.com.fieldforce.models.trip.GetVisitFromPlaceListResponse
import ethicstechno.com.fieldforce.ui.adapter.LeaveTypeAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import java.util.concurrent.Executors


class AddVisitFragment : HomeBaseFragment(), View.OnClickListener, LeaveTypeAdapter.TypeSelect,
    DatePickerListener, UserSearchDialogUtil.PartyDealerDialogDetect {

    lateinit var binding: FragmentAddVisitBinding
    val visitTypeList: ArrayList<LeaveTypeListResponse> = arrayListOf()
    var selectedVisitType: LeaveTypeListResponse? = null
    var selectedPartyDealer: AccountMasterList? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    var fromLatitude = 0.0
    var fromLongitude = 0.0
    var totalDistance = 0.0F
    var currentAddress = ""
    private var visitData: VisitListResponse = VisitListResponse()
    private var visitReportData: VisitReportListResponse = VisitReportListResponse()
    var tripId = 0
    var previousVisitId = 0
    private var base64Image = ""
    private var imageFile: File? = null
    private var presetDate = ""
    private var presetTime = ""
    private var isReadOnly = false

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

    companion object {
        fun newInstance(
            partyDealerData: VisitListResponse,
            visitReport: VisitReportListResponse,
            readOnly: Boolean
        ): AddVisitFragment {
            val args = Bundle()
            args.putParcelable(ARG_PARAM1, partyDealerData)
            args.putParcelable(ARG_PARAM2, visitReport)
            args.putBoolean(ARG_PARAM3, readOnly)
            val fragment = AddVisitFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_visit, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            visitData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM1, VisitListResponse::class.java) ?: VisitListResponse()
            } else {
                it.getParcelable(ARG_PARAM1) ?: VisitListResponse()
            }

            visitReportData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM2, VisitReportListResponse::class.java)
                    ?: VisitReportListResponse()
            } else {
                it.getParcelable(ARG_PARAM2) ?: VisitReportListResponse()
            }
            isReadOnly = it.getBoolean(ARG_PARAM3, false)
        }
        initView()
    }

    private fun initView() {
        mActivity.bottomHide()
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.text =
            if (isReadOnly) mActivity.getString(R.string.visit_details) else mActivity.getString(R.string.add_visit_details)

        if (!isReadOnly && !AppPreference.getBooleanPreference(mActivity, IS_TRIP_START)) {
            CommonMethods.showAlertDialog(
                mActivity,
                mActivity.getString(R.string.alert),
                mActivity.getString(R.string.start_trip_validation_for_visit),
                okListener = object : PositiveButtonListener {
                    override fun okClickListener() {
                        AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                            mActivity.onBackPressedDispatcher.onBackPressed()
                        } else {
                            mActivity.onBackPressed()
                        }
                    }
                },
                isCancelVisibility = false
            )
        }

        binding.toolbar.imgBack.setOnClickListener(this)
        binding.tvDate.text =
            if (isReadOnly) visitReportData.visitDate else CommonMethods.getCurrentDate()
        binding.tvCityName.text = if (isReadOnly) visitReportData.cityName else visitData.cityName
        binding.etContactPerson.setText(if (isReadOnly) visitReportData.contactPersonName else visitData.contactPersonName)
        binding.tvSelectParty.text =
            if (isReadOnly) visitReportData.accountName else visitData.accountName
        binding.tvNextVisitFollowUpDate.text =
            if (isReadOnly) visitReportData.nextVisitDate else CommonMethods.getCurrentDate()
        binding.tvNextFollowUpTime.text =
            if (isReadOnly) visitReportData.nextVisitTime else CommonMethods.getCurrentTimeIn12HrFormat()
        binding.llVisitImage.visibility = if (isReadOnly) View.VISIBLE else View.GONE
        binding.llVisitImageUpload.visibility = if (isReadOnly) View.GONE else View.VISIBLE
        if (isReadOnly) {
            binding.etVisitDetails.setText(visitReportData.visitDetails)
            binding.etNextVisitSubject.setText(visitReportData.nextVisitSubject)
            binding.etRemarks.setText(visitReportData.remarks)
            ImageUtils().loadImageUrl(
                mActivity,
                appDao.getAppRegistration()?.apiHostingServer + visitReportData.filePath,
                binding.imgVisitImage
            )
            binding.imgVisitImage.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.tvAddVisit.visibility = View.GONE
            /*binding.imgDropDown.visibility = View.GONE
            binding.imgSearch.visibility = View.GONE*/
            binding.etVisitDetails.isEnabled = false
            binding.etNextVisitSubject.isEnabled = false
            binding.etRemarks.isEnabled = false
            binding.etContactPerson.isEnabled = false
            binding.spVisitType.isEnabled = false
        } else {
            presetDate = CommonMethods.getCurrentDate()
            presetTime = CommonMethods.getCurrentTimeIn12HrFormat()
            binding.tvNextFollowUpTime.setOnClickListener(this)
            binding.tvNextVisitFollowUpDate.setOnClickListener(this)
            //binding.llSelectParty.setOnClickListener(this)
            binding.tvAddVisit.setOnClickListener(this)
        }
        binding.cardImage.setOnClickListener(this)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                executeAPIsAndSetupData()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }

    }

    private suspend fun executeAPIsAndSetupData() {
        withContext(Dispatchers.IO) {
            try {
                val categoryListDeferred = async { callVisitTypeListApi() }
                categoryListDeferred.await()
                if (!isReadOnly) {
                    val visitFromPlaceDeferred = async { callGetVisitFromPlaceApi() }
                    visitFromPlaceDeferred.await()
                } else {
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("TAG", "executeAPIsAndSetupData: " + e.message.toString())
            }
        }


        //locationDeferred.await()

    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            mActivity.bottomHide()
        }
    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
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

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }
            R.id.tvAddVisit -> {
                visitValidation()
            }
            R.id.tvNextFollowUpTime -> {
                showTimePickerDialog()
            }
            R.id.tvNextVisitFollowUpDate -> {
                CommonMethods.openDatePickerDialog(this, mActivity)
            }
            /*R.id.llSelectParty -> {
                if(partyDealerList.size > 0) {
                    val partyDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_PARTY_DEALER,
                        partyDealerList = partyDealerList,
                        partyDealerInterfaceDetect = this as UserSearchDialogUtil.PartyDealerDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    partyDialog.showUserSearchDialog()
                }
            }*/
            R.id.cardImage -> {
                if (isReadOnly) {
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDao.getAppRegistration()?.apiHostingServer + visitReportData.filePath
                    )
                } else {
                    askCameraGalleryPermission()
                }
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

    }

    private fun openAlbum() {
        AlbumUtility(mActivity, true).openAlbumAndHandleImageSelection(
            onImageSelected = {
                imageFile = it

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
                            Glide.with(mActivity)
                                .load(modifiedImageFile)
                                .into(binding.imgVisit)
                            base64Image =
                                CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                    .toString()
                            binding.imgVisit.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    }
                }
            },
            onError = {
                CommonMethods.showToastMessage(mActivity, it)
            }
        )

    }


    private fun visitValidation() {
        if (binding.spVisitType.selectedItemPosition == 0) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.select_visit_type)
            )
            return
        }
        if (binding.etContactPerson.text.toString().trim().isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.enter_contact_person)
            )
            return
        }
        if (binding.etVisitDetails.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.enter_visit_details)
            )
            return
        }

        if ((binding.tvNextVisitFollowUpDate.text.toString() != presetDate || binding.tvNextFollowUpTime.text.toString() != presetTime) && binding.etNextVisitSubject.text.toString()
                .trim().isEmpty()
        ) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.enter_next_visit_followup_subject)
            )
            return
        }

        callAddVisitApi()
    }

    private fun callGetVisitFromPlaceApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val fromPlaceReq = JsonObject()
        fromPlaceReq.addProperty("UserId", loginData.userId)

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
                            tripId = it[0].tripId
                            previousVisitId = it[0].previousVisitId
                            fromLatitude = it[0].fromPlaceLatitude
                            fromLongitude = it[0].fromPlaceLongitude
                            askLocationPermission()
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<GetVisitFromPlaceListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })

    }

    private fun callVisitTypeListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val visitTypeCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getVisitTypeList()

        visitTypeCall?.enqueue(object : Callback<List<LeaveTypeListResponse>> {
            override fun onResponse(
                call: Call<List<LeaveTypeListResponse>>,
                response: Response<List<LeaveTypeListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            visitTypeList.clear()
                            visitTypeList.add(LeaveTypeListResponse(0, 0, "Select Visit Type", 0.0))
                            visitTypeList.addAll(it)
                            setupCategorySpinner()
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<LeaveTypeListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })

    }

    private fun setupCategorySpinner() {
        val adapter = LeaveTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            visitTypeList,
            this
        )
        binding.spVisitType.adapter = adapter

        selectedVisitType = if (isReadOnly) {
            val visitType = LeaveTypeListResponse(
                0,
                visitReportData.categoryMasterId,
                visitReportData.categoryName,
                0.0
            )
            val pos =
                visitTypeList.indexOfFirst { it.categoryMasterId == visitType.categoryMasterId }
            binding.spVisitType.setSelection(pos)
            visitType
        } else {
            binding.spVisitType.setSelection(0)
            visitTypeList[0]
        }
    }

    private fun callAddVisitApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val nextVisitDateTime =
            binding.tvNextVisitFollowUpDate.text.toString() + ", " + binding.tvNextFollowUpTime.text.toString()
        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val addVisitReq = JsonObject()
        addVisitReq.addProperty("VisitId", 0)
        addVisitReq.addProperty("CategoryMasterId", selectedVisitType?.categoryMasterId)
        addVisitReq.addProperty("CategoryName", selectedVisitType?.categoryName)
        addVisitReq.addProperty("AccountMasterId", visitData.accountMasterId)
        addVisitReq.addProperty("AccountName", visitData.accountName)
        addVisitReq.addProperty("CityId", visitData.cityId)
        addVisitReq.addProperty("CityName", visitData.cityName)
        addVisitReq.addProperty("ContactPersonName", binding.etContactPerson.text.toString())
        addVisitReq.addProperty("VisitDetails", binding.etVisitDetails.text.toString())
        addVisitReq.addProperty("NextVisitDateTime", nextVisitDateTime)

        addVisitReq.addProperty(
            "NextVisitSubject",
            binding.etNextVisitSubject.text.trim().toString()
        )
        addVisitReq.addProperty("Remakrs", binding.etRemarks.text.trim().toString())
        addVisitReq.addProperty(
            "FilePath",
            if (base64Image == null || base64Image.isEmpty()) "" else base64Image
        )
        addVisitReq.addProperty("Latitude", currentLatitude)
        addVisitReq.addProperty("Longitude", currentLongitude)
        addVisitReq.addProperty("Location", currentAddress)
        addVisitReq.addProperty("TripId", tripId)
        addVisitReq.addProperty("PreviousVisitId", previousVisitId)
        addVisitReq.addProperty("MapKM", totalDistance)
        addVisitReq.addProperty("IsActive", true.toString())
        addVisitReq.addProperty("CommandId", 0)
        addVisitReq.addProperty("CreateBy", 0)
        addVisitReq.addProperty("CreateDateTime", CommonMethods.getCurrentDateTime())
        addVisitReq.addProperty("UpdateBy", 0)
        addVisitReq.addProperty("UpdateDateTime", CommonMethods.getCurrentDateTime())
        addVisitReq.addProperty("Deleteby", 0)
        addVisitReq.addProperty("DeleteDateTime", CommonMethods.getCurrentDateTime())
        addVisitReq.addProperty("Success", false.toString())
        addVisitReq.addProperty("ReturnMessage", "")
        addVisitReq.addProperty("UserId", loginData.userId)
        addVisitReq.addProperty("VisitDate", binding.tvNextVisitFollowUpDate.text.toString())
        addVisitReq.addProperty("VisitTime", binding.tvNextFollowUpTime.text.toString())
        addVisitReq.addProperty("FromDate", "")
        addVisitReq.addProperty("ToDate", "")


        print("MY REQ ::::::: " + addVisitReq)
        Log.e("TAG", "callAddExpenseApi: ADD EXPENSE REQ :: " + addVisitReq)
        val expenseTypeCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.addVisit(addVisitReq)

        expenseTypeCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        Log.e("TAG", "onResponse: ADD EXPENSE RESPONSE :: " + it.toString())
                        CommonMethods.showAlertDialog(
                            mActivity,
                            mActivity.getString(R.string.add_visit),
                            it.returnMessage,
                            object : PositiveButtonListener {
                                override fun okClickListener() {
                                    if (it.success) {
                                        AppPreference.saveBooleanPreference(
                                            mActivity,
                                            IS_DATA_UPDATE,
                                            true
                                        )
                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                            mActivity.onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            mActivity.onBackPressed()
                                        }
                                    }
                                }
                            }, isCancelVisibility = false
                        )
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }

        })

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
            val exception = "[DalPaperDetailsActivity] *ERROR* IN  :: error = $e"
            Log.e("TAG", "locationEnableDialog: $exception")
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
                    val fromLocation = Location("")
                    fromLocation.latitude = loginData.hqLatitude
                    fromLocation.longitude = loginData.hqLongitude
                    val toLocation = Location("")
                    toLocation.latitude = currentLatitude
                    toLocation.longitude = currentLongitude

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

                    currentAddress = CommonMethods.getAddressFromLocation(
                        mActivity,
                        currentLatitude,
                        currentLongitude
                    ) ?: ""

                    val distanceCalculatorUtils = DistanceCalculatorUtils()
                    distanceCalculatorUtils.getDistance(
                        fromLatitude,
                        fromLongitude,
                        currentLatitude,
                        currentLongitude,
                        appDao.getLoginData().googleApiKey!!,
                        //mActivity.getString(R.string.google_key),
                        object : DistanceCalculatorUtils.DistanceCallback {
                            override fun onDistanceCalculated(distance: Float) {
                                totalDistance = distance
                                Log.e("TAG", "onDistanceCalculated: " + totalDistance)
                            }
                        }
                    )

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
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onTypeSelect(typeData: LeaveTypeListResponse) {
        selectedVisitType = typeData
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a TimePickerDialog
        val timePickerDialog = TimePickerDialog(
            mActivity,
            { view, selectedHour, selectedMinute ->
                // Handle the selected time (hour and minute) here
                val amPm: String
                val hour12: Int

                if (selectedHour >= 12) {
                    amPm = "PM"
                    hour12 = if (selectedHour > 12) selectedHour - 12 else selectedHour
                } else {
                    amPm = "AM"
                    hour12 = if (selectedHour == 0) 12 else selectedHour
                }

                val time = String.format("%02d:%02d %s", hour12, selectedMinute, amPm)
                binding.tvNextFollowUpTime.text = time
            },
            hour,
            minute,
            false
        )

        timePickerDialog.show()
    }

    override fun onDateSelect(date: String) {
        binding.tvNextVisitFollowUpDate.text = date
    }

    override fun partyDealerSelect(partyDealerData: AccountMasterList) {
        selectedPartyDealer = partyDealerData
        Log.e("TAG", "partyDealerSelect: " + selectedPartyDealer?.accountName)
        binding.tvSelectParty.text = partyDealerData.accountName
    }


}