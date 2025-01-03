package ethicstechno.com.fieldforce.ui.fragments.dashboard

import AnimationType
import addFragment
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentDashboardDrillBinding
import ethicstechno.com.fieldforce.databinding.ItemDashboardDrillBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.ui.adapter.CommonStringListAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.PaymentFollowUpFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DashboardDrillFragment : HomeBaseFragment(), View.OnClickListener, FilterDialogListener {

    lateinit var binding: FragmentDashboardDrillBinding
    var dashboardDrillList: ArrayList<DashboardDrillResponse> = arrayListOf()
    lateinit var dashBoardDrillAdapter: DashboardDrillAdapter
    lateinit var dashBoardData: DashboardListResponse
    lateinit var dashBoardDrillData: DashboardDrillResponse
    var isDrillDown = false
    var fromDate = ""
    var toDate = ""
    var parameterString = ""
    var apiEndPoint = ""
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
    var selectedFilterPosition = 0
    var selectedReportGroupByPosition = 0
    var isFromPartyDealerORVisit = false

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
                CommonMethods.showFilterDialog(
                    this,
                    mActivity,
                    startDate = fromDate,
                    endDate = toDate,
                    dateOptionPosition = selectedDateOption,
                    filterList = filterList,
                    reportGroupByList = reportGroupByList,
                    isFilterVisible = true,
                    isReportGroupByVisible = true,
                    selectedFilterPosition = selectedFilterPosition,
                    selectedReportGroupByPosition = selectedReportGroupByPosition
                )
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
        dashboardListReq.addProperty("Filter", filterString)

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


}