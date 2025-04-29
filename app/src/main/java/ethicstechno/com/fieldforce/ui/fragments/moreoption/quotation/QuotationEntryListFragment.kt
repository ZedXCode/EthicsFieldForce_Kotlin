package ethicstechno.com.fieldforce.ui.fragments.moreoption.quotation

import AnimationType
import addFragment
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentOrderEntryListBinding
import ethicstechno.com.fieldforce.databinding.ItemOrderLayoutBinding
import ethicstechno.com.fieldforce.databinding.ItemUserBinding
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.ApproveRejectResponse
import ethicstechno.com.fieldforce.models.ReportResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.quotation.QuotationListResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.BranchMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CompanyMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.DivisionMasterResponse
import ethicstechno.com.fieldforce.ui.adapter.LeaveTypeAdapter
import ethicstechno.com.fieldforce.ui.adapter.spinneradapter.DateOptionAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CUSTOM_RANGE
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DOCUMENT_NAME_QUOTATION
import ethicstechno.com.fieldforce.utils.FORM_ID_QUOTATION_ENTRY
import ethicstechno.com.fieldforce.utils.FORM_ID_QUOTATION_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FOR_BRANCH
import ethicstechno.com.fieldforce.utils.FOR_COMPANY
import ethicstechno.com.fieldforce.utils.FOR_DIVISION
import ethicstechno.com.fieldforce.utils.ID_ZERO
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.LAST_30_DAYS
import ethicstechno.com.fieldforce.utils.LAST_7_DAYS
import ethicstechno.com.fieldforce.utils.QUOTATION_PRINT
import ethicstechno.com.fieldforce.utils.REPORT_M
import ethicstechno.com.fieldforce.utils.THIS_MONTH
import ethicstechno.com.fieldforce.utils.TODAY
import ethicstechno.com.fieldforce.utils.YESTERDAY
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class QuotationEntryListFragment : HomeBaseFragment(), View.OnClickListener,
    UserSearchDialogUtil.CompanyDialogDetect, UserSearchDialogUtil.DivisionDialogDetect,
    UserSearchDialogUtil.BranchDialogDetect, LeaveTypeAdapter.TypeSelect
