package ethicstechno.com.fieldforce.ui.fragments.moreoption.partydealer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
import ethicstechno.com.fieldforce.databinding.FragmentAddPartyDealerBinding
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.CommonDropDownResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseCityListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.reports.UserListResponse
import ethicstechno.com.fieldforce.ui.adapter.ImageAdapter
import ethicstechno.com.fieldforce.ui.adapter.LeaveTypeAdapter
import ethicstechno.com.fieldforce.ui.adapter.UserAdapterForSpinner
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.ARG_PARAM2
import ethicstechno.com.fieldforce.utils.AlbumUtility
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DROP_DOWN_INDUSTRY
import ethicstechno.com.fieldforce.utils.DROP_DOWN_REFERENCE_SOURCE
import ethicstechno.com.fieldforce.utils.DROP_DOWN_REGION
import ethicstechno.com.fieldforce.utils.FORM_ID_PARTY_DEALER
import ethicstechno.com.fieldforce.utils.FOR_INDUSTRY_TYPE
import ethicstechno.com.fieldforce.utils.FOR_PLACE
import ethicstechno.com.fieldforce.utils.FOR_REFERENCE_SOURCE
import ethicstechno.com.fieldforce.utils.FOR_REGION_TYPE
import ethicstechno.com.fieldforce.utils.IMAGE_FILE_1
import ethicstechno.com.fieldforce.utils.IMAGE_FILE_2
import ethicstechno.com.fieldforce.utils.IMAGE_FILE_3
import ethicstechno.com.fieldforce.utils.IMAGE_FILE_4
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.IS_MOCK_LOCATION
import ethicstechno.com.fieldforce.utils.IS_VALID
import ethicstechno.com.fieldforce.utils.ImagePreviewCommonDialog
import ethicstechno.com.fieldforce.utils.MobileAndLandLineInputFilter
import ethicstechno.com.fieldforce.utils.PermissionUtil
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.Executors

