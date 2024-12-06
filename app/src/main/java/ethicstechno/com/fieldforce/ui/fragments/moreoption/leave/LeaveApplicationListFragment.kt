package ethicstechno.com.fieldforce.ui.fragments.moreoption.leave

import AnimationType
import addFragment
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentLeaveListBinding
import ethicstechno.com.fieldforce.databinding.ItemExpenseBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveApplicationListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LeaveApplicationListFragment : HomeBaseFragment(), View.OnClickListener,
    FilterDialogListener {

    lateinit var leaveApplicationBinding: FragmentLeaveListBinding
    var leaveApplicationList: ArrayList<LeaveApplicationListResponse> = arrayListOf()
    var startDate = ""
    var endDate = ""
    private var selectedDateOptionPosition = 4 // THIS MONTH
    private var selectedStatus = 0 // THIS MONTH
    var isForApproval = false
    var leaveApplicationForDelete: ArrayList<String> = arrayListOf()
    var leaveApplicationAdapter=  LeaveListAdapter(leaveApplicationList)

    companion object {

        fun newInstance(
            isForApproval: Boolean
        ): LeaveApplicationListFragment {
            val args = Bundle()
            args.putBoolean(ARG_PARAM1, isForApproval)
            val fragment = LeaveApplicationListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        leaveApplicationBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_leave_list, container, false)
        return leaveApplicationBinding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            isForApproval = it.getBoolean(ARG_PARAM1, false)
        }
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
        leaveApplicationBinding.toolbar.imgBack.visibility = View.VISIBLE
        leaveApplicationBinding.toolbar.imgFilter.visibility = View.VISIBLE
        leaveApplicationBinding.toolbar.tvHeader.text = if (isForApproval) getString(
            R.string.leave_approval_list
        ) else getString(R.string.leave_application_list)
        leaveApplicationBinding.toolbar.imgFilter.setOnClickListener(this)
        leaveApplicationBinding.toolbar.imgBack.setOnClickListener(this)
        leaveApplicationBinding.tvAddLeave.setOnClickListener(this)
        leaveApplicationBinding.tvAccept.setOnClickListener(this)
        leaveApplicationBinding.tvReject.setOnClickListener(this)
        leaveApplicationBinding.llBottom.visibility = View.VISIBLE
        if (isForApproval) {
            setupSearchFilter()
            leaveApplicationBinding.toolbar.imgFilter.visibility = View.GONE
            leaveApplicationBinding.toolbar.svView.visibility = View.VISIBLE
            leaveApplicationBinding.toolbar.svView.queryHint = HtmlCompat.fromHtml(mActivity.getString(R.string.search_here), HtmlCompat.FROM_HTML_MODE_LEGACY)
            leaveApplicationBinding.tvAddLeave.visibility = View.GONE
        } else {
            leaveApplicationBinding.tvAddLeave.visibility = View.VISIBLE
        }
        callLeaveApplicationListApi()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {

            if (AppPreference.getBooleanPreference(mActivity, IS_DATA_UPDATE)) {
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                leaveApplicationBinding.toolbar.imgBack.visibility = View.VISIBLE
                leaveApplicationBinding.toolbar.imgFilter.visibility = View.VISIBLE
                leaveApplicationBinding.toolbar.svView.onActionViewCollapsed()
                initView()
            }
            if(leaveApplicationList.size == 0){
                leaveApplicationBinding.llAcceptReject.visibility = View.GONE
            }
        }
    }

    private fun setupLeaveApplicationAdapter() {
        leaveApplicationAdapter = LeaveListAdapter(leaveApplicationList)
        leaveApplicationBinding.rvLeave.adapter = leaveApplicationAdapter
        leaveApplicationBinding.rvLeave.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    private fun callLeaveApplicationListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseListReq = JsonObject()
        expenseListReq.addProperty("UserId", loginData.userId)


        CommonMethods.getBatteryPercentage(mActivity)
        val leaveListCall = if (isForApproval) {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.getLeaveApplicationApprovalList(expenseListReq)
        } else {
            expenseListReq.addProperty("LeaveApprovalStatus", selectedStatus)
            expenseListReq.addProperty("FromDate", startDate)
            expenseListReq.addProperty("ToDate", endDate)
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.getLeaveApplicationList(expenseListReq)
        }


        leaveListCall?.enqueue(object : Callback<List<LeaveApplicationListResponse>> {
            override fun onResponse(
                call: Call<List<LeaveApplicationListResponse>>,
                response: Response<List<LeaveApplicationListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            leaveApplicationBinding.rvLeave.visibility = View.VISIBLE
                            leaveApplicationBinding.tvNoData.visibility = View.GONE
                            leaveApplicationList.clear()
                            leaveApplicationList.addAll(it)
                            setupLeaveApplicationAdapter()
                        } else {
                            leaveApplicationBinding.rvLeave.visibility = View.GONE
                            leaveApplicationBinding.tvNoData.visibility = View.VISIBLE
                        }
                    }
                }else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null
                    )
                }

            }

            override fun onFailure(call: Call<List<LeaveApplicationListResponse>>, t: Throwable) {
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


    inner class LeaveListAdapter(
        leaveListing: List<LeaveApplicationListResponse>
    ) : RecyclerView.Adapter<LeaveListAdapter.ViewHolder>() {
        private val itemStates = mutableMapOf<Int, Boolean>()
        var filteredItems: List<LeaveApplicationListResponse> = leaveListing
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemExpenseBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        fun filter(query: String) {
            filteredItems = leaveApplicationList.filter { item ->
                item.userName.contains(query, ignoreCase = true)
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

        inner class ViewHolder(private val binding: ItemExpenseBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(leaveData: LeaveApplicationListResponse) {
                if (isForApproval) {
                    binding.cbApprove.visibility = View.VISIBLE
                    binding.llTop.visibility = View.VISIBLE
                    binding.tvUserName.text = leaveData.userName

                    binding.cbApprove.isChecked =
                        itemStates[bindingAdapterPosition] ?: false // Restore the state

                    binding.cbApprove.setOnCheckedChangeListener { p0, isChecked ->
                        itemStates[bindingAdapterPosition] = isChecked // Update the state
                        leaveData.isChecked = isChecked // Update the data model
                        if (isChecked) {
                            leaveApplicationForDelete.add(leaveData.leaveApplicationId.toString())
                        } else {
                            leaveApplicationForDelete.remove(leaveData.leaveApplicationId.toString())
                        }
                        if (leaveApplicationForDelete.size > 0) {
                            leaveApplicationBinding.llAcceptReject.visibility = View.VISIBLE
                            leaveApplicationBinding.llBottom.visibility = View.VISIBLE
                        } else {
                            leaveApplicationBinding.llAcceptReject.visibility = View.GONE
                            leaveApplicationBinding.llBottom.visibility = View.GONE
                        }
                    }
                } else {
                    binding.cbApprove.visibility = View.GONE
                    binding.llTop.visibility = View.GONE
                }
                binding.tvLabel1.text = getString(R.string.date)
                binding.tvValue1.text = " : " + leaveData.leaveApplicationDate

                binding.tvLabel2.text = getString(R.string.type)
                binding.tvValue2.text = " : " + leaveData.leaveCategoryName

                binding.tvLabel3.text = getString(R.string.from)
                binding.tvValue3.text = " : " + leaveData.leaveFromDate

                binding.tvLabel4.text = getString(R.string.to_date)
                binding.tvValue4.text = " : " + leaveData.leaveToDate

                binding.tvLabel5.text = getString(R.string.days)
                binding.tvValue5.text = " : " + leaveData.totalLeaveDays

                binding.tvLabel6.text = getString(R.string.status)
                binding.tvValue6.text = " : " + leaveData.leaveApprovalStatusName

                binding.llMain.setOnClickListener {
                    if (isForApproval) {
                        mActivity.addFragment(
                            AddLeaveApplicationFragment.newInstance(
                                true,
                                leaveData,
                                true
                            ), true, true, AnimationType.fadeInfadeOut
                        )
                    } else {
                        mActivity.addFragment(
                            AddLeaveApplicationFragment.newInstance(
                                true,
                                leaveData,
                                false
                            ), true, true, AnimationType.fadeInfadeOut
                        )
                    }
                }

                //binding.executePendingBindings()
            }
        }
    }

    private fun setupSearchFilter() {
        leaveApplicationBinding.toolbar.svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                leaveApplicationAdapter.filter(newText.orEmpty())
                return true
            }
        })
        leaveApplicationBinding.toolbar.svView.setOnSearchClickListener {
            leaveApplicationBinding.toolbar.imgBack.visibility = View.GONE
            leaveApplicationBinding.toolbar.imgFilter.visibility = View.GONE
        }
        leaveApplicationBinding.toolbar.svView.setOnCloseListener {
            leaveApplicationBinding.toolbar.imgBack.visibility = View.VISIBLE
            leaveApplicationBinding.toolbar.imgFilter.visibility = View.VISIBLE
            false
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack ->
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            R.id.tvAddLeave -> {
                mActivity.addFragment(
                    AddLeaveApplicationFragment(),
                    addToBackStack = true,
                    ignoreIfCurrent = true,
                    animationType = AnimationType.fadeInfadeOut
                )
            }
            R.id.imgFilter -> {
                CommonMethods.showFilterDialog(
                    this,
                    mActivity,
                    startDate,
                    endDate,
                    selectedDateOptionPosition,
                    true,
                    selectedStatusPosition = selectedStatus
                )
            }
            R.id.tvAccept -> {
                showRemarksDialog(true)
            }
            R.id.tvReject -> {
                showRemarksDialog(false)
            }
        }
    }


    private fun showRemarksDialog(isLeaveApprove: Boolean) {
        val companyDialog = Dialog(mActivity)
        companyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        companyDialog.setCancelable(true)
        companyDialog.setContentView(R.layout.company_selection_dialog)

        val flCompany: FrameLayout = companyDialog.findViewById(R.id.flCompany)
        val tvLabel: TextView = companyDialog.findViewById(R.id.tvLabel)
        val etRemarks: EditText = companyDialog.findViewById(R.id.etRemarks)
        val btnSubmit: TextView = companyDialog.findViewById(R.id.btnSubmit)
        flCompany.visibility = View.GONE
        etRemarks.visibility = View.VISIBLE
        tvLabel.text = getString(R.string.remark)


        btnSubmit.setOnClickListener {
            if (etRemarks.text.toString().trim().isEmpty()) {
                showToastMessage(mActivity, getString(R.string.enter_remark))
                return@setOnClickListener
            }
            companyDialog.dismiss()
            for (i in leaveApplicationForDelete.indices) {
                callApproveRejectLeaveApi(
                    isLeaveApprove,
                    etRemarks.text.toString().trim(),
                    leaveApplicationForDelete[i]
                )
            }
        }

        val window = companyDialog.window
        window!!.setLayout(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.WRAP_CONTENT
        )
        companyDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        companyDialog.show()
    }


    private fun callApproveRejectLeaveApi(
        isLeaveApprove: Boolean,
        remarks: String,
        leaveApplicationId: String
    ) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseListReq = JsonObject()
        expenseListReq.addProperty("UserId", loginData.userId)
        expenseListReq.addProperty("LeaveApplicationId", leaveApplicationId)
        expenseListReq.addProperty(
            "LeaveApprovalStatus",
            if (isLeaveApprove) APPROVE_STATUS else REJECT_STATUS
        )
        expenseListReq.addProperty("LeaveApprovalRemarks", remarks)


        CommonMethods.getBatteryPercentage(mActivity)
        val leaveApprovalCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.leaveApplicationApproval(expenseListReq)

        leaveApprovalCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            leaveApplicationForDelete.clear()
                            leaveApplicationBinding.llBottom.visibility = View.GONE
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.leave_approval),
                                it.returnMessage/*if (isLeaveApprove) getString(R.string.leave_approve_msg) else getString(
                                    R.string.leave_reject_msg
                                )*/,
                                isCancelVisibility = false,
                                okListener = object : PositiveButtonListener {
                                    override fun okClickListener() {
                                        callLeaveApplicationListApi()
                                    }
                                })
                        }
                    }
                }else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null
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
                        null
                    )
                }
            }
        })

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
        selectedStatus = statusPosition
        Log.e("TAG", "onFilterSubmitClick: "+statusPosition )
        callLeaveApplicationListApi()
    }

}