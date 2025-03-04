package ethicstechno.com.fieldforce.ui.fragments.moreoption.visit

import AnimationType
import addFragment
import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import ethicstechno.com.fieldforce.BuildConfig
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAddVisitBinding
import ethicstechno.com.fieldforce.listener.DatePickerListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.CommonDropDownResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.AddVisitSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.BranchMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CompanyMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.DivisionMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.InquiryResponse
import ethicstechno.com.fieldforce.models.reports.VisitReportListResponse
import ethicstechno.com.fieldforce.models.trip.GetVisitFromPlaceListResponse
import ethicstechno.com.fieldforce.ui.adapter.ImageAdapter
import ethicstechno.com.fieldforce.ui.adapter.LeaveTypeAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.inquiry.AddInquiryEntryFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry.AddOrderEntryFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.ARG_PARAM2
import ethicstechno.com.fieldforce.utils.ARG_PARAM3
import ethicstechno.com.fieldforce.utils.AlbumUtility
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.modifyOrientation
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DROP_DOWN_STAGE
import ethicstechno.com.fieldforce.utils.DROP_DOWN_VISIT_TYPE
import ethicstechno.com.fieldforce.utils.DistanceCalculatorUtils
import ethicstechno.com.fieldforce.utils.FORM_ID_VISIT
import ethicstechno.com.fieldforce.utils.FOR_BRANCH
import ethicstechno.com.fieldforce.utils.FOR_COMPANY
import ethicstechno.com.fieldforce.utils.FOR_DIVISION
import ethicstechno.com.fieldforce.utils.FOR_STAGE
import ethicstechno.com.fieldforce.utils.FOR_VISIT_TYPE
import ethicstechno.com.fieldforce.utils.ID_ZERO
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.IS_MOCK_LOCATION
import ethicstechno.com.fieldforce.utils.IS_TRIP_START
import ethicstechno.com.fieldforce.utils.ImagePreviewCommonDialog
import ethicstechno.com.fieldforce.utils.PermissionUtil
import ethicstechno.com.fieldforce.utils.TimeType
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors


