package ethicstechno.com.fieldforce.ui.fragments.navigationdrawer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentProfileBinding
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseCityListResponse
import ethicstechno.com.fieldforce.models.profile.CountryListResponse
import ethicstechno.com.fieldforce.models.profile.StateListResponse
import ethicstechno.com.fieldforce.models.profile.UserProfileResponse
import ethicstechno.com.fieldforce.models.profile.ZoneListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.Executors

class ProfileFragment : HomeBaseFragment(), View.OnClickListener,
    UserSearchDialogUtil.CountrySearchDialogDetect,
    UserSearchDialogUtil.StateSearchDialogDetect, UserSearchDialogUtil.PlaceSearchDialogDetect,
    UserSearchDialogUtil.ZoneSearchDialogDetect {

    //private var prefREM: SharedPreferences? = null
    lateinit var binding: FragmentProfileBinding
    var countryList: ArrayList<CountryListResponse> = arrayListOf()
    var zoneList: ArrayList<ZoneListResponse> = arrayListOf()
    var stateList: ArrayList<StateListResponse> = arrayListOf()
    var cityList: ArrayList<ExpenseCityListResponse> = arrayListOf()

    lateinit var selectedCountry: CountryListResponse
    lateinit var selectedState: StateListResponse
    lateinit var selectedCity: ExpenseCityListResponse
    lateinit var selectedZone: ZoneListResponse
    lateinit var userProfileResponse: UserProfileResponse
    var imageFile: File? = null
    var base64Image: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity.bottomHide()
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.text = getString(R.string.user_detaiils)
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.tvSelectCountry.setOnClickListener(this)
        binding.tvSelectState.setOnClickListener(this)
        binding.tvSelectCity.setOnClickListener(this)
        binding.tvSelectZone.setOnClickListener(this)
        binding.tvUpdate.setOnClickListener(this)
        binding.imgEditProfile.setOnClickListener(this)
        binding.imgProfile.setOnClickListener(this)
        callProfileDetailsApi()
    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }


    private fun callProfileDetailsApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val userProfileReq = JsonObject()
        userProfileReq.addProperty("UserId", loginData.userId)

        val reportListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getUserProfile(userProfileReq)

        reportListCall?.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(
                call: Call<UserProfileResponse>,
                response: Response<UserProfileResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success == true) {
                            userProfileResponse = it
                            displayProfileData(it)
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

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
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

    private fun displayProfileData(userProfileResponse: UserProfileResponse) {
        ImageUtils().loadCircleIMageUrl(
            mActivity,
            appDatabase.appDao().getAppRegistration().apiHostingServer + userProfileResponse.photo,
            binding.imgProfile
        )
        binding.etFirstName.setText(userProfileResponse.firstName)
        binding.etMiddleName.setText(userProfileResponse.middleName)
        binding.etLastName.setText(userProfileResponse.lastName)
        binding.etAddress.setText(userProfileResponse.address)
        binding.tvSelectCountry.text = userProfileResponse.countryName
        binding.tvSelectState.text = userProfileResponse.stateName
        binding.tvSelectZone.text = userProfileResponse.zoneName
        binding.etPincode.setText(userProfileResponse.pinCode)
        binding.etMobileNo.setText(userProfileResponse.personalMobileNo)
        binding.etMail.setText(userProfileResponse.personalEmailId)
        binding.tvSelectCity.text = userProfileResponse.cityName
        selectedZone = ZoneListResponse(
            userProfileResponse.zoneId.toInt(),
            userProfileResponse.zoneName
        )
        selectedCountry = CountryListResponse(
            userProfileResponse.countryId.toInt(),
            userProfileResponse.countryName
        )
        selectedState = StateListResponse(
            userProfileResponse.stateId.toInt(),
            userProfileResponse.countryId.toInt(),
            userProfileResponse.stateName
        )
        selectedCity = ExpenseCityListResponse(
            userProfileResponse.stateId.toInt(),
            userProfileResponse.cityId.toInt(),
            userProfileResponse.cityName,
            0,
            ""
        )
        callCountryListApi()
        callStateListApi()
        callCityListApi()
        callZoneListApi()
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
            R.id.tvSelectCountry -> {
                if (countryList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_COUNTRY,
                        countryList = countryList,
                        countryInterfaceDetect = this as UserSearchDialogUtil.CountrySearchDialogDetect
                    )
                    userDialog.showUserSearchDialog()
                }
            }
            R.id.tvSelectState -> {
                if (stateList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_STATE,
                        stateList = stateList,
                        stateInterfaceDetect = this as UserSearchDialogUtil.StateSearchDialogDetect
                    )
                    userDialog.showUserSearchDialog()
                }
            }
            R.id.tvSelectCity -> {
                if (cityList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_PLACE,
                        placeList = cityList,
                        placeDialogInterfaceDetect = this as UserSearchDialogUtil.PlaceSearchDialogDetect
                    )
                    userDialog.showUserSearchDialog()
                }
            }
            R.id.tvSelectZone -> {
                if (zoneList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_ZONE,
                        zoneList = zoneList,
                        zoneInterfaceDetect = this as UserSearchDialogUtil.ZoneSearchDialogDetect
                    )
                    userDialog.showUserSearchDialog()
                }
            }
            R.id.tvUpdate -> {
                profileSubmitValidation()
            }
            R.id.imgEditProfile -> {
                askCameraGalleryPermission()
            }
            R.id.imgProfile -> {
                if (userProfileResponse != null && userProfileResponse.photo != null && userProfileResponse.photo!!.isNotEmpty()) {
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDatabase.appDao()
                            .getAppRegistration().apiHostingServer + userProfileResponse.photo
                    )
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
            val arrListOffPermission = arrayListOf<String>(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
            PermissionUtil(mActivity).requestPermissions(arrListOffPermission) {
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
                    val modifiedImageFile: File = CommonMethods.addDateAndTimeToFile(
                        it,
                        CommonMethods.createImageFile(mActivity)!!
                    )

                    if (modifiedImageFile != null) {
                        Thread.sleep(1000)

                        handler.post {
                            Glide.with(mActivity)
                                .load(modifiedImageFile)
                                .into(binding.imgProfile)
                            base64Image =
                                CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                    .toString()
                            binding.imgProfile.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    }
                }
            },
            onError = {
                CommonMethods.showToastMessage(mActivity, it)
            }
        )

    }

    private fun profileSubmitValidation() {

        if (binding.etFirstName.text.isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_first_name))
            return
        }
        if (binding.etMiddleName.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_middle_name))
            return
        }
        if (binding.etLastName.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_last_name))
            return
        }
        if (binding.etAddress.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_address))
            return
        }
        if (binding.tvSelectCountry.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_country))
            return
        }
        if (binding.tvSelectState.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_state))
            return
        }
        if (binding.tvSelectCity.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_city))
            return
        }
        if (binding.tvSelectZone.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_zone))
            return
        }
        if (binding.etPincode.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_pincode))
            return
        }
        if (binding.etMobileNo.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_mobile))
            return
        }
        if (!(CommonMethods.isMobileNumberValid(binding.etMobileNo.text.trim().toString()))) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_valid_mobile))
            return
        }
        if (binding.etMail.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_email))
            return
        }
        if (!(CommonMethods.isEmailValid(binding.etMail.text.trim().toString()))) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.email_not_valid_msg))
            return
        }
        callUpdateProfileApi()
    }

    private fun callUpdateProfileApi() {

        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val profileUpdateReq = JsonObject()
        profileUpdateReq.addProperty("UserId", loginData.userId)
        profileUpdateReq.addProperty("FirstName", binding.etFirstName.text.toString().trim())
        profileUpdateReq.addProperty("MiddleName", binding.etMiddleName.text.toString().trim())
        profileUpdateReq.addProperty("LastName", binding.etLastName.text.toString().trim())
        profileUpdateReq.addProperty("CountryId", selectedCountry.countryId)
        profileUpdateReq.addProperty("StateId", selectedState.stateId)
        profileUpdateReq.addProperty("ZoneId", selectedZone.zoneId)
        profileUpdateReq.addProperty("CityId", selectedCity.cityId)
        profileUpdateReq.addProperty("PinCode", binding.etPincode.text.toString().trim())
        profileUpdateReq.addProperty("Address", binding.etAddress.text.toString().trim())
        profileUpdateReq.addProperty("PersonalEmailId", binding.etMail.text.toString().trim())
        profileUpdateReq.addProperty("PersonalMobileNo", binding.etMobileNo.text.toString().trim())
        profileUpdateReq.addProperty("IsActive", true.toString())
        profileUpdateReq.addProperty("UpdateBy", loginData.userId)
        profileUpdateReq.addProperty("Photo", base64Image.ifEmpty { "" })

        val profileCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.updateUserProfile(profileUpdateReq)

        profileCall?.enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(
                call: Call<UserProfileResponse>,
                response: Response<UserProfileResponse>
            ) {
                CommonMethods.hideLoading()

                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success!!) {
                            Log.e("TAG", "onResponse: photo updates" + it.photo)
                            appDao.updateProfilePath(it.photo, appDao.getLoginData().userId)
                            appDao.updateUsername(it.userName, appDao.getLoginData().userId)
                            CommonMethods.showAlertDialog(
                                mActivity,
                                getString(R.string.profile_update),
                                it.returnMessage,
                                okListener = object : PositiveButtonListener {
                                    override fun okClickListener() {
                                        mActivity.refreshProfileImage()
                                        callProfileDetailsApi()
                                    }
                                }, isCancelVisibility = false
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

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
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

    private fun callCountryListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val countryListReq = JsonObject()
        countryListReq.addProperty("UserId", loginData.userId)

        val countryCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCountryList(countryListReq)

        countryCall?.enqueue(object : Callback<List<CountryListResponse>> {
            override fun onResponse(
                call: Call<List<CountryListResponse>>,
                response: Response<List<CountryListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            countryList.clear()
                            countryList.addAll(it)
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

            override fun onFailure(call: Call<List<CountryListResponse>>, t: Throwable) {
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

    private fun callZoneListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.network_error))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val zoneListReq = JsonObject()
        zoneListReq.addProperty("ZoneId", 0)

        val zoneCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getZoneList(zoneListReq)

        zoneCall?.enqueue(object : Callback<List<ZoneListResponse>> {
            override fun onResponse(
                call: Call<List<ZoneListResponse>>,
                response: Response<List<ZoneListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            zoneList.clear()
                            zoneList.addAll(it)
                            //setupReportRecyclerView()
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

            override fun onFailure(call: Call<List<ZoneListResponse>>, t: Throwable) {
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

    private fun callStateListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.network_error))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val stateListReq = JsonObject()
        stateListReq.addProperty("CountryId", selectedCountry.countryId)//SelectedCountry
        stateListReq.addProperty("StateId", 0)

        val stateCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getStateList(stateListReq)

        stateCall?.enqueue(object : Callback<List<StateListResponse>> {
            override fun onResponse(
                call: Call<List<StateListResponse>>,
                response: Response<List<StateListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            stateList.clear()
                            stateList.addAll(it)
                            //setupReportRecyclerView()
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

            override fun onFailure(call: Call<List<StateListResponse>>, t: Throwable) {
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

    private fun callCityListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.network_error))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val cityListReq = JsonObject()
        cityListReq.addProperty("StateId", selectedState.stateId)//SelectedCountry
        cityListReq.addProperty("CityId", 0)

        val cityCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCityListApi(cityListReq)

        cityCall?.enqueue(object : Callback<List<ExpenseCityListResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseCityListResponse>>,
                response: Response<List<ExpenseCityListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            cityList.clear()
                            cityList.addAll(it)
                            //setupReportRecyclerView()
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

            override fun onFailure(call: Call<List<ExpenseCityListResponse>>, t: Throwable) {
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

    override fun countrySelect(countryData: CountryListResponse) {
        binding.tvSelectCountry.text = countryData.countryName
        selectedCountry = countryData
        selectedState = StateListResponse(0, 0, "")
        binding.tvSelectState.hint = getString(R.string.select_state)
        binding.tvSelectCity.hint = getString(R.string.select_city)
        binding.tvSelectState.text = ""
        binding.tvSelectCity.text = ""
        callStateListApi()
    }

    override fun stateSelect(stateData: StateListResponse) {
        binding.tvSelectState.text = stateData.stateName
        selectedState = stateData
        selectedCity = ExpenseCityListResponse(0, 0, "", 0, "")
        binding.tvSelectCity.text = getString(R.string.select_city)
        callCityListApi()
    }

    override fun placeSelect(cityData: ExpenseCityListResponse) {
        binding.tvSelectCity.text = cityData.cityName
        selectedCity = cityData
        binding.tvSelectCity.text = cityData.cityName
    }

    override fun zoneSelect(zoneData: ZoneListResponse) {
        binding.tvSelectZone.text = zoneData.zoneName
        selectedZone = zoneData
    }

}