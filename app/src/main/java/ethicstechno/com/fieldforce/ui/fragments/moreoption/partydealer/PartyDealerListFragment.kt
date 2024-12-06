package ethicstechno.com.fieldforce.ui.fragments.moreoption.partydealer

import AnimationType
import addFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentPartyDealerListBinding
import ethicstechno.com.fieldforce.databinding.PartyDealerItemListBinding
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.VisitListResponse
import ethicstechno.com.fieldforce.models.reports.VisitReportListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.dashboard.DashboardDrillFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.visit.AddVisitFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.MapViewFragment
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PartyDealerListFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentPartyDealerListBinding
    val partyDealerList: ArrayList<AccountMasterList> = arrayListOf()
    private lateinit var partyDealerAdapter: PartyDealerListAdapter

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
        binding.toolbar.svView.visibility = View.VISIBLE
        binding.toolbar.svView.queryHint = HtmlCompat.fromHtml(
            mActivity.getString(R.string.search_here),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.tvHeader.text = mActivity.getString(R.string.party_dealer_list)
        binding.tvAddPartyDealer.setOnClickListener(this)
        callPartyDealerList()
        setupSearchFilter()

    }

    private fun setupSearchFilter() {
        binding.toolbar.svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(::partyDealerAdapter.isInitialized){
                    partyDealerAdapter.filter(newText.orEmpty())
                }
                //partyDealerAdapter.filter(newText.orEmpty())
                return true
            }
        })
        binding.toolbar.svView.setOnSearchClickListener {
            binding.toolbar.imgBack.visibility = View.GONE
        }
        binding.toolbar.svView.setOnCloseListener {
            binding.toolbar.imgBack.visibility = View.VISIBLE
            false
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
            Log.e("TAG", "onHiddenChanged: "+AppPreference.getBooleanPreference(mActivity, IS_DATA_UPDATE))
            if (AppPreference.getBooleanPreference(mActivity, IS_DATA_UPDATE)) {
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                binding.toolbar.imgBack.visibility = View.VISIBLE
                binding.toolbar.svView.onActionViewCollapsed()
                callPartyDealerList()
            }
        }
    }


    private fun callPartyDealerList() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val partyDealerListReq = JsonObject()
        partyDealerListReq.addProperty("UserId", loginData.userId)

        val partyDealerCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getAccountMasterList(partyDealerListReq)

        partyDealerCall?.enqueue(object : Callback<List<AccountMasterList>> {
            override fun onResponse(
                call: Call<List<AccountMasterList>>,
                response: Response<List<AccountMasterList>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            binding.rvPartyDealer.visibility = View.VISIBLE
                            binding.tvNoData.visibility = View.GONE
                            partyDealerList.clear()
                            partyDealerList.addAll(it)
                            setupPartyDealer()
                        } else {
                            binding.rvPartyDealer.visibility = View.GONE
                            binding.tvNoData.visibility = View.VISIBLE
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
                if(mActivity != null) {
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

    private fun setupPartyDealer() {
        partyDealerAdapter = PartyDealerListAdapter(partyDealerList)
        binding.rvPartyDealer.adapter = partyDealerAdapter
        binding.rvPartyDealer.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
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
                item.accountName.contains(query, ignoreCase = true) ||
                        item.email.contains(query, ignoreCase = true) ||
                        item.phoneNo.contains(query, ignoreCase = true) ||
                        item.categoryName.contains(query, ignoreCase = true)
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
                    val visitData = VisitListResponse(
                        partyDealerData.accountMasterId,
                        partyDealerData.categoryMasterId,
                        partyDealerData.categoryName,
                        partyDealerData.accountName,
                        partyDealerData.cityId,
                        partyDealerData.cityName,
                        partyDealerData.phoneNo,
                        partyDealerData.email,
                        partyDealerData.contactPersonName,
                        partyDealerData.latitude,
                        partyDealerData.longitude,
                        partyDealerData.location,
                        partyDealerData.isActive,
                        partyDealerData.userId,
                        partyDealerData.reportSetupId,
                        partyDealerData.reportName,
                        partyDealerData.apiName,
                        partyDealerData.storeProcedureName,
                        partyDealerData.reportGroupBy,
                        partyDealerData.filter,
                        partyDealerData.parameterString
                    )
                    mActivity.addFragment(
                        AddVisitFragment.newInstance(visitData, VisitReportListResponse(), false),
                        addToBackStack = true,
                        ignoreIfCurrent = true,
                        animationType = AnimationType.fadeInfadeOut
                    )
                }

                itemBinding.imgPartyLocation.setOnClickListener {
                    if (partyDealerData.latitude == 0.0 || partyDealerData.longitude == 0.0) {
                        CommonMethods.showAlertDialog(
                            mActivity, mActivity.getString(R.string.party_dealer), getString(
                                R.string.location_not_found
                            ), isCancelVisibility = false, okListener = null
                        )
                        return@setOnClickListener
                    }
                    mActivity.addFragment(
                        MapViewFragment.newInstance(
                            0,
                            0,
                            true,
                            partyDealerData.latitude,
                            partyDealerData.longitude,
                            partyDealerData.accountName,
                            partyDealerData.location,
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
                }
                itemBinding.llPartyAccount.setOnClickListener {
                    Log.e("TAG", "bind: PARTY DEALER Data,  STOREPROCEDURE : "+partyDealerData.storeProcedureName+", REPORTGROUP BY : "+partyDealerData.reportGroupBy+"" +
                            ", ParameterString : "+partyDealerData.parameterString+", FILTER :: "+partyDealerData.filter)
                    mActivity.addFragment(
                        DashboardDrillFragment.newInstance(
                            false,
                            DashboardListResponse(),
                            DashboardDrillResponse(),
                            partyDealerData.storeProcedureName,
                            partyDealerData.reportSetupId,
                            arrayListOf(),
                            partyDealerData.reportName,
                            partyDealerData.filter,
                            partyDealerData.reportGroupBy,
                            true,
                            isFromPartyDealerORVisit = true,
                            parameterString = partyDealerData.parameterString
                        ), true, true, AnimationType.fadeInfadeOut
                    )
                }

                itemBinding.llTop.setOnClickListener {
                    mActivity.addFragment(
                        AddPartyDealerFragment.newInstance(isUpdate = true, partyDealerData),
                        addToBackStack = true,
                        ignoreIfCurrent = true,
                        animationType = AnimationType.fadeInfadeOut
                    )
                }
            }
        }
    }

}