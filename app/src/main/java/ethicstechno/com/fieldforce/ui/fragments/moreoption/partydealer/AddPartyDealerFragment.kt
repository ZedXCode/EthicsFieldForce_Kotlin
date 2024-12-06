package ethicstechno.com.fieldforce.ui.fragments.moreoption.partydealer

import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAddPartyDealerBinding
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseCityListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.reports.UserListResponse
import ethicstechno.com.fieldforce.ui.adapter.LeaveTypeAdapter
import ethicstechno.com.fieldforce.ui.adapter.UserAdapterForSpinner
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPartyDealerFragment : HomeBaseFragment(), View.OnClickListener,
    UserSearchDialogUtil.PlaceSearchDialogDetect,
    LeaveTypeAdapter.TypeSelect, UserAdapterForSpinner.UserSelect {

    lateinit var binding: FragmentAddPartyDealerBinding
    val placeList: ArrayList<ExpenseCityListResponse> = arrayListOf()
    val categoryList: ArrayList<LeaveTypeListResponse> = arrayListOf()
    val userList: ArrayList<UserListResponse> = arrayListOf()
    private var selectedPlace: ExpenseCityListResponse? = null
    private var selectedCategory: LeaveTypeListResponse? = null
    private var selectedUser: UserListResponse? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    var currentAddress = ""
    var isUpdate = false
    private var partyDealerDataForUpdate: AccountMasterList = AccountMasterList()

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
        binding.tvSubmit.setOnClickListener(this)
        binding.imgFetchCurrentLocation.setOnClickListener(this)
        binding.tvLiveLocation.setHorizontallyScrolling(true)
        binding.tvLiveLocation.movementMethod = ScrollingMovementMethod()
        binding.tvLiveLocation.setTextIsSelectable(true)

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
        if (!isUpdate) {
            withContext(Dispatchers.IO) {
                try {
                    val categoryList = async { callCategoryListApi() }
                    val placeList = async { callPlaceListApi() }
                    val userList = async { callUserListApi() }
                    // Await all deferred results concurrently
                    categoryList.await()
                    placeList.await()
                    userList.await()
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

        selectedCategory = LeaveTypeListResponse(
            0,
            partyDealerDataForUpdate.categoryMasterId,
            partyDealerDataForUpdate.categoryName,
            0.0
        )
        selectedPlace = ExpenseCityListResponse(
            0,
            partyDealerDataForUpdate.cityId,
            partyDealerDataForUpdate.cityName,
            0,
            ""
        )
        selectedUser = UserListResponse(
            partyDealerDataForUpdate.handledByUserId,
            partyDealerDataForUpdate.handledByUserName
        )

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
        setWidgetsClickability(false)
    }

    private fun setWidgetsClickability(flag: Boolean) {
        binding.toolbar.tvHeader.text = getString(R.string.update_party_dealer)
        binding.spCategory.isEnabled = flag
        binding.spAccountHandleBy.isEnabled = flag
        binding.etPartyAccountName.isEnabled = flag
        binding.etAddress.isEnabled = flag
        binding.etZipcode.isEnabled = flag
        binding.llSelectPlace.isEnabled = flag
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
        }
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
        deletePartyDealerReq.addProperty("AccountName", partyDealerDataForUpdate.accountName)

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
                if(mActivity != null) {
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
        placeListReq.addProperty("StateId", 0)
        placeListReq.addProperty("CityId", 0)

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
                if(mActivity != null) {
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

        val appRegistrationData = appDao.getAppRegistration()

        val categoryCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getPartyDealerCategoriesList()

        categoryCall?.enqueue(object : Callback<List<LeaveTypeListResponse>> {
            override fun onResponse(
                call: Call<List<LeaveTypeListResponse>>,
                response: Response<List<LeaveTypeListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            categoryList.clear()
                            categoryList.add(
                                LeaveTypeListResponse(
                                    -1,
                                    -1,
                                    getString(R.string.select_category),
                                    0.0
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

            override fun onFailure(call: Call<List<LeaveTypeListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
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

            override fun onFailure(call: Call<List<UserListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
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
            selectedUser = UserListResponse(partyDealerDataForUpdate.handledByUserId, partyDealerDataForUpdate.handledByUserName)
            val userPos = userList.indexOfFirst { it.userId == selectedUser?.userId ?: 0 }//(selectedUser)
            if (userPos != -1) {
                binding.spAccountHandleBy.setSelection(userPos)
            }
        } else {
            selectedUser = UserListResponse(loginData.userId, loginData.userName ?: "")
            val userPos = userList.indexOfFirst { it.userId == selectedUser?.userId ?: 0 }//(selectedUser)
            if (userPos != -1) {
                binding.spAccountHandleBy.setSelection(userPos)
            }
            Log.e("TAG", "setupUserSpinner:USER LIST SIZE :: "+userList.size+" USER POSITION :: "+userPos)
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
            val categoryPos = categoryList.indexOfFirst { it.categoryMasterId == selectedCategory?.categoryMasterId ?: 0 }
            if(categoryPos != -1) {
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
        addPartyDealerReq.addProperty("PinCode", binding.etZipcode.text.toString().trim())
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
        addPartyDealerReq.addProperty("IsActive", true.toString())
        addPartyDealerReq.addProperty("IsDeleted", false.toString())
        addPartyDealerReq.addProperty("CommandId", 0)
        addPartyDealerReq.addProperty("CreateBy", 0)
        addPartyDealerReq.addProperty("CreateDateTime", CommonMethods.getCurrentDateTime())
        addPartyDealerReq.addProperty("UpdateBy", 0)
        addPartyDealerReq.addProperty("UpdateDateTime", CommonMethods.getCurrentDateTime())
        addPartyDealerReq.addProperty("Deleteby", 0)
        addPartyDealerReq.addProperty("DeleteDateTime", CommonMethods.getCurrentDateTime())
        addPartyDealerReq.addProperty("Success", false.toString())
        addPartyDealerReq.addProperty("ReturnMessage", "")
        addPartyDealerReq.addProperty("UserId", loginData.userId)


        print("MY REQ ::::::: " + addPartyDealerReq)
        Log.e("TAG", "callAddExpenseApi: ADD EXPENSE REQ :: " + addPartyDealerReq)
        val partyDealerCall: Call<CommonSuccessResponse>? = if (isUpdate) {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.updatePartyDealer(addPartyDealerReq)
        } else {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.addPartyDealer(addPartyDealerReq)
        }
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
                if(mActivity != null) {
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

    override fun onTypeSelect(typeData: LeaveTypeListResponse) {
        selectedCategory = typeData
    }

    override fun onUserSelect(userData: UserListResponse) {
        selectedUser = userData
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