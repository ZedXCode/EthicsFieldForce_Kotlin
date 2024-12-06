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
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAddLeaveBinding
import ethicstechno.com.fieldforce.listener.DatePickerListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveApplicationListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveApplicationResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.hideKeyboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddLeaveApplicationFragment : HomeBaseFragment(), View.OnClickListener, DatePickerListener {

    lateinit var binding: FragmentAddLeaveBinding
    var leaveTypeList: ArrayList<LeaveTypeListResponse> = arrayListOf()
    lateinit var selectedLeaveType: LeaveTypeListResponse
    var isForUpdate = false
    private var isFromApproval = false
    var leaveApplicationData = LeaveApplicationListResponse()

    companion object {
        fun newInstance(
            isForUpdate: Boolean,
            expenseDataForUpdate: LeaveApplicationListResponse = LeaveApplicationListResponse(),
            isApproval: Boolean
        ): AddLeaveApplicationFragment {
            val args = Bundle()
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
        binding.toolbar.imgBack.visibility = View.VISIBLE
        if (isForUpdate) {
            if (leaveApplicationData.leaveApprovalStatus == LEAVE_RAISED_STATUS) {
                binding.toolbar.imgDelete.visibility = if (isForUpdate) View.VISIBLE else View.GONE
                binding.toolbar.imgEdit.visibility = if (isForUpdate) View.VISIBLE else View.GONE
            } else {
                binding.toolbar.imgDelete.visibility = View.GONE
                binding.toolbar.imgEdit.visibility = View.GONE
            }
            binding.tvLeaveBalance.visibility = View.GONE
        } else {
            binding.toolbar.imgDelete.visibility = View.GONE
            binding.toolbar.imgEdit.visibility = View.GONE
        }

        if (isFromApproval) {
            binding.tvSubmit.visibility = View.GONE
            binding.llAcceptReject.visibility = View.VISIBLE
            binding.toolbar.imgDelete.visibility = View.GONE
            binding.toolbar.imgEdit.visibility = View.GONE
            binding.tvAccept.setOnClickListener(this)
            binding.tvReject.setOnClickListener(this)
        }


        binding.toolbar.tvHeader.text = getString(R.string.leave_application)
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.imgDelete.setOnClickListener(this)
        binding.toolbar.imgEdit.setOnClickListener(this)
        binding.tvFromDate.setOnClickListener(this)
        binding.tvToDate.setOnClickListener(this)
        binding.tvSubmit.setOnClickListener(this)
        binding.tvReportTo.text = loginData.reportingToUserName
        binding.tvDate.text = CommonMethods.getCurrentDate()
        binding.tvFromDate.text = CommonMethods.getCurrentDate()
        binding.tvToDate.text = CommonMethods.getCurrentDate()

        if (isForUpdate || isFromApproval) {

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

            binding.tvLeaveType.text = leaveApplicationData.leaveCategoryName
            binding.tvLeaveType.visibility = View.VISIBLE
            binding.flLeaveType.visibility = View.GONE

            binding.tvDate.text = leaveApplicationData.leaveApplicationDate
            binding.tvFromDate.text = leaveApplicationData.leaveFromDate
            binding.tvToDate.text = leaveApplicationData.leaveToDate
            binding.etReason.setText(leaveApplicationData.leaveReason)
            binding.cbHalfDay.isChecked = leaveApplicationData.isHalfDayLeave
            setWidgetsClickability(false)
        } else {
            callLeaveTypeList()
        }
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
        if (flag) {
            binding.toolbar.imgEdit.visibility = View.GONE
            binding.tvLeaveType.visibility = View.GONE
            binding.flLeaveType.visibility = View.VISIBLE
            binding.tvLeaveBalance.visibility = View.VISIBLE
            callLeaveTypeList()
        }
    }

    private fun callLeaveTypeList() {
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
                            setupLeaveTypeSpinner()
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

    private fun setupLeaveTypeSpinner() {
        val adapter = LeaveTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            leaveTypeList
        )
        binding.spLeaveType.adapter = adapter
        if (isForUpdate) {
            val indexToSelect =
                leaveTypeList.indexOfFirst { it.categoryMasterId == leaveApplicationData.leaveCategoryMasterId }
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
        leaveApplicationReq.addProperty("UserName", loginData.userName)
        leaveApplicationReq.addProperty("LeaveApplicationDate", binding.tvDate.text.toString())
        leaveApplicationReq.addProperty("LeaveCategoryMasterId", selectedLeaveType.categoryMasterId)
        leaveApplicationReq.addProperty("LeaveCategoryName", selectedLeaveType.categoryName)
        leaveApplicationReq.addProperty("LeaveFromDate", binding.tvFromDate.text.toString())
        leaveApplicationReq.addProperty("LeaveToDate", binding.tvToDate.text.toString())
        leaveApplicationReq.addProperty("IsHalfDayLeave", binding.cbHalfDay.isChecked)
        leaveApplicationReq.addProperty("LeaveReason", binding.etReason.text.toString())
        leaveApplicationReq.addProperty("ReportingToUserId", loginData.reportingToUserId)
        leaveApplicationReq.addProperty("ReportingToUserName", loginData.reportingToUserName)

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
        binding.tvDate.text = date
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

}