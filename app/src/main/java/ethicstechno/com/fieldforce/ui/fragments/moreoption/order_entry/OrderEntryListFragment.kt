package ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry

import AnimationType
import addFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.internal.service.Common
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentOrderEntryListBinding
import ethicstechno.com.fieldforce.databinding.ItemOrderLayoutBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.orderentry.OrderListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class OrderEntryListFragment : HomeBaseFragment(), View.OnClickListener,
    FilterDialogListener {

    lateinit var orderEntryListBinding: FragmentOrderEntryListBinding
    var orderList: ArrayList<OrderListResponse> = arrayListOf()
    var startDate = ""
    var endDate = ""
    private var selectedDateOptionPosition = 4 // THIS MONTH
    private var selectedPartyDealer : AccountMasterList? = null
    var leaveApplicationAdapter = OrderEntryListAdapter(orderList)
    private var accountMasterList: ArrayList<AccountMasterList> = arrayListOf()


    companion object {

        fun newInstance(
            isForApproval: Boolean
        ): OrderEntryListFragment {
            val args = Bundle()
            args.putBoolean(ARG_PARAM1, isForApproval)
            val fragment = OrderEntryListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        orderEntryListBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_order_entry_list, container, false)
        return orderEntryListBinding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*arguments?.let {
            isForApproval = it.getBoolean(ARG_PARAM1, false)
        }*/
        initView()

    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {
        mActivity.bottomHide()
        startDate = CommonMethods.getStartDateOfCurrentMonth()
        endDate = CommonMethods.getCurrentDate()
        orderEntryListBinding.toolbar.imgBack.visibility = View.VISIBLE
        orderEntryListBinding.toolbar.imgFilter.visibility = View.VISIBLE
        orderEntryListBinding.toolbar.imgFilter.visibility = View.VISIBLE
        orderEntryListBinding.toolbar.tvHeader.text = getString(R.string.order_entry_list)
        orderEntryListBinding.toolbar.imgFilter.setOnClickListener(this)
        orderEntryListBinding.toolbar.imgBack.setOnClickListener(this)
        orderEntryListBinding.tvAddOrderEntry.setOnClickListener(this)
        orderEntryListBinding.toolbar.imgFilter.setOnClickListener(this)
        orderEntryListBinding.llBottom.visibility = View.VISIBLE
        callOrderListApi()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            Log.e("TAG", "onHiddenChanged: "+ IS_DATA_UPDATE )
            if (AppPreference.getBooleanPreference(mActivity, IS_DATA_UPDATE)) {
                Log.e("TAG", "onHiddenChanged: API CALLED" )
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                orderEntryListBinding.toolbar.imgBack.visibility = View.VISIBLE
                orderEntryListBinding.toolbar.imgFilter.visibility = View.VISIBLE
                initView()
                callOrderListApi()
            }
        }
    }

    private fun setupOrderListAdapter() {
        leaveApplicationAdapter = OrderEntryListAdapter(orderList)
        orderEntryListBinding.rvOrderEntry.adapter = leaveApplicationAdapter
        orderEntryListBinding.rvOrderEntry.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    private fun callAccountMasterList() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val paramString = "FieldName=Order/Party"
        val jsonReq = JsonObject()
        jsonReq.addProperty("AccountMasterId", 0)
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty("ParameterString", paramString)

        val accountMasterCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getOrderAccountMasterList(jsonReq)

        accountMasterCall?.enqueue(object : Callback<List<AccountMasterList>> {
            override fun onResponse(
                call: Call<List<AccountMasterList>>,
                response: Response<List<AccountMasterList>>
            ) {
                CommonMethods.hideLoading()
                when {
                    response.code() == 200 -> {
                        response.body()?.let {
                            if (it.isNotEmpty()) {
                                accountMasterList.clear()
                                accountMasterList.add(AccountMasterList())
                                accountMasterList.addAll(it)
                                showFilterDialog()
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<AccountMasterList>>, t: Throwable) {
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


    private fun callOrderListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }

        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseListReq = JsonObject()
        expenseListReq.addProperty("OrderId", 0)
        expenseListReq.addProperty("UserId", loginData.userId)
        expenseListReq.addProperty("AccountMasterId", if(selectedPartyDealer != null) selectedPartyDealer?.accountMasterId else 0)
        expenseListReq.addProperty("FromDate", startDate)
        expenseListReq.addProperty("ToDate", endDate)

        CommonMethods.getBatteryPercentage(mActivity)
        val leaveListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getOrderList(expenseListReq)


        leaveListCall?.enqueue(object : Callback<List<OrderListResponse>> {
            override fun onResponse(
                call: Call<List<OrderListResponse>>,
                response: Response<List<OrderListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            orderEntryListBinding.rvOrderEntry.visibility = View.VISIBLE
                            orderEntryListBinding.tvNoData.visibility = View.GONE
                            orderList.clear()
                            orderList.addAll(it)
                            setupOrderListAdapter()
                        } else {
                            orderEntryListBinding.rvOrderEntry.visibility = View.GONE
                            orderEntryListBinding.tvNoData.visibility = View.VISIBLE
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

            override fun onFailure(call: Call<List<OrderListResponse>>, t: Throwable) {
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


    inner class OrderEntryListAdapter(
        orderList: List<OrderListResponse>
    ) : RecyclerView.Adapter<OrderEntryListAdapter.ViewHolder>() {
        var filteredItems: List<OrderListResponse> = orderList
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemOrderLayoutBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        fun filter(query: String) {
            filteredItems = orderList.filter { item ->
                item.userName!!.contains(query, ignoreCase = true)
            }
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return filteredItems.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val menuData = filteredItems[position]
            holder.bind(menuData)
        }

        inner class ViewHolder(private val binding: ItemOrderLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(orderData: OrderListResponse) {

                binding.tvOrderDate.text = "Order Date : " + orderData.orderDate
                binding.tvCategory.text = "Category : " + orderData.categoryName
                binding.tvParty.text = "Party : " + orderData.accountName
                binding.tvAmount.text = "Amount : " + orderData.orderAmount
                binding.tvPlace.text = "Place : " + orderData.cityName

                binding.llMain.setOnClickListener {
                    mActivity.addFragment(
                        AddOrderEntryFragment.newInstance(orderData.orderId ?: 0),
                        addToBackStack = true,
                        ignoreIfCurrent = true,
                        animationType = AnimationType.rightInLeftOut
                    )
                }

                //binding.executePendingBindings()
            }
        }
    }

    private fun setupSearchFilter() {
        orderEntryListBinding.toolbar.svView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                leaveApplicationAdapter.filter(newText.orEmpty())
                return true
            }
        })
        orderEntryListBinding.toolbar.svView.setOnSearchClickListener {
            orderEntryListBinding.toolbar.imgBack.visibility = View.GONE
            orderEntryListBinding.toolbar.imgFilter.visibility = View.GONE
        }
        orderEntryListBinding.toolbar.svView.setOnCloseListener {
            orderEntryListBinding.toolbar.imgBack.visibility = View.VISIBLE
            orderEntryListBinding.toolbar.imgFilter.visibility = View.VISIBLE
            false
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                CommonMethods.selectedPartyDealer = AccountMasterList()
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }
            R.id.tvAddOrderEntry -> {
                CommonMethods.selectedPartyDealer = AccountMasterList()
                mActivity.addFragment(
                    AddOrderEntryFragment(),
                    addToBackStack = true,
                    ignoreIfCurrent = true,
                    animationType = AnimationType.fadeInfadeOut
                )
            }

            R.id.imgFilter -> {
                if(accountMasterList.size > 0){
                    showFilterDialog()
                }else{
                    orderEntryListBinding.llFetchPartyDealer.visibility = View.VISIBLE
                    startAnimation()
                    callAccountMasterList()
                }
            }
        }
    }
    private fun startAnimation() {
        val a: Animation = AnimationUtils.loadAnimation(mActivity, R.anim.blink_animation)
        a.reset()
        orderEntryListBinding.tvFetchPartyDealer.clearAnimation()
        orderEntryListBinding.tvFetchPartyDealer.startAnimation(a)
    }

    private fun showFilterDialog() {
        orderEntryListBinding.llFetchPartyDealer.visibility = View.GONE
        CommonMethods.showFilterDialog(
            this,
            mActivity,
            startDate,
            endDate,
            selectedDateOptionPosition,
            isPartyDealerVisible = true,
            partyDealerList= accountMasterList,
            selectedPartyDealerObj = if(selectedPartyDealer == null) AccountMasterList() else selectedPartyDealer!!
        )
    }

    override fun onFilterSubmitClick(
        startDateFromListener: String,
        endDateFromListener: String,
        dateOptionFromListener: String,
        dateOptionPosition: Int,
        statusPosition: Int,
        selectedItemPosition: FilterListResponse,
        toString: FilterListResponse,
        visitType: LeaveTypeListResponse,
        partyDealer: AccountMasterList,
        visitPosition: Int
    ) {
        startDate = startDateFromListener
        endDate = endDateFromListener
        selectedDateOptionPosition = dateOptionPosition
        selectedPartyDealer = partyDealer
        Log.e("TAG", "onFilterSubmitClick: "+selectedPartyDealer?.accountMasterId )
        callOrderListApi()
    }

}