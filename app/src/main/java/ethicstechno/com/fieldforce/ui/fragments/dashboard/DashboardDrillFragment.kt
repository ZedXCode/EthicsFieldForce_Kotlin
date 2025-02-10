package ethicstechno.com.fieldforce.ui.fragments.dashboard

import AnimationType
import addFragment
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentDashboardDrillBinding
import ethicstechno.com.fieldforce.databinding.ItemDashboardDrillBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.listener.ItemClickListener
import ethicstechno.com.fieldforce.models.CommonProductFilterResponse
import ethicstechno.com.fieldforce.models.DropDownItem
import ethicstechno.com.fieldforce.models.ReportResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.BranchMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CompanyMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.DivisionMasterResponse
import ethicstechno.com.fieldforce.ui.adapter.CommonStringListAdapter
import ethicstechno.com.fieldforce.ui.adapter.LeaveTypeAdapter
import ethicstechno.com.fieldforce.ui.adapter.spinneradapter.DateOptionAdapter
import ethicstechno.com.fieldforce.ui.adapter.spinneradapter.FilterAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry.AddProductDialogFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.PaymentFollowUpFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.ARG_PARAM10
import ethicstechno.com.fieldforce.utils.ARG_PARAM11
import ethicstechno.com.fieldforce.utils.ARG_PARAM12
import ethicstechno.com.fieldforce.utils.ARG_PARAM13
import ethicstechno.com.fieldforce.utils.ARG_PARAM14
import ethicstechno.com.fieldforce.utils.ARG_PARAM15
import ethicstechno.com.fieldforce.utils.ARG_PARAM16
import ethicstechno.com.fieldforce.utils.ARG_PARAM2
import ethicstechno.com.fieldforce.utils.ARG_PARAM3
import ethicstechno.com.fieldforce.utils.ARG_PARAM4
import ethicstechno.com.fieldforce.utils.ARG_PARAM5
import ethicstechno.com.fieldforce.utils.ARG_PARAM6
import ethicstechno.com.fieldforce.utils.ARG_PARAM7
import ethicstechno.com.fieldforce.utils.ARG_PARAM8
import ethicstechno.com.fieldforce.utils.ARG_PARAM9
import ethicstechno.com.fieldforce.utils.CUSTOM_RANGE
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DIALOG_PRODUCT_GROUP_TYPE
import ethicstechno.com.fieldforce.utils.FORM_ID_ORDER_ENTRY
import ethicstechno.com.fieldforce.utils.FOR_BRANCH
import ethicstechno.com.fieldforce.utils.FOR_COMPANY
import ethicstechno.com.fieldforce.utils.FOR_DIVISION
import ethicstechno.com.fieldforce.utils.ID_ZERO
import ethicstechno.com.fieldforce.utils.LAST_30_DAYS
import ethicstechno.com.fieldforce.utils.LAST_7_DAYS
import ethicstechno.com.fieldforce.utils.LIST_TYPE_FILTER
import ethicstechno.com.fieldforce.utils.LIST_TYPE_REPORT_GROUP_BY
import ethicstechno.com.fieldforce.utils.PermissionUtil
import ethicstechno.com.fieldforce.utils.REPORT_R
import ethicstechno.com.fieldforce.utils.THIS_MONTH
import ethicstechno.com.fieldforce.utils.TODAY
import ethicstechno.com.fieldforce.utils.YESTERDAY
import ethicstechno.com.fieldforce.utils.dialog.SearchDialogUtil
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardDrillFragment : HomeBaseFragment(), View.OnClickListener, FilterDialogListener,
    UserSearchDialogUtil.CompanyDialogDetect, UserSearchDialogUtil.DivisionDialogDetect,
    UserSearchDialogUtil.BranchDialogDetect, LeaveTypeAdapter.TypeSelect{

    lateinit var binding: FragmentDashboardDrillBinding
    var dashboardDrillList: ArrayList<DashboardDrillResponse> = arrayListOf()
    lateinit var dashBoardDrillAdapter: DashboardDrillAdapter
    lateinit var dashBoardData: DashboardListResponse
    lateinit var dashBoardDrillData: DashboardDrillResponse
    var isDrillDown = false
    var fromDate = ""
    var toDate = ""
    var parameterString = ""
    private var apiEndPoint = ""
    var storeProcedureName = ""
    var reportGroupBy = ""
    var reportSetupId = 0
    var reportName = ""
    var headerColumn1Listing: ArrayList<String> = arrayListOf()
    var filterList: ArrayList<FilterListResponse> = arrayListOf()
    var reportGroupByList: ArrayList<FilterListResponse> = arrayListOf()
    var selectedDateOption = 4 //THIS MONTH
    var filterString = ""
    var isFromReport = false
    private var selectedFilterPosition = 0
    private var selectedReportGroupByPosition = 0
    private var isFromPartyDealerORVisit = false
    private var selectedDateOptionPosition = 4
    private var groupListNew: ArrayList<DropDownItem> = arrayListOf()
    private var itemList2: ArrayList<DropDownItem> = arrayListOf()
    private var selectedItemList2: ArrayList<DropDownItem> = arrayListOf()
    private var itemList3: ArrayList<DropDownItem> = arrayListOf()
    private var selectedItemList3: ArrayList<DropDownItem> = arrayListOf()
    private var itemList4: ArrayList<DropDownItem> = arrayListOf()
    private var selectedItemList4: ArrayList<DropDownItem> = arrayListOf()
    private var itemList5: ArrayList<DropDownItem> = arrayListOf()
    private var selectedItemList5: ArrayList<DropDownItem> = arrayListOf()

    private var filter2KeyIds = ""
    private var filter3KeyIds = ""
    private var filter4KeyIds = ""
    private var filter5KeyIds = ""

    private var header2Name = ""
    private var header3Name = ""
    private var header4Name = ""
    private var header5Name = ""

    lateinit var tvSelectGroup: TextView
    lateinit var tvFilter2: TextView
    lateinit var tvFilter3: TextView
    lateinit var tvFilter4: TextView
    lateinit var tvFilter5: TextView
    lateinit var llFilter1: LinearLayout
    lateinit var llFilter2: LinearLayout
    private lateinit var groupSearchDialog: SearchDialogUtil<DropDownItem>
    var selectedGroupId = 0
    var selectedGroup: DropDownItem? = null
    var isFilterApiCalled = false
    var productFilter = false
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
    private var isExpanded = false
    private var isShippingExpanded = false



    private val groupItemClickListener = object : ItemClickListener<DropDownItem> {
        override fun onItemSelected(item: DropDownItem) {
            // Handle user item selection
            //binding.viewSelectedGroup.visibility = View.VISIBLE
            groupSearchDialog.closeDialog()
            tvSelectGroup.text = item.dropdownValue
            selectedGroupId = (item.dropdownKeyId ?: "0").toInt()
            selectedGroup = item
        }
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



    companion object {
        fun newInstance(
            isDrillDown: Boolean,
            dashboardData: DashboardListResponse,
            drillData: DashboardDrillResponse,
            storeProcedureName: String,
            reportSetupId: Int,
            headerColumn1Listing: ArrayList<String>,
            reportName: String,
            filterString: String,
            reportGroupByString: String,
            isFromReport: Boolean = false,
            startDate: String = "",
            endDate: String = "",
            dateOptionPos: Int = 4,
            isFromPartyDealerORVisit: Boolean = false,
            parameterString: String = "",
            productFilter: Boolean = false
        ): DashboardDrillFragment {
            val args = Bundle()
            args.putBoolean(ARG_PARAM1, isDrillDown)
            args.putParcelable(ARG_PARAM2, dashboardData)
            args.putParcelable(ARG_PARAM3, drillData)
            args.putString(ARG_PARAM4, storeProcedureName)
            args.putInt(ARG_PARAM5, reportSetupId)
            args.putStringArrayList(ARG_PARAM6, headerColumn1Listing)
            args.putString(ARG_PARAM7, reportName)
            args.putString(ARG_PARAM8, filterString)
            args.putString(ARG_PARAM9, reportGroupByString)
            args.putBoolean(ARG_PARAM10, isFromReport)
            args.putString(ARG_PARAM11, startDate)
            args.putString(ARG_PARAM12, endDate)
            args.putInt(ARG_PARAM13, dateOptionPos)
            args.putBoolean(ARG_PARAM14, isFromPartyDealerORVisit)
            args.putString(ARG_PARAM15, parameterString)
            args.putBoolean(ARG_PARAM16, productFilter)
            val fragment = DashboardDrillFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard_drill, container, false)
        return binding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        mActivity.bottomHide()
        arguments?.let {
            headerColumn1Listing.clear()
            isDrillDown = it.getBoolean(ARG_PARAM1, false)
            dashBoardData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM2, DashboardListResponse::class.java)
                    ?: DashboardListResponse()
            } else {
                it.getParcelable(ARG_PARAM2) ?: DashboardListResponse()
            }
            dashBoardDrillData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM3, DashboardDrillResponse::class.java)
                    ?: DashboardDrillResponse()
            } else {
                it.getParcelable(ARG_PARAM3) ?: DashboardDrillResponse()
            }
            storeProcedureName = it.getString(ARG_PARAM4, "")
            reportSetupId = it.getInt(ARG_PARAM5, 0)
            headerColumn1Listing = it.getStringArrayList(ARG_PARAM6) ?: arrayListOf()
            reportName = it.getString(ARG_PARAM7, "")
            filterString = it.getString(ARG_PARAM8, "")
            reportGroupBy = it.getString(ARG_PARAM9, "")
            isFromReport = it.getBoolean(ARG_PARAM10, false)
            fromDate = it.getString(ARG_PARAM11, "")
            toDate = it.getString(ARG_PARAM12, "")
            selectedDateOption = it.getInt(ARG_PARAM13, 4)
            isFromPartyDealerORVisit = it.getBoolean(ARG_PARAM14, false)
            productFilter = it.getBoolean(ARG_PARAM16, false)
            //reportGroupBy = if (isDrillDown) reportGroupBy else dashBoardData.reportGroupBy


            parameterString = if (isFromPartyDealerORVisit) it.getString(
                ARG_PARAM15,
                ""
            ) else (if (isDrillDown) dashBoardDrillData.parameterString else "")
            apiEndPoint =
                if (isDrillDown || isFromReport) "api/Report/GetReportData" else dashBoardData.apiName
            storeProcedureName =
                if (isDrillDown || isFromReport) storeProcedureName else dashBoardData.storeProcedureName
            reportName = if (isDrillDown || isFromReport) reportName else dashBoardData.reportName
        }
        Log.e(
            "TAG",
            "initView: PARAMTERSTRING ::  " + parameterString + ", REPORTGROUP BY : " + reportGroupBy + ", FILTER :: " + filterString
        )
        binding.reportHeader.tvDateOption.text = CommonMethods.dateTypeList[selectedDateOption]
        fromDate = fromDate.ifEmpty { CommonMethods.getStartDateOfCurrentMonth() }
        toDate = toDate.ifEmpty { CommonMethods.getCurrentDate() }
        binding.reportHeader.tvDateRange.text = "$fromDate to $toDate"

        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.imgFilter.visibility = View.VISIBLE
        binding.toolbar.svView.visibility = View.VISIBLE
        binding.toolbar.svView.queryHint = HtmlCompat.fromHtml(
            mActivity.getString(R.string.search_here),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.toolbar.tvHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        binding.toolbar.tvHeader.text = reportName
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.imgFilter.setOnClickListener(this)
        binding.toolbar.imgShare.visibility = View.VISIBLE
        binding.toolbar.imgShare.setOnClickListener(this)
        binding.reportHeader.tvFilter.text = filterString
        binding.reportHeader.tvReportGroupBy.text = reportGroupBy

        setupHeaderColumnAdapter()
        setupSearchFilter()
        callFilterListingApi(true)//For filter listing
        callFilterListingApi(false)//For report group by listing
        callDashboardDrillApi()
    }

    private fun setupHeaderColumnAdapter() {
        val adapter = CommonStringListAdapter(headerColumn1Listing)
        val layoutManager = LinearLayoutManager(mActivity, RecyclerView.HORIZONTAL, false)
        binding.reportHeader.rvHeaderColumn.layoutManager = layoutManager
        binding.reportHeader.rvHeaderColumn.adapter = adapter
    }

    private fun setupSearchFilter() {
        binding.toolbar.svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(::dashBoardDrillAdapter.isInitialized) {
                    dashBoardDrillAdapter.filter(newText.orEmpty())
                }
                return true
            }
        })
        binding.toolbar.svView.setOnSearchClickListener {
            binding.toolbar.imgBack.visibility = View.GONE
            binding.toolbar.imgFilter.visibility = View.GONE
        }
        binding.toolbar.svView.setOnCloseListener {
            binding.toolbar.imgBack.visibility = View.VISIBLE
            binding.toolbar.imgFilter.visibility = View.VISIBLE
            false
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            if (headerColumn1Listing.size > 0) {
                headerColumn1Listing.apply {
                    headerColumn1Listing.remove(headerColumn1Listing.last())
                }
            }
            mActivity.bottomHide()
        }
    }

    private fun callFilterListingApi(isFilter: Boolean) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        //val paramString = parameterString//if (isDrillDown) dashBoardDrillData.parameterString else ""
        //selectedFilterString = "None"//if (isDrillDown) dashBoardDrillData.filter else ""
        //val storeProcedure = storeProcedureName//if (isDrillDown) dashBoardDrillData.storeProcedureName else dashBoardData.storeProcedureName
        //val reportGroupBy = reportgr//if (isDrillDown) dashBoardDrillData.reportGroupBy else dashBoardData.reportGroupBy
        val dashboardListReq = JsonObject()
        dashboardListReq.addProperty("ReportSetupId", reportSetupId)
        dashboardListReq.addProperty(
            "DropDownFieldName",
            if (isFilter) LIST_TYPE_FILTER else LIST_TYPE_REPORT_GROUP_BY
        )

        val dashbaordListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDropDownValueList(dashboardListReq)

        dashbaordListCall?.enqueue(object : Callback<List<FilterListResponse>> {
            override fun onResponse(
                call: Call<List<FilterListResponse>>,
                response: Response<List<FilterListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (isFilter) {
                            filterList.apply {
                                clear()
                                add(FilterListResponse(0, filterString, filterString))
                                if (it.isNotEmpty()) addAll(it)
                            }
                        } else {
                            reportGroupByList.apply {
                                clear()
                                add(FilterListResponse(0, reportGroupBy, reportGroupBy))
                                if (it.isNotEmpty()) addAll(it)
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

            override fun onFailure(call: Call<List<FilterListResponse>>, t: Throwable) {
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


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }
            R.id.imgFilter -> {
                showFilterDialog()
            }
            R.id.imgShare -> {
                if (dashboardDrillList.isEmpty()) {
                    return
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    createAndOpenPDF()
                } else {
                    val arrayPermissions = arrayListOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    PermissionUtil(mActivity).requestPermissions(arrayPermissions) {
                        createAndOpenPDF()
                    }
                }

            }
        }
    }

    private fun showFilterDialog() {

        val filterDialog = Dialog(mActivity)

        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        filterDialog.setCancelable(true)
        filterDialog.setContentView(R.layout.filter_report_layout)

        val spDateOption: Spinner = filterDialog.findViewById(R.id.spDateOption)
        val tvStartDate: TextView = filterDialog.findViewById(R.id.tvStartDate)
        val tvEndDate: TextView = filterDialog.findViewById(R.id.tvEndDate)
        val spReportGroupBy: Spinner = filterDialog.findViewById(R.id.spReportGroupBy)
        val spFilter: Spinner = filterDialog.findViewById(R.id.spFilter)
        tvFilter2 = filterDialog.findViewById(R.id.tvFilter2)
        tvFilter3 = filterDialog.findViewById(R.id.tvFilter3)
        tvFilter4 = filterDialog.findViewById(R.id.tvFilter4)
        tvFilter5 = filterDialog.findViewById(R.id.tvFilter5)
        llFilter1 = filterDialog.findViewById(R.id.llFilter1)
        llFilter2 = filterDialog.findViewById(R.id.llFilter2)
        tvSelectGroup = filterDialog.findViewById(R.id.tvSelectGroup)

        val btnSubmit = filterDialog.findViewById<TextView>(R.id.btnSubmit)
        val lylProductFilter = filterDialog.findViewById<LinearLayout>(R.id.lylProductFilter)

        val llHeader = filterDialog.findViewById<LinearLayout>(R.id.llHeader)
        val llOptionalFields = filterDialog.findViewById<CardView>(R.id.llOptionalFields)
        val llOptionalFieldsProducts = filterDialog.findViewById<CardView>(R.id.llOptionalFieldsProducts)
        val llHeaderProduct = filterDialog.findViewById<LinearLayout>(R.id.llHeaderProduct)
        val ivToggle = filterDialog.findViewById<ImageView>(R.id.ivToggle)
        val ivToggleProduct = filterDialog.findViewById<ImageView>(R.id.ivToggleProduct)

        tvSelectCompany = filterDialog.findViewById(R.id.tvSelectCompany)
        tvSelectBranch = filterDialog.findViewById(R.id.tvSelectBranch)
        tvSelectDivision = filterDialog.findViewById(R.id.tvSelectDivision)
        llSelectCompany = filterDialog.findViewById(R.id.llSelectCompany)
        llSelectBranch = filterDialog.findViewById(R.id.llSelectBranch)
        llSelectDivision = filterDialog.findViewById(R.id.llSelectDivision)
        spCategory = filterDialog.findViewById(R.id.spCategory)


        llHeader.setOnClickListener {
            toggleSectionVisibility(llOptionalFields, ivToggle, true)
        }

        llHeaderProduct.setOnClickListener {
            toggleSectionVisibility(llOptionalFieldsProducts, ivToggleProduct, true)
        }

        callCompanyListApi()

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
                CommonMethods.showToastMessage(
                    mActivity,
                    getString(R.string.company_list_not_found)
                )
            }
        }
        llSelectBranch.setOnClickListener {
            callBranchListApi(true)
        }
        llSelectDivision.setOnClickListener {
            callDivisionListApi(true)
        }

        if(productFilter) {
            lylProductFilter.visibility = View.VISIBLE
            if(!isFilterApiCalled) {
                callCommonProductFilterApi()
            }

            if (selectedGroup != null) {
                tvSelectGroup.text = selectedGroup?.dropdownValue
            }
            if (selectedItemList2.isNotEmpty()) {
                tvFilter2.text =
                    if (selectedItemList2.isEmpty()) header2Name else selectedItemList2[0].dropdownValue
            }
            if (selectedItemList3.isNotEmpty()) {
                tvFilter3.text =
                    if (selectedItemList3.isEmpty()) header3Name else selectedItemList3[0].dropdownValue
            }
            if (selectedItemList4.isNotEmpty()) {
                tvFilter4.text =
                    if (selectedItemList4.isEmpty()) header4Name else selectedItemList4[0].dropdownValue
            }
            if (selectedItemList5.isNotEmpty()) {
                tvFilter5.text =
                    if (selectedItemList5.isEmpty()) header5Name else selectedItemList5[0].dropdownValue
            }

            tvSelectGroup.setOnClickListener {
                showGroupDialog()
            }
            tvFilter2.setOnClickListener {
                showCustomDialog("Size List", itemList2, selectedItemList2) { selectedItems ->
                    selectedItemList2 =
                        if (selectedItems == null || selectedItems.isEmpty()) arrayListOf() else selectedItems as ArrayList<DropDownItem>
                    tvFilter2.text =
                        if (selectedItemList2.isEmpty()) header2Name else selectedItemList2[0].dropdownValue
                    //viewSelectedFilter2.visibility = if (selectedItemList2.isEmpty()) View.GONE else View.VISIBLE
                    filter2KeyIds =
                        if (selectedItemList2.isEmpty()) "" else selectedItemList2.joinToString("|") { "${it.dropdownKeyId}" }

                }
            }
            tvFilter3.setOnClickListener {
                showCustomDialog(
                    "Design List",
                    itemList3,
                    selectedItemList3
                ) { selectedItems ->
                    selectedItemList3 =
                        if (selectedItems == null || selectedItems.isEmpty()) arrayListOf() else selectedItems as ArrayList<DropDownItem>
                    tvFilter3.text =
                        if (selectedItemList3.isEmpty()) header3Name else selectedItemList3[0].dropdownValue
                    //viewSelectedFilter3.visibility = if (selectedItemList3.isEmpty()) View.GONE else View.VISIBLE
                    filter3KeyIds =
                        if (selectedItemList3.isEmpty()) "" else selectedItemList3.joinToString("|") { "${it.dropdownKeyId}" }
                    //callProductGroupListApi(false, selectedGroupId)
                }
            }
            tvFilter4.setOnClickListener {
                showCustomDialog(
                    "Grade List",
                    itemList4,
                    selectedItemList4
                ) { selectedItems ->
                    selectedItemList4 =
                        if (selectedItems == null || selectedItems.isEmpty()) arrayListOf() else selectedItems as ArrayList<DropDownItem>
                    tvFilter4.text =
                        if (selectedItemList4.isEmpty()) header4Name else selectedItemList4[0].dropdownValue
                    //binding.viewSelectedFilter4.visibility = if (selectedItemList4.isEmpty()) View.GONE else View.VISIBLE
                    filter4KeyIds =
                        if (selectedItemList4.isEmpty()) "" else selectedItemList4.joinToString("|") { "${it.dropdownKeyId}" }
                    //callProductGroupListApi(false, selectedGroupId)
                }
            }
            tvFilter5.setOnClickListener {
                showCustomDialog(
                    "Series List",
                    itemList5,
                    selectedItemList5
                ) { selectedItems ->
                    selectedItemList5 =
                        if (selectedItems == null || selectedItems.isEmpty()) arrayListOf() else selectedItems as ArrayList<DropDownItem>
                    tvFilter5.text =
                        if (selectedItemList5.isEmpty()) header5Name else selectedItemList5[0].dropdownValue
                    //binding.viewSelectedFilter5.visibility = if (selectedItemList5.isEmpty()) View.GONE else View.VISIBLE
                    filter5KeyIds =
                        if (selectedItemList5.isEmpty()) "" else selectedItemList5.joinToString("|") { "${it.dropdownKeyId}" }
                    //callProductGroupListApi(false, selectedGroupId)
                }
            }
        }


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

        val adapter = FilterAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            reportGroupByList,
        )
        spReportGroupBy.adapter = adapter
        spReportGroupBy.setSelection(selectedReportGroupByPosition)

        val adapterFilter = FilterAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            filterList,
        )
        spFilter.adapter = adapterFilter
        spFilter.setSelection(selectedFilterPosition)

        val adapter1 = DateOptionAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            CommonMethods.dateTypeList,
        )
        spDateOption.adapter = adapter1
        spDateOption.setSelection(selectedDateOptionPosition)

        btnSubmit.setOnClickListener{
            fromDate = tvStartDate.text.toString()
            toDate = tvEndDate.text.toString()
            filterString = filterList[spFilter.selectedItemPosition].dropDownFieldValue
            selectedDateOption = spDateOption.selectedItemPosition
            reportGroupBy = reportGroupByList[spReportGroupBy.selectedItemPosition].dropDownFieldValue
            selectedFilterPosition =
                filterList.indexOfFirst { it.dropDownFieldValue == filterList[spFilter.selectedItemPosition].dropDownFieldValue }
            selectedReportGroupByPosition =
                reportGroupByList.indexOfFirst { it.dropDownFieldValue == filterList[spFilter.selectedItemPosition].dropDownFieldValue }
            binding.reportHeader.tvFilter.text = filterString
            binding.reportHeader.tvDateOption.text = spDateOption.selectedItem.toString()
            binding.reportHeader.tvReportGroupBy.text = reportGroupBy
            binding.reportHeader.tvDateRange.text = "$fromDate to $toDate"

            callDashboardDrillApi()
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

    private fun showCustomDialog(
        title: String,
        dropdownItemList: List<DropDownItem>,
        selectedItems: List<DropDownItem>,
        onSelectionComplete: (List<DropDownItem>) -> Unit
    ) {
        val builder = AlertDialog.Builder(mActivity, R.style.MyAlertDialogStyle)
        val inquiryDialog = builder.create()
        inquiryDialog.setCancelable(false)
        inquiryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val inflater = mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = inflater.inflate(R.layout.add_inquiry_dialog, null)
        val rvInquiry = layout.findViewById<RecyclerView>(R.id.rvInquiry)
        val cbAll = layout.findViewById<CheckBox>(R.id.cbSelectedItems)
        val cbSelected = layout.findViewById<CheckBox>(R.id.cbSelectedFilters)
        val tvClearAll = layout.findViewById<TextView>(R.id.tvClearAll)
        val btnSubmit = layout.findViewById<Button>(R.id.btnSubmit)
        val imgBack = layout.findViewById<ImageView>(R.id.imgBack)
        val edtSearch = layout.findViewById<EditText>(R.id.edtSearch)
        val tvTitle = layout.findViewById<TextView>(R.id.tvTitle)

        if(selectedItems.size > 0){
            cbSelected.visibility = View.VISIBLE
            cbSelected.text = "Selected (${selectedItems.size})"
        }

        tvTitle.text = title
        val adapter = AddProductDialogFragment.ProductFilterAdapter(
            mActivity,
            dropdownItemList
        ) { selectedCount ->
            if (selectedCount > 0) {
                cbSelected.visibility = View.VISIBLE
            } else {
                cbSelected.visibility = View.GONE
            }
            cbSelected.text = "Selected ($selectedCount)"
        }
        if (selectedItems.isNotEmpty()) {
            adapter.updateCheckedItems(selectedItems)
            cbAll.isChecked = selectedItems.size == dropdownItemList.size
        }



        rvInquiry.layoutManager = LinearLayoutManager(mActivity)
        rvInquiry.adapter = adapter

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        imgBack.setOnClickListener {
            inquiryDialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            val selected = adapter.getSelectedItems()
            //if (selected.isNotEmpty()) {
            onSelectionComplete(selected)
            inquiryDialog.dismiss()
            //} /*else {
            //CommonMethods.showToastMessage(mActivity, getString(R.string.no_items_selected))
            //}*/
        }

        cbAll.setOnCheckedChangeListener { _, isChecked ->
            cbSelected.visibility = View.GONE
            adapter.selectAll(isChecked)
            cbAll.isChecked = isChecked // Reflect state in the checkbox
        }

        cbSelected.setOnCheckedChangeListener { _, isChecked ->
            val selectedCount = adapter.getSelectedItems().size
            cbSelected.text = if (isChecked) {
                "Selected ($selectedCount)"
            } else {
                "Selected ($selectedCount)"
            }

            if (isChecked) {
                // Show only the selected items
                val selectedItems = adapter.getSelectedItems()
                adapter.filterSelectedItems(selectedItems)
            } else {
                // Show all items
                adapter.resetFilter()
            }
        }



        tvClearAll.setOnClickListener {
            adapter.clearAllFilters()
            cbAll.isChecked = false
            cbSelected.isChecked = false
        }

        inquiryDialog.setOnDismissListener {
            edtSearch.removeTextChangedListener(null) // Clean up listeners
        }

        inquiryDialog.setView(layout)
        inquiryDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)
        inquiryDialog.show()
    }

    private fun toggleSectionVisibility(
        view: View,
        toggleIcon: ImageView,
        isForOtherFields: Boolean
    ) {
        if (isForOtherFields) {
            if (isExpanded) {
                CommonMethods.collapse(view)
                toggleIcon.setImageResource(R.drawable.ic_add_circle)
            } else {
                CommonMethods.expand(view)
                toggleIcon.setImageResource(R.drawable.ic_remove_circle)
            }
            isExpanded = !isExpanded
        } else {
            if (isShippingExpanded) {
                CommonMethods.collapse(view)
                toggleIcon.setImageResource(R.drawable.ic_add_circle)
            } else {
                CommonMethods.expand(view)
                toggleIcon.setImageResource(R.drawable.ic_remove_circle)
            }
            isShippingExpanded = !isShippingExpanded
        }
    }

    private fun showGroupDialog() {
        try {
            if (groupListNew.size > 0) {
                groupSearchDialog = SearchDialogUtil(
                    activity = mActivity,
                    items = groupListNew,
                    layoutId = R.layout.item_user, // The layout resource for each item
                    bind = { view, item ->
                        val textView: TextView = view.findViewById(R.id.tvUserName)
                        textView.text = item.dropdownValue // Bind data to the view
                    },
                    itemClickListener = groupItemClickListener,
                    title = "Group List",
                    dialogType = DIALOG_PRODUCT_GROUP_TYPE
                )
                groupSearchDialog.showSearchDialog()
            }
        } catch (e: Exception) {
            Log.e("TAG", "onClick: " + e.message)
        }
    }



    private fun callCommonProductFilterApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        jsonReq.addProperty("userId", loginData.userId)
        jsonReq.addProperty("parameterString", "")

        val commonProductFilterCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCommonProductFilterList(jsonReq)

        commonProductFilterCall?.enqueue(object : Callback<CommonProductFilterResponse> {
            override fun onResponse(
                call: Call<CommonProductFilterResponse>,
                response: Response<CommonProductFilterResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.list1?.items?.size!! > 0) {
                            groupListNew.clear()
                            groupListNew.addAll(it.list1.items)
                        }
                        if (it.list2?.items?.size!! > 0) {
                            itemList2.clear()
                            itemList2.addAll(it.list2.items)
                            tvFilter2.text = it.list2.headerName ?: ""
                            header2Name = it.list2.headerName ?: ""
                        }
                        if (it.list3?.items?.size!! > 0) {
                            itemList3.clear()
                            itemList3.addAll(it.list3.items)
                            tvFilter3.text = it.list3.headerName ?: ""
                            header3Name = it.list3.headerName ?: ""
                        }
                        if (it.list4?.items?.size!! > 0) {
                            itemList4.clear()
                            itemList4.addAll(it.list4.items)
                            tvFilter4.text = it.list4.headerName ?: ""
                            header4Name = it.list4.headerName ?: ""
                        }
                        if (it.list5?.items?.size!! > 0) {
                            itemList5.clear()
                            itemList5.addAll(it.list5.items)
                            tvFilter5.text = it.list5.headerName ?: ""
                            header5Name = it.list5.headerName ?: ""
                        }

                        if (itemList2.isEmpty() && itemList3.isEmpty()) {
                            llFilter1.visibility = View.GONE
                        }
                        if (itemList4.isEmpty() && itemList5.isEmpty()) {
                            llFilter2.visibility = View.GONE
                        }
                        isFilterApiCalled = true
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
                CommonMethods.hideLoading()
            }

            override fun onFailure(call: Call<CommonProductFilterResponse>, t: Throwable) {
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


    private fun callDashboardDrillApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()
        //val paramString = parameterString//if (isDrillDown) dashBoardDrillData.parameterString else ""
        //selectedFilterString = "None"//if (isDrillDown) dashBoardDrillData.filter else ""
        //val storeProcedure = storeProcedureName//if (isDrillDown) dashBoardDrillData.storeProcedureName else dashBoardData.storeProcedureName
        //val reportGroupBy = reportgr//if (isDrillDown) dashBoardDrillData.reportGroupBy else dashBoardData.reportGroupBy
        val dashboardListReq = JsonObject()
        dashboardListReq.addProperty("StoreProcedureName", storeProcedureName)
        dashboardListReq.addProperty("UserId", loginData.userId)
        dashboardListReq.addProperty("FromDate", fromDate)
        dashboardListReq.addProperty("ToDate", toDate)
        dashboardListReq.addProperty("ReportGroupBy", reportGroupBy)
        dashboardListReq.addProperty("ParameterString", parameterString)
        if(productFilter) {
            dashboardListReq.addProperty("Filter", "$filterString and $header2Name IN ($filter2KeyIds) AND $header3Name IN ($filter3KeyIds) AND $header4Name IN ($filter4KeyIds) AND $header5Name IN ($filter5KeyIds)")
        }else {
            dashboardListReq.addProperty("Filter", "$filterString")
        }

        Log.e("TAG", "callDashboardDrillApi: DASHBOARD LIST REQUEST  ::  " + dashboardListReq)
        val dashbaordListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.dashboardDrill(apiEndPoint, dashboardListReq)

        dashbaordListCall?.enqueue(object : Callback<List<DashboardDrillResponse>> {
            override fun onResponse(
                call: Call<List<DashboardDrillResponse>>,
                response: Response<List<DashboardDrillResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            binding.rvDashboard.visibility = View.VISIBLE
                            binding.tvNoData.visibility = View.GONE
                            dashboardDrillList.clear()
                            if (isDrillDown) {
                                binding.cardDrillTop.visibility = View.VISIBLE
                                binding.tvColumn1.text = dashBoardDrillData.column1
                                binding.tvColumn2.text = dashBoardDrillData.column2
                                binding.tvValue1.text = dashBoardDrillData.value1
                                binding.tvValue2.text = dashBoardDrillData.value2
                            }
                            dashboardDrillList.addAll(it)
                            setupDashboardDrillAdapter()
                        } else {
                            binding.rvDashboard.visibility = View.GONE
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

            override fun onFailure(call: Call<List<DashboardDrillResponse>>, t: Throwable) {
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

    private fun callCompanyListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("companyMasterId", ID_ZERO)
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty("ParameterString", FORM_ID_ORDER_ENTRY)

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
            "CompanyMasterId=${selectedCompany?.companyMasterId} and $FORM_ID_ORDER_ENTRY"
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
                                    branchInterfaceDetect = this@DashboardDrillFragment as UserSearchDialogUtil.BranchDialogDetect,
                                    userDialogInterfaceDetect = null
                                )
                                userDialog.showUserSearchDialog()
                            }
                        } else {
                            CommonMethods.showToastMessage(
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
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("divisionMasterId", ID_ZERO)
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and $FORM_ID_ORDER_ENTRY"
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
                                    divisionInterfaceDetect = this@DashboardDrillFragment as UserSearchDialogUtil.DivisionDialogDetect,
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
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
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
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and DivisionMasterid=${selectedDivision?.divisionMasterId} and $FORM_ID_ORDER_ENTRY"
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

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun setupDashboardDrillAdapter() {
        dashBoardDrillAdapter = DashboardDrillAdapter(dashboardDrillList)
        binding.rvDashboard.adapter = dashBoardDrillAdapter
        binding.rvDashboard.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    inner class DashboardDrillAdapter(
        private val dashboardList: List<DashboardDrillResponse>
    ) : RecyclerView.Adapter<DashboardDrillAdapter.ViewHolder>() {
        var filteredItems: List<DashboardDrillResponse> = dashboardList
        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemDashboardDrillBinding.inflate(inflater, parent, false)
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
            filteredItems = dashboardList.filter { item ->
                item.column1.contains(query, ignoreCase = true) ||
                        item.value1.contains(query, ignoreCase = true) ||
                        item.column2.contains(query, ignoreCase = true) ||
                        item.column2.contains(query, ignoreCase = true)
            }
            notifyDataSetChanged()
        }

        inner class ViewHolder(private val binding: ItemDashboardDrillBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(drillData: DashboardDrillResponse) {

                binding.tvColumn1.setTextColor(ContextCompat.getColor(mActivity, R.color.black))
                binding.tvValue1.setTextColor(ContextCompat.getColor(mActivity, R.color.black))
                binding.tvColumn2.setTextColor(ContextCompat.getColor(mActivity, R.color.black))
                binding.tvValue2.setTextColor(ContextCompat.getColor(mActivity, R.color.black))
                //}
                if(drillData.reportViewFlag){
                    binding.ivShare.visibility = View.VISIBLE
                }else{
                    binding.ivShare.visibility = View.GONE
                }

                binding.ivShare.setOnClickListener{
                    callGetReport(drillData)
                }

                if (drillData.popUpScreenFlag == 1) {
                    binding.imgPaymentFollowUp.visibility = View.VISIBLE
                } else {
                    binding.imgPaymentFollowUp.visibility = View.GONE
                }

                binding.imgPaymentFollowUp.setOnClickListener {
                    mActivity.addFragment(
                        PaymentFollowUpFragment.newInstance(drillData),
                        true,
                        true,
                        AnimationType.fadeInfadeOut
                    )
                }

                binding.tvColumn1.text = drillData.column1
                binding.tvValue1.text = drillData.value1
                binding.tvColumn2.text = drillData.column2
                binding.tvValue2.text = drillData.value2
                binding.cardMain.setOnClickListener {

                    Log.e(
                        "TAG",
                        "bind: " + if (isDrillDown) reportSetupId else dashBoardData.reportSetupId.toString()
                    )
                    if (drillData.drillDownFlag) {
                        Log.e("TAG", "bind: " + drillData.reportGroupBy)
                        headerColumn1Listing.add(drillData.column1)
                        mActivity.addFragment(
                            newInstance(
                                true,
                                dashboardData = DashboardListResponse(),
                                drillData,
                                storeProcedureName = if (isDrillDown || isFromReport) storeProcedureName else dashBoardData.storeProcedureName,
                                reportSetupId = if (isDrillDown || isFromReport) reportSetupId else dashBoardData.reportSetupId,
                                headerColumn1Listing = headerColumn1Listing,
                                reportName = reportName,
                                filterString = drillData.filter,
                                reportGroupByString = drillData.reportGroupBy,
                                startDate = fromDate,
                                endDate = toDate,
                                dateOptionPos = selectedDateOption
                            ),
                            true,
                            false,
                            AnimationType.fadeInfadeOut
                        )
                    }
                }
                // binding.executePendingBindings()
            }

        }
    }

    override fun onFilterSubmitClick(
        startDate: String,
        endDate: String,
        dateOption: String,
        dateOptionPosition: Int,
        statusPosition: Int,
        filterStringFromSubmit: FilterListResponse,
        reportGroupByStringFromSubmit: FilterListResponse,
        visitType: CategoryMasterResponse,
        partyDealer: AccountMasterList,
        visitPosition: Int
    ) {
        fromDate = startDate
        toDate = endDate
        filterString = filterStringFromSubmit.dropDownFieldValue
        selectedDateOption = dateOptionPosition
        reportGroupBy = reportGroupByStringFromSubmit.dropDownFieldValue
        selectedFilterPosition =
            filterList.indexOfFirst { it.dropDownFieldValue == filterStringFromSubmit.dropDownFieldValue }
        selectedReportGroupByPosition =
            reportGroupByList.indexOfFirst { it.dropDownFieldValue == filterStringFromSubmit.dropDownFieldValue }
        binding.reportHeader.tvFilter.text = filterString
        binding.reportHeader.tvDateOption.text = dateOption
        binding.reportHeader.tvReportGroupBy.text = reportGroupBy
        binding.reportHeader.tvDateRange.text = "$fromDate to $toDate"
        callDashboardDrillApi()

    }

    private fun callGetReport(drillData: DashboardDrillResponse) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val filterParameterString =
            "CompanyMasterId=${selectedCompany?.companyMasterId ?: 0} and BranchMasterId=${selectedBranch?.branchMasterId ?: 0} and DivisionMasterid=${selectedDivision?.divisionMasterId ?: 0} and" +
                    " CategoryMasterId=${selectedCategory?.categoryMasterId ?: 0} and \"$header2Name IN ($filter2KeyIds) AND $header3Name IN ($filter3KeyIds) AND $header4Name IN " +
                    "($filter4KeyIds) AND $header5Name IN ($filter5KeyIds)"

        val dashboardListReq = JsonObject()
        dashboardListReq.addProperty("reportSetupId", reportSetupId)
        dashboardListReq.addProperty("reportName", drillData.reportFileName)
        dashboardListReq.addProperty("UserId", loginData.userId)
        dashboardListReq.addProperty("reportType", REPORT_R)
        dashboardListReq.addProperty("formId", reportSetupId)
        dashboardListReq.addProperty("fromDate", fromDate)
        dashboardListReq.addProperty("toDate", toDate)
        dashboardListReq.addProperty("reportGroupBy", drillData.reportGroupBy)
        dashboardListReq.addProperty("parameterString", drillData.parameterString)
        dashboardListReq.addProperty("filter", filterString)
        dashboardListReq.addProperty("filterParameterString", filterParameterString)
        dashboardListReq.addProperty("documentId", 0)





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
                        if(reportResponse.fileName.isEmpty()){
                            CommonMethods.showToastMessage(mActivity, "No Report Found")
                            return
                        }
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


    private fun createAndOpenPDF() {
        showToastMessage(mActivity, "Report Generating..")
        // Step 1: Create a new document
        val document = Document(PageSize.A4)

        // Step 2: Specify the file path where the PDF will be saved
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = reportName + "_" + timeStamp + ".pdf"
        val filePath = File(directory, fileName)

        try {
            val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))

            document.open()

            val titleFont = Font(Font.FontFamily.HELVETICA, 26f, Font.BOLD, BaseColor.BLUE)
            val titlePhrase = Phrase(reportName, titleFont)
            val headerCell = PdfPCell(titlePhrase)
            headerCell.colspan = 3
            headerCell.horizontalAlignment = Element.ALIGN_CENTER
            headerCell.verticalAlignment = Element.ALIGN_MIDDLE
            headerCell.fixedHeight = 40f
            headerCell.border = PdfPCell.NO_BORDER
            val headerTable = PdfPTable(1)
            headerTable.widthPercentage = 100f
            headerTable.addCell(headerCell)
            document.add(headerTable)
            document.add(Paragraph("\n"))
            val headerFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor.WHITE)
            val infoFont = Font(Font.FontFamily.HELVETICA, 16f, Font.NORMAL, BaseColor.WHITE)
            val infoPhrase = Phrase(
                "Report Group By : $reportGroupBy \n" +
                        "Filter : $filterString \n" +
                        "Date Option : ${CommonMethods.dateTypeList[selectedDateOption]} \n" +
                        "Date Range : $fromDate to $toDate",
                infoFont
            )
            val infoCell = PdfPCell(infoPhrase)
            infoCell.colspan = 3
            infoCell.horizontalAlignment = Element.ALIGN_LEFT
            infoCell.verticalAlignment = Element.ALIGN_MIDDLE
            infoCell.fixedHeight = 90f
            infoCell.paddingTop = 8f
            infoCell.paddingRight = 8f
            infoCell.paddingLeft = 8f
            infoCell.paddingBottom = 8f
            infoCell.backgroundColor = BaseColor.BLUE // Add background color
            val infoTable = PdfPTable(1)
            infoTable.widthPercentage = 100f
            infoTable.addCell(infoCell)
            document.add(infoTable)

            document.add(Paragraph("\n"))

            if (isDrillDown) {
                val infoFont1 = Font(Font.FontFamily.HELVETICA, 16f, Font.NORMAL, BaseColor.WHITE)

                val leftAlignedText = Phrase()
                leftAlignedText.add(Chunk(dashBoardDrillData.column1, headerFont))
                leftAlignedText.add(Chunk("\n"))
                leftAlignedText.add(Chunk(dashBoardDrillData.column2, infoFont))

                val rightAlignedText = "${dashBoardDrillData.value1}\n${dashBoardDrillData.value2}"

                val leftCell = PdfPCell(leftAlignedText)
                leftCell.backgroundColor = BaseColor.BLUE
                leftCell.horizontalAlignment = Element.ALIGN_LEFT
                leftCell.verticalAlignment = Element.ALIGN_MIDDLE
                leftCell.fixedHeight = 50f
                leftCell.borderWidth = 0f

                val rightCell = PdfPCell(Phrase(Chunk(rightAlignedText, infoFont1)))
                rightCell.backgroundColor = BaseColor.BLUE
                rightCell.horizontalAlignment = Element.ALIGN_RIGHT
                rightCell.verticalAlignment = Element.ALIGN_MIDDLE
                rightCell.fixedHeight = 50f
                rightCell.borderWidth = 0f
                val infoTable1 = PdfPTable(2)
                infoTable1.widthPercentage = 100f

                infoTable1.addCell(leftCell)
                infoTable1.addCell(rightCell)

                document.add(infoTable1)

                document.add(Paragraph("\n"))
            }

            val table = PdfPTable(floatArrayOf(10f, 60f, 30f))
            table.widthPercentage = 100f


            table.addCell(getStyledCell("Sr.No.", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("Particular", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("Value", headerFont, BaseColor.BLUE))

            val dataFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.BLACK)
            val column1Bold = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.BLACK)

            for (i in dashboardDrillList.indices) {
                table.addCell(getStyledCell((i + 1).toString(), dataFont, BaseColor.WHITE))

                val cellText = Phrase()
                cellText.add(Chunk(dashboardDrillList[i].column1, column1Bold))
                cellText.add(Chunk("\n"))
                cellText.add(Chunk(dashboardDrillList[i].column2, dataFont))
                val cell = PdfPCell(cellText)
                cell.horizontalAlignment = Element.ALIGN_LEFT // Set left alignment for the cell
                cell.verticalAlignment =
                    Element.ALIGN_MIDDLE // Set center vertical alignment for the cell
                table.addCell(cell)

                val cellText1 = Phrase()
                cellText1.add(Chunk(dashboardDrillList[i].value1, dataFont))
                cellText1.add(Chunk("\n"))
                cellText1.add(Chunk(dashboardDrillList[i].value2, dataFont))
                val cell1 = PdfPCell(cellText1)
                cell1.horizontalAlignment = Element.ALIGN_RIGHT // Set left alignment for the cell
                cell1.verticalAlignment =
                    Element.ALIGN_MIDDLE // Set center vertical alignment for the cell

                table.addCell(cell1)

            }

// Step 11: Add the table to the document
            document.add(table)

// Step 12: Close the document
            document.close()

// Step 13: Dispose of the PdfWriter
            writer.close()


            // Step 12: Open the PDF using an intent
            CommonMethods.openPDFWithIntent(mActivity, filePath)

        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            CommonMethods.showAlertDialog(
                mActivity, "Error ", "Error is : " + e.message.toString(),
                okListener = null, isCancelVisibility = false
            )
            e.printStackTrace()
        }
    }

    // Function to create a styled cell with specified font and background color
    fun getStyledCell(content: String, font: Font, bgColor: BaseColor): PdfPCell {
        val cell = PdfPCell(Phrase(content, font))
        cell.backgroundColor = bgColor
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(4f)// = 8f
        return cell
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

}