package ethicstechno.com.fieldforce.ui.fragments.moreoption

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAddExpenceBinding
import ethicstechno.com.fieldforce.listener.DatePickerListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.*
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.Executors

class AddExpenseFragment : HomeBaseFragment(), DatePickerListener, View.OnClickListener,
    UserSearchDialogUtil.PlaceSearchDialogDetect {

    lateinit var binding: FragmentAddExpenceBinding
    var placeList: ArrayList<ExpenseCityListResponse> = arrayListOf()
    var expenseTypeList: ArrayList<ExpenseTypeListResponse> = arrayListOf()
    lateinit var selectedPlace: ExpenseCityListResponse
    var selectedExpenseType: ExpenseTypeListResponse = ExpenseTypeListResponse()
    var imageFile: File? = null
    var base64Image: String = ""
    var isForUpdate = false
    var isPreviewImage = false
    var isEditMode = false
    var isForDetails = false
    var expenseDataForUpdate: ExpenseListResponse = ExpenseListResponse()
    var isAttachmentRequired = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_expence, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(
            isForUpdate: Boolean,
            expenseDataForUpdate: ExpenseListResponse = ExpenseListResponse(),
            isForDetails: Boolean
        ): AddExpenseFragment {
            val args = Bundle()
            args.putBoolean(ARG_PARAM1, isForUpdate)
            args.putParcelable(ARG_PARAM2, expenseDataForUpdate)
            args.putBoolean(ARG_PARAM3, isForDetails)
            val fragment = AddExpenseFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            isForUpdate = it.getBoolean(ARG_PARAM1)
            expenseDataForUpdate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM2, ExpenseListResponse::class.java) ?: ExpenseListResponse()
            } else {
                it.getParcelable(ARG_PARAM2) ?: ExpenseListResponse()
            }
            isForDetails = it.getBoolean(ARG_PARAM3)
        }
        initView()
    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {
        mActivity.bottomHide()
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.imgEdit.setOnClickListener(this)
        binding.toolbar.imgDelete.setOnClickListener(this)
        binding.toolbar.tvHeader.text =
            if (isForUpdate) mActivity.getString(R.string.expense_details) else mActivity.getString(R.string.add_expense)

        binding.tvDate.text =
            if (isForUpdate) expenseDataForUpdate.expenseDate else CommonMethods.getCurrentDate()

        binding.cardImage.setOnClickListener(this)


        if (isForUpdate && !isForDetails) {
            if (expenseDataForUpdate.expenseStatusName == EXPENSE_RAISED) {
                binding.toolbar.imgDelete.visibility = if (isForUpdate) View.VISIBLE else View.GONE
                binding.toolbar.imgEdit.visibility = if (isForUpdate) View.VISIBLE else View.GONE
            } else {
                binding.toolbar.imgDelete.visibility = View.GONE
                binding.toolbar.imgEdit.visibility = View.GONE
            }
        } else {
            binding.toolbar.imgDelete.visibility = View.GONE
            binding.toolbar.imgEdit.visibility = View.GONE
        }

        if (isForDetails && isForUpdate) {
            binding.etApprovalAmount.setText(expenseDataForUpdate.expenseAmount.toString())
            binding.llApprovalAmount.visibility = View.VISIBLE
            binding.tvExpenseType.visibility = View.VISIBLE
            binding.flExpnseType.visibility = View.GONE
            binding.llAcceptReject.visibility = View.VISIBLE
            binding.llApprovalRemarks.visibility = View.VISIBLE
            binding.tvAccept.setOnClickListener(this)
            binding.tvReject.setOnClickListener(this)
        } else {
            binding.llApprovalRemarks.visibility = View.GONE
            binding.llApprovalAmount.visibility = View.GONE
            binding.tvExpenseType.visibility = View.GONE
            binding.flExpnseType.visibility = View.VISIBLE
            binding.llAcceptReject.visibility = View.GONE
        }
        if (isForDetails) {
            binding.tvAddExpense.visibility = View.GONE
        } else {
            binding.llSelectPlace.setOnClickListener(this)
            binding.tvDate.setOnClickListener(this)
            binding.tvAddExpense.setOnClickListener(this)
        }
        if (isForUpdate) {
            isPreviewImage = true
            setWidgetsClickability(false)
            /*if (!isForDetails) {
                binding.toolbar.imgEdit.visibility = View.VISIBLE
                binding.toolbar.imgDelete.visibility = View.VISIBLE
                binding.toolbar.imgEdit.setOnClickListener(this)
                binding.toolbar.imgDelete.setOnClickListener(this)
            }*/
            binding.tvExpenseType.text = expenseDataForUpdate.expenseTypeName
            binding.tvSelectPlace.text = expenseDataForUpdate.cityName
            binding.tvSelectControlMode.text = expenseDataForUpdate.controlModeName
            binding.tvSelectVehicalType.text = expenseDataForUpdate.vehicleTypeName

            binding.etRemarks.setText(expenseDataForUpdate.remarks)
            binding.tvEligibleAmount.text = expenseDataForUpdate.eligibleAmount.toString()
            binding.etExpenseAmount.setText(expenseDataForUpdate.expenseAmount.toString())
            binding.imgExpense.scaleType = ImageView.ScaleType.CENTER_CROP
            ImageUtils().loadImageUrl(
                mActivity,
                appDatabase.appDao()
                    .getAppRegistration().apiHostingServer + expenseDataForUpdate.documentPath,
                binding.imgExpense
            )
            binding.tvExpenseType.visibility = View.VISIBLE
            binding.flExpnseType.visibility = View.GONE
        } else {
            callCityListApi()
        }

    }

    private fun setWidgetsClickability(flag: Boolean) {
        binding.llSelectPlace.isEnabled = false
        binding.tvDate.isEnabled = false
        binding.etExpenseAmount.isEnabled = flag
        binding.etRemarks.isEnabled = flag
        binding.tvSelectControlMode.isEnabled = false
        binding.flExpnseType.isEnabled = false
        binding.spExpenseType.isEnabled = false
        binding.tvAddExpense.visibility = if (flag) View.VISIBLE else View.GONE
        if (flag) {
            isEditMode = true
            binding.toolbar.imgEdit.visibility = View.GONE
        }
    }

    private fun callCityListApi() {
        Log.e("TAG", "callCityListApi: ")
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val placeListReq = JsonObject()
        placeListReq.addProperty("StateId", "0")
        placeListReq.addProperty("CityId", "0")
        placeListReq.addProperty("UserId", loginData.userId)
        placeListReq.addProperty("ExpenseDate", binding.tvDate.text.toString())

        val placeListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getExpenseCityList(placeListReq)

        placeListCall?.enqueue(object : Callback<List<ExpenseCityListResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseCityListResponse>>,
                response: Response<List<ExpenseCityListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            placeList.clear()
                            placeList.addAll(it)
                            if (isForUpdate) {
                                selectedPlace = ExpenseCityListResponse(
                                    0,
                                    expenseDataForUpdate.cityId,
                                    expenseDataForUpdate.cityName,
                                    0,
                                    ""
                                )
                                binding.tvSelectPlace.text = selectedPlace.cityName
                                callGetExpenseTypeList(selectedPlace.cityId)
                            }
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        mActivity.getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<ExpenseCityListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }

        })

    }

    private fun callGetExpenseTypeList(placeId: Int) {
        Log.e("TAG", "callGetExpenseTypeList: ")
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseTypeReq = JsonObject()
        expenseTypeReq.addProperty("UserId", loginData.userId)
        expenseTypeReq.addProperty("CityId", placeId)
        expenseTypeReq.addProperty("ExpenseDate", binding.tvDate.text.toString())

        val expenseTypeCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getExpenseTypeList(expenseTypeReq)

        expenseTypeCall?.enqueue(object : Callback<List<ExpenseTypeListResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseTypeListResponse>>,
                response: Response<List<ExpenseTypeListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            expenseTypeList.clear()
                            expenseTypeList.add(ExpenseTypeListResponse())
                            expenseTypeList.addAll(it)
                            setupExpenseTypeSpinner(expenseTypeList)
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

            override fun onFailure(call: Call<List<ExpenseTypeListResponse>>, t: Throwable) {
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

    private fun setupExpenseTypeSpinner(expenseTypeList: ArrayList<ExpenseTypeListResponse>) {

        val adapter = ExpenseTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            expenseTypeList
        )
        binding.spExpenseType.adapter = adapter

        if (isForUpdate) {
            selectedExpenseType = ExpenseTypeListResponse(
                expenseDataForUpdate.expenseTypeId,
                expenseDataForUpdate.expenseTypeName,
                expenseDataForUpdate.controlModeId,
                expenseDataForUpdate.controlModeName,
                expenseDataForUpdate.vehicleTypeId,
                expenseDataForUpdate.vehicleTypeName,
                0,
                0,
                true,
                "",
                expenseDataForUpdate.mapKm,
                expenseDataForUpdate.actualKm,
                expenseDataForUpdate.eligibleAmount,
                expenseDataForUpdate.expenseAmount,
                expenseDataForUpdate.expenseDate
            )
            isAttachmentRequired = selectedExpenseType.isAttachmentRequired
            val pos = expenseTypeList.indexOfFirst { it.expenseTypeId ==  selectedExpenseType.expenseTypeId}
            if(pos != -1) {
                binding.spExpenseType.setSelection(pos)
            }
            binding.tvSelectControlMode.text = selectedExpenseType.controlModeName
            binding.tvSelectVehicalType.text = selectedExpenseType.vehicleTypeName
        } else {
            binding.spExpenseType.setSelection(0)
            selectedExpenseType = expenseTypeList[0]
            isAttachmentRequired = selectedExpenseType.isAttachmentRequired
            binding.spExpenseType.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    val expenseData = expenseTypeList[position]

                    // Display the selected item in the TextView
                    if (binding.spExpenseType.selectedItemPosition != 0) {
                        if (!isForUpdate && expenseData.expenseTypeId > 0) {
                            binding.tvDate.isEnabled = false
                            Log.e("TAG", "onItemSelected: " + expenseData.expenseTypeId)
                            selectedExpenseType = expenseData
                            isAttachmentRequired = selectedExpenseType.isAttachmentRequired
                            binding.tvSelectControlMode.text = expenseData.controlModeName
                            binding.tvSelectVehicalType.text = expenseData.vehicleTypeName

                            binding.tvEligibleAmount.text =
                                expenseData.eligibleAmount.toString()//it[0].eligibleAmount.toString()
                            binding.etExpenseAmount.setText(expenseData.expenseAmount.toString())
                            if (selectedExpenseType.controlModeId == CONTROL_TYPE_FIX_PER_KM) {
                                binding.tvEligibleAmountLabel.text =
                                    "Eligible Amount (MAP KM:${expenseData.mapKM})"
                                binding.tvExpenseAmountLabel.text =
                                    "Expense Amount (Actual KM:${expenseData.actualKM})"
                            } else {
                                binding.tvEligibleAmountLabel.text =
                                    mActivity.getString(R.string.eligible_amount)
                                binding.tvExpenseAmountLabel.text =
                                    mActivity.getString(R.string.expense_amount)
                            }

                            Log.e("TAG", "getView: ")
                            //callExpenseLimitApi()
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
        }


    }

    private fun callAddExpenseApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val addUpdateExpenseReq = JsonObject()
        addUpdateExpenseReq.addProperty(
            "ExpenseId",
            if (isForUpdate) expenseDataForUpdate.expenseId else 0
        )
        addUpdateExpenseReq.addProperty("UserId", loginData.userId)
        addUpdateExpenseReq.addProperty("ExpenseAmount", binding.etExpenseAmount.text.toString())
        addUpdateExpenseReq.addProperty("Remarks", binding.etRemarks.text.toString())
        addUpdateExpenseReq.addProperty("DocumentPath", base64Image.ifEmpty { "" })

        if (!isForUpdate) {
            addUpdateExpenseReq.addProperty(
                "CityId",
                if (isForUpdate) expenseDataForUpdate.cityId else selectedPlace.cityId
            )
            addUpdateExpenseReq.addProperty("ExpenseDate", binding.tvDate.text.toString())
            addUpdateExpenseReq.addProperty(
                "ExpenseTypeId",
                if (isForUpdate) expenseDataForUpdate.expenseTypeId else selectedExpenseType.expenseTypeId
            )
            addUpdateExpenseReq.addProperty(
                "ControlModeId",
                if (isForUpdate) expenseDataForUpdate.controlModeId else selectedExpenseType.controlModeId
            )
            addUpdateExpenseReq.addProperty(
                "VehicleTypeId",
                if (isForUpdate) expenseDataForUpdate.vehicleTypeId else selectedExpenseType.vehicleTypeId
            )
            addUpdateExpenseReq.addProperty(
                "EligibleAmount",
                binding.tvEligibleAmount.text.toString()
            )
            addUpdateExpenseReq.addProperty("CreateBy", loginData.userId)
            addUpdateExpenseReq.addProperty("TripId", "0")
            addUpdateExpenseReq.addProperty("StatusId", "1")
            if (selectedExpenseType.controlModeId == CONTROL_TYPE_FIX_PER_KM) {
                addUpdateExpenseReq.addProperty(
                    "MapKm",
                    if (isForUpdate) expenseDataForUpdate.mapKm else selectedExpenseType.mapKM
                )
                addUpdateExpenseReq.addProperty(
                    "ActualKm",
                    if (isForUpdate) expenseDataForUpdate.actualKm else selectedExpenseType.actualKM
                )
            }
        }


        print("MY REQ ::::::: " + addUpdateExpenseReq)
        Log.e("TAG", "callAddExpenseApi: ADD EXPENSE REQ :: " + addUpdateExpenseReq)

        val addUpdateExpenseCall = if (isForUpdate) {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.updateExpense(addUpdateExpenseReq)
        } else {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.addExpense(addUpdateExpenseReq)
        }


        addUpdateExpenseCall?.enqueue(object : Callback<AddExpenseResponse> {
            override fun onResponse(
                call: Call<AddExpenseResponse>,
                response: Response<AddExpenseResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        Log.e("TAG", "onResponse: ADD EXPENSE RESPONSE :: " + it.toString())
                        CommonMethods.showAlertDialog(
                            mActivity,
                            if (isForUpdate) mActivity.getString(R.string.update_expense) else mActivity.getString(R.string.add_expense_dialog_title),
                            it.returnMessage,
                            object : PositiveButtonListener {
                                override fun okClickListener() {
                                    if (it.success) {
                                        AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, true)
                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                            mActivity.onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            mActivity.onBackPressed()
                                        }
                                    }
                                }
                            }, isCancelVisibility = false)
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        mActivity.getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<AddExpenseResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }

        })

    }

    override fun onDateSelect(date: String) {
        binding.tvSelectPlace.text = ""
        selectedPlace = ExpenseCityListResponse();
        binding.tvDate.text = date
        callCityListApi()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }
            R.id.llSelectPlace -> {
                if (placeList.size > 0 && selectedExpenseType.expenseTypeId == 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_PLACE,
                        placeList = placeList,
                        placeDialogInterfaceDetect = this as UserSearchDialogUtil.PlaceSearchDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                }
            }
            R.id.tvDate -> {
                CommonMethods.openDatePickerDialog(this, mActivity)
            }
            R.id.cardImage -> {
                if (isPreviewImage) {
                    Log.e("TAG", "onClick: preview iamge")
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDatabase.appDao()
                            .getAppRegistration().apiHostingServer + expenseDataForUpdate.documentPath
                    )
                } else {
                    askCameraGalleryPermission()
                }
            }
            R.id.tvAddExpense -> {
                addExpenseValidation()
            }
            R.id.tvAccept -> {
                if (binding.etExpenseApprovalRemarks.text.toString().isEmpty()) {
                    binding.etExpenseApprovalRemarks.requestFocus()
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.enter_approval_remarks)
                    )
                    return
                }
                callApproveRejectExpense(
                    true,
                    binding.etExpenseApprovalRemarks.text.toString().trim(),
                    expenseDataForUpdate.expenseId,
                    binding.etApprovalAmount.text.toString().trim().toDouble()
                )
                //showRemarksDialog(true)
            }
            R.id.tvReject -> {
                if (binding.etExpenseApprovalRemarks.text.toString().isEmpty()) {
                    binding.etExpenseApprovalRemarks.requestFocus()
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.enter_approval_remarks)
                    )
                    return
                }
                callApproveRejectExpense(
                    false,
                    binding.etExpenseApprovalRemarks.text.toString().trim(),
                    expenseDataForUpdate.expenseId,
                    binding.etApprovalAmount.text.toString().trim().toDouble()
                )
                //showRemarksDialog(false)
            }
            R.id.imgEdit -> {
                isPreviewImage = false
                if (expenseDataForUpdate.expenseStatusName == STATUS_APPROVED) {
                    binding.toolbar.tvHeader.text = mActivity.getString(R.string.update_expense)
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.update_expense),
                        mActivity.getString(R.string.approved_expense_msg),
                        isCancelVisibility = false,
                        okListener = null
                    )
                } else {
                    binding.toolbar.tvHeader.text = mActivity.getString(R.string.update_expense)
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.update_expense),
                        mActivity.getString(R.string.edit_msg),
                        positiveButtonText = mActivity.getString(R.string.yes),
                        negativeButtonText = mActivity.getString(R.string.no),
                        okListener = object : PositiveButtonListener {
                            override fun okClickListener() {
                                setWidgetsClickability(true)
                                CommonMethods.showToastMessage(
                                    mActivity,
                                    mActivity.getString(R.string.edit_this_expense)
                                )
                            }
                        }
                    )
                }
            }
            R.id.imgDelete -> {
                showRemarksDialog()
            }
        }
    }

    private fun addExpenseValidation() {
        if (binding.tvSelectPlace.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.place_validation))
            return
        }
        if (binding.spExpenseType.selectedItemPosition == 0) {
            CommonMethods.run { showToastMessage(mActivity, getString(R.string.expense_type_msg)) }
            return
        }
        if (binding.etExpenseAmount.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.expense_amount_validation))
            return
        }
        if (!isForUpdate) {
            if (isAttachmentRequired && base64Image.isEmpty()) {
                CommonMethods.showToastMessage(
                    mActivity,
                    getString(R.string.upload_expense_validation)
                )
                return
            }
        }
        callAddExpenseApi()
    }

    private fun showRemarksDialog() {
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
                CommonMethods.showToastMessage(mActivity, getString(R.string.enter_remark))
                return@setOnClickListener
            }
            companyDialog.dismiss()
            callDeleteExpense(etRemarks.text.toString().trim())
        }

        val window = companyDialog.window
        window!!.setLayout(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.WRAP_CONTENT
        )
        companyDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        companyDialog.show()
    }

    private fun callDeleteExpense(remarks: String) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseDeleteReq = JsonObject()
        expenseDeleteReq.addProperty("UserId", loginData.userId)
        expenseDeleteReq.addProperty("ExpenseId", expenseDataForUpdate.expenseId)
        expenseDeleteReq.addProperty("Remarks", remarks)

        Log.e("TAG", "callAddLeaveApplicationApi: " + expenseDeleteReq.toString())
        CommonMethods.getBatteryPercentage(mActivity)

        val deleteExpenseCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.expenseDelete(expenseDeleteReq)

        deleteExpenseCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.delete_expense),
                                it.returnMessage
                                    ?: getString(R.string.expense_delete_success),
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
                                getString(R.string.expense_details),
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

    private fun callApproveRejectExpense(
        isExpenseApprove: Boolean,
        remarks: String,
        expenseId: Int,
        approvalAmount: Double
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
        expenseListReq.addProperty("ExpenseId", expenseId)
        expenseListReq.addProperty("ApprovedAmount", approvalAmount)
        expenseListReq.addProperty("Remarks", remarks)
        val leaveApprovalCall = if (isExpenseApprove) {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.expenseApprove(expenseListReq)
        } else {
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
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.expense_approval),
                                it.returnMessage/*if (isExpenseApprove) getString(R.string.expense_approve_msg) else getString(
                                    R.string.expense_reject_msg
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


    private fun askCameraGalleryPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                openAlbum()
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                openAlbum()
            }
        } else {
            val arrListOffPermission = arrayListOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOffPermission) {
                openAlbum()
            }
        }

    }

    private fun openAlbum() {
        AlbumUtility(mActivity, true).openAlbumAndHandleImageSelection(
            onImageSelected = {
                imageFile = it

                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    // This code runs on a background thread
                    val modifiedImageFile: File = CommonMethods.addDateAndTimeToFile(
                        it,
                        CommonMethods.createImageFile(mActivity)!!
                    )

                    if (modifiedImageFile != null) {

                        // Simulate a time-consuming task
                        Thread.sleep(1000)

                        // Update the UI on the main thread using runOnUiThread
                        handler.post {
                            Glide.with(mActivity)
                                .load(modifiedImageFile)
                                .into(binding.imgExpense)
                            base64Image =
                                CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                    .toString()
                            binding.imgExpense.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    }
                }
            },
            onError = {
                CommonMethods.showToastMessage(mActivity, it)
            }
        )

    }

    override fun placeSelect(placeData: ExpenseCityListResponse) {
        Log.e("TAG", "userSelect: " + placeData.cityName)
        binding.tvSelectPlace.text = placeData.cityName
        selectedPlace = placeData
        binding.tvDate.isEnabled = false
        callGetExpenseTypeList(selectedPlace.cityId)
    }

    inner class ExpenseTypeAdapter(
        private val context: Context, private val spinnerLayout: Int,
        private var expenseTypeList: ArrayList<ExpenseTypeListResponse>,
    ) :
        ArrayAdapter<ExpenseTypeListResponse>(context, spinnerLayout, expenseTypeList) {
        private val mContext: Context = context

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val expenseData = expenseTypeList[position]
            name.text = expenseData.expenseTypeName
            Log.e("TAG", "getView: " + name.text.toString())
            /*if(binding.spExpenseType.selectedItemPosition != 0) {
                if (!isForUpdate && expenseData.expenseTypeId > 0) {
                    selectedExpenseType = expenseData
                    binding.tvSelectControlMode.text = expenseData.controlModeName
                    binding.tvSelectVehicalType.text = expenseData.vehicleTypeName

                    binding.tvEligibleAmount.text = expenseData.eligibleAmount.toString()//it[0].eligibleAmount.toString()
                    binding.etExpenseAmount.setText(expenseData.expenseAmount.toString())
                    if (selectedExpenseType.controlModeId == CONTROL_TYPE_FIX_PER_KM) {
                        binding.tvEligibleAmountLabel.text =
                            "Eligible Amount (MAP KM:${expenseData.mapKM})"
                        binding.tvExpenseAmountLabel.text =
                            "Expense Amount (Actual KM:${expenseData.actualKM})"
                    } else {
                        binding.tvEligibleAmountLabel.text =
                            getString(R.string.eligible_amount)
                        binding.tvExpenseAmountLabel.text =
                            getString(R.string.expense_amount)
                    }

                    Log.e("TAG", "getView: ")
                    //callExpenseLimitApi()
                }
            }*/
            return listItem
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val currentSupervisor = expenseTypeList[position]
            name.text = currentSupervisor.expenseTypeName
            return listItem
        }


    }


}