class AddVisitFragment : HomeBaseFragment(), View.OnClickListener, LeaveTypeAdapter.TypeSelect,
    DatePickerListener, UserSearchDialogUtil.PartyDealerDialogDetect,
    UserSearchDialogUtil.CompanyDialogDetect,
    UserSearchDialogUtil.DivisionDialogDetect, UserSearchDialogUtil.BranchDialogDetect,
    UserSearchDialogUtil.CommonDropDownDialogDetect {

    lateinit var binding: FragmentAddVisitBinding
    val visitTypeList: ArrayList<CommonDropDownResponse> = arrayListOf()
    val categoryList: ArrayList<CategoryMasterResponse> = arrayListOf()
    val companyMasterList: ArrayList<CompanyMasterResponse> = arrayListOf()
    val branchMasterList: ArrayList<BranchMasterResponse> = arrayListOf()
    val divisionMasterList: ArrayList<DivisionMasterResponse> = arrayListOf()
    val stageList: ArrayList<CommonDropDownResponse> = arrayListOf()
    val inquiryList: ArrayList<InquiryResponse> = arrayListOf()

    //val stageMasterList: ArrayList<StageMasterResponse> = arrayListOf()
    //val categoryMasterList: ArrayList<CategoryRe> = arrayListOf()
    var selectedVisitType: CommonDropDownResponse? = null
    var selectedCategory: CategoryMasterResponse? = null
    var selectedPartyDealer: AccountMasterList? = null

    var selectedCompany: CompanyMasterResponse? = null
    var selectedBranch: BranchMasterResponse? = null
    var selectedDivision: DivisionMasterResponse? = null
    var selectedStage: CommonDropDownResponse? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    var fromLatitude = 0.0
    var fromLongitude = 0.0
    var totalDistance = 0.0F
    var currentAddress = ""
    private var visitData: AccountMasterList = AccountMasterList()
    private var visitReportData: VisitReportListResponse = VisitReportListResponse()
    var tripId = 0
    var previousVisitId = 0
    private var base64SelfieImage = ""
    private var selfieImageFile: File? = null
    private var presetDate = ""
    private var presetTime = ""
    private var isReadOnly = false
    private var isExpanded = false
    private var selectedModeOfCommunication = 0
    private var selectedStatus = 0
    private var imageAnyList: ArrayList<Any> = arrayListOf()
    private var imageAdapter: ImageAdapter? = null
    private var inquiryDialog: AlertDialog? = null
    private var visitDetailsList: List<InquiryResponse> = arrayListOf()
    var initialTime = ""
    private var isCompanyChange = false
    var visitId = 0
    private lateinit var options: FaceDetectorOptions
    var totalFaceCount: Int = 0

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
            partyDealerData: AccountMasterList,
            visitId: Int,
            readOnly: Boolean
        ): AddVisitFragment {
            val args = Bundle()
            args.putParcelable(ARG_PARAM1, partyDealerData)
            args.putInt(ARG_PARAM2, visitId)
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
                it.getParcelable(ARG_PARAM1, AccountMasterList::class.java) ?: AccountMasterList()
            } else {
                it.getParcelable(ARG_PARAM1) ?: AccountMasterList()
            }

            /*visitReportData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM2, VisitReportListResponse::class.java)
                    ?: VisitReportListResponse()
            } else {
                it.getParcelable(ARG_PARAM2) ?: VisitReportListResponse()
            }*/
            visitId = it.getInt(ARG_PARAM2, 0)
            isReadOnly = it.getBoolean(ARG_PARAM3, false)
        }
        initView()
    }

    private fun initView() {
        mActivity.bottomHide()
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.text =
            if (isReadOnly) mActivity.getString(R.string.visit_details) else mActivity.getString(R.string.add_visit_details)
        options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.1f)
            .enableTracking()
            .build()
        setupRecyclerView()

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
        if (isReadOnly) {
            callVisitDetailsApi();
        } else {
            presetDate = CommonMethods.getCurrentDate()
            presetTime = CommonMethods.getCurrentTimeIn12HrFormat()
            binding.tvNextFollowUpTime.setOnClickListener(this)
            binding.tvNextVisitFollowUpDate.setOnClickListener(this)
            //binding.llSelectParty.setOnClickListener(this)
            binding.tvAddVisit.setOnClickListener(this)
            binding.llSelectCompany.setOnClickListener(this)
            binding.llSelectBranch.setOnClickListener(this)
            binding.llSelectDivision.setOnClickListener(this)
            binding.llSelectStage.setOnClickListener(this)
            binding.llSelectVisitType.setOnClickListener(this)
            binding.cardImageCapture.setOnClickListener(this)
            binding.cardAddInquiry.setOnClickListener(this)
            binding.cardImageSelfieCapture.setOnClickListener(this)
            binding.tvDate.text = CommonMethods.getCurrentDate()
            binding.tvCityName.text = visitData.cityName
            binding.etContactPerson.setText(visitData.contactPersonName)
            binding.tvSelectParty.text = visitData.accountName
            binding.tvNextVisitFollowUpDate.text = CommonMethods.getCurrentDate()
            binding.tvNextFollowUpTime.text = CommonMethods.getCurrentTimeIn12HrFormat()

            binding.tvStartTime.text = CommonMethods.getCurrentTimeIn12HrFormat()
            binding.tvEndTime.text = CommonMethods.getCurrentTimeIn12HrFormat()
            initialTime = CommonMethods.getCurrentTimeIn12HrFormat()

            binding.tvStartTime.setOnClickListener {
                selectTime(TimeType.FROM_START_TIME, binding.tvStartTime.text.toString()) { time ->
                    // Handle selected start time
                    binding.tvStartTime.text = time
                }
            }
            binding.tvEndTime.setOnClickListener {
                selectTime(TimeType.TO_END_TIME, binding.tvEndTime.text.toString()) { time ->
                    // Handle selected start time
                    binding.tvEndTime.text = time
                }
            }

            binding.radioGroupModeOfCom.setOnCheckedChangeListener { group, checkedId ->
                val radioButton = view?.findViewById<RadioButton>(checkedId)
                when (radioButton?.text) {
                    mActivity.getString(R.string.personally) -> selectedModeOfCommunication = 1
                    mActivity.getString(R.string.phone) -> selectedModeOfCommunication = 2
                    mActivity.getString(R.string.email) -> selectedModeOfCommunication = 3
                }
            }
            binding.radioGroupStatus.setOnCheckedChangeListener { group, checkedId ->
                val radioButton = view?.findViewById<RadioButton>(checkedId)
                when (radioButton?.text) {
                    mActivity.getString(R.string.not_now) -> {
                        selectedStatus = 1
                        binding.llNextVisitLayout.visibility = View.GONE
                    }

                    mActivity.getString(R.string.next_visit) -> {
                        selectedStatus = 2
                        binding.llNextVisitLayout.visibility = View.VISIBLE
                    }

                    mActivity.getString(R.string.close) -> {
                        selectedStatus = 3
                        binding.llNextVisitLayout.visibility = View.GONE
                    }
                }
            }
            binding.lylCbOrderInquiry.visibility = View.VISIBLE
            binding.cbAddInqury.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    binding.cbAddOrder.isChecked = false
                }
            }
            binding.cbAddOrder.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    binding.cbAddInqury.isChecked = false
                }
            }
        }
        binding.llHeader.setOnClickListener {
            toggleSectionVisibility(binding.llOptionalFields, binding.ivToggle)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                executeAPIsAndSetupData()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }

    }

    private fun toggleSectionVisibility(view: View, toggleIcon: ImageView) {
        if (isExpanded) {
            CommonMethods.collapse(view)
            toggleIcon.setImageResource(R.drawable.ic_add_circle)
        } else {
            CommonMethods.expand(view)
            toggleIcon.setImageResource(R.drawable.ic_remove_circle)
        }
        isExpanded = !isExpanded
    }


    private suspend fun executeAPIsAndSetupData() {
        if (!isReadOnly) {
            withContext(Dispatchers.IO) {
                try {
                    val visitListDeferred =
                        async { callCommonDropDownListApi(DROP_DOWN_VISIT_TYPE) }
                    val companyListDeferred = async { callCompanyListApi() }
                    //val branchListDeferred = async { callBranchListApi() }
                    //val divisionListDeferred = async { callDivisionListApi() }
                    val stageListDeferred = async { callCommonDropDownListApi(DROP_DOWN_STAGE) }
                    //val categoryListDeferred = async { callCateogyListApi() }

                    //categoryListDeferred.await()
                    visitListDeferred.await()
                    companyListDeferred.await()
                    //branchListDeferred.await()
                    //divisionListDeferred.await()
                    stageListDeferred.await()
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
        }
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
                CommonMethods.openDatePickerDialog(this, mActivity, true)
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

            R.id.llSelectCompany -> {
                if (companyMasterList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_COMPANY,
                        companyList = companyMasterList,
                        companyInterfaceDetect = this as UserSearchDialogUtil.CompanyDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.company_list_not_found)
                    )
                }
            }

            R.id.llSelectBranch -> {
                if (selectedCompany == null || selectedCompany?.companyMasterId == 0) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.please_select_company)
                    )
                    return;
                }
                if (branchMasterList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_BRANCH,
                        branchList = branchMasterList,
                        branchInterfaceDetect = this as UserSearchDialogUtil.BranchDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.branch_list_not_found)
                    )
                }
            }

            R.id.llSelectDivision -> {
                if (selectedBranch == null || selectedBranch?.branchMasterId == 0) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.please_select_branch)
                    )
                    return;
                }
                if (divisionMasterList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_DIVISION,
                        divisionList = divisionMasterList,
                        divisionInterfaceDetect = this as UserSearchDialogUtil.DivisionDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.branch_list_not_found)
                    )
                }
            }

            R.id.llSelectStage -> {
                if (stageList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_STAGE,
                        commonDropDownList = stageList,
                        commonDropDownInterfaceDetect = this as UserSearchDialogUtil.CommonDropDownDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.stage_list_not_found)
                    )
                }
            }

            R.id.llSelectVisitType -> {
                if (visitTypeList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_VISIT_TYPE,
                        commonDropDownList = visitTypeList,
                        commonDropDownInterfaceDetect = this as UserSearchDialogUtil.CommonDropDownDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.stage_list_not_found)
                    )
                }
            }

            R.id.cardImageCapture -> {
                if (imageAnyList.size == 4) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.photo_upload_limit_reached)
                    )
                    return
                }
                askCameraGalleryPermission()
            }

            R.id.cardImageSelfieCapture -> {
                if (isReadOnly) {
                    /*ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDao.getAppRegistration()?.apiHostingServer + visitReportData.filePath
                    )*/
                } else {
                    askCameraGalleryPermission(isOnlyCamera = true)
                }
            }

            R.id.cardAddInquiry -> {
                callInquiryListApi()
            }
        }
    }

    private fun askCameraGalleryPermission(isOnlyCamera: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                if (isOnlyCamera) openAlbumForSelfie() else openAlbumForList()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                if (isOnlyCamera) openAlbumForSelfie() else openAlbumForList()
            }
        } else {
            val arrListOffPermission = arrayListOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOffPermission) {
                if (isOnlyCamera) openAlbumForSelfie() else openAlbumForList()
            }
        }

    }

    private fun openAlbumForList() {
        val maxImageLimit = 4
        val remainingLimit = maxImageLimit - imageAnyList.size

        AlbumUtility(mActivity, true).openAlbumAndHandleImageMultipleSelection(
            onImagesSelected = { selectedFiles ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val modifiedImageFiles = selectedFiles.mapNotNull { file ->
                        CommonMethods.addDateAndTimeToFile(
                            file, CommonMethods.createImageFile(mActivity) ?: return@mapNotNull null
                        )
                    }

                    withContext(Dispatchers.Main) {
                        if (modifiedImageFiles.isNotEmpty()) {
                            imageAnyList.addAll(modifiedImageFiles)
                            imageAdapter?.addImage(imageAnyList, false)
                            handleAssetRVView(imageAnyList.size)
                        }
                    }
                }
            },
            onError = {
                CommonMethods.showToastMessage(mActivity, it)
            },
            remainingLimit
        )
    }

    private fun openAlbumForSelfie() {
        AlbumUtility(mActivity, true).openAlbumAndHandleCameraSelection(
            isFrontCamera = true,
            onImageSelected = { file ->
                selfieImageFile = file

                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    val modifiedImageFile = CommonMethods.addDateAndTimeToFile(
                        file,
                        CommonMethods.createImageFile(mActivity)!!
                    )

                    if (modifiedImageFile != null) {
                        Thread.sleep(1000) // Simulate time-consuming task

                        handler.post {
                            base64SelfieImage =
                                CommonMethods.convertImageFileToBase64(modifiedImageFile).toString()
                            //binding.tvSelfiUploaded.text = mActivity.getString(R.string.selfie_uploaded)
                            //binding.tvSelfiUploaded.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGreen))

                            val bitmap = BitmapFactory.decodeFile(modifiedImageFile.absolutePath)
                            val rotatedBitmap = modifyOrientation(
                                bitmap,
                                modifiedImageFile.absolutePath
                            ) // Ensure correct orientation

                            binding.imgSelfie.visibility = View.VISIBLE
                            binding.imgSelfie.setImageBitmap(rotatedBitmap)

                            try {
                                val inputImage = InputImage.fromBitmap(
                                    rotatedBitmap,
                                    0
                                ) // Use fromBitmap instead of fromFilePath
                                processImage(inputImage, rotatedBitmap)
                            } catch (e: Exception) {
                                Log.e("TAG", "Error processing image", e)
                            }
                        }
                    }
                }
            },
            onError = { error ->
                CommonMethods.showToastMessage(mActivity, error)
            }
        )
    }

    private fun processImage(image: InputImage, imageBitmap: Bitmap?) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) // Use ACCURATE mode for better results
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) // Detect facial landmarks
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // Enable smiling & eye open detection
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                displayFaces(faces, imageBitmap)
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Face detection failed", e)
            }
    }

    private fun displayFaces(faces: List<Face>, mBitmap: Bitmap?) {
        if (mBitmap == null) return

        val bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(mActivity, R.color.colorRed)
            style = Paint.Style.STROKE
            strokeWidth = 30f
        }

        for (face in faces) {
            val bounds = face.boundingBox
            canvas.drawRect(bounds, paint)
        }

        binding.imgSelfie.setImageBitmap(bitmap)

        if (faces.isNotEmpty()) {
            binding.tvSelfiUploaded.text = "Face Detected"
            totalFaceCount = faces.size
            binding.tvSelfiUploaded.setTextColor(
                ContextCompat.getColor(
                    mActivity,
                    R.color.colorGreenDark
                )
            )
        } else {
            binding.tvSelfiUploaded.text = "Face not detected, please recapture photo"
            binding.tvSelfiUploaded.setTextColor(
                ContextCompat.getColor(
                    mActivity,
                    R.color.colorRed
                )
            )
        }
    }

    private fun setupRecyclerView() {
        val apiUrl = appDao.getAppRegistration().apiHostingServer
        binding.rvImages.layoutManager =
            LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(mActivity, apiUrl)
        binding.rvImages.adapter = imageAdapter
        handleAssetRVView(0)

        imageAdapter?.setOnClick(object : ImageAdapter.OnAssetImageCancelClick {
            override fun onImageCancel(position: Int) {
                if (!isReadOnly && position != RecyclerView.NO_POSITION && imageAnyList.size > 0) {
                    imageAnyList.removeAt(position)
                    imageAdapter?.addImage(imageAnyList, false)
                    handleAssetRVView(imageAnyList.size)
                }
            }

            override fun onImagePreview(position: Int) {
                if (imageAnyList[position] is String) {
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDatabase.appDao()
                            .getAppRegistration().apiHostingServer + imageAnyList[position]
                    )
                } else {
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity, imageAnyList[position]
                    )
                }
            }
        })
    }

    fun handleAssetRVView(imageList: Int) {
        try {
            binding.txtNoFilePathImages.visibility = if (imageList == 0) View.VISIBLE else View.GONE
            binding.rvImages.visibility = if (imageList == 0) View.GONE else View.VISIBLE
        } catch (e: java.lang.Exception) {
            Log.e("TAG", "handleAssetRVView: *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
        }
    }

    private fun visitValidation() {
        /*if (binding.spVisitType.selectedItemPosition == 0) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.select_visit_type)
            )
            return
        }*/
        /*if (binding.etContactPerson.text.toString().trim().isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.enter_contact_person)
            )
            return
        }*/
        if (binding.etVisitDetails.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.enter_visit_details)
            )
            return
        }

        if (selectedStatus == 2) {
            if (binding.tvNextVisitFollowUpDate.text.toString().trim().isEmpty()) {
                CommonMethods.showToastMessage(mActivity, "Please select next visit follow-up date")
                return
            }
            if (binding.tvNextFollowUpTime.text.toString().trim().isEmpty()) {
                CommonMethods.showToastMessage(mActivity, "Please select next visit follow-up time")
                return
            }
            if (binding.etNextVisitSubject.text.toString().trim().isEmpty()) {
                CommonMethods.showToastMessage(
                    mActivity,
                    "Please select next visit follow-up subject"
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

    private fun callCategoryListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        if (selectedDivision == null || selectedDivision?.divisionMasterId == 0) {
            CommonMethods.showToastMessage(
                mActivity,
                "Please select division"
            )
            return;
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val jsonReq = JsonObject()
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and DivisionMasterid=${selectedDivision?.divisionMasterId} and $FORM_ID_VISIT"
        )

        val visitTypeCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCategoryMasterList(jsonReq)

        visitTypeCall?.enqueue(object : Callback<List<CategoryMasterResponse>> {
            override fun onResponse(
                call: Call<List<CategoryMasterResponse>>,
                response: Response<List<CategoryMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            categoryList.clear()
                            categoryList.add(CategoryMasterResponse(categoryName = "Select Category"))
                            categoryList.addAll(it)
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

            override fun onFailure(call: Call<List<CategoryMasterResponse>>, t: Throwable) {
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

    private fun callCompanyListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("companyMasterId", ID_ZERO)
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty("searchCriteria", "")
        objReq.addProperty("ParameterString", FORM_ID_VISIT)


        val companyCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCompanyMasterList(objReq)

        companyCall?.enqueue(object : Callback<List<CompanyMasterResponse>> {
            override fun onResponse(
                call: Call<List<CompanyMasterResponse>>,
                response: Response<List<CompanyMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            companyMasterList.clear()
                            companyMasterList.add(
                                CompanyMasterResponse(
                                    companyMasterId = 0,
                                    companyName = mActivity.getString(R.string.select_company),
                                )
                            )
                            companyMasterList.addAll(it)
                            if (it.size == 1) {
                                isCompanyChange = true
                                selectedCompany = CompanyMasterResponse(
                                    companyMasterId = companyMasterList[1].companyMasterId,
                                    companyName = companyMasterList[1].companyName
                                )
                                binding.tvSelectCompany.text = selectedCompany?.companyName ?: ""
                                callBranchListApi()
                            }
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

            override fun onFailure(call: Call<List<CompanyMasterResponse>>, t: Throwable) {
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

    private fun callBranchListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        /*if (selectedCompany == null || selectedCompany?.companyMasterId == 0) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.please_select_company)
            )
            return;
        }*/
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("BranchMasterId", ID_ZERO)
        objReq.addProperty("UserId", loginData.userId)
        objReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and $FORM_ID_VISIT"
        )

        val branchCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getBranchMasterList(objReq)

        branchCall?.enqueue(object : Callback<List<BranchMasterResponse>> {
            override fun onResponse(
                call: Call<List<BranchMasterResponse>>,
                response: Response<List<BranchMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            branchMasterList.clear()
                            branchMasterList.add(
                                BranchMasterResponse(
                                    branchMasterId = 0,
                                    branchName = mActivity.getString(R.string.select_branch)
                                )
                            )
                            branchMasterList.addAll(it)
                            if (isCompanyChange && it.size == 1) {
                                selectedBranch = BranchMasterResponse(
                                    branchMasterId = branchMasterList[1].branchMasterId,
                                    branchName = branchMasterList[1].branchName
                                )
                                binding.tvSelectBranch.text = selectedBranch?.branchName ?: ""
                                callDivisionListApi()
                            } else {
                                //callDivisionListApi()
                                /*val userDialog = UserSearchDialogUtil(
                                    mActivity,
                                    type = FOR_BRANCH,
                                    branchList = branchMasterList,
                                    branchInterfaceDetect = this@AddVisitFragment as UserSearchDialogUtil.BranchDialogDetect,
                                    userDialogInterfaceDetect = null
                                )
                                userDialog.showUserSearchDialog()*/
                            }
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                getString(R.string.branch_list_not_found)
                            )
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

            override fun onFailure(call: Call<List<BranchMasterResponse>>, t: Throwable) {
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

    private fun callDivisionListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        /*if (selectedBranch == null || selectedBranch?.branchMasterId == 0) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.please_select_branch)
            )
            return;
        }*/
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("divisionMasterId", ID_ZERO)
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty("searchCriteria", "")
        objReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and $FORM_ID_VISIT"
        )

        val divisionCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDivisionMasterList(objReq)

        divisionCall?.enqueue(object : Callback<List<DivisionMasterResponse>> {
            override fun onResponse(
                call: Call<List<DivisionMasterResponse>>,
                response: Response<List<DivisionMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            divisionMasterList.clear()
                            divisionMasterList.add(
                                DivisionMasterResponse(
                                    divisionMasterId = 0,
                                    divisionName = mActivity.getString(R.string.select_division)
                                )
                            )
                            divisionMasterList.addAll(it)
                            if (isCompanyChange && it.size == 1) {
                                selectedDivision = DivisionMasterResponse(
                                    divisionMasterId = divisionMasterList[1].divisionMasterId,
                                    divisionName = divisionMasterList[1].divisionName
                                )
                                binding.tvSelectDivision.text = selectedDivision?.divisionName ?: ""
                                callCategoryListApi()
                            } else {
                                /*val userDialog = UserSearchDialogUtil(
                                    mActivity,
                                    type = FOR_DIVISION,
                                    divisionList = divisionMasterList,
                                    divisionInterfaceDetect = this@AddVisitFragment as UserSearchDialogUtil.DivisionDialogDetect,
                                    userDialogInterfaceDetect = null
                                )
                                userDialog.showUserSearchDialog()*/
                                //callCategoryListApi()
                            }
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                getString(R.string.division_list_not_found)
                            )
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

            override fun onFailure(call: Call<List<DivisionMasterResponse>>, t: Throwable) {
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

    private fun callCommonDropDownListApi(apiType: String) {
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

        val stageCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDropDownMasterDetails(apiType, "")

        stageCall?.enqueue(object : Callback<List<CommonDropDownResponse>> {
            override fun onResponse(
                call: Call<List<CommonDropDownResponse>>,
                response: Response<List<CommonDropDownResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            if (apiType == DROP_DOWN_STAGE) {
                                stageList.clear()
                                stageList.addAll(it)
                            } else {
                                visitTypeList.clear()
                                visitTypeList.addAll(it)
                            }
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                getString(R.string.something_went_wrong)
                            )
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<CommonDropDownResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }

        })

    }

    private fun callInquiryListApi() {
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
        val jsonReq = JsonObject().apply {
            addProperty("categoryMasterId", selectedCategory?.categoryMasterId)
            addProperty("companyMasterId", selectedCompany?.companyMasterId)
            addProperty("branchMasterId", selectedBranch?.branchMasterId)
            addProperty("divisionMasterId", selectedDivision?.divisionMasterId)
            addProperty("userId", loginData.userId)
            addProperty("serachCriteria", "")
            addProperty("accountMasterId", visitData.accountMasterId)
        }
        val inquiryCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getVisitInquiryList(jsonReq)

        inquiryCall?.enqueue(object : Callback<List<InquiryResponse>> {
            override fun onResponse(
                call: Call<List<InquiryResponse>>,
                response: Response<List<InquiryResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            inquiryList.clear()
                            inquiryList.add(
                                InquiryResponse(
                                    documentId = 0,
                                    productName = mActivity.getString(R.string.select_inquiry),
                                )
                            )
                            inquiryList.addAll(it)
                            showInquiryDialog(it)
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                "Inquiry List not found"
                            )
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<InquiryResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }

        })

    }

    private fun showInquiryDialog(inquiries: List<InquiryResponse>, isViewOnly: Boolean = false) {
        val builder = AlertDialog.Builder(mActivity, R.style.MyAlertDialogStyle)
        inquiryDialog = builder.create()
        inquiryDialog!!.setCancelable(false)
        inquiryDialog!!.setCanceledOnTouchOutside(false)
        inquiryDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        inquiryDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val inflater =
            mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = inflater.inflate(R.layout.add_inquiry_dialog, null)
        val rvInquiry = layout.findViewById<RecyclerView>(R.id.rvInquiry)
        val cbAll = layout.findViewById<CheckBox>(R.id.cbSelectedItems)
        val btnSubmit = layout.findViewById<Button>(R.id.btnSubmit)
        val imgBack = layout.findViewById<ImageView>(R.id.imgBack)
        val edtSearch = layout.findViewById<EditText>(R.id.edtSearch)
        val tvClearAll = layout.findViewById<TextView>(R.id.tvClearAll)
        val tvTitle = layout.findViewById<TextView>(R.id.tvTitle)
        val adapter = InquiryAdapter(mActivity, inquiries, isViewOnly)

        if (isViewOnly) {
            btnSubmit.visibility = View.GONE
            cbAll.visibility = View.GONE
            tvClearAll.visibility = View.GONE
            tvTitle.setText("Inquiry/Quotation List")
        }

        edtSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        if (visitDetailsList.isNotEmpty()) {
            adapter.updateCheckedItems(visitDetailsList)
        }
        rvInquiry.layoutManager = LinearLayoutManager(mActivity)
        rvInquiry.adapter = adapter

        imgBack.setOnClickListener {
            inquiryDialog?.dismiss()
        }
        btnSubmit.setOnClickListener {
            visitDetailsList = adapter.getSelectedItems()
            if (visitDetailsList.isNotEmpty()) {
                inquiryDialog?.dismiss()
                binding.tvTotalSelectedItem.text =
                    visitDetailsList.size.toString() + " Inquiry/Quotation Selected"
                binding.tvTotalSelectedItem.setTextColor(
                    ContextCompat.getColor(
                        mActivity,
                        R.color.colorGreen
                    )
                )
            } else {
                CommonMethods.showToastMessage(mActivity, getString(R.string.no_items_selected))
            }
        }

        cbAll.setOnCheckedChangeListener { _, isChecked ->
            adapter.selectAll(isChecked)
        }

        inquiryDialog!!.setView(layout)
        inquiryDialog!!.window!!.setBackgroundDrawableResource(R.drawable.dialog_shape)
        inquiryDialog!!.show()
    }

    /*private fun setupCategorySpinner() {
        val adapter = LeaveTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            categoryList,
            this,
        )
        binding.spCategory.adapter = adapter
        selectedCategory = if (isReadOnly) {
            val categoryData = CategoryMasterResponse(
                categoryMasterId = visitReportData.categoryMasterId,
                categoryName = visitReportData.categoryName
            )
            val pos =
                categoryList.indexOfFirst { it.categoryMasterId == categoryData.categoryMasterId }
            binding.spCategory.setSelection(pos)
            categoryData
        } else {
            if (categoryList.size == 2) {
                selectedCategory = CategoryMasterResponse(
                    categoryMasterId = categoryList[1].categoryMasterId,
                    categoryName = categoryList[1].categoryName
                )
                binding.spCategory.setSelection(1)
                categoryList[1]
            } else {
                binding.spCategory.setSelection(0)
                categoryList[0]
            }
        }


    }
*/
    private fun setupCategorySpinner() {
        val adapter = LeaveTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            categoryList,
            this,
        )
        binding.spCategory.adapter = adapter
        if (isCompanyChange && categoryList.size == 2) {
            selectedCategory = CategoryMasterResponse(
                categoryMasterId = categoryList[1].categoryMasterId,
                categoryName = categoryList[1].categoryName
            )
            binding.spCategory.setSelection(1)
            categoryList[1]
        } else {
            if (isCompanyChange) {
                binding.spCategory.setSelection(0)
                categoryList[0]
            } else {
                val selectedCategoryIndex =
                    categoryList.indexOfFirst { it.categoryMasterId == selectedCategory?.categoryMasterId }
                binding.spCategory.setSelection(selectedCategoryIndex)
            }
        }
    }

    private fun callAddVisitApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        if (base64SelfieImage.isNotEmpty() && totalFaceCount <= 0) {
            CommonMethods.showToastMessage(mActivity, "Please capture valid selfie picture")
            return
        }
        CommonMethods.showLoading(mActivity)

        val nextVisitDateTime =
            binding.tvNextVisitFollowUpDate.text.toString() + ", " + binding.tvNextFollowUpTime.text.toString()
        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val addVisitReq = JsonObject()
        addVisitReq.addProperty("VisitId", 0)
        addVisitReq.addProperty("CategoryMasterId", selectedCategory?.categoryMasterId)
        addVisitReq.addProperty("AccountMasterId", visitData.accountMasterId)
        addVisitReq.addProperty("CityId", visitData.cityId)
        addVisitReq.addProperty("ContactPersonName", binding.etContactPerson.text.toString())
        addVisitReq.addProperty("VisitDetails", binding.etVisitDetails.text.toString())
        addVisitReq.addProperty("NextVisitDateTime", nextVisitDateTime)
        addVisitReq.addProperty(
            "NextVisitSubject",
            binding.etNextVisitSubject.text.trim().toString()
        )
        addVisitReq.addProperty("Remakrs", binding.etRemarks.text.trim().toString())
        addVisitReq.addProperty("Latitude", currentLatitude)
        addVisitReq.addProperty("Longitude", currentLongitude)
        addVisitReq.addProperty("Location", currentAddress)
        addVisitReq.addProperty("TripId", tripId)
        addVisitReq.addProperty("PreviousVisitId", previousVisitId)
        addVisitReq.addProperty("MapKM", totalDistance)
        addVisitReq.addProperty("UserId", loginData.userId)
        addVisitReq.addProperty("CompanyMasterId", selectedCompany?.companyMasterId)
        addVisitReq.addProperty("BranchMasterId", selectedBranch?.branchMasterId)
        addVisitReq.addProperty("DivisionMasterId", selectedDivision?.divisionMasterId)
        addVisitReq.addProperty("DDMVisitTypeId", selectedVisitType?.dropdownKeyId)
        addVisitReq.addProperty("ModeOfCommunication", selectedModeOfCommunication)
        addVisitReq.addProperty("StartTime", binding.tvStartTime.text.toString())
        addVisitReq.addProperty("EndTime", binding.tvEndTime.text.toString())
        addVisitReq.addProperty("VisitStatus", selectedStatus)
        addVisitReq.addProperty("DDMStageId", selectedStage?.dropdownKeyId)
        addVisitReq.addProperty("SelfieFilePath", base64SelfieImage)

        if (imageAnyList.size > 0) {
            if (imageAnyList[0] is String) {
                addVisitReq.addProperty("FilePath", imageAnyList[0].toString())
            } else {
                val imageBase64 =
                    CommonMethods.convertImageFileToBase64(imageAnyList[0] as File).toString()
                addVisitReq.addProperty("FilePath", imageBase64)
            }
            if (imageAnyList.size > 1) {
                if (imageAnyList[1] is String) {
                    addVisitReq.addProperty("FilePath2", imageAnyList[1].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[1] as File).toString()
                    addVisitReq.addProperty("FilePath2", imageBase64)
                }
            } else {
                addVisitReq.addProperty("FilePath2", "")
            }
            if (imageAnyList.size > 2) {
                if (imageAnyList[2] is String) {
                    addVisitReq.addProperty("FilePath3", imageAnyList[2].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[2] as File).toString()
                    addVisitReq.addProperty("FilePath3", imageBase64)
                }
            } else {
                addVisitReq.addProperty("FilePath3", "")
            }
            if (imageAnyList.size > 3) {
                if (imageAnyList[3] is String) {
                    addVisitReq.addProperty("FilePath4", imageAnyList[3].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[3] as File).toString()
                    addVisitReq.addProperty("FilePath4", imageBase64)
                }
            } else {
                addVisitReq.addProperty("FilePath4", "")
            }
        } else {
            addVisitReq.addProperty("FilePath", "")
            addVisitReq.addProperty("FilePath2", "")
            addVisitReq.addProperty("FilePath3", "")
            addVisitReq.addProperty("FilePath4", "")
        }

        addVisitReq.addProperty("Success", false)
        addVisitReq.addProperty("ReturnMessage", "")

        val jsonArray = JsonArray()
        for (i in visitDetailsList) {
            val jsonObj = JsonObject()
            jsonObj.addProperty("VisitDetailsId", 0)
            jsonObj.addProperty("VisitId", 0)
            jsonObj.addProperty("TableName", i.tableName)//dynamic get from visitdetailslist
            jsonObj.addProperty("ReferenceId", i.documentDetailsId)
            jsonObj.addProperty("Remarks", "")
            jsonObj.addProperty("EditUserMasterId", loginData.userId)
            jsonObj.addProperty("EditVersion", BuildConfig.VERSION_NAME)
            jsonArray.add(jsonObj)
        }
        addVisitReq.add("visitDetail", jsonArray)

        val addVisitCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.addVisit(addVisitReq)

        addVisitCall?.enqueue(object : Callback<AddVisitSuccessResponse> {
            override fun onResponse(
                call: Call<AddVisitSuccessResponse>,
                response: Response<AddVisitSuccessResponse>
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
                                        if (binding.cbAddOrder.isChecked) {
                                            mActivity.addFragment(
                                                AddOrderEntryFragment.newInstance(
                                                    orderId = 0,
                                                    allowDelete = false,
                                                    allowEdit = false,
                                                    accountName = visitData.accountName,
                                                    accountMasterId = visitData.accountMasterId,
                                                    contactPersonName = binding.etContactPerson.text.toString()
                                                        .trim() ?: ""
                                                ),
                                                addToBackStack = true,
                                                ignoreIfCurrent = true,
                                                animationType = AnimationType.fadeInfadeOut
                                            )
                                        }
                                        if (binding.cbAddInqury.isChecked) {
                                            mActivity.addFragment(
                                                AddInquiryEntryFragment.newInstance(
                                                    orderId = 0,
                                                    allowDelete = false,
                                                    allowEdit = false,
                                                    accountName = visitData.accountName,
                                                    accountMasterId = visitData.accountMasterId,
                                                    contactPersonName = binding.etContactPerson.text.toString()
                                                        .trim() ?: ""
                                                ),
                                                addToBackStack = true,
                                                ignoreIfCurrent = true,
                                                animationType = AnimationType.fadeInfadeOut
                                            )
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

            override fun onFailure(call: Call<AddVisitSuccessResponse>, t: Throwable) {
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

    private fun callVisitDetailsApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                mActivity.getString(R.string.network_error),
                mActivity.getString(R.string.network_error_msg),
                null
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val visitDetailsReq = JsonObject()
        visitDetailsReq.addProperty("UserId", loginData.userId)
        visitDetailsReq.addProperty("parameterString", FORM_ID_VISIT)
        visitDetailsReq.addProperty("visitId", visitId)

        CommonMethods.getBatteryPercentage(mActivity)

        val visitReportCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getVisitDetails(visitDetailsReq)


        visitReportCall?.enqueue(object : Callback<List<VisitReportListResponse>> {
            override fun onResponse(
                call: Call<List<VisitReportListResponse>>,
                response: Response<List<VisitReportListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            setupVisitDetails(it[0])
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                getString(R.string.no_visit_details_found)
                            )
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

            override fun onFailure(call: Call<List<VisitReportListResponse>>, t: Throwable) {
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

    private fun setupVisitDetails(visitReportData: VisitReportListResponse) {
        binding.tvDate.text = visitReportData.visitDate
        binding.tvCityName.text = visitReportData.cityName
        binding.etContactPerson.setText(visitReportData.contactPersonName)
        binding.tvSelectParty.text = visitReportData.accountName
        binding.tvNextVisitFollowUpDate.text = visitReportData.nextVisitDate
        binding.tvNextFollowUpTime.text = visitReportData.nextVisitTime

        binding.etVisitDetails.setText(visitReportData.visitDetails)
        binding.etNextVisitSubject.setText(visitReportData.nextVisitSubject)
        binding.etRemarks.setText(visitReportData.remarks)
        binding.tvSelectCompany.text = visitReportData.companyName.ifEmpty { "" }
        binding.tvSelectBranch.text = visitReportData.branchName.ifEmpty { "" }
        binding.tvSelectDivision.text = visitReportData.divisionName.ifEmpty { "" }
        binding.tvCategory.text = visitReportData.categoryName.ifEmpty { "" }
        binding.tvSelectVisitType.text = visitReportData.dmdVisitTypeName.ifEmpty { "" }
        binding.tvStartTime.text = CommonMethods.convertToAmPm(visitReportData.startTime)
        binding.tvEndTime.text = CommonMethods.convertToAmPm(visitReportData.endTime)
        binding.tvSelectStage.text = visitReportData.dmdStageName.ifEmpty { "" }
        if (visitReportData.listInquiryFilter.isNotEmpty()) {
            inquiryList.addAll(visitReportData.listInquiryFilter)
            binding.tvTotalSelectedItem.text =
                inquiryList.size.toString() + " Inquiry/Quotation Selected"
            binding.tvTotalSelectedItem.setTextColor(
                ContextCompat.getColor(
                    mActivity,
                    R.color.colorGreen
                )
            )
            binding.tvTotalSelectedItem.setOnClickListener {
                showInquiryDialog(inquiryList, true)
            }
            if (visitReportData.selfieFilePath.isNotEmpty()) {
                binding.tvSelfiUploaded.setTextColor(
                    ContextCompat.getColor(
                        mActivity,
                        R.color.colorGreen
                    )
                )
                binding.tvSelfiUploaded.text = "View Photo"
            }
            binding.tvSelfiUploaded.setOnClickListener {
                if (visitReportData.selfieFilePath.isNotEmpty()) {
                    binding.tvSelfiUploaded.setTextColor(
                        ContextCompat.getColor(
                            mActivity,
                            R.color.colorGreen
                        )
                    )
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDatabase.appDao()
                            .getAppRegistration().apiHostingServer + visitReportData.selfieFilePath
                    )
                }
            }
        }

        disableRadioModeOfCom(false)
        disableRadioStatus(false)
        selectedModeOfCommunication = visitReportData.modeOfCommunication ?: 0
        when (selectedModeOfCommunication) {
            1 -> binding.radioGroupModeOfCom.check(R.id.rbPersonally)
            2 -> binding.radioGroupModeOfCom.check(R.id.rbPhone)
            3 -> binding.radioGroupModeOfCom.check(R.id.rbEmail)
            else -> binding.radioGroupModeOfCom.clearCheck()
        }
        when (visitReportData.visitStatus ?: 0) {
            1 -> binding.radioGroupStatus.check(R.id.rbNotNow)
            2 -> binding.radioGroupStatus.check(R.id.rbNextVisit)
            3 -> binding.radioGroupStatus.check(R.id.rbClose)
            else -> binding.radioGroupStatus.clearCheck()
        }
        if (selectedModeOfCommunication == 2) {
            binding.llNextVisitLayout.visibility = View.VISIBLE
        }

        binding.flCategory.visibility = View.GONE
        binding.tvCategory.visibility = View.VISIBLE
        binding.tvAddVisit.visibility = View.GONE
        /*binding.imgDropDown.visibility = View.GONE
        binding.imgSearch.visibility = View.GONE*/
        binding.etVisitDetails.isEnabled = false
        binding.etNextVisitSubject.isEnabled = false
        binding.etRemarks.isEnabled = false
        binding.etContactPerson.isEnabled = false

        if ((visitReportData.filePath ?: "").isNotEmpty()) {
            imageAnyList.add((visitReportData.filePath) ?: "")
        }
        if ((visitReportData.filePath2 ?: "").isNotEmpty()) {
            imageAnyList.add((visitReportData.filePath2) ?: "")
        }
        if ((visitReportData.filePath3 ?: "").isNotEmpty()) {
            imageAnyList.add((visitReportData.filePath3) ?: "")
        }
        if ((visitReportData.filePath4 ?: "").isNotEmpty()) {
            imageAnyList.add((visitReportData.filePath4) ?: "")
        }
        imageAdapter?.addImage(imageAnyList, true)
        handleAssetRVView(imageAnyList.size)
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

    override fun onTypeSelect(typeData: CategoryMasterResponse) {
        selectedCategory = typeData
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a TimePickerDialog
        val timePickerDialog = TimePickerDialog(
            mActivity,
            { _, selectedHour, selectedMinute ->
                // Determine AM/PM and convert to 12-hour format
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val hour12 = when {
                    selectedHour == 0 -> 12 // Midnight case
                    selectedHour > 12 -> selectedHour - 12 // Convert 24-hour to 12-hour
                    else -> selectedHour // Morning case
                }

                // Format time as hh:mm AM/PM
                val time = String.format("%02d:%02d %s", hour12, selectedMinute, amPm)

                // Update the UI with the selected time
                binding.tvNextFollowUpTime.text = time
            },
            hour,
            minute,
            false // Use 12-hour format
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

    fun selectTime(
        timeType: TimeType,
        selectedTime: String,
        onTimeSelected: (String) -> Unit
    ) {
        val currentTime = CommonMethods.getCurrentTimeIn12HrFormat()

        when (timeType) {
            TimeType.FROM_START_TIME -> {
                openTimePicker(timeType, selectedTime) { startTime ->
                    val endTime = binding.tvEndTime.text.toString()

                    if (!isTimeLessThanCurrent(startTime, currentTime)) {
                        CommonMethods.showToastMessage(
                            mActivity, "Start time must be less than current time"
                        )
                        return@openTimePicker
                    }

                    if (endTime.isEmpty() || isEndTimeValid(startTime, endTime)) {
                        binding.tvStartTime.text = startTime
                        if (endTime.isEmpty()) {
                            binding.tvEndTime.text = startTime
                        }
                        onTimeSelected(startTime)
                    } else {
                        CommonMethods.showToastMessage(
                            mActivity, "Start time must be less than end time"
                        )
                    }
                }
            }

            TimeType.TO_END_TIME -> {
                val startTime = binding.tvStartTime.text.toString()

                if (startTime.isEmpty()) {
                    CommonMethods.showToastMessage(
                        mActivity, getString(R.string.please_select_the_start_time_first)
                    )
                    return
                }

                openTimePicker(timeType, selectedTime) { endTime ->
                    if (!isTimeLessThanCurrent(endTime, currentTime)) {
                        CommonMethods.showToastMessage(
                            mActivity, "End time must be less than current time"
                        )
                        return@openTimePicker
                    }

                    if (isEndTimeValid(startTime, endTime)) {
                        binding.tvEndTime.text = endTime
                        onTimeSelected(endTime)
                    } else {
                        CommonMethods.showToastMessage(
                            mActivity, "End time must be greater than start time"
                        )
                    }
                }
            }
        }
    }

    fun isEndTimeValid(startTime: String, endTime: String): Boolean {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val startTimeDate = timeFormat.parse(startTime)
        val endTimeDate = timeFormat.parse(endTime)
        return endTimeDate.after(startTimeDate)
    }

    fun isTimeLessThanCurrent(time: String, currentTime: String): Boolean {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val selectedTimeDate = timeFormat.parse(time)
        val currentTimeDate = timeFormat.parse(currentTime)
        return selectedTimeDate.before(currentTimeDate)
    }


    fun openTimePicker(
        timeType: TimeType,
        previousSelectedTime: String = "",
        onTimeSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Parse the previous selected time if available
        if (previousSelectedTime.isNotEmpty()) {
            try {
                val parsedDate = timeFormat.parse(previousSelectedTime)
                parsedDate?.let {
                    calendar.time = it
                }
            } catch (e: Exception) {
                e.printStackTrace() // Handle parsing error if the time format is invalid
            }
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = android.app.TimePickerDialog(
            mActivity,
            { _, hourOfDay, selectedMinute ->
                // Set the selected time
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, selectedMinute)

                // Format time as hh:mm a (e.g., 11:24 PM)
                val formattedTime = timeFormat.format(calendar.time)
                onTimeSelected(formattedTime)
            },
            hour,
            minute,
            false // Use 12-hour format
        )

        timePickerDialog.show()
    }


    override fun companySelect(dropDownData: CompanyMasterResponse) {
        selectedCompany = dropDownData
        isCompanyChange = true
        binding.tvSelectCompany.text = selectedCompany?.companyName ?: ""
        resetSelection(
            resetBranch = true,
            resetDivision = true,
            resetCategory = true
        )
        if ((selectedCompany?.companyMasterId ?: 0) > 0) {
            callBranchListApi()
        }
    }

    override fun branchSelect(dropDownData: BranchMasterResponse) {
        selectedBranch = dropDownData
        binding.tvSelectBranch.text = selectedBranch?.branchName ?: ""
        resetSelection(
            resetBranch = false,
            resetDivision = true,
            resetCategory = true
        )
        if ((selectedBranch?.branchMasterId ?: 0) > 0) {
            callDivisionListApi()
        }
    }

    override fun divisionSelect(dropDownData: DivisionMasterResponse) {
        selectedDivision = dropDownData
        binding.tvSelectDivision.text = selectedDivision?.divisionName ?: ""
        resetSelection(
            resetBranch = false,
            resetDivision = false,
            resetCategory = true
        )
        if ((selectedDivision?.divisionMasterId ?: 0) > 0) {
            callCategoryListApi()
        }
    }

    override fun dropDownSelect(dropDownData: CommonDropDownResponse, dropDownType: String) {
        if (dropDownType == DROP_DOWN_STAGE) {
            binding.tvSelectStage.text = dropDownData.dropdownValue
            selectedStage = dropDownData
        } else {
            binding.tvSelectVisitType.text = dropDownData.dropdownValue
            selectedVisitType = dropDownData
        }
    }

    class InquiryAdapter(
        private val context: Context,
        private val items: List<InquiryResponse>,
        private val isViewOnly: Boolean = false
    ) :
        RecyclerView.Adapter<InquiryAdapter.ItemViewHolder>() {

        private var filteredItems: MutableList<InquiryResponse> = items.toMutableList()

        // Method to re-check previously selected items
        fun updateCheckedItems(selectedItems: List<InquiryResponse>) {
            items.forEach { item ->
                item.isSelected =
                    selectedItems.any { it.documentDetailsId == item.documentDetailsId }
            }
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_inquiry_layout, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = filteredItems[position]
            holder.bind(item)

            if (isViewOnly) {
                holder.itemCheckBox.visibility = View.GONE
            }
            // Clear previous listeners
            holder.itemCheckBox.setOnCheckedChangeListener(null)

            // Set checkbox state based on isSelected
            holder.itemCheckBox.isChecked = item.isSelected

            // Update the item's state on user interaction
            holder.itemCheckBox.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
            }
        }

        override fun getItemCount(): Int = filteredItems.size

        fun selectAll(isChecked: Boolean) {
            items.forEach { it.isSelected = isChecked }
            notifyDataSetChanged()
        }

        fun getSelectedItems(): List<InquiryResponse> {
            return items.filter { it.isSelected }
        }

        fun filter(query: String) {
            filteredItems = if (query.isEmpty()) {
                items.toMutableList()
            } else {
                items.filter {
                    (it.productName ?: "").contains(query, ignoreCase = true) ||
                            (it.categoryName ?: "").contains(query, ignoreCase = true) ||
                            it.documentMode.toString().contains(query, ignoreCase = true) ||
                            it.quantity.toString().contains(query, ignoreCase = true) ||
                            it.rate.toString().contains(query, ignoreCase = true) ||
                            it.amount.toString().contains(query, ignoreCase = true)
                }.toMutableList()
            }
            notifyDataSetChanged()
        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemCheckBox: CheckBox = itemView.findViewById(R.id.itemCheckBox)

            fun bind(item: InquiryResponse) {
                val tvProductName = itemView.findViewById<TextView>(R.id.tvProductName)
                val tvEntryNo = itemView.findViewById<TextView>(R.id.tvEntryNo)
                val tvDate = itemView.findViewById<TextView>(R.id.tvEntryDate)
                val tvQty = itemView.findViewById<TextView>(R.id.tvQuantity)
                val tvRate = itemView.findViewById<TextView>(R.id.tvRate)
                val tvAmount = itemView.findViewById<TextView>(R.id.tvAmount)
                tvProductName.text = item.productName
                tvEntryNo.text = "${item.categoryName}-${item.documentNo}"
                tvDate.text = item.documentDate
                tvQty.text = item.quantity.toString()
                tvRate.text = item.rate.toString()
                tvAmount.text = item.amount.toString()
            }
        }
    }


    private fun resetSelection(
        resetBranch: Boolean,
        resetDivision: Boolean,
        resetCategory: Boolean
    ) {
        if (resetBranch) {
            selectedBranch = null
            binding.tvSelectBranch.hint = mActivity.getString(R.string.select_branch)
            binding.tvSelectBranch.text = ""
            branchMasterList.clear()
        }
        if (resetDivision) {
            selectedDivision = null
            binding.tvSelectDivision.hint = mActivity.getString(R.string.select_division)
            binding.tvSelectDivision.text = ""
            divisionMasterList.clear()
        }
        if (resetCategory) {
            selectedCategory = null
            binding.tvCategory.hint = mActivity.getString(R.string.select_category)
            binding.tvCategory.text = ""
            categoryList.clear()
            categoryList.add(CategoryMasterResponse(categoryName = "Select Category"))
            setupCategorySpinner()
        }
    }

    private fun disableRadioModeOfCom(isRadioEnable: Boolean) {
        if (!isRadioEnable) {
            for (i in 0 until binding.radioGroupModeOfCom.childCount) {
                val radioButton = binding.radioGroupModeOfCom.getChildAt(i)
                radioButton.isEnabled = false
                radioButton.isClickable = false
            }
        } else {
            for (i in 0 until binding.radioGroupModeOfCom.childCount) {
                val radioButton = binding.radioGroupModeOfCom.getChildAt(i)
                radioButton.isEnabled = true
                radioButton.isClickable = true
            }
        }
    }

    private fun disableRadioStatus(isRadioEnable: Boolean) {
        if (!isRadioEnable) {
            for (i in 0 until binding.radioGroupStatus.childCount) {
                val radioButton = binding.radioGroupStatus.getChildAt(i)
                radioButton.isEnabled = false
                radioButton.isClickable = false
            }
        } else {
            for (i in 0 until binding.radioGroupStatus.childCount) {
                val radioButton = binding.radioGroupStatus.getChildAt(i)
                radioButton.isEnabled = true
                radioButton.isClickable = true
            }
        }
    }

}