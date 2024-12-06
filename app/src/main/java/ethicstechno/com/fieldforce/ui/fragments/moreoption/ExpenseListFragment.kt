package ethicstechno.com.fieldforce.ui.fragments.moreoption

import AnimationType
import addFragment
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import ethicstechno.com.fieldforce.databinding.FragmentExpenseListBinding
import ethicstechno.com.fieldforce.databinding.ItemExpenseBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpenseListFragment : HomeBaseFragment(), View.OnClickListener, FilterDialogListener {

    lateinit var expenseBinding: FragmentExpenseListBinding
    var expenseList: ArrayList<ExpenseListResponse> = arrayListOf()
    var startDate = ""
    var endDate = ""
    private var selectedDateOptionPosition = 4 // THIS MONTH
    private var selectedStatus = 0 // THIS MONTH
    var isForApproval = false
    var expenseListForDelete: ArrayList<ExpenseListResponse> = arrayListOf()
    var expenseAdapter = ExpenseListAdapter(expenseList)


    companion object {

        fun newInstance(
            isForApproval: Boolean
        ): ExpenseListFragment {
            val args = Bundle()
            args.putBoolean(ARG_PARAM1, isForApproval)
            val fragment = ExpenseListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        expenseBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_expense_list, container, false)
        return expenseBinding.root
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


    private fun initView() {
        mActivity.bottomHide()
        startDate = CommonMethods.getStartDateOfCurrentMonth()
        endDate = CommonMethods.getCurrentDate()
        expenseBinding.toolbar.imgBack.visibility = View.VISIBLE
        expenseBinding.toolbar.tvHeader.text =
            if (isForApproval) getString(R.string.expense_approval_list) else getString(
                R.string.expense_list
            )
        expenseBinding.toolbar.imgFilter.setOnClickListener(this)
        expenseBinding.toolbar.imgBack.setOnClickListener(this)
        expenseBinding.tvAddExpense.setOnClickListener(this)
        expenseBinding.tvAccept.setOnClickListener(this)
        expenseBinding.tvReject.setOnClickListener(this)
        if (isForApproval) {
            setupSearchFilter()
            expenseBinding.toolbar.imgFilter.visibility =View.GONE
            expenseBinding.toolbar.svView.visibility = View.VISIBLE
            expenseBinding.toolbar.svView.queryHint = HtmlCompat.fromHtml(mActivity.getString(R.string.search_here), HtmlCompat.FROM_HTML_MODE_LEGACY)
            expenseBinding.tvAddExpense.visibility = View.GONE
        } else {
            expenseBinding.toolbar.imgFilter.visibility = View.VISIBLE
            expenseBinding.llBottom.visibility = View.VISIBLE
            expenseBinding.tvAddExpense.visibility = View.VISIBLE
        }
        callExpenseListApi()
    }

    private fun setupSearchFilter() {
        expenseBinding.toolbar.svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                expenseAdapter.filter(newText.orEmpty())
                return true
            }
        })
        expenseBinding.toolbar.svView.setOnSearchClickListener {
            expenseBinding.toolbar.imgBack.visibility = View.GONE
            expenseBinding.toolbar.imgFilter.visibility = View.GONE
        }
        expenseBinding.toolbar.svView.setOnCloseListener {
            expenseBinding.toolbar.imgBack.visibility = View.VISIBLE
            expenseBinding.toolbar.imgFilter.visibility = View.VISIBLE
            false
        }
    }

    private fun setupExpenseAdapter() {
        expenseAdapter = ExpenseListAdapter(expenseList)
        expenseBinding.rvExpense.adapter = expenseAdapter
        expenseBinding.rvExpense.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    private fun callExpenseListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseListReq = JsonObject()
        expenseListReq.addProperty("UserId", loginData.userId)

        val expenseListCall = if (isForApproval) {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.getExpenseApprovalList(expenseListReq)
        } else {
            expenseListReq.addProperty("StatusId", selectedStatus)
            expenseListReq.addProperty("FromDate", startDate)
            expenseListReq.addProperty("ToDate", endDate)
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.getExpenseList(expenseListReq)
        }

        expenseListCall?.enqueue(object : Callback<List<ExpenseListResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseListResponse>>,
                response: Response<List<ExpenseListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            expenseBinding.rvExpense.visibility = View.VISIBLE
                            expenseBinding.tvNoData.visibility = View.GONE
                            expenseList.clear()
                            expenseList.addAll(it)
                            setupExpenseAdapter()
                        }else{
                            expenseBinding.rvExpense.visibility = View.GONE
                            expenseBinding.tvNoData.visibility = View.VISIBLE
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

            override fun onFailure(call: Call<List<ExpenseListResponse>>, t: Throwable) {
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


    inner class ExpenseListAdapter(
        private val expenseList: List<ExpenseListResponse>
    ) : RecyclerView.Adapter<ExpenseListAdapter.ViewHolder>() {
        private val itemStates = mutableMapOf<Int, Boolean>()
        var filteredItems: List<ExpenseListResponse> = expenseList
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemExpenseBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return filteredItems.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val menuData = filteredItems[position]
            holder.bind(menuData)
        }
        fun filter(query: String) {
            filteredItems = expenseList.filter { item ->
                item.userName.contains(query, ignoreCase = true)
            }
            notifyDataSetChanged()
        }

        inner class ViewHolder(private val binding: ItemExpenseBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(expenseData: ExpenseListResponse) {

                if (isForApproval) {
                    binding.cbApprove.visibility = View.VISIBLE
                    binding.tvUserName.text = expenseData.userName

                    binding.cbApprove.isChecked =
                        itemStates[bindingAdapterPosition] ?: false // Restore the state

                    binding.cbApprove.setOnCheckedChangeListener { p0, isChecked ->
                        itemStates[bindingAdapterPosition] = isChecked // Update the state
                        expenseData.isChecked = isChecked // Update the data model
                        if (isChecked) {
                            expenseListForDelete.add(expenseData)
                        } else {
                            expenseListForDelete.remove(expenseData)
                        }
                        if (expenseListForDelete.size > 0) {
                            expenseBinding.llAcceptReject.visibility = View.VISIBLE
                            expenseBinding.llBottom.visibility = View.VISIBLE
                        } else {
                            expenseBinding.llAcceptReject.visibility = View.GONE
                            expenseBinding.llBottom.visibility = View.GONE
                        }
                    }
                    binding.cardMain.setOnClickListener {
                        mActivity.addFragment(AddExpenseFragment.newInstance(true, expenseData, true),
                            addToBackStack = true,
                            ignoreIfCurrent = true,
                            animationType = AnimationType.fadeInfadeOut
                        )
                    }
                } else {
                    binding.cbApprove.visibility = View.GONE
                    binding.cardMain.setOnClickListener {
                        mActivity.addFragment(AddExpenseFragment.newInstance(true, expenseData, false),
                            addToBackStack = true,
                            ignoreIfCurrent = true,
                            animationType = AnimationType.fadeInfadeOut
                        )
                    }
                }
                binding.tvUserName.text = expenseData.userName
                binding.tvValue1.text = " : " + expenseData.expenseDate
                binding.tvValue2.text = " : " + expenseData.cityName
                binding.tvValue3.text = " : " + expenseData.expenseTypeName
                binding.tvValue4.text = " : " + expenseData.expenseStatusName
                binding.tvValue5.text = " : " + expenseData.eligibleAmount.toString()
                binding.tvValue6.text = " : " + expenseData.expenseAmount.toString()
                //binding.executePendingBindings()
            }
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
            R.id.tvAddExpense -> {
                mActivity.addFragment(
                    AddExpenseFragment(),
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

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            if (AppPreference.getBooleanPreference(mActivity, IS_DATA_UPDATE)) {
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                expenseBinding.toolbar.imgBack.visibility = View.VISIBLE
                expenseBinding.toolbar.imgFilter.visibility = View.VISIBLE
                expenseBinding.toolbar.svView.onActionViewCollapsed()
                initView()
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
            for (i in expenseListForDelete.indices) {
                callApproveRejectExpense(
                    isLeaveApprove,
                    etRemarks.text.toString().trim(),
                    expenseListForDelete[i].expenseId,
                    expenseListForDelete[i].expenseAmount
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
    private fun callApproveRejectExpense(isExpenseApprove: Boolean, remarks: String, expenseId: Int, approvalAmount: Double) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseListReq = JsonObject()
        expenseListReq.addProperty("UserId", loginData.userId)
        expenseListReq.addProperty("ExpenseId", expenseId)
        expenseListReq.addProperty("ApprovedAmount", approvalAmount)
        expenseListReq.addProperty("Remarks", remarks)
        val leaveApprovalCall = if(isExpenseApprove){
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.expenseApprove(expenseListReq)
        }else{
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.expenseReject(expenseListReq)
        }

        leaveApprovalCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            expenseBinding.llBottom.visibility = View.GONE
                            expenseListForDelete.clear()
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.expense_approval),
                                it.returnMessage,/*if(isExpenseApprove) getString(R.string.expense_approve_msg) else getString(
                                    R.string.expense_reject_msg),*/
                                isCancelVisibility = false,
                                okListener = object : PositiveButtonListener {
                                    override fun okClickListener() {
                                        callExpenseListApi()
                                    }
                                })
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
        callExpenseListApi()
    }

}