/*FilterDialogListener*/ {

    lateinit var orderEntryListBinding: FragmentOrderEntryListBinding
    var orderList: ArrayList<QuotationListResponse> = arrayListOf()
    var startDate = ""
    var endDate = ""
    private var selectedDateOptionPosition = 4 // THIS MONTH
    private var selectedPartyDealer: AccountMasterList? = null
    var leaveApplicationAdapter = OrderEntryListAdapter(orderList)
    private var accountMasterList: ArrayList<AccountMasterList> = arrayListOf()
    val companyMasterList: ArrayList<CompanyMasterResponse> = arrayListOf()
    val branchMasterList: ArrayList<BranchMasterResponse> = arrayListOf()
    val divisionMasterList: ArrayList<DivisionMasterResponse> = arrayListOf()
    val categoryList: ArrayList<CategoryMasterResponse> = arrayListOf()
    var selectedCompany: CompanyMasterResponse? = null
    var selectedBranch: BranchMasterResponse? = null
    var selectedDivision: DivisionMasterResponse? = null
    private var selectedCategory: CategoryMasterResponse? = null
    private lateinit var llSelectCompany: LinearLayout
    private lateinit var llSelectBranch: LinearLayout
    private lateinit var llSelectDivision: LinearLayout
    private lateinit var tvSelectCompany: TextView
    private lateinit var tvSelectBranch: TextView
    private lateinit var tvSelectDivision: TextView
    private lateinit var tvSelectPartyDealer: TextView
    lateinit var spCategory: Spinner
    private lateinit var flPartyDealer: FrameLayout
    private lateinit var partyDealerDialog: AlertDialog
    private lateinit var partyDealerAdapter: PartyDealerListAdapter
    private lateinit var paginationLoader: ProgressBar
    private lateinit var tvSearchGO: TextView
    private lateinit var imgSearchClose: ImageView
    private lateinit var edtSearchPartyDealer: EditText
    private var partyDealerPageNo = 1
    private var isScrolling = false
    private var isLastPage = false
    private var layoutManager: LinearLayoutManager? = null
    private var selectedPartyDealerId: Int = 0
    lateinit var rvItems: RecyclerView
    lateinit var tvPartyNotFound: TextView

    var statusColor = ""

    var isForApproval = false
    var orderForDelete: ArrayList<String> = arrayListOf()
    companion object {

        fun newInstance(
            //isFromInquiry: Boolean,
            isForApproval: Boolean
        ): QuotationEntryListFragment {
            val args = Bundle()
            args.putBoolean(ARG_PARAM1, isForApproval)
            val fragment = QuotationEntryListFragment()
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
        arguments?.let {
            isForApproval = it.getBoolean(ARG_PARAM1, false)
        }
        return orderEntryListBinding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun companySelect(dropDownData: CompanyMasterResponse) {
        selectedCompany = dropDownData
        tvSelectCompany.text = selectedCompany?.companyName ?: ""
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
        tvSelectBranch.text = selectedBranch?.branchName ?: ""
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
        tvSelectDivision.text = selectedDivision?.divisionName ?: ""
        resetSelection(
            resetBranch = false,
            resetDivision = false,
            resetCategory = true
        )
        if ((selectedDivision?.divisionMasterId ?: 0) > 0) {
            callCategoryListApi()
        }
    }

    override fun onTypeSelect(typeData: CategoryMasterResponse) {
        selectedCategory = typeData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
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
        orderEntryListBinding.toolbar.imgBack.visibility = View.VISIBLE
        orderEntryListBinding.toolbar.imgFilter.visibility = View.VISIBLE
        orderEntryListBinding.toolbar.imgFilter.visibility = View.VISIBLE

        //orderEntryListBinding.toolbar.tvHeader.text = getString(R.string.order_entry_list)
        orderEntryListBinding.toolbar.tvHeader.text = if (isForApproval) getString(
            R.string.quotation_approval_list
        ) else getString(R.string.quotation_entry_list)

        orderEntryListBinding.toolbar.imgFilter.visibility = View.VISIBLE
        orderEntryListBinding.toolbar.imgFilter.setOnClickListener(this)
        orderEntryListBinding.toolbar.imgBack.setOnClickListener(this)
        //orderEntryListBinding.tvAddOrderEntry.text = getString(R.string.add_quotation_entry)
        orderEntryListBinding.tvAddOrderEntry.setOnClickListener(this)
        //orderEntryListBinding.llBottom.visibility = View.VISIBLE
        selectedCompany = CompanyMasterResponse(companyMasterId = 0)
        selectedBranch = BranchMasterResponse(branchMasterId = 0)
        selectedDivision = DivisionMasterResponse(divisionMasterId = 0)
        selectedCategory = CategoryMasterResponse(categoryMasterId = 0)

        orderEntryListBinding.tvAccept.setOnClickListener(this)
        orderEntryListBinding.tvReject.setOnClickListener(this)
        orderEntryListBinding.llBottom.visibility = View.VISIBLE

        if (isForApproval) {
            setupSearchFilter()
            orderEntryListBinding.toolbar.svView.visibility = View.VISIBLE
            orderEntryListBinding.toolbar.svView.queryHint = HtmlCompat.fromHtml(mActivity.getString(R.string.search_here), HtmlCompat.FROM_HTML_MODE_LEGACY)
            orderEntryListBinding.tvAddOrderEntry.visibility = View.GONE
        } else {
            orderEntryListBinding.tvAddOrderEntry.visibility = View.VISIBLE
        }

        callOrderListApi()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            Log.e("TAG", "onHiddenChanged: " + IS_DATA_UPDATE)
            if (AppPreference.getBooleanPreference(mActivity, IS_DATA_UPDATE)) {
                Log.e("TAG", "onHiddenChanged: API CALLED")
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                orderEntryListBinding.toolbar.imgBack.visibility = View.VISIBLE
                orderEntryListBinding.toolbar.imgFilter.visibility = View.VISIBLE
                initView()
            }
        }
    }

    private fun setupOrderListAdapter() {
        leaveApplicationAdapter = OrderEntryListAdapter(orderList)
        orderEntryListBinding.rvOrderEntry.adapter = leaveApplicationAdapter
        orderEntryListBinding.rvOrderEntry.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }


    private fun callOrderListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }

        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val orderEntryListReq = JsonObject()

        val isApprovalFlag = if (isForApproval) 1 else 0

        orderEntryListReq.apply {
            addProperty("QuotationId", 0)
            addProperty("UserId", loginData.userId)
            addProperty("AccountMasterId", selectedPartyDealerId)
            addProperty("FromDate", startDate)
            addProperty("ToDate", endDate)
            addProperty("isFromApproval", isApprovalFlag)
            addProperty("ParameterString", "CompanyMasterId=${selectedCompany?.companyMasterId} and " +
                    "BranchMasterId=${selectedBranch?.branchMasterId} and " +
                    "DivisionMasterid=${selectedDivision?.divisionMasterId} and " +
                    "CategoryMasterId=${selectedCategory?.categoryMasterId} and $FORM_ID_QUOTATION_ENTRY")
        }

        val quotationEntryListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getQuotationList(orderEntryListReq)


        quotationEntryListCall?.enqueue(object : Callback<List<QuotationListResponse>> {
            override fun onResponse(
                call: Call<List<QuotationListResponse>>,
                response: Response<List<QuotationListResponse>>
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

            override fun onFailure(call: Call<List<QuotationListResponse>>, t: Throwable) {
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
        orderList: List<QuotationListResponse>
    ) : RecyclerView.Adapter<OrderEntryListAdapter.ViewHolder>() {
        var filteredItems: List<QuotationListResponse> = orderList
        private val itemStates = mutableMapOf<Int, Boolean>()
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

            fun bind(orderData: QuotationListResponse) {
                if (isForApproval) {
                    binding.cbApprove.visibility = View.VISIBLE
                    binding.llTop.visibility = View.VISIBLE

                    binding.tvUserName.visibility = View.VISIBLE
                    binding.tvUserName.text = orderData.userName

                    binding.cbApprove.isChecked =
                        itemStates[bindingAdapterPosition] ?: false // Restore the state

                    binding.cbApprove.setOnCheckedChangeListener { p0, isChecked ->
                        itemStates[bindingAdapterPosition] = isChecked // Update the state
                        orderData.isChecked = isChecked // Update the data model
                        if (isChecked) {
                            orderForDelete.add(orderData.quotationId.toString())
                            //orderForDelete.add(orderData.toString())
                        } else {
                            orderForDelete.remove(orderData.quotationId.toString())
                            //orderForDelete.remove(orderData.toString())
                        }
                        if (orderForDelete.size > 0) {
                            orderEntryListBinding.llAcceptReject.visibility = View.VISIBLE
                            orderEntryListBinding.llBottom.visibility = View.VISIBLE
                        } else {
                            orderEntryListBinding.llAcceptReject.visibility = View.GONE
                            orderEntryListBinding.llBottom.visibility = View.GONE
                        }
                    }
                } else {
                    binding.cbApprove.visibility = View.GONE
                    binding.llTop.visibility = View.GONE
                }

                binding.tvDateTitle.text = "Date"
                binding.tvDateName.text = orderData.quotationDate

                binding.tvOrderNoTitle.text = "Quotation No"
                binding.tvOrderNoTitleName.text = orderData.categoryName+"/"+ orderData.documentNo

                binding.tvPartyNameTitle.text = "Party"
                binding.tvPartyName.text = orderData.accountName

                binding.tvQuantityTitle.text = "Quantity"
                binding.tvQuantityName.text = orderData.totalQuantity.toString()

                binding.tvItemcountTitle.text = "Item count"
                binding.tvItemcountName.text = orderData.productCount.toString()

                binding.tvBranchTitle.text = "Branch"
                binding.tvBranchName.text =  orderData.branchName

                binding.tvAmountTitle.text = "Amount"
                binding.tvAmountName.text =  orderData.quotationAmount.toString()

                binding.tvStatus.text = orderData.quotationStatusName

//
//                binding.tvLabel3.text = "Date"
//                binding.tvValue3.text = orderData.quotationDate
//
//                binding.tvLabel1.text = "Order No"
//                binding.tvValue1.text = orderData.categoryName+"/"+ orderData.documentNo
//
//
//                binding.tvLabel5.text = "Party"
//                binding.tvValue5.text = orderData.accountName
//
//                //binding.tvAmount.text = "Amount : " + orderData.orderAmount
//
//                binding.tvLabel4.text = "Amount"
//                binding.tvValue4.text = orderData.quotationAmount.toString()
//
//                binding.tvLabel2.text = "Place"
//                binding.tvValue2.text = orderData.cityName
//
//                binding.tvLabel6.text = "Branch"
//                binding.tvValue6.text = orderData.branchName
//
//                binding.tvStatus.text = orderData.quotationStatusName

                statusColor = orderData.statusColor.toString()
                if (statusColor.isEmpty()) {
                    binding.tvStatus.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#FFC107"))
                }else{
                    binding.tvStatus.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor(statusColor))
                }

                if (AppPreference.getBooleanPreference(mActivity, QUOTATION_PRINT)){
                    binding.ivShare.visibility = View.VISIBLE
                }else{
                    binding.ivShare.visibility = View.GONE
                }

                binding.ivShare.setOnClickListener{
                    callGetReport(orderData.quotationId)
                }

                binding.llMain.setOnClickListener {
                    mActivity.addFragment(
                        AddQuotationEntryFragment.newInstance(orderData.quotationId ?: 0, orderData.allowEdit, orderData.allowDelete, isForApproval),
                        addToBackStack = true,
                        ignoreIfCurrent = true,
                        animationType = AnimationType.rightInLeftOut
                    )
                }

                //binding.executePendingBindings()
            }
        }
    }

    private fun callGetReport(quotationId: Int?) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val dashboardListReq = JsonObject()
        dashboardListReq.addProperty("reportSetupId", 0)
        dashboardListReq.addProperty("reportName", "")
        dashboardListReq.addProperty("UserId", loginData.userId)
        dashboardListReq.addProperty("reportType", REPORT_M)// r
        dashboardListReq.addProperty("formId", FORM_ID_QUOTATION_ENTRY_NUMBER)
        dashboardListReq.addProperty("fromDate", "")
        dashboardListReq.addProperty("toDate", "")
        dashboardListReq.addProperty("reportGroupBy", "")
        dashboardListReq.addProperty("parameterString", "")
        dashboardListReq.addProperty("filter", "")
        dashboardListReq.addProperty("documentId", quotationId)

        val dashboardListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getReport(dashboardListReq)

        dashboardListCall?.enqueue(object : Callback<ReportResponse> {
            override fun onResponse(
                call: Call<ReportResponse>,
                response: Response<ReportResponse>
            ) {
                CommonMethods.hideLoading()
                if (response.isSuccessful) {
                    response.body()?.let { reportResponse ->
                        //val fileName = reportResponse.fileName
                        val fileName = appDatabase.appDao()
                            .getAppRegistration().apiHostingServer +reportResponse.fileName
                        Log.d("FileName", fileName)

                        //openUrlInChrome(fileName)
                        CommonMethods.showToastMessage(mActivity, "Downloading Report...")
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fileName))
                        mActivity.startActivity(browserIntent)
                        //downloadAndOpenPDF(mActivity, fileName)
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

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
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
                    AddQuotationEntryFragment(),
                    addToBackStack = true,
                    ignoreIfCurrent = true,
                    animationType = AnimationType.fadeInfadeOut
                )
            }

            R.id.imgFilter -> {
                showFilterDialog()
                /* else {
                    orderEntryListBinding.llFetchPartyDealer.visibility = View.VISIBLE
                    startAnimation()
                    callAccountMasterList()
                }*/
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
            for (i in orderForDelete.indices) {
                callApproveRejectOrderApi(
                    isLeaveApprove,
                    etRemarks.text.toString().trim(),
                    orderForDelete[i]
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

    private fun callApproveRejectOrderApi(
        isLeaveApprove: Boolean,
        remarks: String,
        orderId: String
    ) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseListReq = JsonObject()
        expenseListReq.addProperty("loginUserId", loginData.userId)

        val objDetailsArray = JsonArray()
       // for (i in orderDetailsList) {
            val objDetails = JsonObject()
            objDetails.addProperty("documentId", orderId)
            objDetails.addProperty("remarks", remarks)
            objDetails.addProperty("isApprove",isLeaveApprove)
            objDetails.addProperty("documentName",DOCUMENT_NAME_QUOTATION)
            objDetailsArray.add(objDetails)
       // }
        expenseListReq.add("authorizeApprove",objDetailsArray)

        CommonMethods.getBatteryPercentage(mActivity)
        val leaveApprovalCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getApprovaReject(expenseListReq)

        leaveApprovalCall?.enqueue(object : Callback<ApproveRejectResponse> {
            override fun onResponse(
                call: Call<ApproveRejectResponse>,
                response: Response<ApproveRejectResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            orderForDelete.clear()
                            orderEntryListBinding.llBottom.visibility = View.GONE
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.order_approval),
                                it.returnMessage/*if (isLeaveApprove) getString(R.string.leave_approve_msg) else getString(
                                    R.string.leave_reject_msg
                                )*/,
                                isCancelVisibility = false,
                                okListener = object : PositiveButtonListener {
                                    override fun okClickListener() {
                                        callOrderListApi()
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

            override fun onFailure(call: Call<ApproveRejectResponse>, t: Throwable) {
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

    private fun showFilterDialog() {

        val filterDialog = Dialog(mActivity)

        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        filterDialog.setCancelable(true)
        filterDialog.setContentView(R.layout.filter_order_entry_list)

        val spDateOption: Spinner = filterDialog.findViewById(R.id.spDateOption)
        val tvStartDate: TextView = filterDialog.findViewById(R.id.tvStartDate)
        val tvEndDate: TextView = filterDialog.findViewById(R.id.tvEndDate)
        tvSelectCompany = filterDialog.findViewById(R.id.tvSelectCompany)
        tvSelectBranch = filterDialog.findViewById(R.id.tvSelectBranch)
        tvSelectDivision = filterDialog.findViewById(R.id.tvSelectDivision)
        tvSelectPartyDealer = filterDialog.findViewById(R.id.tvPartyDealer)
        llSelectCompany = filterDialog.findViewById(R.id.llSelectCompany)
        llSelectBranch = filterDialog.findViewById(R.id.llSelectBranch)
        llSelectDivision = filterDialog.findViewById(R.id.llSelectDivision)
        spCategory = filterDialog.findViewById(R.id.spCategory)
        flPartyDealer = filterDialog.findViewById(R.id.flPartyDealer)
        spCategory = filterDialog.findViewById(R.id.spCategory)
        val btnSubmit = filterDialog.findViewById<TextView>(R.id.btnSubmit)

        callCompanyListApi()

        tvStartDate.setOnClickListener {
            if (spDateOption.selectedItem == CUSTOM_RANGE) {
                CommonMethods.openStartDatePickerDialog(true, mActivity, tvStartDate, tvEndDate)
            }
        }
        tvEndDate.setOnClickListener {
            if (spDateOption.selectedItem == CUSTOM_RANGE) {
                CommonMethods.openStartDatePickerDialog(false, mActivity, tvStartDate, tvEndDate)
            }
        }

        flPartyDealer.setOnClickListener {
            /*if (selectedCategory == null || selectedCategory?.categoryMasterId!! <= 0) {
                showToastMessage(mActivity, "Please select order category")
                return@setOnClickListener
            }*/
            showPartyDealerListDialog()
        }

        val adapter = DateOptionAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            CommonMethods.dateTypeList,
        )
        spDateOption.adapter = adapter
        spDateOption.setSelection(selectedDateOptionPosition)

        llSelectCompany.setOnClickListener {
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
                showToastMessage(
                    mActivity,
                    getString(R.string.company_list_not_found)
                )
            }
        }
        llSelectBranch.setOnClickListener {
            callBranchListApi(true)
            /*if (selectedCompany == null || selectedCompany?.companyMasterId == 0) {
                CommonMethods.showToastMessage(
                    mActivity,
                    mActivity.getString(R.string.please_select_company)
                )
                return@setOnClickListener;
            }*/
            /*if (branchMasterList.size > 0) {
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
            }*/
        }
        llSelectDivision.setOnClickListener {
            callDivisionListApi(true)
            /*if (selectedBranch == null || selectedBranch?.branchMasterId == 0) {
                CommonMethods.showToastMessage(
                    mActivity,
                    mActivity.getString(R.string.please_select_branch)
                )
                return@setOnClickListener;
            }*/
            /*if (divisionMasterList.size > 0) {
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
                    getString(R.string.division_list_not_found)
                )
            }*/
        }

        btnSubmit.setOnClickListener{
            startDate = tvStartDate.text.toString()
            endDate = tvEndDate.text.toString()
            selectedDateOptionPosition = spDateOption.selectedItemPosition
            callOrderListApi()
            filterDialog.dismiss()
        }

        spDateOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (spDateOption.selectedItem) {
                    TODAY -> {
                        CommonMethods.setStartEndDate(
                            CommonMethods.getCurrentDate(),
                            CommonMethods.getCurrentDate(),
                            tvStartDate, tvEndDate
                        )
                    }

                    YESTERDAY -> {
                        CommonMethods.setStartEndDate(
                            CommonMethods.getYesterdayDate(),
                            CommonMethods.getYesterdayDate(),
                            tvStartDate, tvEndDate
                        )
                    }

                    LAST_7_DAYS -> {
                        CommonMethods.setStartEndDate(
                            CommonMethods.getStartDateForLast7Days(),
                            CommonMethods.getCurrentDate(),
                            tvStartDate, tvEndDate
                        )
                    }

                    LAST_30_DAYS -> {
                        CommonMethods.setStartEndDate(
                            CommonMethods.getStartDateForLast30Days(),
                            CommonMethods.getCurrentDate(),
                            tvStartDate, tvEndDate
                        )
                    }

                    THIS_MONTH -> {
                        CommonMethods.setStartEndDate(
                            CommonMethods.getStartDateOfCurrentMonth(),
                            CommonMethods.getCurrentDate(),
                            tvStartDate, tvEndDate
                        )
                    }

                    CUSTOM_RANGE -> {
                    }

                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }

        val window = filterDialog.window
        window!!.setLayout(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.WRAP_CONTENT
        )
        filterDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        filterDialog.show()
    }

    private fun callCompanyListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("companyMasterId", ID_ZERO)
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty("ParameterString", FORM_ID_QUOTATION_ENTRY)

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
                                selectedCompany = CompanyMasterResponse(
                                    companyMasterId = companyMasterList[1].companyMasterId,
                                    companyName = companyMasterList[1].companyName
                                )
                                tvSelectCompany.text = selectedCompany?.companyName ?: ""
                            }
                            callBranchListApi()
                        }
                        callCategoryListApi()//for not independent
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

    private fun callBranchListApi(isFromOnClick: Boolean = false) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("BranchMasterId", ID_ZERO)
        objReq.addProperty("UserId", loginData.userId)
        objReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and $FORM_ID_QUOTATION_ENTRY"
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
                            if (it.size == 1) {
                                selectedBranch = BranchMasterResponse(
                                    branchMasterId = branchMasterList[1].branchMasterId,
                                    branchName = branchMasterList[1].branchName
                                )
                                tvSelectBranch.text = selectedBranch?.branchName ?: ""
                                callDivisionListApi()
                            }
                            if(isFromOnClick){
                                val userDialog = UserSearchDialogUtil(
                                    mActivity,
                                    type = FOR_BRANCH,
                                    branchList = branchMasterList,
                                    branchInterfaceDetect = this@QuotationEntryListFragment as UserSearchDialogUtil.BranchDialogDetect,
                                    userDialogInterfaceDetect = null
                                )
                                userDialog.showUserSearchDialog()
                            }
                        } else {
                            showToastMessage(
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

    private fun callDivisionListApi(isFromOnClick: Boolean = false) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("divisionMasterId", ID_ZERO)
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and $FORM_ID_QUOTATION_ENTRY"
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
                            if (it.size == 1) {
                                selectedDivision = DivisionMasterResponse(
                                    divisionMasterId = divisionMasterList[1].divisionMasterId,
                                    divisionName = divisionMasterList[1].divisionName
                                )
                                tvSelectDivision.text = selectedDivision?.divisionName ?: ""
                                callCategoryListApi()
                            }
                            if(isFromOnClick){
                                val userDialog = UserSearchDialogUtil(
                                    mActivity,
                                    type = FOR_DIVISION,
                                    divisionList = divisionMasterList,
                                    divisionInterfaceDetect = this@QuotationEntryListFragment as UserSearchDialogUtil.DivisionDialogDetect,
                                    userDialogInterfaceDetect = null
                                )
                                userDialog.showUserSearchDialog()
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

    private fun callCategoryListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        /*if (selectedDivision == null || selectedDivision?.divisionMasterId == 0) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.please_select_branch)
            )
            return;
        }*/
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val jsonReq = JsonObject()
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and DivisionMasterid=${selectedDivision?.divisionMasterId} and $FORM_ID_QUOTATION_ENTRY"
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

    private fun setupCategorySpinner() {
        val adapter = LeaveTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            categoryList,
            this,
        )
        spCategory.adapter = adapter
        if (categoryList.size == 2) {
            selectedCategory = CategoryMasterResponse(
                categoryMasterId = categoryList[1].categoryMasterId,
                categoryName = categoryList[1].categoryName
            )
            spCategory.setSelection(1)
            categoryList[1]
        } else {
            spCategory.setSelection(0)
            categoryList[0]
        }
    }

    private fun callAccountMasterList() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        paginationLoader.visibility = View.VISIBLE

        val appRegistrationData = appDao.getAppRegistration()

        val fieldName = "FieldName=Order/Party"
        val parameterString =
            "CompanyMasterId=${selectedCompany?.companyMasterId ?: 0} and BranchMasterId=${selectedBranch?.branchMasterId ?: 0} and DivisionMasterid=${selectedDivision?.divisionMasterId ?: 0} and" +
                    " CategoryMasterId=${selectedCategory?.categoryMasterId ?: 0} and $FORM_ID_QUOTATION_ENTRY and $fieldName and AccountName like '${edtSearchPartyDealer.text.toString()}%'"

        val jsonReq = JsonObject()
        jsonReq.addProperty("AccountMasterId", 0)
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty("ParameterString", parameterString)
        jsonReq.addProperty("pageNo", partyDealerPageNo)

        val accountMasterCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getPartyDealerSearchList(jsonReq)

        accountMasterCall?.enqueue(object : Callback<List<AccountMasterList>> {
            override fun onResponse(
                call: Call<List<AccountMasterList>>,
                response: Response<List<AccountMasterList>>
            ) {
                paginationLoader.visibility = View.GONE
                when {
                    response.code() == 200 -> {
                        response.body()?.let {
                            handleApiResponse(response)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<AccountMasterList>>, t: Throwable) {
                paginationLoader.visibility = View.GONE
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

    private fun handleApiResponse(
        response: Response<List<AccountMasterList>>
    ) {
        paginationLoader.visibility = View.GONE

        if (response.isSuccessful) {
            response.body()?.let { data ->
                if (data.isNotEmpty()) {
                    rvItems.visibility = View.VISIBLE
                    tvPartyNotFound.visibility = View.GONE
                    if (partyDealerPageNo == 1) {
                        accountMasterList.clear()
                        isLastPage = false
                    }
                    accountMasterList.addAll(data)
                    partyDealerAdapter.notifyDataSetChanged()
                    isScrolling = false
                } else {
                    isLastPage = true
                    rvItems.visibility = View.GONE
                    tvPartyNotFound.visibility = View.VISIBLE
                    accountMasterList.clear()
                }
            }
        }
    }


    private fun showPartyDealerListDialog() {
        try {
            val builder = AlertDialog.Builder(mActivity, R.style.MyAlertDialogStyle)
            partyDealerDialog = builder.create()
            partyDealerDialog.setCancelable(false)
            partyDealerDialog.setCanceledOnTouchOutside(false)
            partyDealerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            partyDealerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val inflater =
                mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.dialog_searchable_listing, null)
            rvItems = layout.findViewById<RecyclerView>(R.id.rvItems)
            tvPartyNotFound = layout.findViewById<TextView>(R.id.tvNoDataFound)
            val imgClose = layout.findViewById<ImageView>(R.id.imgClose)
            edtSearchPartyDealer = layout.findViewById<EditText>(R.id.edtSearch)
            val tvTitle = layout.findViewById<TextView>(R.id.tvTitle)
            paginationLoader = layout.findViewById(R.id.loader)
            tvSearchGO = layout.findViewById(R.id.tvSearchGO)
            imgSearchClose = layout.findViewById(R.id.imgCloseSearch)
            imgSearchClose.visibility = View.VISIBLE

            tvTitle.text = "Party/Dealer List"

            setupPaginationRecyclerView(rvItems)

            if (tvSelectPartyDealer.text.toString().trim().isNotEmpty()) {
                edtSearchPartyDealer.setText(tvSelectPartyDealer.text.toString().trim())
                tvSearchGO.visibility = View.GONE
                partyDealerPageNo = 1
            }
            callAccountMasterList()
            tvSearchGO.setOnClickListener {
                partyDealerPageNo = 1
                callAccountMasterList()
                tvSearchGO.visibility = View.GONE
            }

            imgClose.setOnClickListener { partyDealerDialog.dismiss() }
            imgSearchClose.setOnClickListener{
                edtSearchPartyDealer.setText("")
                partyDealerPageNo = 1
                callAccountMasterList()
            }

            edtSearchPartyDealer.addTextChangedListener(object : TextWatcher {
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
                        tvSearchGO.visibility = View.VISIBLE
                    } else {
                        tvSearchGO.visibility = View.GONE
                    }

                    // Filter the list based on the dialogType and searchText
                    /*val filteredList = if (searchText.isNotEmpty()) {
                        accountMasterList.filter { item ->
                            if (item is AccountMasterList) {
                                item.accountName.contains(searchText, ignoreCase = true)
                            } else {
                                false
                            }
                        }

                    } else {
                        accountMasterList // If the search text is empty, return the full list
                    }*/

                    // Update the adapter with the filtered list
                    //partyDealerAdapter.updateItems(ArrayList(filteredList))
                }

                override fun afterTextChanged(s: Editable?) {
                    // No action needed here
                }
            })

            partyDealerDialog.setView(layout, 0, 0, 0, 0)
            partyDealerDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)
            partyDealerDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            //FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun setupPaginationRecyclerView(rvItems: RecyclerView) {
        layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        rvItems.layoutManager = layoutManager

        partyDealerAdapter = PartyDealerListAdapter(accountMasterList)
        rvItems.adapter = partyDealerAdapter

        rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isScrolling && !isLastPage && (visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                    isScrolling = true
                    partyDealerPageNo++
                    callAccountMasterList()
                }
            }
        })
    }


    private fun resetSelection(
        resetBranch: Boolean,
        resetDivision: Boolean,
        resetCategory: Boolean
    ) {
        if (resetBranch) {
            selectedBranch = null
            tvSelectBranch.hint = mActivity.getString(R.string.select_branch)
            tvSelectBranch.text = ""
            branchMasterList.clear()
        }
        if (resetDivision) {
            selectedDivision = null
            tvSelectDivision.hint = mActivity.getString(R.string.select_division)
            tvSelectDivision.text = ""
            divisionMasterList.clear()
        }
        if (resetCategory) {
            selectedCategory = null
            //tvCategory.hint = mActivity.getString(R.string.select_category)
            //tvCategory.text = ""
            categoryList.clear()
            categoryList.add(CategoryMasterResponse(categoryName = "Select Category"))
            setupCategorySpinner()
        }
    }

    inner class PartyDealerListAdapter(
        partyDealerList: ArrayList<AccountMasterList>
    ) : RecyclerView.Adapter<PartyDealerListAdapter.ViewHolder>() {
        var filteredItems: List<AccountMasterList> = partyDealerList
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemUserBinding.inflate(inflater, parent, false)
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
            filteredItems = accountMasterList.filter { item ->
                (item.accountName ?: "").contains(query, ignoreCase = true)
            }
            notifyDataSetChanged()
        }

        fun updateItems(arrayList: ArrayList<AccountMasterList>) {
            this.filteredItems = arrayList
            notifyDataSetChanged()

        }

        inner class ViewHolder(private val itemBinding: ItemUserBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            fun bind(partyDealerData: AccountMasterList) {
                itemBinding.tvUserName.text = partyDealerData.accountName ?: ""
                itemBinding.tvUserName.setOnClickListener {
                    tvSelectPartyDealer.text = partyDealerData.accountName
                    selectedPartyDealerId = partyDealerData.accountMasterId
                    partyDealerDialog.dismiss()
                }
            }
        }
    }


}