class AddPartyDealerFragment : HomeBaseFragment(), View.OnClickListener,
    UserSearchDialogUtil.PlaceSearchDialogDetect,
    LeaveTypeAdapter.TypeSelect, UserAdapterForSpinner.UserSelect,
    UserSearchDialogUtil.CommonDropDownDialogDetect {

    lateinit var binding: FragmentAddPartyDealerBinding
    val placeList: ArrayList<ExpenseCityListResponse> = arrayListOf()
    val categoryList: ArrayList<CategoryMasterResponse> = arrayListOf()
    val userList: ArrayList<UserListResponse> = arrayListOf()
    val regionList: ArrayList<CommonDropDownResponse> = arrayListOf()
    val industryList: ArrayList<CommonDropDownResponse> = arrayListOf()
    val referenceSourceList: ArrayList<CommonDropDownResponse> = arrayListOf()
    private var selectedPlace: ExpenseCityListResponse? = null
    private var selectedCategory: CategoryMasterResponse? = null
    private var selectedUser: UserListResponse? = null
    private var selectedRegion: CommonDropDownResponse? = null
    private var selectedIndustry: CommonDropDownResponse? = null
    private var selectedReferenceSource: CommonDropDownResponse? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    var currentAddress = ""
    var isUpdate = false
    private var partyDealerDataForUpdate: AccountMasterList = AccountMasterList()
    private var base64Image1 = ""
    private var imageFile1: File? = null
    private var base64Image2 = ""
    private var imageFile2: File? = null
    private var base64Image3 = ""
    private var imageFile3: File? = null
    private var base64Image4 = ""
    private var imageFile4: File? = null
    private var imageAnyList: ArrayList<Any> = arrayListOf()
    private var imageAdapter: ImageAdapter? = null
    var isEditClick = false

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
            isUpdate: Boolean,
            partyDealerData: AccountMasterList,
        ): AddPartyDealerFragment {
            val args = Bundle()
            args.putBoolean(ARG_PARAM1, isUpdate)
            args.putParcelable(ARG_PARAM2, partyDealerData)
            val fragment = AddPartyDealerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_party_dealer, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            isUpdate = it.getBoolean(ARG_PARAM1, false)
            partyDealerDataForUpdate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM2, AccountMasterList::class.java) ?: AccountMasterList()
            } else {
                it.getParcelable(ARG_PARAM2) ?: AccountMasterList()
            }
        }
        initView()
    }

    private fun initView() {
        mActivity.bottomHide()
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.text =
            if (isUpdate) getString(R.string.party_dealer) else getString(R.string.add_party_dealer)
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.llSelectPlace.setOnClickListener(this)
        binding.llSelectRegion.setOnClickListener(this)
        binding.llSelectIndustryType.setOnClickListener(this)
        binding.llSelectReferenceSource.setOnClickListener(this)
        binding.tvSubmit.setOnClickListener(this)
        binding.imgFetchCurrentLocation.setOnClickListener(this)
        binding.tvLiveLocation.setHorizontallyScrolling(true)
        binding.tvLiveLocation.movementMethod = ScrollingMovementMethod()
        binding.tvLiveLocation.setTextIsSelectable(true)
        binding.cardImage1.setOnClickListener(this)
        binding.cardImage2.setOnClickListener(this)
        binding.cardImage3.setOnClickListener(this)
        binding.cardImage4.setOnClickListener(this)
        binding.cardImageCapture.setOnClickListener(this)
        binding.etPhone.filters = arrayOf(MobileAndLandLineInputFilter(), InputFilter.LengthFilter(15))
        binding.etContactPersonPhone.filters = arrayOf(MobileAndLandLineInputFilter(), InputFilter.LengthFilter(15))

        setupImageUploadRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                executeAPIsAndSetupData()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
    }

    private fun setupImageUploadRecyclerView() {
        val apiUrl = appDao.getAppRegistration().apiHostingServer
        binding.rvImages.layoutManager =
            LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
        Log.e("TAG", "setupRecyclerView:API URL : :  " + apiUrl)
        imageAdapter = ImageAdapter(mActivity, apiUrl)
        binding.rvImages.adapter = imageAdapter
        handleAssetRVView(0)

        imageAdapter?.setOnClick(object : ImageAdapter.OnAssetImageCancelClick {
            override fun onImageCancel(position: Int) {
                if (isUpdate) {
                    if (!binding.toolbar.imgEdit.isVisible) {
                        if (position != RecyclerView.NO_POSITION && imageAnyList.size > 0) {
                            imageAnyList.removeAt(position)
                            imageAdapter?.addImage(imageAnyList, false)
                            handleAssetRVView(imageAnyList.size)
                        }
                    }
                } else {
                    if (position != RecyclerView.NO_POSITION && imageAnyList.size > 0) {
                        imageAnyList.removeAt(position)
                        imageAdapter?.addImage(imageAnyList, false)
                        handleAssetRVView(imageAnyList.size)
                    }
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

    private suspend fun executeAPIsAndSetupData() {
        if (!isUpdate) {
            withContext(Dispatchers.IO) {
                try {
                    val categoryList = async { callCategoryListApi() }
                    val placeList = async { callPlaceListApi() }
                    val userList = async { callUserListApi() }
                    val regionList = async { callCommonDropDownListApi(DROP_DOWN_REGION) }
                    val industryTypeList = async { callCommonDropDownListApi(DROP_DOWN_INDUSTRY) }
                    val referenceSourceList =
                        async { callCommonDropDownListApi(DROP_DOWN_REFERENCE_SOURCE) }
                    // Await all deferred results concurrently
                    categoryList.await()
                    placeList.await()
                    userList.await()
                    regionList.await()
                    industryTypeList.await()
                    referenceSourceList.await()
                    // Process the results as needed
                    // ...
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    // Handle exceptions
                }
            }
        }
        if (isUpdate) {
            binding.tvLiveLocation.text = partyDealerDataForUpdate.location
            currentAddress = binding.tvLiveLocation.text.toString()
            setupDataForUpdate()
        } else {
            withContext(Dispatchers.IO) {
                val locationDeferred = async { askLocationPermission() }
                locationDeferred.await()
            }
        }
    }

    private fun setupDataForUpdate() {

        binding.flCategory.visibility = View.GONE
        binding.flAccountHandle.visibility = View.GONE
        binding.tvCategory.visibility = View.VISIBLE
        binding.tvAccountHandleBy.visibility = View.VISIBLE

        binding.toolbar.imgEdit.visibility = View.VISIBLE
        binding.toolbar.imgDelete.visibility = View.VISIBLE
        binding.toolbar.imgDelete.setOnClickListener(this)
        binding.toolbar.imgEdit.setOnClickListener(this)

        binding.tvSelectPlace.text = partyDealerDataForUpdate.cityName
        binding.tvAccountHandleBy.text = partyDealerDataForUpdate.handledByUserName
        binding.tvCategory.text = partyDealerDataForUpdate.categoryName

        selectedCategory = CategoryMasterResponse(
            categoryMasterId = partyDealerDataForUpdate.categoryMasterId,
            categoryName = partyDealerDataForUpdate.categoryName,
        )
        selectedPlace = ExpenseCityListResponse(
            0,
            partyDealerDataForUpdate.cityId,
            partyDealerDataForUpdate.cityName ?: "",
            0,
            ""
        )
        selectedUser = UserListResponse(
            partyDealerDataForUpdate.handledByUserId,
            partyDealerDataForUpdate.handledByUserName ?: ""
        )

        selectedRegion = CommonDropDownResponse(
            dropdownKeyId = partyDealerDataForUpdate.ddmRegionId.toString(),
            dropdownName = partyDealerDataForUpdate.regionName
        )

        selectedReferenceSource = CommonDropDownResponse(
            dropdownKeyId = partyDealerDataForUpdate.ddmReferenceSourceId.toString(),
            dropdownName = partyDealerDataForUpdate.referenceSourceName
        )

        selectedIndustry = CommonDropDownResponse(
            dropdownKeyId = partyDealerDataForUpdate.ddmIndustryTypeId.toString(),
            dropdownName = partyDealerDataForUpdate.industryTypeName
        )
        //selectedReferenceSource = CommonDropDownResponse()

        binding.etPartyAccountName.setText(partyDealerDataForUpdate.accountName)
        binding.etAddress.setText(partyDealerDataForUpdate.address)
        binding.etZipcode.setText(partyDealerDataForUpdate.pinCode.toString())
        binding.tvSelectPlace.text = selectedPlace?.cityName ?: ""
        binding.etPhone.setText(partyDealerDataForUpdate.phoneNo)
        binding.etEmail.setText(partyDealerDataForUpdate.email)
        binding.etContactPerson.setText(partyDealerDataForUpdate.contactPersonName)
        binding.etContactPersonEmail.setText(partyDealerDataForUpdate.contactPersonEmail)
        binding.etContactPersonPhone.setText(partyDealerDataForUpdate.contactPersonPhoneNo)
        binding.etRemarks.setText(partyDealerDataForUpdate.remarks)
        binding.tvSelectIndustry.text = partyDealerDataForUpdate.industryTypeName
        binding.tvSelectRegion.text = partyDealerDataForUpdate.regionName
        binding.tvSelectReferenceResource.text = partyDealerDataForUpdate.referenceSourceName

        if ((partyDealerDataForUpdate.filePath1 ?: "").isNotEmpty()) {
            imageAnyList.add((partyDealerDataForUpdate.filePath1) ?: "")
        }
        if ((partyDealerDataForUpdate.filePath2 ?: "").isNotEmpty()) {
            imageAnyList.add((partyDealerDataForUpdate.filePath2) ?: "")
        }
        if ((partyDealerDataForUpdate.filePath3 ?: "").isNotEmpty()) {
            imageAnyList.add((partyDealerDataForUpdate.filePath3) ?: "")
        }
        if ((partyDealerDataForUpdate.filePath4 ?: "").isNotEmpty()) {
            imageAnyList.add((partyDealerDataForUpdate.filePath4) ?: "")
        }

        Log.e("TAG", "setupDataForUpdate:BEFORE " + imageAnyList.toString())

        imageAdapter?.addImage(imageAnyList, true)
        handleAssetRVView(imageAnyList.size)


        setWidgetsClickability(isEditClick)
    }

    private fun setWidgetsClickability(flag: Boolean) {
        binding.toolbar.tvHeader.text = getString(R.string.update_party_dealer)
        binding.spCategory.isEnabled = flag
        binding.spAccountHandleBy.isEnabled = flag
        binding.etPartyAccountName.isEnabled = flag
        binding.etAddress.isEnabled = flag
        binding.etZipcode.isEnabled = flag
        binding.llSelectPlace.isEnabled = flag
        binding.llSelectRegion.isEnabled = flag
        binding.llSelectIndustryType.isEnabled = flag
        binding.llSelectReferenceSource.isEnabled = flag
        binding.cardImageCapture.isEnabled = flag
        binding.etPhone.isEnabled = flag
        binding.etEmail.isEnabled = flag
        binding.etContactPerson.isEnabled = flag
        binding.etContactPersonEmail.isEnabled = flag
        binding.etContactPersonPhone.isEnabled = flag
        binding.etRemarks.isEnabled = flag
        binding.tvSubmit.visibility = if (flag) View.VISIBLE else View.GONE
        binding.imgFetchCurrentLocation.isEnabled = flag
        if (flag) {
            binding.toolbar.imgEdit.visibility = View.GONE
            binding.tvCategory.visibility = View.GONE
            binding.tvAccountHandleBy.visibility = View.GONE

            binding.flCategory.visibility = View.VISIBLE
            binding.flAccountHandle.visibility = View.VISIBLE

            callPlaceListApi()
            callCategoryListApi()
            callUserListApi()
            callCommonDropDownListApi(DROP_DOWN_REGION)
            callCommonDropDownListApi(DROP_DOWN_INDUSTRY)
            callCommonDropDownListApi(DROP_DOWN_REFERENCE_SOURCE)

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

            R.id.llSelectPlace -> {
                if (placeList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_PLACE,
                        placeList = placeList,
                        placeDialogInterfaceDetect = this as UserSearchDialogUtil.PlaceSearchDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.place_list_not_found)
                    )
                }
            }

            R.id.llSelectRegion -> {
                if (regionList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_REGION_TYPE,
                        commonDropDownList = regionList,
                        commonDropDownInterfaceDetect = this as UserSearchDialogUtil.CommonDropDownDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.region_list_not_found)
                    )
                }
            }

            R.id.llSelectIndustryType -> {
                if (industryList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_INDUSTRY_TYPE,
                        commonDropDownList = industryList,
                        commonDropDownInterfaceDetect = this as UserSearchDialogUtil.CommonDropDownDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.industry_list_not_found)
                    )
                }
            }

            R.id.llSelectReferenceSource -> {
                if (referenceSourceList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_REFERENCE_SOURCE,
                        commonDropDownList = referenceSourceList,
                        commonDropDownInterfaceDetect = this as UserSearchDialogUtil.CommonDropDownDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.reference_source_list_not_found)
                    )
                }
            }

            R.id.tvSubmit -> {
                partyDealerValidation()
            }

            R.id.imgEdit -> {
                CommonMethods.showAlertDialog(
                    mActivity,
                    getString(R.string.update_party_dealer),
                    getString(R.string.edit_msg),
                    positiveButtonText = getString(R.string.yes),
                    negativeButtonText = getString(R.string.no),
                    okListener = object : PositiveButtonListener {
                        override fun okClickListener() {
                            isEditClick = true
                            setWidgetsClickability(true)
                            CommonMethods.showToastMessage(
                                mActivity,
                                getString(R.string.edit_party_dealer)
                            )
                        }
                    }
                )

            }

            R.id.imgDelete -> {
                CommonMethods.showAlertDialog(
                    mActivity,
                    getString(R.string.alert),
                    getString(R.string.delete_msg),
                    positiveButtonText = getString(R.string.yes),
                    negativeButtonText = getString(R.string.no),
                    okListener = object : PositiveButtonListener {
                        override fun okClickListener() {
                            callDeletePartyDealerApi()
                        }
                    })
            }

            R.id.imgFetchCurrentLocation -> {
                askLocationPermission()
            }

            R.id.cardImage1 -> {
                if (partyDealerDataForUpdate.accountMasterId > 0) {
                    if (partyDealerDataForUpdate.filePath1 == null) {
                        CommonMethods.showToastMessage(
                            mActivity,
                            mActivity.getString(R.string.no_image_found)
                        )
                        return
                    }
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDao.getAppRegistration().apiHostingServer + partyDealerDataForUpdate.filePath1
                    )
                } else {
                    askCameraGalleryPermission()
                }
            }

            R.id.cardImage2 -> {
                if (partyDealerDataForUpdate.accountMasterId > 0) {
                    if (partyDealerDataForUpdate.filePath2 == null) {
                        CommonMethods.showToastMessage(
                            mActivity,
                            mActivity.getString(R.string.no_image_found)
                        )
                        return
                    }
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDao.getAppRegistration().apiHostingServer + partyDealerDataForUpdate.filePath2
                    )
                } else {
                    askCameraGalleryPermission()
                }
            }

            R.id.cardImage3 -> {
                if (partyDealerDataForUpdate.accountMasterId > 0) {
                    if (partyDealerDataForUpdate.filePath3 == null) {
                        CommonMethods.showToastMessage(
                            mActivity,
                            mActivity.getString(R.string.no_image_found)
                        )
                        return
                    }
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDao.getAppRegistration().apiHostingServer + partyDealerDataForUpdate.filePath3
                    )
                } else {
                    askCameraGalleryPermission()
                }
            }

            R.id.cardImage4 -> {
                if (partyDealerDataForUpdate.accountMasterId > 0) {
                    if (partyDealerDataForUpdate.filePath4 == null) {
                        CommonMethods.showToastMessage(
                            mActivity,
                            mActivity.getString(R.string.no_image_found)
                        )
                        return
                    }
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDao.getAppRegistration().apiHostingServer + partyDealerDataForUpdate.filePath4
                    )
                } else {
                    askCameraGalleryPermission()
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

        }
    }

    private fun askCameraGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                openAlbumForList()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                openAlbumForList()
            }
        } else {
            val arrListOffPermission = arrayListOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOffPermission) {
                openAlbumForList()
            }
        }

    }

    /*private fun openAlbumForList() {
        AlbumUtility(mActivity, true).openAlbumAndHandleImageMultipleSelection(
            onImageSelected = {
                lifecycleScope.launch(Dispatchers.IO) {
                    val modifiedImageFile = CommonMethods.addDateAndTimeToFile(
                        it, CommonMethods.createImageFile(mActivity) ?: return@launch
                    )

                    withContext(Dispatchers.Main) {
                        if (modifiedImageFile != null) {
                            imageAnyList.add(modifiedImageFile)
                            imageAdapter?.addImage(imageAnyList, false)
                            handleAssetRVView(imageAnyList.size)
                        }
                    }
                }
            },
            onError = {
                CommonMethods.showToastMessage(mActivity, it)
            }
        )
    }*/
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

    private fun openAlbum(imageType: Int) {
        AlbumUtility(mActivity, true).openAlbumAndHandleImageSelection(
            onImageSelected = {
                when (imageType) {
                    IMAGE_FILE_1 -> imageFile1 = it
                    IMAGE_FILE_2 -> imageFile2 = it
                    IMAGE_FILE_3 -> imageFile3 = it
                    IMAGE_FILE_4 -> imageFile4 = it
                }

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
                            when (imageType) {
                                IMAGE_FILE_1 -> {
                                    Glide.with(mActivity)
                                        .load(modifiedImageFile)
                                        .into(binding.imgPartyDealer1)
                                    base64Image1 =
                                        CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                            .toString()
                                    binding.imgPartyDealer1.scaleType =
                                        ImageView.ScaleType.CENTER_CROP
                                }

                                IMAGE_FILE_2 -> {
                                    Glide.with(mActivity)
                                        .load(modifiedImageFile)
                                        .into(binding.imgPartyDealer2)
                                    base64Image2 =
                                        CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                            .toString()
                                    binding.imgPartyDealer2.scaleType =
                                        ImageView.ScaleType.CENTER_CROP
                                }

                                IMAGE_FILE_3 -> {
                                    Glide.with(mActivity)
                                        .load(modifiedImageFile)
                                        .into(binding.imgPartyDealer3)
                                    base64Image3 =
                                        CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                            .toString()
                                    binding.imgPartyDealer3.scaleType =
                                        ImageView.ScaleType.CENTER_CROP
                                }

                                IMAGE_FILE_4 -> {
                                    Glide.with(mActivity)
                                        .load(modifiedImageFile)
                                        .into(binding.imgPartyDealer4)
                                    base64Image4 =
                                        CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                            .toString()
                                    binding.imgPartyDealer4.scaleType =
                                        ImageView.ScaleType.CENTER_CROP
                                }
                            }

                        }
                    }
                }
            },
            onError = {
                CommonMethods.showToastMessage(mActivity, it)
            }
        )

    }


    private fun callDeletePartyDealerApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val deletePartyDealerReq = JsonObject()
        deletePartyDealerReq.addProperty("UserId", loginData.userId)
        deletePartyDealerReq.addProperty(
            "AccountMasterId",
            partyDealerDataForUpdate.accountMasterId
        )
        deletePartyDealerReq.addProperty("ParameterString", "")
        //deletePartyDealerReq.addProperty("AccountName", partyDealerDataForUpdate.accountName)

        val deletePartyDealerCalll = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.deletePartyDealer(deletePartyDealerReq)

        deletePartyDealerCalll?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            CommonMethods.showToastMessage(
                                mActivity,
                                it.returnMessage ?: "Party/Dealer deleted successfully"
                            )
                            AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, true)
                            if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                mActivity.onBackPressedDispatcher.onBackPressed()
                            } else {
                                mActivity.onBackPressed()
                            }
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity, getString(R.string.error),
                        getString(R.string.error_message),
                        null, isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
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

    private fun partyDealerValidation() {
        /*if (binding.spCategory.selectedItemPosition == 0) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.please_select_category))
            return
        }*/
        if (binding.etPartyAccountName.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.party_account_name_validate_msg)
            )
            return
        }
        if (binding.etZipcode.text.isNotEmpty() && binding.etZipcode.text.toString().length != 6) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.please_enter_valid_zipcode)
            )
            return
        }

        if(binding.etPhone.text.toString().trim().isNotEmpty()){
            val validationMessage = CommonMethods.validateMobileLandlineNumber(binding.etPhone.text.toString().trim())
            if(validationMessage != IS_VALID){
                CommonMethods.showToastMessage(mActivity, validationMessage)
                return
            }
        }

        if (binding.etEmail.text.toString().trim().isNotEmpty() && !(CommonMethods.isEmailValid(
                binding.etEmail.text.trim().toString()
            ))
        ) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.email_not_valid_msg))
            return
        }

        if(binding.etContactPersonPhone.text.toString().trim().isNotEmpty()){
            val validationMessage = CommonMethods.validateMobileLandlineNumber(binding.etContactPersonPhone.text.toString().trim())
            if(validationMessage != IS_VALID){
                CommonMethods.showToastMessage(mActivity, validationMessage)
                return
            }
        }

        if (binding.etContactPersonEmail.text.toString().trim()
                .isNotEmpty() && !(CommonMethods.isEmailValid(
                binding.etContactPersonEmail.text.trim().toString()
            ))
        ) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.enter_valid_contact_person_email)
            )
            return
        }
        /*if (binding.etAddress.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.address_validate_msg))
            return
        }
        if (binding.etZipcode.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.zipcode_validate_msg))
            return
        }
        if (binding.tvSelectPlace.text.isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.select_place_validate))
            return
        }
        if (binding.etPhone.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_phone_validate_msg))
            return
        }
        if (!(CommonMethods.isMobileNumberValid(binding.etPhone.text.trim().toString()))) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_valid_mobile))
            return
        }
        if (binding.etEmail.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_email_validate))
            return
        }
        if (!(CommonMethods.isEmailValid(binding.etEmail.text.trim().toString()))) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.email_not_valid_msg))
            return
        }
        if (binding.etContactPerson.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.contact_person_validate_msg)
            )
            return
        }
        if (binding.etContactPersonPhone.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.contact_person_phone_validate_msg)
            )
            return
        }
        if (!(CommonMethods.isMobileNumberValid(
                binding.etContactPersonPhone.text.trim().toString()
            ))
        ) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_valid_mobile))
            return
        }
        if (binding.etContactPersonEmail.text.trim().toString().isEmpty()) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.contact_person_email_validate_msg)
            )
            return
        }
        if (!(CommonMethods.isEmailValid(binding.etContactPersonEmail.text.trim().toString()))) {
            CommonMethods.showToastMessage(
                mActivity,
                getString(R.string.contact_person_email_valid_validate_msg)
            )
            return
        }
        if (currentAddress.isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.location_ads_error_msg))
            return
        }*/
        callAddPartyDealerApi()
    }

    private fun callPlaceListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val placeListReq = JsonObject()
        placeListReq.addProperty("UserId", loginData.userId)
        placeListReq.addProperty("ParameterString", "")

        val placeListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCityStateCountryList(placeListReq)

        placeListCall?.enqueue(object : Callback<List<ExpenseCityListResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseCityListResponse>>,
                response: Response<List<ExpenseCityListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            placeList.clear()
                            placeList.addAll(it)
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null, isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<ExpenseCityListResponse>>, t: Throwable) {
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

    private fun callCategoryListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val objReq = JsonObject()
        //objReq.addProperty("categoryMasterId", 0)
        objReq.addProperty("UserId", loginData.userId)
        objReq.addProperty("ParameterString", FORM_ID_PARTY_DEALER)

        val appRegistrationData = appDao.getAppRegistration()

        val categoryCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCategoryMasterList(objReq)

        categoryCall?.enqueue(object : Callback<List<CategoryMasterResponse>> {
            override fun onResponse(
                call: Call<List<CategoryMasterResponse>>,
                response: Response<List<CategoryMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            categoryList.clear()
                            categoryList.add(
                                CategoryMasterResponse(
                                    categoryName = getString(R.string.select_category),
                                )
                            )
                            categoryList.addAll(it)
                            setupCategorySpinner()
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

            override fun onFailure(call: Call<List<CategoryMasterResponse>>, t: Throwable) {
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

    private fun callUserListApi() {
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
                            userList.clear()
                            userList.addAll(it)
                            setupUserSpinner()
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                "User List not found"
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

            override fun onFailure(call: Call<List<UserListResponse>>, t: Throwable) {
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

    private fun callCommonDropDownListApi(dropDownType: String) {
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

        val regionCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDropDownMasterDetails(dropDownType, "")

        regionCall?.enqueue(object : Callback<List<CommonDropDownResponse>> {
            override fun onResponse(
                call: Call<List<CommonDropDownResponse>>,
                response: Response<List<CommonDropDownResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            when (dropDownType) {
                                DROP_DOWN_REGION -> {
                                    regionList.clear()
                                    regionList.addAll(it)
                                }

                                DROP_DOWN_INDUSTRY -> {
                                    industryList.clear()
                                    industryList.addAll(it)
                                }

                                DROP_DOWN_REFERENCE_SOURCE -> {
                                    referenceSourceList.clear()
                                    referenceSourceList.addAll(it)
                                }

                                else -> {

                                }
                            }
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                "$dropDownType List not found"
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

    private fun setupUserSpinner() {
        val adapter = UserAdapterForSpinner(
            mActivity,
            R.layout.simple_spinner_item,
            userList,
            this
        )
        binding.spAccountHandleBy.adapter = adapter
        if (isUpdate) {
            selectedUser = UserListResponse(
                partyDealerDataForUpdate.handledByUserId,
                (partyDealerDataForUpdate.handledByUserName ?: "")
            )
            val userPos =
                userList.indexOfFirst { it.userId == selectedUser?.userId ?: 0 }//(selectedUser)
            if (userPos != -1) {
                binding.spAccountHandleBy.setSelection(userPos)
            }
        } else {
            selectedUser = UserListResponse(loginData.userId, loginData.userName ?: "")
            val userPos =
                userList.indexOfFirst { it.userId == selectedUser?.userId ?: 0 }//(selectedUser)
            if (userPos != -1) {
                binding.spAccountHandleBy.setSelection(userPos)
            }
            Log.e(
                "TAG",
                "setupUserSpinner:USER LIST SIZE :: " + userList.size + " USER POSITION :: " + userPos
            )
            binding.spAccountHandleBy.setSelection(userPos)
            //selectedUser = userList[userPosition]
        }
    }

    private fun setupCategorySpinner() {
        val adapter = LeaveTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            categoryList,
            this
        )
        binding.spCategory.adapter = adapter

        if (isUpdate) {
            val categoryPos =
                categoryList.indexOfFirst { it.categoryMasterId == selectedCategory?.categoryMasterId ?: 0 }
            if (categoryPos != -1) {
                binding.spCategory.setSelection(categoryPos)
            }
        } else {
            binding.spCategory.setSelection(0)
            selectedCategory = categoryList[0]
        }
    }

    private fun callAddPartyDealerApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val addPartyDealerReq = JsonObject()
        addPartyDealerReq.addProperty(
            "AccountMasterId",
            if (isUpdate) partyDealerDataForUpdate.accountMasterId else 0
        )
        addPartyDealerReq.addProperty("CategoryMasterId", selectedCategory?.categoryMasterId ?: 0)
        addPartyDealerReq.addProperty("CategoryName", selectedCategory?.categoryName ?: "")
        addPartyDealerReq.addProperty(
            "AccountName",
            binding.etPartyAccountName.text.toString().trim()
        )
        addPartyDealerReq.addProperty("Address", binding.etAddress.text.toString().trim())
        addPartyDealerReq.addProperty(
            "PinCode", binding.etZipcode.text.toString().trim().ifEmpty { "0" }
        )
        addPartyDealerReq.addProperty("CityId", selectedPlace?.cityId ?: 0)
        addPartyDealerReq.addProperty("CityName", selectedPlace?.cityName ?: "")
        addPartyDealerReq.addProperty("PhoneNo", binding.etPhone.text.toString().trim())
        addPartyDealerReq.addProperty("Email", binding.etEmail.text.toString().trim())
        addPartyDealerReq.addProperty(
            "ContactPersonName",
            binding.etContactPerson.text.toString().trim()
        )
        addPartyDealerReq.addProperty(
            "ContactPersonPhoneNo",
            binding.etContactPersonPhone.text.toString()
        )
        addPartyDealerReq.addProperty(
            "ContactPersonEmail",
            binding.etContactPersonEmail.text.toString()
        )
        addPartyDealerReq.addProperty("ParentAccountMasterId", 0)
        addPartyDealerReq.addProperty("ParentAccountName", "")
        addPartyDealerReq.addProperty("HandledByUserId", selectedUser?.userId ?: 0)
        addPartyDealerReq.addProperty("HandledByUserName", selectedUser?.userName ?: "")
        addPartyDealerReq.addProperty("Remakrs", binding.etRemarks.text.trim().toString())
        addPartyDealerReq.addProperty("Latitude", currentLatitude)
        addPartyDealerReq.addProperty("Longitude", currentLongitude)
        addPartyDealerReq.addProperty("Location", currentAddress)
        addPartyDealerReq.addProperty("IsActive", true)
        addPartyDealerReq.addProperty("IsDeleted", false)
        addPartyDealerReq.addProperty("CommandId", 0)
        addPartyDealerReq.addProperty("CreateBy", 0)
        addPartyDealerReq.addProperty("CreateDateTime", CommonMethods.getCurrentDateTime())
        addPartyDealerReq.addProperty("UpdateBy", 0)
        addPartyDealerReq.addProperty("UpdateDateTime", CommonMethods.getCurrentDateTime())
        addPartyDealerReq.addProperty("Deleteby", 0)
        addPartyDealerReq.addProperty("DeleteDateTime", CommonMethods.getCurrentDateTime())
        addPartyDealerReq.addProperty("Success", false)
        addPartyDealerReq.addProperty("ReturnMessage", "")
        addPartyDealerReq.addProperty("UserId", loginData.userId)
        addPartyDealerReq.addProperty("DDMRegionId", selectedRegion?.dropdownKeyId)
        addPartyDealerReq.addProperty("DDMIndustryTypeId", selectedIndustry?.dropdownKeyId)
        addPartyDealerReq.addProperty(
            "DDMReferenceSourceId",
            selectedReferenceSource?.dropdownKeyId
        )
        if (imageAnyList.size > 0) {
            if (imageAnyList[0] is String) {
                addPartyDealerReq.addProperty("FilePath1", imageAnyList[0].toString())
            } else {
                val imageBase64 =
                    CommonMethods.convertImageFileToBase64(imageAnyList[0] as File).toString()
                addPartyDealerReq.addProperty("FilePath1", imageBase64)
            }
            if (imageAnyList.size > 1) {
                if (imageAnyList[1] is String) {
                    addPartyDealerReq.addProperty("FilePath2", imageAnyList[1].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[1] as File).toString()
                    addPartyDealerReq.addProperty("FilePath2", imageBase64)
                }
            } else {
                addPartyDealerReq.addProperty("FilePath2", "")
            }
            if (imageAnyList.size > 2) {
                if (imageAnyList[2] is String) {
                    addPartyDealerReq.addProperty("FilePath3", imageAnyList[2].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[2] as File).toString()
                    addPartyDealerReq.addProperty("FilePath3", imageBase64)
                }
            } else {
                addPartyDealerReq.addProperty("FilePath3", "")
            }
            if (imageAnyList.size > 3) {
                if (imageAnyList[3] is String) {
                    addPartyDealerReq.addProperty("FilePath4", imageAnyList[3].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[3] as File).toString()
                    addPartyDealerReq.addProperty("FilePath4", imageBase64)
                }
            } else {
                addPartyDealerReq.addProperty("FilePath4", "")
            }
        } else {
            addPartyDealerReq.addProperty("FilePath1", "")
            addPartyDealerReq.addProperty("FilePath2", "")
            addPartyDealerReq.addProperty("FilePath3", "")
            addPartyDealerReq.addProperty("FilePath4", "")
        }



        print("MY REQ ::::::: " + addPartyDealerReq)
        Log.e("TAG", "callAddExpenseApi: ADD EXPENSE REQ :: " + addPartyDealerReq)

        val partyDealerCall: Call<CommonSuccessResponse>? = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.addUpdatePartyDealer(addPartyDealerReq)

        partyDealerCall?.enqueue(object : Callback<CommonSuccessResponse> {
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
                            getString(R.string.add_party_dealer),
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
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
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

    override fun placeSelect(placeDataFromSelect: ExpenseCityListResponse) {
        binding.tvSelectPlace.text = placeDataFromSelect.cityName
        selectedPlace = placeDataFromSelect
    }

    override fun onTypeSelect(typeData: CategoryMasterResponse) {
        selectedCategory = typeData
    }

    override fun onUserSelect(userData: UserListResponse) {
        selectedUser = userData
    }

    override fun dropDownSelect(dropDownData: CommonDropDownResponse, dropdDownType: String) {
        when (dropdDownType) {
            DROP_DOWN_REGION -> {
                binding.tvSelectRegion.text = dropDownData.dropdownValue
                selectedRegion = dropDownData
            }

            DROP_DOWN_INDUSTRY -> {
                binding.tvSelectIndustry.text = dropDownData.dropdownValue
                selectedIndustry = dropDownData
            }

            DROP_DOWN_REFERENCE_SOURCE -> {
                binding.tvSelectReferenceResource.text = dropDownData.dropdownValue
                selectedReferenceSource = dropDownData
            }

            else -> {}
        }

    }

    private fun locationEnableDialog() {
        try {
            if (!appDao.getLoginData().todayClockInDone) {
                binding.tvLiveLocation.text = "Fetching Location..."
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
                    binding.tvLiveLocation.text = currentAddress

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