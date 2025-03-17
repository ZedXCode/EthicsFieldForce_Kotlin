package ethicstechno.com.fieldforce.ui.fragments.moreoption

import AnimationType
import addFragment
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
import ethicstechno.com.fieldforce.models.CommonDropDownResponse
import ethicstechno.com.fieldforce.models.ReportResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
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
import ethicstechno.com.fieldforce.utils.EXPENSE_ENTRY_PRINT
import ethicstechno.com.fieldforce.utils.FORM_ID_EXPENSE_ENTRY
import ethicstechno.com.fieldforce.utils.FORM_ID_EXPENSE_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FOR_BRANCH
import ethicstechno.com.fieldforce.utils.FOR_COMPANY
import ethicstechno.com.fieldforce.utils.FOR_DIVISION
import ethicstechno.com.fieldforce.utils.ID_ZERO
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.LAST_30_DAYS
import ethicstechno.com.fieldforce.utils.LAST_7_DAYS
import ethicstechno.com.fieldforce.utils.REPORT_M
import ethicstechno.com.fieldforce.utils.THIS_MONTH
import ethicstechno.com.fieldforce.utils.TODAY
import ethicstechno.com.fieldforce.utils.YESTERDAY
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ExpenseListFragment : HomeBaseFragment(), View.OnClickListener, FilterDialogListener,
    UserSearchDialogUtil.CompanyDialogDetect, UserSearchDialogUtil.DivisionDialogDetect,
    UserSearchDialogUtil.BranchDialogDetect,
           LeaveTypeAdapter.TypeSelect{

    lateinit var expenseBinding: FragmentExpenseListBinding
    var expenseList: ArrayList<ExpenseListResponse> = arrayListOf()


    var startDate = ""
    var endDate = ""
    private var selectedDateOptionPosition = 4
    private var selectedStatusPosition: Int = 0// THIS MONTH
    private var selectedStatus = 0 // THIS MONTH
    private var expenseId = 0 // THIS MONTH
    var isForApproval = false
    var expenseListForDelete: ArrayList<ExpenseListResponse> = arrayListOf()
    var expenseAdapter = ExpenseListAdapter(expenseList)

    private lateinit var tvSelectCompany: TextView
    private lateinit var tvSelectBranch: TextView
    private lateinit var tvSelectDivision: TextView

    private lateinit var llSelectCompany: LinearLayout
    private lateinit var llSelectBranch: LinearLayout
    private lateinit var llSelectDivision: LinearLayout

    private lateinit var llStatus: LinearLayout
    private lateinit var spStatus: Spinner

    lateinit var spCategory: Spinner

    val companyMasterList: ArrayList<CompanyMasterResponse> = arrayListOf()
    val branchMasterList: ArrayList<BranchMasterResponse> = arrayListOf()
    val divisionMasterList: ArrayList<DivisionMasterResponse> = arrayListOf()

    var selectedCompany: CompanyMasterResponse? = null
    var selectedBranch: BranchMasterResponse? = null
    var selectedDivision: DivisionMasterResponse? = null
    private var selectedCategory: CategoryMasterResponse? = null

    val categoryList: ArrayList<CategoryMasterResponse> = arrayListOf()
    var statusColor = ""


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
            expenseBinding.toolbar.imgFilter.visibility =View.VISIBLE
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

        val parameterString =
            "CompanyMasterId=${selectedCompany?.companyMasterId ?: 0} and BranchMasterId=${selectedBranch?.branchMasterId ?: 0} and DivisionMasterid=${selectedDivision?.divisionMasterId ?: 0} and" +
                    " CategoryMasterId=${selectedCategory?.categoryMasterId ?: 0} and $FORM_ID_EXPENSE_ENTRY"

        val expenseListReq = JsonObject()
        expenseListReq.addProperty("UserId", loginData.userId)
        expenseListReq.addProperty("ParameterString", parameterString)


        val expenseListCall = if (isForApproval) {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.getExpenseApprovalList(expenseListReq)
        } else {
            expenseListReq.addProperty("expenseId",expenseId)
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
                    binding.llTop.visibility = View.VISIBLE
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
                        mActivity.addFragment(AddExpenseFragment.newInstance(expenseData.expenseId ?: 0,true, expenseData, true),
                            addToBackStack = true,
                            ignoreIfCurrent = true,
                            animationType = AnimationType.fadeInfadeOut
                        )
                        Log.d("EXPENSEDATA===>","true data")
                    }
                } else {
                    binding.cbApprove.visibility = View.GONE
                    binding.cardMain.setOnClickListener {
                        mActivity.addFragment(AddExpenseFragment.newInstance(expenseData.expenseId ?: 0,true, expenseData, false),
                            addToBackStack = true,
                            ignoreIfCurrent = true,
                            animationType = AnimationType.fadeInfadeOut
                        )
                        Log.d("EXPENSEDATA===>","false data")
                    }
                }
                binding.tvUserName.text = expenseData.userName
                binding.tvValue1.text = "" + expenseData.expenseDate
                binding.tvValue2.text = "" + expenseData.cityName
                binding.tvValue3.text = "" + expenseData.expenseTypeName
                binding.tvValue4.text = "" + expenseData.expenseStatusName
                binding.tvValue5.text = "" + expenseData.eligibleAmount.toString()
                binding.tvValue6.text = "" + expenseData.expenseAmount.toString()
                //binding.executePendingBindings()

                statusColor = expenseData.statusColor.toString()
                if (statusColor.isEmpty()) {
                    binding.tvValue4.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#FFC107"))
                }else{
                    binding.tvValue4.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor(statusColor))
                }

                if (AppPreference.getBooleanPreference(mActivity, EXPENSE_ENTRY_PRINT)){
                    binding.ivShare.visibility = View.VISIBLE
                }else{
                    binding.ivShare.visibility = View.GONE
                }

                binding.ivShare.setOnClickListener{
                    callGetReport(expenseData.expenseId)
                }
            }
        }
    }

    private fun callGetReport(expenseId: Int) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val dashboardListReq = JsonObject()
        dashboardListReq.addProperty("reportSetupId", 0)
        dashboardListReq.addProperty("reportName", "")//Demo
        dashboardListReq.addProperty("UserId", loginData.userId)
        dashboardListReq.addProperty("reportType", REPORT_M)// r
        dashboardListReq.addProperty("formId", FORM_ID_EXPENSE_ENTRY_NUMBER)
        dashboardListReq.addProperty("fromDate", "")
        dashboardListReq.addProperty("toDate", "")
        dashboardListReq.addProperty("reportGroupBy", "")
        dashboardListReq.addProperty("parameterString", "")
        dashboardListReq.addProperty("filter", "")
        dashboardListReq.addProperty("documentId", expenseId)

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

    // Function to download and open the PDF
    fun downloadAndOpenPDF(context: Context, url: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        // Run the download on a background thread
        Thread {
            try {
                val response = client.newCall(request).execute()
                val inputStream: InputStream? = response.body?.byteStream()
                if (inputStream != null) {
                    // Save the file
                    val file = saveFileToStorage(context, inputStream)
                    // Open the file
                    openPDF(file, context)
                }
            } catch (e: IOException) {
                Log.e("DownloadError", "Error downloading file", e)

                // Show the Toast on the main thread
                Handler(context.mainLooper).post {
                    Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
    // Function to save the file locally
    private fun saveFileToStorage(context: Context, inputStream: InputStream): File {
        val fileName = "downloaded_report.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            Log.e("SaveFileError", "Error saving file", e)
        }

        return file
    }

    private fun openPDF(file: File?, context: Context) {
        val pdfUrl = "http://ffms.ethicstechno.com:41429/CrystalExportReportPath/212_StockReport_05_02_2025_22_07_04.pdf"
        // Attempt to open the URL in the browser
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
        context.startActivity(browserIntent)
    }


    private fun openUrlInChrome(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.setPackage("com.android.chrome")
            mActivity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            mActivity.startActivity(intent)
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
//                CommonMethods.showFilterDialog(
//                    this,
//                    mActivity,
//                    startDate,
//                    endDate,
//                    selectedDateOptionPosition,
//                    true,
//                    selectedStatusPosition = selectedStatus
//                )
                showFilterDialog()
            }
            R.id.tvAccept -> {
                showRemarksDialog(true)
            }
            R.id.tvReject -> {
                showRemarksDialog(false)
            }
        }
    }

    private fun showFilterDialog() {

        val filterDialog = Dialog(mActivity)

        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        filterDialog.setCancelable(true)
        filterDialog.setContentView(R.layout.filter_expense_entry_list)

        val spDateOption: Spinner = filterDialog.findViewById(R.id.spDateOption)
        val tvStartDate: TextView = filterDialog.findViewById(R.id.tvStartDate)
        val tvEndDate: TextView = filterDialog.findViewById(R.id.tvEndDate)
        tvSelectCompany = filterDialog.findViewById(R.id.tvSelectCompany)
        tvSelectBranch = filterDialog.findViewById(R.id.tvSelectBranch)
        tvSelectDivision = filterDialog.findViewById(R.id.tvSelectDivision)

        spStatus = filterDialog.findViewById(R.id.spStatus)
        llStatus = filterDialog.findViewById(R.id.llStatus)

        llSelectCompany = filterDialog.findViewById(R.id.llSelectCompany)
        llSelectBranch = filterDialog.findViewById(R.id.llSelectBranch)
        llSelectDivision = filterDialog.findViewById(R.id.llSelectDivision)
        spCategory = filterDialog.findViewById(R.id.spCategory)

        val btnSubmit = filterDialog.findViewById<TextView>(R.id.btnSubmit)

        startDate = CommonMethods.getStartDateOfCurrentMonth()
        endDate = CommonMethods.getCurrentDate()
        tvStartDate.text = startDate
        tvEndDate.text = endDate

        callCompanyListApi()

        val adapter = DateOptionAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            CommonMethods.statusList,
        )
        spStatus.adapter = adapter
        spStatus.setSelection(selectedStatusPosition)

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
        val adapter1 = DateOptionAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            CommonMethods.dateTypeList,
        )
        spDateOption.adapter = adapter1
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
                CommonMethods.showToastMessage(
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
            selectedStatus = spStatus.selectedItemPosition
            callExpenseListApi()
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
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("companyMasterId", ID_ZERO)
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty("ParameterString", FORM_ID_EXPENSE_ENTRY)

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
            "CompanyMasterId=${selectedCompany?.companyMasterId} and $FORM_ID_EXPENSE_ENTRY"
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
                                    branchInterfaceDetect = this@ExpenseListFragment as UserSearchDialogUtil.BranchDialogDetect,
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
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and $FORM_ID_EXPENSE_ENTRY"
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
                                    divisionInterfaceDetect = this@ExpenseListFragment as UserSearchDialogUtil.DivisionDialogDetect,
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
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and DivisionMasterid=${selectedDivision?.divisionMasterId} and $FORM_ID_EXPENSE_ENTRY"
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
        visitType: CommonDropDownResponse,
        partyDealer: AccountMasterList,
        visitPosition: Int
    ) {
        startDate = startDateFromListener
        endDate = endDateFromListener
        selectedDateOptionPosition = dateOptionPosition
        selectedStatus = statusPosition
        callExpenseListApi()
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