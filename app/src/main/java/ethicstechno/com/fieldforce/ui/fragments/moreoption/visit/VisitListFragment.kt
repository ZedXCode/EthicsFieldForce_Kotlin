package ethicstechno.com.fieldforce.ui.fragments.moreoption.visit

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
import ethicstechno.com.fieldforce.databinding.FragmentVisitListBinding
import ethicstechno.com.fieldforce.databinding.ItemListBinding
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.dashboard.DashboardDrillFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.partydealer.AddPartyDealerFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.MapViewFragment
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.FORM_ID_VISIT
import ethicstechno.com.fieldforce.utils.FOR_DASHBOARD_REPORT_REDIRECT
import ethicstechno.com.fieldforce.utils.FOR_LOCATION_REDIRECT
import ethicstechno.com.fieldforce.utils.FOR_PARTY_DEALER_LIST
import ethicstechno.com.fieldforce.utils.FOR_PARTY_DEALER_UPDATE
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

class VisitListFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentVisitListBinding
    val visitList: ArrayList<AccountMasterList> = arrayListOf()
    private var visitListAdapter: VisitListAdapter? = null
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_visit_list, container, false)
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
        binding.toolbar.tvHeader.text = mActivity.getString(R.string.party_near_by_me)
        binding.tvAddPartyDealer.setOnClickListener(this)

        setupRecyclerView(binding.rvVisit)
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
            R.id.imgSearch -> {
                binding.toolbar.clSearchView.visibility = View.VISIBLE
                binding.toolbar.imgSearch.visibility = View.GONE
            }

            R.id.imgSearchViewClose -> {
                binding.toolbar.clSearchView.visibility = View.GONE
                binding.toolbar.imgSearch.visibility = View.VISIBLE
                binding.toolbar.edtSearch.setText("")
                partyDealerPageNo = 1
                callVisitList(0, FOR_PARTY_DEALER_LIST)
            }

            R.id.tvSearchGO -> {
                isSearchTriggered = true
                partyDealerPageNo = 1
                callVisitList(0, FOR_PARTY_DEALER_LIST)
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
                //check trp is working or not
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
            if (AppPreference.getBooleanPreference(mActivity, IS_DATA_UPDATE)) {
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                binding.toolbar.imgBack.visibility = View.VISIBLE
                callVisitList(0, FOR_PARTY_DEALER_LIST)
            }
            mActivity.bottomHide()
        }
    }


    private fun callVisitList(accountMasterId: Int, apiType: Int) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.network_error_msg)
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val visitListReq = JsonObject()
        visitListReq.addProperty("AccountMasterId", 0)
        visitListReq.addProperty("UserId", loginData.userId)
        visitListReq.addProperty(
            "ParameterString",
            "$FORM_ID_VISIT AND Latitude=$currentLatitude AND Longitude=$currentLongitude"
        )

        val partyDealerCall = when (apiType) {
            /*FOR_LOCATION_REDIRECT -> {
                visitListReq.addProperty("parameterString", "")
                WebApiClient.getInstance(mActivity)
                    .webApi_without(appRegistrationData.apiHostingServer)
                    ?.getOrderAccountMasterList(visitListReq)
            }*/

            FOR_DASHBOARD_REPORT_REDIRECT, FOR_PARTY_DEALER_UPDATE, FOR_LOCATION_REDIRECT -> {
                visitListReq.addProperty("ParameterString", "")
                WebApiClient.getInstance(mActivity)
                    .webApi_without(appRegistrationData.apiHostingServer)
                    ?.getPartyDealerDetails(visitListReq)
            }

            else -> {
                visitListReq.addProperty(
                    "ParameterString",
                    "$FORM_ID_VISIT AND Latitude=$currentLatitude AND Longitude=$currentLongitude and AccountName like '%${binding.toolbar.edtSearch.text}%'"
                )
                visitListReq.addProperty("PageNo", partyDealerPageNo)
                WebApiClient.getInstance(mActivity)
                    .webApi_without(appRegistrationData.apiHostingServer)
                    ?.getPartyDealerList(visitListReq)
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
                                        binding.rvVisit.visibility = View.VISIBLE
                                        binding.tvNoData.visibility = View.GONE
                                        visitList.clear()
                                        visitList.addAll(it)
                                        //setupPartyDealer()
                                    }
                                }
                            } else {
                                binding.rvVisit.visibility = View.GONE
                                binding.tvNoData.visibility = View.VISIBLE
                            }
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

            override fun onFailure(call: Call<List<AccountMasterList>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
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
                    binding.rvVisit.visibility = View.VISIBLE
                    binding.tvNoData.visibility = View.GONE
                    if (partyDealerPageNo == 1) {
                        visitList.clear()
                        isLastPage = false
                    }
                    visitList.addAll(data)
                    visitListAdapter?.notifyDataSetChanged()
                    isScrolling = false
                } else {
                    isLastPage = true
                    if(partyDealerPageNo <= 1){
                        binding.rvVisit.visibility = View.GONE
                        binding.tvNoData.visibility = View.VISIBLE
                    }
                }
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
                        callVisitList(0, FOR_PARTY_DEALER_LIST)
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

    private fun setupRecyclerView(rvItems: RecyclerView) {
        layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        rvItems.layoutManager = layoutManager

        visitListAdapter = VisitListAdapter(visitList)
        rvItems.adapter = visitListAdapter

        rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isScrolling && !isLastPage && !isSearchTriggered && (visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                    isScrolling = true
                    partyDealerPageNo++
                    callVisitList(0, FOR_PARTY_DEALER_LIST)
                }
            }
        })
    }

    inner class VisitListAdapter(
        partyDealerList: ArrayList<AccountMasterList>
    ) : RecyclerView.Adapter<VisitListAdapter.ViewHolder>() {
        var filteredItems: List<AccountMasterList> = partyDealerList
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemListBinding.inflate(inflater, parent, false)
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
            filteredItems = visitList.filter { item ->
                item.accountName.contains(query, ignoreCase = true) ||
                        (item.email ?: "").contains(query, ignoreCase = true) ||
                        (item.phoneNo ?: "").contains(query, ignoreCase = true) ||
                        item.categoryName.contains(query, ignoreCase = true) ||
                        (item.cityName ?: "").contains(query, ignoreCase = true)
            }
            notifyDataSetChanged()
        }

        inner class ViewHolder(private val binding: ItemListBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(visitData: AccountMasterList) {
                binding.llPartyBottom.visibility = View.GONE
                binding.tvAccountName.text = visitData.accountName
                binding.tvPlace.text = visitData.cityName
                binding.tvEmail.text = visitData.email
                binding.tvMobile.text = visitData.phoneNo
                binding.tvCategory.text = " - " + visitData.categoryName

                binding.tvAddVisit.setOnClickListener {
                    mActivity.addFragment(
                        AddVisitFragment.newInstance(visitData, 0, false),
                        true,
                        true,
                        AnimationType.fadeInfadeOut
                    )
                }

                binding.imgLocation.setOnClickListener {
                    /*mActivity.addFragment(
                        MapViewFragment.newInstance(
                            0,
                            0,
                            true,
                            visitData.latitude,
                            visitData.longitude,
                            visitData.accountName,
                            visitData.location ?: "",
                            0.0,
                            0.0,
                            "",
                            "",
                            false,
                        ),
                        addToBackStack = true,
                        ignoreIfCurrent = true,
                        animationType = AnimationType.fadeInfadeOut
                    )*/
                    callVisitList(visitData.accountMasterId, FOR_LOCATION_REDIRECT)
                }

                binding.llAccount.setOnClickListener {
                    Log.e(
                        "TAG",
                        "bind: VISIT LISTING Data STOREPROCEDURE : " + visitData.storeProcedureName + ", REPORTGROUP BY : " + visitData.reportGroupBy + "" +
                                ", ParameterString : " + visitData.parameterString + ", FILTER :: " + visitData.filter
                    )
                    callVisitList(
                        visitData.accountMasterId,
                        FOR_DASHBOARD_REPORT_REDIRECT
                    )
                }

                binding.llMain.setOnClickListener {
                }
            }
        }
    }

}