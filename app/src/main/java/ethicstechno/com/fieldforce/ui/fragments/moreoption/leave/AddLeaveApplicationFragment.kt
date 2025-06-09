package ethicstechno.com.fieldforce.ui.fragments.moreoption.leave

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAddLeaveBinding
import ethicstechno.com.fieldforce.listener.DatePickerListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.AppRegistrationResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.LeaveDetailListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveApplicationListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveApplicationResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeDrpdownResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.BranchMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CompanyMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.DivisionMasterResponse
import ethicstechno.com.fieldforce.ui.adapter.LeaveTypeAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.APPROVE_STATUS
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.ARG_PARAM17
import ethicstechno.com.fieldforce.utils.ARG_PARAM2
import ethicstechno.com.fieldforce.utils.ARG_PARAM3
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.hideKeyboard
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.FORM_ID_LEAVE_APPLICATION_ENTRY
import ethicstechno.com.fieldforce.utils.FOR_BRANCH
import ethicstechno.com.fieldforce.utils.FOR_COMPANY
import ethicstechno.com.fieldforce.utils.FOR_DIVISION
import ethicstechno.com.fieldforce.utils.ID_ZERO
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.LEAVE_APPROVED_STATUS
import ethicstechno.com.fieldforce.utils.LEAVE_REJECTED_STATUS
import ethicstechno.com.fieldforce.utils.REJECT_STATUS
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AddLeaveApplicationFragment : HomeBaseFragment(), View.OnClickListener, DatePickerListener,
    UserSearchDialogUtil.CompanyDialogDetect, UserSearchDialogUtil.DivisionDialogDetect,
    UserSearchDialogUtil.BranchDialogDetect,LeaveTypeAdapter.TypeSelect  {

    lateinit var binding: FragmentAddLeaveBinding
    var leaveTypeList: ArrayList<LeaveTypeListResponse> = arrayListOf()
    var leaveTypeOtherList: ArrayList<LeaveTypeDrpdownResponse> = arrayListOf()
    lateinit var selectedLeaveType: LeaveTypeListResponse
    lateinit var selectedLeaveDropType: LeaveTypeDrpdownResponse
    var isForUpdate = false
    private var isFromApproval = false
    var leaveApplicationData = LeaveApplicationListResponse()

    private var isExpanded = false
    var selectedCompany: CompanyMasterResponse? = null
    var selectedBranch: BranchMasterResponse? = null
    var selectedDivision: DivisionMasterResponse? = null
    var selectedCategory: CategoryMasterResponse? = null
    val categoryList: ArrayList<CategoryMasterResponse> = arrayListOf()

    val branchMasterList: ArrayList<BranchMasterResponse> = arrayListOf()
    val companyMasterList: ArrayList<CompanyMasterResponse> = arrayListOf()
    val divisionMasterList: ArrayList<DivisionMasterResponse> = arrayListOf()

    private var leaveApplicationId: Int = 0
    lateinit var selectedLeaveDropdownType: LeaveTypeDrpdownResponse
    private var isCompanyChange = false
    private var detail:String = ""
    companion object {
        fun newInstance(
            leaveApplicationId: Int,
            isForUpdate: Boolean,
            expenseDataForUpdate: LeaveApplicationListResponse = LeaveApplicationListResponse(),
            isApproval: Boolean
        ): AddLeaveApplicationFragment {
            val args = Bundle()
            args.putInt(ARG_PARAM17, leaveApplicationId)
            args.putBoolean(ARG_PARAM1, isForUpdate)
            args.putParcelable(ARG_PARAM2, expenseDataForUpdate)
            args.putBoolean(ARG_PARAM3, isApproval)
            val fragment = AddLeaveApplicationFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_leave, container, false)
        return binding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            isForUpdate = it.getBoolean(ARG_PARAM1, false)
            leaveApplicationData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM2, LeaveApplicationListResponse::class.java)
                    ?: LeaveApplicationListResponse()
            } else {
                it.getParcelable(ARG_PARAM2) ?: LeaveApplicationListResponse()
            }
            isFromApproval = it.getBoolean(ARG_PARAM3, false)
        }

        initView()
    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {
        arguments?.let {
            leaveApplicationId = it.getInt(ARG_PARAM17, -1)
            Log.d("EXPENSEID===>",""+leaveApplicationId)
        }

        binding.llHeader.setOnClickListener {
            toggleSectionVisibility(binding.llOptionalFields, binding.ivToggle)
        }

        binding.toolbar.imgBack.visibility = View.VISIBLE
        if (isFromApproval) {
            binding.tvSubmit.visibility = View.GONE
            binding.llAcceptReject.visibility = View.VISIBLE
            //binding.toolbar.imgDelete.visibility = View.GONE
            //binding.toolbar.imgEdit.visibility = View.GONE
            binding.tvAccept.setOnClickListener(this)
            binding.tvReject.setOnClickListener(this)
        }

        selectedCompany = CompanyMasterResponse(companyMasterId = 0, companyName = "")
        selectedBranch = BranchMasterResponse(branchMasterId = 0, branchName = "")
        selectedDivision = DivisionMasterResponse(divisionMasterId = 0, divisionName = "")
        selectedCategory = CategoryMasterResponse(categoryMasterId = 0, categoryName = "")


        binding.toolbar.tvHeader.text = getString(R.string.leave_application)
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.imgDelete.setOnClickListener(this)
        binding.toolbar.imgEdit.setOnClickListener(this)
        binding.tvFromDate.setOnClickListener(this)
        binding.tvToDate.setOnClickListener(this)
        binding.tvSubmit.setOnClickListener(this)
        binding.tvReportTo.text = loginData.reportingToUserName
        //binding.tvDate.text = CommonMethods.getCurrentDate()
        binding.tvFromDate.text = CommonMethods.getCurrentDate()
        binding.tvToDate.text = CommonMethods.getCurrentDate()

//  -----------------------------------------------------
        binding.tvCompanyDate.setOnClickListener(this)
        binding.llSelectCompany.setOnClickListener(this)
        binding.llSelectBranch.setOnClickListener(this)
        binding.llSelectDivision.setOnClickListener(this)

//        binding.llHeader.setOnClickListener {
//            toggleSectionVisibility(binding.llOptionalFields, binding.ivToggle)
//        }
        binding.tvCompanyDate.text = CommonMethods.getCurrentDate()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                executeAPIsAndSetupData()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
//----------------------------------------------------------
        /*if (isForUpdate || isFromApproval) {

            if (isForUpdate && !isFromApproval && (leaveApplicationData.leaveApprovalStatus == LEAVE_APPROVED_STATUS || leaveApplicationData.leaveApprovalStatus == LEAVE_REJECTED_STATUS)) {
                binding.llApprovalRemarks.visibility = View.VISIBLE
                binding.etLeaveApprovalRemarks.setText(leaveApplicationData.leaveApprovalRemarks)
                binding.etLeaveApprovalRemarks.isEnabled = false
            } else {
                if (isFromApproval) {
                    binding.llApprovalRemarks.visibility = View.VISIBLE
                    binding.etLeaveApprovalRemarks.isEnabled = true
                } else {
                    binding.llApprovalRemarks.visibility = View.GONE
                }
            }

            //binding.tvLeaveType.text = leaveApplicationData.leaveTypeName
            //binding.tvLeaveType.visibility = View.VISIBLE
            //binding.flLeaveType.visibility = View.GONE

            *//*binding.tvDate.text = leaveApplicationData.leaveApplicationDate
            binding.tvFromDate.text = leaveApplicationData.leaveFromDate
            binding.tvToDate.text = leaveApplicationData.leaveToDate
            binding.etReason.setText(leaveApplicationData.leaveReason)
            binding.cbHalfDay.isChecked = leaveApplicationData.isHalfDayLeave*//*

            Log.d("FROMDATEOTHER===>",""+leaveApplicationData.leaveFromDate)
            Log.d("TODATEOTHER===>",""+leaveApplicationData.leaveToDate)
            setWidgetsClickability(false)
        } else {
            callLeaveTypeList()
        }*/

        if (leaveApplicationId > 0) {
            callLiveDetailListApi()
        }else{
            callLeaveTypeList()
        }
    }

    private fun callLiveDetailListApi(){
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()

        val loginData = appDao.getLoginData()
        val expenseListReq = JsonObject()
        expenseListReq.addProperty("userId", loginData.userId)
        expenseListReq.addProperty("leaveApplicationId",leaveApplicationId )

        val expenseListCall =  WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getLeaveApplicationDetailList(expenseListReq)

        expenseListCall?.enqueue(object : Callback<List<LeaveDetailListResponse>> {
            override fun onResponse(
                call: Call<List<LeaveDetailListResponse>>,
                response: Response<List<LeaveDetailListResponse>>
            ) {
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            setupLeaveDetailsData(it[0], appRegistrationData)
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

            override fun onFailure(call: Call<List<LeaveDetailListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                Log.e("TAG", "onFailure: " + t.message.toString())
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

    private fun setupLeaveDetailsData(
        leaveDetails: LeaveDetailListResponse,
        appRegistrationData: AppRegistrationResponse
    ) {
        //binding.tvDate.text = leaveDetails.leaveApplicationDate
        binding.toolbar.imgEdit.visibility = if(leaveApplicationData.allowEdit) View.VISIBLE else View.GONE
        binding.toolbar.imgDelete.visibility = if(leaveApplicationData.allowDelete) View.VISIBLE else View.GONE


        binding.tvReportTo.text = leaveDetails.reportingToUserName
        binding.tvFromDate.text = leaveDetails.leaveFromDate
        binding.tvToDate.text = leaveDetails.leaveToDate
        binding.etReason.setText(leaveDetails.leaveReason)
        binding.cbHalfDay.isChecked = leaveDetails.isHalfDayLeave
        binding.tvLeaveType.text = leaveDetails.leaveTypeName

        binding.etReason.setText(leaveDetails.leaveReason)

        binding.tvSelectCompany.text = leaveDetails.companyName
        binding.llSelectCompany.isClickable = false

        binding.tvSelectBranch.text = leaveDetails.branchName
        binding.llSelectBranch.isClickable = false

        binding.tvSelectDivision.text = leaveDetails.divisionName
        binding.llSelectDivision.isClickable = false

        binding.tvCompanyDate.text = leaveDetails.leaveApplicationDate
        binding.tvCompanyDate.isClickable = false

        Log.d("TEXT===>",""+leaveDetails.categoryName)
        detail = leaveDetails.categoryName

        if (leaveApplicationId > 0) {
            binding.flCategory.visibility = View.GONE
            binding.tvCategory.visibility = View.VISIBLE
            binding.tvLeaveType.visibility = View.VISIBLE
            binding.flLeaveType.visibility = View.GONE
            binding.tvCategory.text = detail
            Log.d("TEXT===>",""+detail)
        }
        binding.tvLeaveBalance.visibility = View.GONE

        if (isForUpdate || isFromApproval) {

            if (isForUpdate && !isFromApproval && (leaveDetails.leaveApprovalStatus == LEAVE_APPROVED_STATUS || leaveDetails.leaveApprovalStatus == LEAVE_REJECTED_STATUS)) {
                binding.llApprovalRemarks.visibility = View.VISIBLE
                binding.etLeaveApprovalRemarks.setText(leaveDetails.leaveApprovalRemarks)
                binding.etLeaveApprovalRemarks.isEnabled = false
            } else {
                if (isFromApproval) {
                    binding.llApprovalRemarks.visibility = View.VISIBLE
                    binding.etLeaveApprovalRemarks.isEnabled = true
                } else {
                    binding.llApprovalRemarks.visibility = View.GONE
                }
            }
            val leaveTypeId = leaveDetails.dDMLeaveTypeId
            callLeaveTypeList(leaveTypeId)

            Log.d("FROMDATEOTHER===>",""+leaveDetails.leaveFromDate)
            Log.d("TODATEOTHER===>",""+leaveDetails.leaveToDate)
            setWidgetsClickability(false)
        } else {
            callLeaveTypeList(leaveDetails.dDMLeaveTypeId)
        }
    }

    private fun toggleSectionVisibility(view: View, toggleIcon: ImageView) {
        if (isExpanded) {
            CommonMethods.collapse(view)
            toggleIcon.setImageResource(R.drawable.ic_add_circle)
        } else {
            CommonMethods.expand(view)
            toggleIcon.setImageResource(R.drawable.ic_remove_circle)
        }
        isExpanded = !isExpanded
    }

    private fun setWidgetsClickability(flag: Boolean) {
        binding.toolbar.tvHeader.text =
            if (flag) getString(R.string.leave_application_update) else getString(R.string.leave_application)
        binding.tvFromDate.isEnabled = flag
        binding.tvToDate.isEnabled = flag
        binding.cbHalfDay.isEnabled = flag
        binding.etReason.isEnabled = flag
        binding.spLeaveType.isEnabled = flag
        binding.tvSubmit.visibility = if (flag) View.VISIBLE else View.GONE
       // binding.llHeader.isClickable = flag
        if (flag) {
            binding.toolbar.imgEdit.visibility = View.GONE
            binding.tvLeaveType.visibility = View.GONE
            binding.flLeaveType.visibility = View.VISIBLE
            binding.tvLeaveBalance.visibility = View.VISIBLE
            //callLeaveTypeList()
        }
    }

//    private fun callLeaveTypeList() {
//        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
//            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
//            return
//        }
//        CommonMethods.showLoading(mActivity)
//
//        val appRegistrationData = appDao.getAppRegistration()
//        val loginData = appDao.getLoginData()
//
//        val expenseTypeReq = JsonObject()
//        expenseTypeReq.addProperty("dropdownName","leavetype")
//
//        CommonMethods.getBatteryPercentage(mActivity)
//    }

    private fun callLeaveTypeList(leaveTypeId : Int = 0) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseTypeReq = JsonObject()
        expenseTypeReq.addProperty("UserId", loginData.userId)

        CommonMethods.getBatteryPercentage(mActivity)

        val expenseTypeCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getLeaveTypeList(expenseTypeReq)

        expenseTypeCall?.enqueue(object : Callback<List<LeaveTypeListResponse>> {
            override fun onResponse(
                call: Call<List<LeaveTypeListResponse>>,
                response: Response<List<LeaveTypeListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            leaveTypeList.clear()
                            leaveTypeList.add(
                                LeaveTypeListResponse(
                                    0,
                                    0,
                                    "Select Leave Type",
                                    0.0
                                )
                            )
                            leaveTypeList.addAll(it)
                            setupLeaveTypeSpinner(leaveTypeId)
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<LeaveTypeListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
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


    private fun setupLeaveTypeSpinner(leaveTypeId : Int) {
        val adapter = LeaveTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            leaveTypeList
        )
        binding.spLeaveType.adapter = adapter
        Log.e("TAG", "setupLeaveTypeSpinner:MY LEAVE TYPE ::  "+leaveTypeId.toString() )
        if (isForUpdate) {
            val indexToSelect =
                leaveTypeList.indexOfFirst { it.categoryMasterId == leaveTypeId }
            Log.e("TAG", "setupExpenseTypeSpinner: " + indexToSelect)
            binding.spLeaveType.setSelection(indexToSelect)
            selectedLeaveType = leaveTypeList[indexToSelect]
            binding.tvLeaveBalance.text = selectedLeaveType.leaveBalance.toString()
        } else {
            binding.spLeaveType.setSelection(0)
            selectedLeaveType = leaveTypeList[0]
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack ->{
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }

            R.id.tvCompanyDate -> {
                val selectedCalendar = if (binding.tvCompanyDate.text.toString()
                        .isEmpty()
                ) null else CommonMethods.dateStringToCalendar(binding.tvCompanyDate.text.toString())
                openDatePicker(
                    selectedCalendar
                ) { selectedDate ->
                    if (binding.tvCompanyDate.text.toString().isNotEmpty()) {
                        binding.tvCompanyDate.text = selectedDate
                    } else {
                        binding.tvCompanyDate.text = selectedDate
                    }
                }
            }


            R.id.llSelectCompany -> {
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

            R.id.llSelectBranch -> {
                if (selectedCompany == null || selectedCompany?.companyMasterId == 0) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.please_select_company)
                    )
                    return
                }
                if (branchMasterList.size > 0) {
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
                }
            }

            R.id.llSelectDivision -> {
                if (selectedBranch == null || selectedBranch?.branchMasterId == 0) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.please_select_branch)
                    )
                    return
                }
                if (divisionMasterList.size > 0) {
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
                        getString(R.string.branch_list_not_found)
                    )
                }
            }

            R.id.tvFromDate ->
                openFromTODatePickerDialog(true)
            R.id.tvToDate ->
                openFromTODatePickerDialog(false)
            R.id.tvSubmit -> {
                if (binding.spLeaveType.selectedItemPosition == 0) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.please_select_leave_type)
                    )
                    return
                }
                if (!CommonMethods.isDateValid(
                        binding.tvFromDate.text.toString(),
                        binding.tvToDate.text.toString()
                    )
                ) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.from_date_to_date_validation)
                    )
                    return
                }
                if (binding.cbHalfDay.isChecked && binding.tvFromDate.text.toString() != binding.tvToDate.text.toString()) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.half_day_leave_validation)
                    )
                    return
                }
                if (binding.etReason.text.toString().isEmpty()) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.write_leave_reason)
                    )
                    return
                }
                callAddLeaveApplicationApi()
            }
            R.id.imgDelete -> {
                CommonMethods.showAlertDialog(
                    mActivity,
                    getString(R.string.leave_application),
                    getString(R.string.delete_msg),
                    object : PositiveButtonListener {
                        override fun okClickListener() {
                            callDeleteLeaveApi()
                        }

                    },
                    isCancelVisibility = true,
                    positiveButtonText = getString(R.string.yes),
                    negativeButtonText = getString(R.string.no)
                )
            }
            R.id.imgEdit -> {
//                CommonMethods.showAlertDialog(
//                    mActivity,
//                    getString(R.string.edit_order_entry),
//                    getString(R.string.are_you_sure_you_want_to_edit_order_entry),
//                    object : PositiveButtonListener {
//                        override fun okClickListener() {
//                            binding.toolbar.imgEdit.visibility = View.GONE
//                            formViewMode(false)
//                        }
//                    },
//                    positiveButtonText = getString(R.string.yes),
//                    negativeButtonText = getString(R.string.no)
//                )
                setWidgetsClickability(true)
            }
            R.id.tvAccept -> {
                if (binding.etLeaveApprovalRemarks.text.toString().isEmpty()) {
                    binding.etLeaveApprovalRemarks.requestFocus()
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.enter_approval_remarks)
                    )
                    return
                }
                callApproveRejectLeaveApi(
                    true,
                    binding.etLeaveApprovalRemarks.text.toString(),
                    leaveApplicationData.leaveApplicationId.toString()
                )
                //showRemarksDialog(true, leaveApplicationData.leaveApplicationId.toString())
            }
            R.id.tvReject -> {
                if (binding.etLeaveApprovalRemarks.text.toString().isEmpty()) {
                    binding.etLeaveApprovalRemarks.requestFocus()
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.enter_approval_remarks)
                    )
                    return
                }
                callApproveRejectLeaveApi(
                    false,
                    binding.etLeaveApprovalRemarks.text.toString(),
                    leaveApplicationData.leaveApplicationId.toString()
                )
                //showRemarksDialog(false, leaveApplicationData.leaveApplicationId.toString())
            }
        }
    }

    private fun callDivisionListApi() {
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
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and $FORM_ID_LEAVE_APPLICATION_ENTRY"
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
                            if (isCompanyChange && it.size == 1) {
                                selectedDivision = DivisionMasterResponse(
                                    divisionMasterId = divisionMasterList[1].divisionMasterId,
                                    divisionName = divisionMasterList[1].divisionName
                                )
                                binding.tvSelectDivision.text = selectedDivision?.divisionName ?: ""
                                callCategoryListApi()
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
        if (selectedDivision == null || selectedDivision?.divisionMasterId == 0) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.please_select_branch)
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val jsonReq = JsonObject()
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and DivisionMasterid=${selectedDivision?.divisionMasterId} and $FORM_ID_LEAVE_APPLICATION_ENTRY"
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

    private fun resetSelection(
        resetBranch: Boolean,
        resetDivision: Boolean,
        resetCategory: Boolean
    ) {
        if (resetBranch) {
            selectedBranch = null
            binding.tvSelectBranch.hint = mActivity.getString(R.string.select_branch)
            binding.tvSelectBranch.text = ""
            branchMasterList.clear()
        }
        if (resetDivision) {
            selectedDivision = null
            binding.tvSelectDivision.hint = mActivity.getString(R.string.select_division)
            binding.tvSelectDivision.text = ""
            //divisionMasterList.clear()
        }
        if (resetCategory) {
            selectedCategory = null
            binding.tvCategory.hint = mActivity.getString(R.string.select_category)
            binding.tvCategory.text = ""
            categoryList.clear()
            categoryList.add(CategoryMasterResponse(categoryName = "Select Category"))
            setupCategorySpinner()
        }
    }

    private fun setupCategorySpinner() {
        val adapter = LeaveTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            categoryList,
            this,
        )
        binding.spCategory.adapter = adapter
        if (isCompanyChange && categoryList.size == 2) {
            selectedCategory = categoryList[1]
            binding.spCategory.setSelection(1)
            Log.d("LIST","true")
            categoryList[1]
        } else if (isCompanyChange) {
            binding.spCategory.setSelection(0)
        } else {
            val selectedCategoryIndex = categoryList.indexOfFirst { it.categoryMasterId == selectedCategory?.categoryMasterId }
            binding.spCategory.setSelection(selectedCategoryIndex)
        }
    }

    private suspend fun executeAPIsAndSetupData() {
        withContext(Dispatchers.IO) {
            try {
                val companyListDeferred = async { callCompanyListApi() }

                companyListDeferred.await()

            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("TAG", "executeAPIsAndSetupData: " + e.message.toString())
            }
        }
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
        objReq.addProperty("ParameterString", FORM_ID_LEAVE_APPLICATION_ENTRY)

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
                                binding.tvSelectCompany.text = selectedCompany?.companyName ?: ""
                                isCompanyChange = true
                            }
                            callBranchListApi()
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

    private fun callBranchListApi() {
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
            "CompanyMasterId=${selectedCompany?.companyMasterId} and $FORM_ID_LEAVE_APPLICATION_ENTRY"
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
                            if (isCompanyChange && it.size == 1) {
                                selectedBranch = BranchMasterResponse(
                                    branchMasterId = branchMasterList[1].branchMasterId,
                                    branchName = branchMasterList[1].branchName
                                )
                                binding.tvSelectBranch.text = selectedBranch?.branchName ?: ""
                                callDivisionListApi()
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

    private fun openDatePicker(
        previousSelectedDate: Calendar? = null,
        onDateSelected: (String) -> Unit,
    ) {
        val calendar = Calendar.getInstance()

        previousSelectedDate?.let {
            calendar.timeInMillis = it.timeInMillis
        }

        val datePickerDialog = DatePickerDialog(
            mActivity,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val dateString = CommonMethods.dateFormat.format(calendar.time)
                onDateSelected(dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate =
            CommonMethods.dateStringToCalendar(CommonMethods.getCurrentDate()).timeInMillis
        datePickerDialog.show()
    }


    private fun callDeleteLeaveApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val leaveDeleteReq = JsonObject()
        leaveDeleteReq.addProperty("LeaveApplicationId", leaveApplicationData.leaveApplicationId)
        leaveDeleteReq.addProperty("UserId", loginData.userId)

        Log.e("TAG", "callAddLeaveApplicationApi: " + leaveDeleteReq.toString())
        CommonMethods.getBatteryPercentage(mActivity)

        val deleteLeaveCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.deleteLeaveApplication(leaveDeleteReq)

        deleteLeaveCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.leave_application),
                                it.returnMessage
                                    ?: getString(R.string.leave_application_delete_success),
                                object : PositiveButtonListener {
                                    override fun okClickListener() {
                                        AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, true)
                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                            mActivity.onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            mActivity.onBackPressed()
                                        }
                                    }
                                }, isCancelVisibility = false)
                        } else {
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.leave_application),
                                it.returnMessage ?: "",
                                object : PositiveButtonListener {
                                    override fun okClickListener() {

                                    }
                                }, isCancelVisibility = false)
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
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
                        null,
                        isCancelVisibility = false
                    )
                }
            }
        })
    }

    private fun callAddLeaveApplicationApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val leaveApplicationReq = JsonObject()
        leaveApplicationReq.addProperty(
            "LeaveApplicationId",
            if (isForUpdate) leaveApplicationData.leaveApplicationId else 0
        )//for update pass id
        leaveApplicationReq.addProperty("UserId", loginData.userId)
        //leaveApplicationReq.addProperty("UserName", loginData.userName)
        leaveApplicationReq.addProperty("LeaveApplicationDate", binding.tvCompanyDate.text.toString())
        //leaveApplicationReq.addProperty("LeaveCategoryMasterId", selectedLeaveType.categoryMasterId)
        //leaveApplicationReq.addProperty("LeaveCategoryName", selectedLeaveType.categoryName)
        leaveApplicationReq.addProperty("LeaveFromDate", binding.tvFromDate.text.toString())
        leaveApplicationReq.addProperty("LeaveToDate", binding.tvToDate.text.toString())
        leaveApplicationReq.addProperty("IsHalfDayLeave", binding.cbHalfDay.isChecked)
        leaveApplicationReq.addProperty("LeaveReason", binding.etReason.text.toString())

        leaveApplicationReq.addProperty("ddmLeaveTypeId",selectedLeaveType.categoryMasterId)
        leaveApplicationReq.addProperty("leaveTypeName",selectedLeaveType.categoryName)

        leaveApplicationReq.addProperty("CompanyMasterId", selectedCompany?.companyMasterId)
        leaveApplicationReq.addProperty("BranchMasterId", selectedBranch?.branchMasterId)
        leaveApplicationReq.addProperty("DivisionMasterId",  selectedDivision?.divisionMasterId)
        leaveApplicationReq.addProperty("categoryMasterId",  selectedCategory?.categoryMasterId)

        //leaveApplicationReq.addProperty("ReportingToUserId", loginData.reportingToUserId)
        //leaveApplicationReq.addProperty("ReportingToUserName", loginData.reportingToUserName)

        Log.e("TAG", "callAddLeaveApplicationApi: " + leaveApplicationReq.toString())
        CommonMethods.getBatteryPercentage(mActivity)

        val expenseTypeCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.leaveApplicationAddUpdate(leaveApplicationReq)

        expenseTypeCall?.enqueue(object : Callback<LeaveApplicationResponse> {
            override fun onResponse(
                call: Call<LeaveApplicationResponse>,
                response: Response<LeaveApplicationResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isSuccess) {
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.leave_application),
                                it.returnMessage/*if (isForUpdate) getString(R.string.leave_application_update_msg) else getString(
                                    R.string.leave_application_add_msg
                                )*/,
                                object : PositiveButtonListener {
                                    override fun okClickListener() {
                                        AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, true)
                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                            mActivity.onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            mActivity.onBackPressed()
                                        }
                                    }

                                },isCancelVisibility = false)
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<LeaveApplicationResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
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

    override fun onDateSelect(date: String) {
        //binding.tvDate.text = date
    }


    private fun callApproveRejectLeaveApi(
        isLeaveApprove: Boolean,
        remarks: String,
        leaveApplicationId: String
    ) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseListReq = JsonObject()
        expenseListReq.addProperty("UserId", loginData.userId)
        expenseListReq.addProperty("LeaveApplicationId", leaveApplicationId.toInt())
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
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.leave_approval),
                                it.returnMessage/*if (isLeaveApprove) getString(R.string.leave_approve_msg) else getString(
                                    R.string.leave_reject_msg
                                )*/,
                                isCancelVisibility = false,
                                okListener = object : PositiveButtonListener {
                                    override fun okClickListener() {
                                        AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, true)
                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                            mActivity.onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            mActivity.onBackPressed()
                                        }
                                    }
                                })
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
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
                        null,
                        isCancelVisibility = false
                    )
                }
            }
        })

    }


    inner class LeaveTypeAdapter(
        private val context: Context,
        private val spinnerLayout: Int,
        private var leaveTypeListResponse: ArrayList<LeaveTypeListResponse>,
    ) : ArrayAdapter<LeaveTypeListResponse>(context, spinnerLayout, leaveTypeListResponse) {
        private val mContext: Context = context

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val leaveTypeData = leaveTypeListResponse[position]
            name.text = leaveTypeData.categoryName
            selectedLeaveType = leaveTypeData
            binding.tvLeaveBalance.text = leaveTypeData.leaveBalance.toString()
            return listItem
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val leaveTypeData = leaveTypeListResponse[position]
            name.text = leaveTypeData.categoryName
            return listItem
        }
    }
    private fun openFromTODatePickerDialog(isFromDate: Boolean): String {
        val newDate = Calendar.getInstance()
        var dateString = ""
        mActivity.hideKeyboard()
        val datePickerDialog = DatePickerDialog(
            mActivity, { view, year, monthOfYear, dayOfMonth ->
                newDate.set(year, monthOfYear, dayOfMonth)
                dateString = CommonMethods.dateFormat.format(newDate.time) ?: ""
                if (isFromDate) {
                    binding.tvFromDate.text = dateString
                } else {
                    binding.tvToDate.text = dateString
                }

            }, newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(
                Calendar.DAY_OF_MONTH
            )
        )
        //datePickerDialog.datePicker.minDate = newDate.timeInMillis
        datePickerDialog.show()
        return dateString
    }

    override fun onTypeSelect(typeData: CategoryMasterResponse) {
        selectedCategory = typeData
    }

    override fun divisionSelect(dropDownData: DivisionMasterResponse) {
        selectedDivision = dropDownData
        binding.tvSelectDivision.text = selectedDivision?.divisionName ?: ""
        resetSelection(
            resetBranch = false,
            resetDivision = false,
            resetCategory = true
        )
        if ((selectedDivision?.divisionMasterId ?: 0) > 0) {
            callCategoryListApi()
        }
    }

    override fun companySelect(dropDownData: CompanyMasterResponse) {
        selectedCompany = dropDownData
        isCompanyChange = true
        binding.tvSelectCompany.text = selectedCompany?.companyName ?: ""
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
        binding.tvSelectBranch.text = selectedBranch?.branchName ?: ""
        resetSelection(
            resetBranch = false,
            resetDivision = true,
            resetCategory = true
        )
        if ((selectedBranch?.branchMasterId ?: 0) > 0) {
            callDivisionListApi()
        }
    }

}