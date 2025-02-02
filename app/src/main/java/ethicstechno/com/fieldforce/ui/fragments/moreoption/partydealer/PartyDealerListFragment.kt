package ethicstechno.com.fieldforce.ui.fragments.moreoption.partydealer

import AnimationType
import addFragment
import android.app.Activity
import android.content.Context
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
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentPartyDealerListBinding
import ethicstechno.com.fieldforce.databinding.PartyDealerItemListBinding
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.dashboard.DashboardDrillFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.visit.AddVisitFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.MapViewFragment
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.FORM_ID_PARTY_DEALER
import ethicstechno.com.fieldforce.utils.FOR_DASHBOARD_REPORT_REDIRECT
import ethicstechno.com.fieldforce.utils.FOR_LOCATION_REDIRECT
import ethicstechno.com.fieldforce.utils.FOR_PARTY_DEALER_LIST
import ethicstechno.com.fieldforce.utils.FOR_PARTY_DEALER_UPDATE
import ethicstechno.com.fieldforce.utils.FOR_PARTY_DEALER_VISIT
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.IS_MOCK_LOCATION
import ethicstechno.com.fieldforce.utils.PermissionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PartyDealerListFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentPartyDealerListBinding
    val partyDealerList: ArrayList<AccountMasterList> = arrayListOf()
    private lateinit var partyDealerAdapter: PartyDealerListAdapter
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var currentLatitude = 0.0
    var currentLongitude = 0.0
    private var partyDealerPageNo = 1
    private var isScrolling = false
    private var isLastPage = false
    private var layoutManager: LinearLayoutManager? = null
    private var isSearchTriggered = false


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
            } else {
                locationEnableDialog()
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_party_dealer_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {
        mActivity.bottomHide()
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.imgSearch.visibility = View.VISIBLE
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.imgSearch.setOnClickListener(this)
        binding.toolbar.imgSearchViewClose.setOnClickListener(this)
        binding.toolbar.tvSearchGO.setOnClickListener(this)
        binding.toolbar.tvHeader.text = mActivity.getString(R.string.party_dealer_list)
        binding.tvAddPartyDealer.setOnClickListener(this)
        setupRecyclerView(binding.rvPartyDealer)
        binding.toolbar.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()

                if (searchText.isNotEmpty()) {
                    binding.toolbar.tvSearchGO.visibility = View.VISIBLE
                } else {
                    binding.toolbar.tvSearchGO.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed here
            }
        })

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
                val locationDeferred = async { askLocationPermission() }
                locationDeferred.await()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("TAG", "executeAPIsAndSetupData: " + e.message.toString())
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

                    Handler(Looper.getMainLooper()).postDelayed({
                        CommonMethods.hideLoading()
                        callPartyDealerList(0, FOR_PARTY_DEALER_LIST)
                    }, 1000)


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


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgSearch -> {
                binding.toolbar.clSearchView.visibility = View.VISIBLE
                binding.toolbar.edtSearch.requestFocus()
                val imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.toolbar.edtSearch, InputMethodManager.SHOW_IMPLICIT)
                binding.toolbar.imgSearch.visibility = View.GONE
            }

            R.id.imgSearchViewClose -> {
                binding.toolbar.clSearchView.visibility = View.GONE
                binding.toolbar.imgSearch.visibility = View.VISIBLE
                binding.toolbar.edtSearch.setText("")
                partyDealerPageNo = 1
                callPartyDealerList(0, FOR_PARTY_DEALER_LIST)
            }

            R.id.tvSearchGO -> {
                isSearchTriggered = true
                partyDealerPageNo = 1
                callPartyDealerList(0, FOR_PARTY_DEALER_LIST)
                binding.toolbar.tvSearchGO.visibility = View.GONE
            }

            R.id.imgBack -> {
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }

            R.id.tvAddPartyDealer -> {
                mActivity.addFragment(
                    AddPartyDealerFragment(),
                    addToBackStack = true,
                    ignoreIfCurrent = true,
                    animationType = AnimationType.fadeInfadeOut
                )
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            mActivity.bottomHide()
            Log.e(
                "TAG",
                "onHiddenChanged: " + AppPreference.getBooleanPreference(mActivity, IS_DATA_UPDATE)
            )
            if (AppPreference.getBooleanPreference(mActivity, IS_DATA_UPDATE)) {
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                binding.toolbar.imgBack.visibility = View.VISIBLE
                callPartyDealerList(0, FOR_PARTY_DEALER_LIST)
            }
        }
    }


    private fun callPartyDealerList(accountMasterId: Int, apiType: Int) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val partyDealerListReq = JsonObject()
        partyDealerListReq.addProperty("accountMasterId", accountMasterId)
        partyDealerListReq.addProperty("userId", loginData.userId)
        if (apiType == FOR_PARTY_DEALER_LIST) {
            partyDealerListReq.addProperty("pageNo", partyDealerPageNo)
        }

        val partyDealerCall = when (apiType) {

            FOR_DASHBOARD_REPORT_REDIRECT, FOR_PARTY_DEALER_UPDATE, FOR_LOCATION_REDIRECT -> {
                partyDealerListReq.addProperty("ParameterString", "")
                WebApiClient.getInstance(mActivity)
                    .webApi_without(appRegistrationData.apiHostingServer)
                    ?.getPartyDealerDetails(partyDealerListReq)
            }

            else -> {
                partyDealerListReq.addProperty(
                    "ParameterString",
                    "$FORM_ID_PARTY_DEALER AND Latitude=$currentLatitude AND Longitude=$currentLongitude and AccountName like '%${binding.toolbar.edtSearch.text}%'"
                )
                WebApiClient.getInstance(mActivity)
                    .webApi_without(appRegistrationData.apiHostingServer)
                    ?.getPartyDealerList(partyDealerListReq)
            }
        }

        partyDealerCall?.enqueue(object : Callback<List<AccountMasterList>> {
            override fun onResponse(
                call: Call<List<AccountMasterList>>,
                response: Response<List<AccountMasterList>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    if (apiType == FOR_PARTY_DEALER_LIST) {
                        handleApiResponse(response)
                    } else {
                        response.body()?.let {
                            if (it.isNotEmpty()) {
                                val partyDealerData = it[0]
                                when (apiType) {
                                    FOR_LOCATION_REDIRECT -> {
                                        if (partyDealerData.latitude > 0 && partyDealerData.longitude > 0) {
                                            mActivity.addFragment(
                                                MapViewFragment.newInstance(
                                                    0,
                                                    0,
                                                    true,
                                                    partyDealerData.latitude,
                                                    partyDealerData.longitude,
                                                    partyDealerData.accountName ?: "",
                                                    partyDealerData.location ?: "",
                                                    0.0,
                                                    0.0,
                                                    "",
                                                    "",
                                                    false,
                                                ),
                                                addToBackStack = true,
                                                ignoreIfCurrent = true,
                                                animationType = AnimationType.fadeInfadeOut
                                            )
                                        } else {
                                            CommonMethods.showAlertDialog(
                                                mActivity,
                                                mActivity.getString(R.string.party_dealer),
                                                getString(
                                                    R.string.location_not_found
                                                ),
                                                isCancelVisibility = false,
                                                okListener = null
                                            )
                                        }
                                    }

                                    FOR_DASHBOARD_REPORT_REDIRECT -> {
                                        mActivity.addFragment(
                                            DashboardDrillFragment.newInstance(
                                                false,
                                                DashboardListResponse(),
                                                DashboardDrillResponse(),
                                                partyDealerData.storeProcedureName ?: "",
                                                partyDealerData.reportSetupId,
                                                arrayListOf(),
                                                partyDealerData.reportName ?: "",
                                                partyDealerData.filter ?: "",
                                                partyDealerData.reportGroupBy ?: "",
                                                true,
                                                isFromPartyDealerORVisit = true,
                                                parameterString = partyDealerData.parameterString
                                                    ?: "",
                                                productFilter = false
                                            ), true, true, AnimationType.fadeInfadeOut
                                        )
                                    }

                                    FOR_PARTY_DEALER_VISIT -> {
                                        /*val visitData = AccountMasterList(
                                            partyDealerData.accountMasterId,
                                            partyDealerData.categoryMasterId,
                                            partyDealerData.categoryName ?: "",
                                            partyDealerData.accountName ?: "",
                                            partyDealerData.cityId,
                                            partyDealerData.cityName ?: "",
                                            partyDealerData.phoneNo ?: "",
                                            partyDealerData.email ?: "",
                                            partyDealerData.contactPersonName ?: "",
                                            partyDealerData.latitude,
                                            partyDealerData.longitude,
                                            partyDealerData.location ?: "",
                                            partyDealerData.isActive,
                                            partyDealerData.userId,
                                            partyDealerData.reportSetupId,
                                            partyDealerData.reportName ?: "",
                                            partyDealerData.apiName ?: "",
                                            partyDealerData.storeProcedureName ?: "",
                                            partyDealerData.reportGroupBy ?: "",
                                            partyDealerData.filter ?: "",
                                            partyDealerData.parameterString ?: ""
                                        )*/

                                        mActivity.addFragment(
                                            AddVisitFragment.newInstance(
                                                partyDealerData,
                                                0,
                                                false
                                            ),
                                            addToBackStack = true,
                                            ignoreIfCurrent = true,
                                            animationType = AnimationType.fadeInfadeOut
                                        )
                                    }

                                    FOR_PARTY_DEALER_UPDATE -> {
                                        mActivity.addFragment(
                                            AddPartyDealerFragment.newInstance(
                                                isUpdate = true,
                                                partyDealerData
                                            ),
                                            addToBackStack = true,
                                            ignoreIfCurrent = true,
                                            animationType = AnimationType.fadeInfadeOut
                                        )
                                    }

                                    else -> {
                                        binding.rvPartyDealer.visibility = View.VISIBLE
                                        binding.tvNoData.visibility = View.GONE
                                        partyDealerList.clear()
                                        partyDealerList.addAll(it)
                                        setupPartyDealer()
                                    }
                                }
                            } else {
                                binding.rvPartyDealer.visibility = View.GONE
                                binding.tvNoData.visibility = View.VISIBLE
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

            override fun onFailure(call: Call<List<AccountMasterList>>, t: Throwable) {
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

    private fun handleApiResponse(
        response: Response<List<AccountMasterList>>
    ) {
        isSearchTriggered = false
        if (response.isSuccessful) {
            response.body()?.let { data ->
                if (data.isNotEmpty()) {
                    if (data.size == 1) {
                        isSearchTriggered = true
                    }
                    binding.rvPartyDealer.visibility = View.VISIBLE
                    binding.tvNoData.visibility = View.GONE
                    if (partyDealerPageNo == 1) {
                        partyDealerList.clear()
                        isLastPage = false
                    }
                    partyDealerList.addAll(data)
                    partyDealerAdapter.notifyDataSetChanged()
                    isScrolling = false
                } else {
                    isLastPage = true
                    if(partyDealerPageNo <= 1){
                        binding.rvPartyDealer.visibility = View.GONE
                        binding.tvNoData.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupPartyDealer() {
        partyDealerAdapter = PartyDealerListAdapter(partyDealerList)
        binding.rvPartyDealer.adapter = partyDealerAdapter
        binding.rvPartyDealer.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    private fun setupRecyclerView(rvItems: RecyclerView) {
        layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        rvItems.layoutManager = layoutManager

        partyDealerAdapter =
            PartyDealerListAdapter(
                partyDealerList
            )
        rvItems.adapter = partyDealerAdapter

        rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isScrolling && !isLastPage && !isSearchTriggered && (visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                    isScrolling = true
                    partyDealerPageNo++
                    callPartyDealerList(0, FOR_PARTY_DEALER_LIST)
                }
            }
        })
    }


    inner class PartyDealerListAdapter(
        partyDealerList: ArrayList<AccountMasterList>
    ) : RecyclerView.Adapter<PartyDealerListAdapter.ViewHolder>() {
        var filteredItems: List<AccountMasterList> = partyDealerList
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = PartyDealerItemListBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return filteredItems.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val dashboardData = filteredItems[position]
            holder.bind(dashboardData)
        }

        fun filter(query: String) {
            filteredItems = partyDealerList.filter { item ->
                (item.accountName ?: "").contains(query, ignoreCase = true) ||
                        (item.email ?: "").contains(query, ignoreCase = true) ||
                        (item.phoneNo ?: "").contains(query, ignoreCase = true) ||
                        (item.categoryName ?: "").contains(query, ignoreCase = true)
            }
            notifyDataSetChanged()
        }

        inner class ViewHolder(private val itemBinding: PartyDealerItemListBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(partyDealerData: AccountMasterList) {
                itemBinding.llBottomVisit.visibility = View.GONE
                itemBinding.tvAccountName.text = partyDealerData.accountName
                itemBinding.tvPlace.text = partyDealerData.cityName
                itemBinding.tvEmail.text = partyDealerData.email
                itemBinding.tvMobile.text = partyDealerData.phoneNo
                itemBinding.tvCategory.text = " - " + partyDealerData.categoryName

                itemBinding.tvAddVisitInParty.setOnClickListener {
                    callPartyDealerList(partyDealerData.accountMasterId, FOR_PARTY_DEALER_VISIT)
                }

                itemBinding.imgPartyLocation.setOnClickListener {
                    callPartyDealerList(partyDealerData.accountMasterId, FOR_LOCATION_REDIRECT)
                }
                itemBinding.llPartyAccount.setOnClickListener {
                    callPartyDealerList(
                        partyDealerData.accountMasterId,
                        FOR_DASHBOARD_REPORT_REDIRECT
                    );
                }

                itemBinding.llTop.setOnClickListener {
                    callPartyDealerList(partyDealerData.accountMasterId, FOR_PARTY_DEALER_UPDATE)
                }
            }
        }
    }


}