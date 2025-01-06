package ethicstechno.com.fieldforce.ui.fragments.bottomnavigation

import AnimationType
import addFragment
import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import ethicstechno.com.fieldforce.BuildConfig
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAttendanceBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.attendance.CurrentMonthAttendanceResponse
import ethicstechno.com.fieldforce.models.attendance.PunchInResponse
import ethicstechno.com.fieldforce.models.attendance.UserLastSyncResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.reports.TripSummeryReportResponse
import ethicstechno.com.fieldforce.models.reports.UserListResponse
import ethicstechno.com.fieldforce.service.EthicsBackgroundService
import ethicstechno.com.fieldforce.ui.adapter.AttendanceAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.MapViewFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.dateTypeList
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.formatDateToMMMyyyy
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DistanceCalculatorUtils
import ethicstechno.com.fieldforce.utils.EXTRA_LATITUDE
import ethicstechno.com.fieldforce.utils.EXTRA_LONGITUDE
import ethicstechno.com.fieldforce.utils.ID_ZERO
import ethicstechno.com.fieldforce.utils.IS_FOR_ATTENDANCE_REPORT
import ethicstechno.com.fieldforce.utils.IS_MOCK_LOCATION
import ethicstechno.com.fieldforce.utils.PRESENT_DAY
import ethicstechno.com.fieldforce.utils.PermissionUtil
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceFragment : HomeBaseFragment(), View.OnClickListener, FilterDialogListener,
    UserSearchDialogUtil.UserSearchDialogDetect, AttendanceAdapter.LocationClick {

    lateinit var binding: FragmentAttendanceBinding
    var attendanceList: ArrayList<CurrentMonthAttendanceResponse> = arrayListOf()
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    var currentAddress = ""
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var totalDistance = 0.0F
    var lastSyncDateTime = ""
    var lastSyncLocation = ""
    var isFromAttendanceReport = false
    var userList: ArrayList<UserListResponse> = arrayListOf()
    var selectedUser = UserListResponse()
    var startDate = ""
    var endDate = ""
    var selectedDateOptionPosition = 4 // This MONTH
    var serviceLat = "0.0"
    var serviceLng = "0.0"
    var userLastSyncData: UserLastSyncResponse? = null

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

    private val settingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val locationManager =
            mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(mActivity)
            fetchLocation(fusedLocationClient!!)
        } else {
            locationEnableDialog()
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.enable_location))
        }
    }

    companion object {

        fun newInstance(
            optionType: Boolean
        ): AttendanceFragment {
            val args = Bundle()
            args.putBoolean(ARG_PARAM1, optionType)
            val fragment = AttendanceFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_attendance, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            isFromAttendanceReport = it.getBoolean(ARG_PARAM1, false)
            if (isFromAttendanceReport) {
                AppPreference.saveBooleanPreference(mActivity, IS_FOR_ATTENDANCE_REPORT, true)
            } else {
                AppPreference.saveBooleanPreference(mActivity, IS_FOR_ATTENDANCE_REPORT, false)
            }
        }
        initView()

    }



    private fun initView() {
        startDate = CommonMethods.getStartDateOfCurrentMonth()
        endDate = CommonMethods.getCurrentDate()

        binding.toolbar.imgMenu.visibility = if (isFromAttendanceReport) View.GONE else View.VISIBLE
        binding.toolbar.imgBack.visibility = if (isFromAttendanceReport) View.VISIBLE else View.GONE
        if(isFromAttendanceReport) {
            binding.toolbar.tvHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F)
        }
        binding.toolbar.tvHeader.text = if(isFromAttendanceReport) mActivity.getString(R.string.attendance_report) else mActivity.getString(R.string.attendance)
        binding.toolbar.imgFilter.visibility =
            if (isFromAttendanceReport) View.VISIBLE else View.GONE
        binding.toolbar.imgShare.visibility =
            if (isFromAttendanceReport) View.VISIBLE else View.GONE

        if (isFromAttendanceReport) {
            mActivity.bottomHide()
            binding.llAttendanceLayout.visibility = View.GONE
            binding.llAttendanceReportLayout.visibility = View.VISIBLE
            binding.toolbar.imgFilter.setOnClickListener(this)
            binding.toolbar.imgShare.setOnClickListener(this)
            binding.llAttendanceHeader.llAttendanceReport.visibility = View.VISIBLE
            binding.llAttendanceHeader.llAttendance.visibility = View.GONE
        } else {
            mActivity.bottomVisible()
            binding.llAttendanceLayout.visibility = View.VISIBLE
            binding.llAttendanceReportLayout.visibility = View.GONE
            binding.llAttendanceHeader.llAttendance.visibility = View.VISIBLE
            binding.llAttendanceHeader.llAttendanceReport.visibility = View.GONE
        }

        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.imgMenu.setOnClickListener(this)
        binding.tvPunchIn.setOnClickListener(this)
        binding.tvPunchOut.setOnClickListener(this)
        binding.llAttendanceReportHeader.tvUsername.setOnClickListener(this)

        val currentDate = Date() // Replace this with your date
        binding.tvCurrentMonth.text = formatDateToMMMyyyy(currentDate)

        if (!loginData.todayClockInDone && !loginData.todayClockOutDone) {
            binding.tvAttendanceLabel.text = mActivity.getString(R.string.please_punch_in_to_start)
            binding.tvPunchOut.setBackgroundResource(R.drawable.button_background_disable)
            binding.tvPunchOut.isEnabled = false
        } else if (loginData.todayClockInDone && !loginData.todayClockOutDone) {
            binding.tvAttendanceLabel.text = "Don't Forget To Punch Out"
            binding.tvPunchIn.setBackgroundResource(R.drawable.button_background_disable)
            binding.tvPunchIn.isEnabled = false
        } else if(loginData.todayClockInDone && loginData.todayClockOutDone){
            binding.tvAttendanceLabel.text = mActivity.getString(R.string.your_today_attendance_done)
            binding.tvPunchIn.isEnabled = false
            binding.tvPunchOut.isEnabled = false
            binding.tvPunchOut.setBackgroundResource(R.drawable.button_background_disable)
            binding.tvPunchIn.setBackgroundResource(R.drawable.button_background_disable)
        }else{
            binding.tvAttendanceLabel.text = mActivity.getString(R.string.your_today_attendance_done)
            binding.tvPunchIn.isEnabled = false
            binding.tvPunchOut.isEnabled = false
            binding.tvPunchOut.setBackgroundResource(R.drawable.button_background_disable)
            binding.tvPunchIn.setBackgroundResource(R.drawable.button_background_disable)
        }



        if (isFromAttendanceReport) {
            selectedUser = UserListResponse(loginData.userId, loginData.userName ?: "")
            binding.llAttendanceReportHeader.tvUsername.text = selectedUser.userName
            setupAttendanceReportData()
            callUserListApi()
        }

        callGetUsersLastSyncLocationApi()
    }

    private fun setupAttendanceReportData() {
        binding.llAttendanceReportHeader.tvUsername.text = loginData.userName
        selectedUser = UserListResponse(loginData.userId, loginData.userName ?: "")
        binding.llAttendanceReportHeader.tvDateOption.text = dateTypeList[4]
        val dateRange =
            "${CommonMethods.getStartDateOfCurrentMonth()} To ${CommonMethods.getCurrentDate()}"
        binding.llAttendanceReportHeader.tvDateRange.text = dateRange
    }

    override fun onResume() {
        super.onResume()
        if(isFromAttendanceReport){
            mActivity.bottomHide()
        }else {
            mActivity.bottomVisible()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            if(isFromAttendanceReport){
                mActivity.bottomHide()
            }else {
                mActivity.bottomVisible()
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgMenu ->
                mActivity.openDrawer()
            R.id.imgBack -> {
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }
            R.id.imgFilter -> {
                CommonMethods.showFilterDialog(
                    this,
                    mActivity,
                    startDate,
                    endDate,
                    selectedDateOptionPosition
                )
            }
            R.id.imgShare -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    createAttendanceReport()
                }else{
                    val arrayPermissions = arrayListOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    PermissionUtil(mActivity).requestPermissions(arrayPermissions){
                        createAttendanceReport()
                    }
                }
            }
            R.id.tvPunchIn -> {
                val locationManager = mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    if(currentLatitude == 0.0 || currentLongitude == 0.0){
                        CommonMethods.showToastMessage(mActivity, getString(R.string.please_wait_fetching_location))
                    }else{
                        callPunchInApi()
                    }
                }else{
                    CommonMethods.showToastMessage(mActivity, getString(R.string.enable_location))
                    openLocationSettings()
                }

            }
            R.id.tvPunchOut -> {
                val locationManager = mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    if(currentLatitude == 0.0 || currentLongitude == 0.0){
                        CommonMethods.showToastMessage(mActivity, getString(R.string.please_wait_fetching_location))
                    }else{
                        callPunchOutApi()
                    }
                }else{
                    CommonMethods.showToastMessage(mActivity, getString(R.string.enable_location))
                    openLocationSettings()
                }
            }
            R.id.tvUsername -> {
                Log.e("TAG", "onClick: ${userList.size}")
                if (userList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        userList = userList,
                        userDialogInterfaceDetect = this as UserSearchDialogUtil.UserSearchDialogDetect,
                    )
                    userDialog.showUserSearchDialog()
                }
            }
        }
    }

    private fun openLocationSettings() {
        val locationManager =
            mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if location services are enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Build an intent to open location settings
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

            // Check if there's an activity that can handle the intent
            if (settingsIntent.resolveActivity(mActivity.packageManager) != null) {
                // Start the activity to open location settings using the launcher
                settingLauncher.launch(settingsIntent)
            } else {
                // Handle the case where there's no activity to handle the intent
                // You may want to display a message to the user
                Log.e("TAG", "No activity to handle the intent")
            }
        }
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

    private fun callGetUsersLastSyncLocationApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null,
                isCancelVisibility = false
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = loginData

        val userLastSyncLocationReq = JsonObject()
        userLastSyncLocationReq.addProperty("UserId", loginData.userId)

        val userLastSyncLocationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getUserLastSyncLocation(userLastSyncLocationReq)

        userLastSyncLocationCall?.enqueue(object : Callback<List<UserLastSyncResponse>> {
            override fun onResponse(
                call: Call<List<UserLastSyncResponse>>,
                response: Response<List<UserLastSyncResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            userLastSyncData = it[0]
                            appDao.updateAttendanceId(userLastSyncData?.attendanceId!!, loginData.userId)
                            if (userLastSyncData?.attendanceId!! > 0) {
                                lastSyncLocation = userLastSyncData?.lastSyncLocation!!
                                lastSyncDateTime = userLastSyncData?.lastSyncDateTime!!
                                binding.tvLocation.text = lastSyncLocation
                            } else {
                                lastSyncDateTime = userLastSyncData?.lastSyncDateTime!!
                            }

                            binding.tvDateTime.text = lastSyncDateTime
                            callCurrentMonthAttendanceApi(
                                CommonMethods.getStartDateOfCurrentMonth(),
                                CommonMethods.getCurrentDate()
                            )
                            if (!isFromAttendanceReport) {
                                askLocationPermission()
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

            override fun onFailure(call: Call<List<UserLastSyncResponse>>, t: Throwable) {
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

    private fun callCurrentMonthAttendanceApi(startDate: String, endDate: String) {
        Log.e("TAG", "callCurrentMonthAttendanceApi: " + startDate + ", " + endDate)
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = loginData

        val attendanceListReq = JsonObject()
        attendanceListReq.addProperty("UserId", if(isFromAttendanceReport) selectedUser.userId else loginData.userId)
        attendanceListReq.addProperty("StartDate", startDate)
        attendanceListReq.addProperty("EndDate", endDate)

        CommonMethods.getBatteryPercentage(mActivity)

        val attendanceListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCurrentMonthAttendance(attendanceListReq)

        attendanceListCall?.enqueue(object : Callback<List<CurrentMonthAttendanceResponse>> {
            override fun onResponse(
                call: Call<List<CurrentMonthAttendanceResponse>>,
                response: Response<List<CurrentMonthAttendanceResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            attendanceList.clear()
                            attendanceList.addAll(it)
                            setupAttendanceAdapter()
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<CurrentMonthAttendanceResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
                Log.e("TAG", "onFailure: ${t.message}")
            }
        })

    }

    private fun setupAttendanceAdapter() {
        val attendanceAdapter = AttendanceAdapter(
            attendanceList,
            null,
            isFromAttendanceReport = isFromAttendanceReport,
            isFromTripSummery = false,
            this
        )
        binding.rvAttendance.adapter = attendanceAdapter
        binding.rvAttendance.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    private fun callPunchInApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val punchInReq = JsonObject()
        punchInReq.addProperty("AttendanceId", ID_ZERO)
        punchInReq.addProperty("UserId", loginData.userId)
        punchInReq.addProperty("PunchInLatitude", currentLatitude)
        punchInReq.addProperty("PunchInLongitude", currentLongitude)
        punchInReq.addProperty("AttendanceStatus", PRESENT_DAY)
        punchInReq.addProperty("PunchInLocation", currentAddress)
        punchInReq.addProperty("MapKMFromHQ", totalDistance)

        Log.e("TAG", "callLoginApi: " + punchInReq.toString())
        val punchInCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.punchInApi(punchInReq)

        punchInCall?.enqueue(object : Callback<PunchInResponse> {
            override fun onResponse(
                call: Call<PunchInResponse>,
                response: Response<PunchInResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (!it.success) {
                            CommonMethods.showAlertDialog(
                                mActivity,
                                getString(R.string.app_name),
                                it.returnMessage,
                                null
                            )
                            return
                        }
                        binding.tvPunchIn.setBackgroundResource(R.drawable.button_background_disable)
                        binding.tvPunchOut.setBackgroundResource(R.drawable.button_background_primary)
                        binding.tvPunchIn.isEnabled = false
                        binding.tvAttendanceLabel.text = getString(R.string.dont_forget_to_punch_out)
                        appDao.updatePunchInFlag(true, loginData.userId)
                        appDao.updateAttendanceId(it.attendanceId, loginData.userId)
                        CommonMethods.showAlertDialog(
                            mActivity,
                            getString(R.string.app_name),
                            it.returnMessage,//getString(R.string.str_punch_in_msg),
                            okListener = object : PositiveButtonListener {
                                override fun okClickListener() {
                                    callCurrentMonthAttendanceApi(
                                        CommonMethods.getStartDateOfCurrentMonth(),
                                        CommonMethods.getCurrentDate()
                                    )
                                    startLocationService()
                                }
                            },
                            isCancelVisibility = false
                        )
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<PunchInResponse>, t: Throwable) {
                Log.e("TAG", "onFailure: " + t.message)
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
                CommonMethods.hideLoading()
            }
        })
    }

    private fun callPunchOutApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val punchOutReq = JsonObject()
        punchOutReq.addProperty("AttendanceId", loginData.attendanceId)
        punchOutReq.addProperty("UserId", loginData.userId)
        punchOutReq.addProperty("PunchOutLatitude", currentLatitude)
        punchOutReq.addProperty("PunchOutLongitude", currentLongitude)
        punchOutReq.addProperty("Remarks", "sample clockout")
        punchOutReq.addProperty("PunchOutLocation", currentAddress)

        Log.e("TAG", "callLoginApi: " + punchOutReq.toString())
        val punchOutCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.punchOutApi(punchOutReq)

        punchOutCall?.enqueue(object : Callback<PunchInResponse> {
            override fun onResponse(
                call: Call<PunchInResponse>,
                response: Response<PunchInResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (!it.success) {
                            CommonMethods.showAlertDialog(
                                mActivity,
                                it.returnMessage,//getString(R.string.punch_out_unsuccessful),
                                it.returnMessage,
                                null
                            )
                            return
                        }

                        appDao.updateAttendanceId(0, loginData.userId)
                        appDao.updatePunchOutFlag(true, loginData.userId)
                        CommonMethods.showAlertDialog(
                            mActivity,
                            it.returnMessage,//getString(R.string.punch_out_success),
                            getString(R.string.punch_out_success_msg),
                            okListener = object : PositiveButtonListener{
                                override fun okClickListener() {
                                 callCurrentMonthAttendanceApi(
                                     CommonMethods.getStartDateOfCurrentMonth(),
                                     CommonMethods.getCurrentDate())
                                }
                            },
                            isCancelVisibility = false
                        )
                        binding.tvPunchIn.isEnabled = false
                        binding.tvPunchOut.isEnabled = false
                        binding.tvAttendanceLabel.text = getString(R.string.your_today_attendance_is_done)
                        binding.tvPunchOut.setBackgroundResource(R.drawable.button_background_disable)
                        binding.tvPunchIn.setBackgroundResource(R.drawable.button_background_disable)
                        val mServiceIntent = Intent(mActivity, EthicsBackgroundService::class.java)
                        if (isMyServiceRunning(EthicsBackgroundService::class.java)) {
                            mActivity.stopService(mServiceIntent)
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<PunchInResponse>, t: Throwable) {
                Log.e("TAG", "onFailure: " + t.message)
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
                CommonMethods.hideLoading()
            }
        })
    }

    private fun callUserListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null
            )
            return
        }

        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val userListReq = JsonObject()
        userListReq.addProperty("UserId", loginData.userId)


        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.getUserList(userListReq)

        appRegistrationCall?.enqueue(object : Callback<List<UserListResponse>> {
            override fun onResponse(
                call: Call<List<UserListResponse>>,
                response: Response<List<UserListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            userList = arrayListOf()
                            userList.addAll(it)

                            //setupUserSpinner()
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                getString(R.string.something_went_wrong)
                            )
                        }
                    }
                }else{
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<UserListResponse>>, t: Throwable) {
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

    private fun locationEnableDialog() {
        try {
            if(!appDao.getLoginData().todayClockInDone) {
                binding.tvLocation.text = getString(R.string.fetching_location)
            }
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
            if(!appDao.getLoginData().todayClockInDone) {
                binding.tvLocation.text = getString(R.string.fetching_location)
            }
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 1000
            locationRequest.fastestInterval = 1000
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
                        AppPreference.saveBooleanPreference(mActivity, IS_MOCK_LOCATION, location.isMock)
                    } else {
                        AppPreference.saveBooleanPreference(mActivity, IS_MOCK_LOCATION, location.isFromMockProvider)
                    }

                    if(AppPreference.getBooleanPreference(mActivity, IS_MOCK_LOCATION)){
                        CommonMethods.showAlertDialog(mActivity, getString(R.string.location_error_title),
                            getString(R.string.mock_location_msg),
                            okListener = object :PositiveButtonListener{
                                override fun okClickListener() {
                                    mActivity.finish()
                                }
                            }, isCancelVisibility = false)
                    }

                    val distanceCalculatorUtils = DistanceCalculatorUtils()
                    distanceCalculatorUtils.getDistance(
                        loginData.hqLatitude,
                        loginData.hqLongitude,
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
                    Log.e("TAG", "onLocationResult: TOTAL DISTANCE :: $totalDistance")
                    currentAddress = CommonMethods.getAddressFromLocation(
                        mActivity,
                        currentLatitude,
                        currentLongitude
                    ) ?: ""
                    if(userLastSyncData?.attendanceId!! == 0) {
                        binding.tvLocation.text = currentAddress
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

    override fun onFilterSubmitClick(
        startDateSubmit: String,
        endDateSubmit: String,
        dateOption: String,
        dateOptionPosition: Int,
        statusPosition: Int,
        selectedItemPosition: FilterListResponse,
        toString: FilterListResponse,
        visitType: CategoryMasterResponse,
        partyDealer: AccountMasterList,
        visitPosition: Int
    ) {
        startDate = startDateSubmit
        endDate = endDateSubmit
        selectedDateOptionPosition = dateOptionPosition
        callCurrentMonthAttendanceApi(startDate, endDate)
        binding.llAttendanceReportHeader.tvDateOption.text = dateOption
        binding.llAttendanceReportHeader.tvDateRange.text = "$startDate To $endDate"
    }

    override fun userSelect(userData: UserListResponse) {
        selectedUser = userData
        binding.llAttendanceReportHeader.tvUsername.text = userData.userName
        callCurrentMonthAttendanceApi(startDate, endDate)
    }

    override fun onLocationClick(currentMonthData: CurrentMonthAttendanceResponse, tripSummeryData: TripSummeryReportResponse) {
        if((currentMonthData.punchInTime ?: "").isEmpty()){
            CommonMethods.showToastMessage(mActivity, "Attendance data not found")
            return
        }
        mActivity.addFragment(
            MapViewFragment.newInstance(
                currentMonthData.attendanceId,
                0,
                false,
                currentMonthData.punchInLatitude,
                currentMonthData.punchInLongitude,
                "Punch In" + " (${currentMonthData.punchInTime})",
                currentMonthData.punchInLocation,
                currentMonthData.punchOutLatitude,
                currentMonthData.punchOutLongitude,
                "Punch Out" + " (${currentMonthData.punchOutTime ?: ""})",
                currentMonthData.punchOutLocation,
                true
            ),
            true, true, AnimationType.fadeInfadeOut
        )
    }

    private fun createAttendanceReport() {
        CommonMethods.showToastMessage(mActivity, "Report Generating..")
        val document = Document(PageSize.A4)
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "ATTENDANCE_REPORT_$timeStamp.pdf"
        val filePath = File(directory, fileName)

        try {
            val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))
            document.open()

// Add a title to the document
            val titleFont = Font(Font.FontFamily.HELVETICA, 26f, Font.BOLD, BaseColor.BLUE)
            val titlePhrase = Phrase("ATTENDANCE REPORT", titleFont)
            val headerCell = PdfPCell(titlePhrase)
            headerCell.colspan = 6
            headerCell.horizontalAlignment = Element.ALIGN_CENTER
            headerCell.verticalAlignment = Element.ALIGN_MIDDLE
            headerCell.fixedHeight = 40f
            headerCell.border = PdfPCell.NO_BORDER
            val headerTable = PdfPTable(1)
            headerTable.widthPercentage = 100f
            headerTable.addCell(headerCell)
            document.add(headerTable)
            document.add(Paragraph("\n"))
// Add customer name, start date, and end date
            val infoFont = Font(Font.FontFamily.HELVETICA, 15f, Font.NORMAL, BaseColor.WHITE)
            val infoPhrase = Phrase(
                "User Name : ${loginData.userName ?: ""}\n" +
                        "Start date: $startDate  " +
                        "End date: $endDate",
                infoFont
            )
            val infoCell = PdfPCell(infoPhrase)
            infoCell.colspan = 6
            infoCell.horizontalAlignment = Element.ALIGN_LEFT
            infoCell.verticalAlignment = Element.ALIGN_MIDDLE
            infoCell.fixedHeight = 60f
            infoCell.paddingTop = 8f
            infoCell.paddingRight = 8f
            infoCell.paddingLeft = 8f
            infoCell.paddingBottom = 8f
            infoCell.backgroundColor = BaseColor.BLUE // Add background color
            val infoTable = PdfPTable(1)
            infoTable.widthPercentage = 100f
            infoTable.addCell(infoCell)
            document.add(infoTable)

            document.add(Paragraph("\n"))

            val table = PdfPTable(5)
            table.widthPercentage = 100f

// Add headers to the table with improved styling
            val headerFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor.WHITE)
            table.addCell(CommonMethods.getStyledCell(getString(R.string.sr_no), headerFont, BaseColor.BLUE))
            table.addCell(CommonMethods.getStyledCell(getString(R.string.date), headerFont, BaseColor.BLUE))
            table.addCell(CommonMethods.getStyledCell(getString(R.string.in_time), headerFont, BaseColor.BLUE))
            table.addCell(CommonMethods.getStyledCell(getString(R.string.out_time), headerFont, BaseColor.BLUE))
            table.addCell(CommonMethods.getStyledCell(getString(R.string.type), headerFont, BaseColor.BLUE))

// Add data to the table
            val dataFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.BLACK)
            for (i in attendanceList.indices) {
                table.addCell(CommonMethods.getStyledCell((i + 1).toString(), dataFont, BaseColor.WHITE))
                table.addCell(CommonMethods.getStyledCell(attendanceList[i].punchInDate, dataFont, BaseColor.WHITE))
                table.addCell(CommonMethods.getStyledCell(attendanceList[i].punchInTime, dataFont, BaseColor.WHITE))
                table.addCell(CommonMethods.getStyledCell(attendanceList[i].punchOutTime, dataFont, BaseColor.WHITE))
                table.addCell(CommonMethods.getStyledCell(attendanceList[i].attendanceStatus, dataFont, BaseColor.WHITE))
            }

            document.add(table)
            document.close()
            writer.close()

            openPDFWithIntent(filePath)

        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
        }
    }

    private fun openPDFWithIntent(file: File) {
        try {
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Use FileProvider for Android 7.0 and above
                FileProvider.getUriForFile(
                    mActivity,
                    BuildConfig.APPLICATION_ID + ".fileProvider",
                    file
                )
            } else {
                // For older devices, use Uri.fromFile
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)

        } catch (e: ActivityNotFoundException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            // Handle the exception if a PDF viewer is not installed
            CommonMethods.showToastMessage(mActivity, "Error : "+e.message.toString())
            Toast.makeText(mActivity, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun startLocationService() {

        val arrListOfPermission = arrayListOf<String>(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
            if (appDao.getLoginData().attendanceId > 0) {

                LocalBroadcastManager.getInstance(mActivity).registerReceiver(
                    object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            serviceLat = intent.getStringExtra(EXTRA_LATITUDE) ?: "0.01"
                            serviceLng = intent.getStringExtra(EXTRA_LONGITUDE) ?: "0.01"
                            if (serviceLat == "0.01" && serviceLng == "0.01") {
                                CommonMethods.showToastMessage(mActivity, "Unable to fetch location. Please allow the location for this app")
                            }
                        }
                    }, IntentFilter(EthicsBackgroundService().ACTION_LOCATION_BROADCAST)
                )
                val mServiceIntent = Intent(mActivity, EthicsBackgroundService::class.java)

                if (!isMyServiceRunning(EthicsBackgroundService::class.java)) {
                    mActivity.startService(mServiceIntent)
                }
            } else {
                serviceLat = "0.01"
                serviceLng = "0.01"
            }
        }


    }

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = mActivity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }

}