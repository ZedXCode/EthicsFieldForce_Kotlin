package ethicstechno.com.fieldforce.ui.fragments.reports

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentPaymentFollowUpBinding
import ethicstechno.com.fieldforce.databinding.ItemPaymentFollowupBinding
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.reports.PaymentFollowUpResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.hideKeyboard
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class PaymentFollowUpFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentPaymentFollowUpBinding
    var paymentFollowUpList: ArrayList<PaymentFollowUpResponse> = arrayListOf()
    private lateinit var reportAdapter: PaymentFollowUpAdapter
    var dashboardDrillResponse = DashboardDrillResponse()
    private var totalAmount = 0.0
    private var remainingAmount = 0.0

    companion object {
        fun newInstance(
            paymentFollowUpData: DashboardDrillResponse
        ): PaymentFollowUpFragment {
            val args = Bundle()
            args.putParcelable(ARG_PARAM1, paymentFollowUpData)
            val fragment = PaymentFollowUpFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_payment_follow_up, container, false)
        return binding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            dashboardDrillResponse = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM1, DashboardDrillResponse::class.java)
                    ?: DashboardDrillResponse()
            } else {
                it.getParcelable(ARG_PARAM1) ?: DashboardDrillResponse()
            }

        }
        initView()
    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            mActivity.bottomHide()
        }
    }


    private fun initView() {
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.tvHeader.text = getString(R.string.payment_folloup)

        binding.tvColumn1.text = dashboardDrillResponse.column1
        binding.tvColumn2.text = dashboardDrillResponse.column2
        binding.tvValue1.text = dashboardDrillResponse.value1
        binding.tvValue2.text = dashboardDrillResponse.value2

        binding.tvDate1.setOnClickListener(this)
        binding.tvDate2.setOnClickListener(this)
        binding.tvDate3.setOnClickListener(this)
        binding.tvDate4.setOnClickListener(this)

        // Set TextChangedListener to all EditTexts
        totalAmount = dashboardDrillResponse.value1.toDouble()
        binding.etAmount1.setText(totalAmount.toString())
        binding.tvDate1.text = CommonMethods.getCurrentDate()

        binding.tvSubmit.setOnClickListener(this)

        binding.etAmount1.addTextChangedListener(
            createTextWatcher(
                binding.etAmount1,
                totalAmount,
                binding.tvDate1
            )
        )
        binding.etAmount2.addTextChangedListener(
            createTextWatcher(
                binding.etAmount2,
                totalAmount,
                binding.tvDate2
            )
        )
        binding.etAmount3.addTextChangedListener(
            createTextWatcher(
                binding.etAmount3,
                totalAmount,
                binding.tvDate3
            )
        )
        binding.etAmount4.addTextChangedListener(
            createTextWatcher(
                binding.etAmount4,
                totalAmount,
                binding.tvDate4
            )
        )

        setOnClickListenerForEditText(binding.etAmount1)
        setOnClickListenerForEditText(binding.etAmount2)
        setOnClickListenerForEditText(binding.etAmount3)
        setOnClickListenerForEditText(binding.etAmount4)
        callPaymentFollowUpListApi()
    }


    private fun setOnClickListenerForEditText(editText: EditText) {
        editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            val currentAmount = getCurrentAmount
            remainingAmount = totalAmount - currentAmount
            if (editText.text.toString().isEmpty()) {
                if (hasFocus) {
                    if (remainingAmount == 0.0) {
                        editText.setText("")
                    } else {
                        editText.setText(remainingAmount.toString())
                    }
                }
            }
        }
    }

    private fun createTextWatcher(
        editText: EditText,
        currentTotalAmount: Double,
        textView: TextView
    ): TextWatcher {

        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTotal()
            }

            override fun afterTextChanged(s: Editable?) {}

            private fun updateTotal() {
                val values = listOf(
                    binding.etAmount1.text.toString().toDoubleOrNull() ?: 0.0,
                    binding.etAmount2.text.toString().toDoubleOrNull() ?: 0.0,
                    binding.etAmount3.text.toString().toDoubleOrNull() ?: 0.0,
                    binding.etAmount4.text.toString().toDoubleOrNull() ?: 0.0
                )

                val sum = values.sum()
                if (sum > currentTotalAmount) {
                    if (textView.text.toString().isEmpty()) {
                        showToastMessage(mActivity, getString(R.string.select_date_validation))
                        editText.setText("")
                    } else {
                        editText.setText("0")
                    }
                }
            }
        }
    }

    val getCurrentAmount: Double
        get() {
            val amount1 = binding.etAmount1.text.toString().toDoubleOrNull() ?: 0.0
            val amount2 = binding.etAmount2.text.toString().toDoubleOrNull() ?: 0.0
            val amount3 = binding.etAmount3.text.toString().toDoubleOrNull() ?: 0.0
            val amount4 = binding.etAmount4.text.toString().toDoubleOrNull() ?: 0.0
            return amount1 + amount2 + amount3 + amount4
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
            R.id.tvDate1 -> {
                openDatePickerDialog(1, binding.tvDate1)
            }
            R.id.tvDate2 -> {
                openDatePickerDialog(2, binding.tvDate2)
            }
            R.id.tvDate3 -> {
                openDatePickerDialog(3, binding.tvDate3)
            }
            R.id.tvDate4 -> {
                openDatePickerDialog(4, binding.tvDate4)
            }
            R.id.tvSubmit -> {
                callAddPaymentFollowUpApi()
            }
        }
    }

    private fun openDatePickerDialog(type: Int, textView: TextView): String {
        val currentAmount = getCurrentAmount
        remainingAmount = totalAmount - currentAmount
        if (textView.text.toString().isEmpty() && remainingAmount == 0.0) {
            showToastMessage(mActivity, "Amount limit is reached. Please adjust expected amount.")
            return ""
        }

        binding.etAmount1.clearFocus()
        binding.etAmount2.clearFocus()
        binding.etAmount3.clearFocus()
        binding.etAmount4.clearFocus()

        val newDate = Calendar.getInstance()
        var dateString = ""
        mActivity.hideKeyboard()
        val datePickerDialog = DatePickerDialog(
            mActivity, { view, year, monthOfYear, dayOfMonth ->
                newDate.set(year, monthOfYear, dayOfMonth)
                dateString = CommonMethods.dateFormat.format(newDate.time) ?: ""
                when (type) {
                    1 -> {
                        binding.tvDate1.text = dateString
                    }
                    2 -> {
                        binding.tvDate2.text = dateString
                    }
                    3 -> {
                        binding.tvDate3.text = dateString
                    }
                    4 -> {
                        binding.tvDate4.text = dateString
                    }
                }

            }, newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(
                Calendar.DAY_OF_MONTH
            )
        )
        //datePickerDialog.datePicker.minDate = newCalendar.timeInMillis
        datePickerDialog.show()
        return dateString
    }

    private fun callPaymentFollowUpListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val paymentFollowUpReq = JsonObject()
        paymentFollowUpReq.addProperty("UserId", loginData.userId)
        paymentFollowUpReq.addProperty("TableName", dashboardDrillResponse.tableName)
        paymentFollowUpReq.addProperty("ReferenceId", dashboardDrillResponse.referenceId)

        val paymentFollowUpCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getPaymentFollowUp(paymentFollowUpReq)

        paymentFollowUpCall?.enqueue(object : Callback<List<PaymentFollowUpResponse>> {
            override fun onResponse(
                call: Call<List<PaymentFollowUpResponse>>,
                response: Response<List<PaymentFollowUpResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            paymentFollowUpList.clear()
                            paymentFollowUpList.addAll(it)
                            setupPaymentFollowUpRecyclerView()
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<PaymentFollowUpResponse>>, t: Throwable) {
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

    private fun callAddPaymentFollowUpApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val addPaymentFollowUpReq = JsonObject()
        addPaymentFollowUpReq.addProperty("PaymentFollowUpId", 0)
        addPaymentFollowUpReq.addProperty("UserId", loginData.userId)
        addPaymentFollowUpReq.addProperty("FollowUpDate", CommonMethods.getCurrentDate())
        addPaymentFollowUpReq.addProperty("TableName", dashboardDrillResponse.tableName)
        addPaymentFollowUpReq.addProperty("ReferenceId", dashboardDrillResponse.referenceId)
        addPaymentFollowUpReq.addProperty("ExpectedDate1", binding.tvDate1.text.toString())
        addPaymentFollowUpReq.addProperty("ExpectedDate2", binding.tvDate2.text.toString())
        addPaymentFollowUpReq.addProperty("ExpectedDate3", binding.tvDate3.text.toString())
        addPaymentFollowUpReq.addProperty("ExpectedDate4", binding.tvDate4.text.toString())
        addPaymentFollowUpReq.addProperty("ExpectedAmount1", binding.etAmount1.text.toString())
        addPaymentFollowUpReq.addProperty("ExpectedAmount2", binding.etAmount2.text.toString())
        addPaymentFollowUpReq.addProperty("ExpectedAmount3", binding.etAmount3.text.toString())
        addPaymentFollowUpReq.addProperty("ExpectedAmount4", binding.etAmount4.text.toString())
        addPaymentFollowUpReq.addProperty("Remarks", binding.etRemarks.text.toString())

        val addPaymentFollowUpCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.addPaymentFollowUp(addPaymentFollowUpReq)

        addPaymentFollowUpCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            showToastMessage(
                                mActivity,
                                it.returnMessage ?: getString(R.string.payment_follow_up_success)
                            )
                            if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                mActivity.onBackPressedDispatcher.onBackPressed()
                            } else {
                                mActivity.onBackPressed()
                            }
                        } else {
                            showToastMessage(mActivity, getString(R.string.something_went_wrong))
                        }
                    }
                } else {
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

    private fun setupPaymentFollowUpRecyclerView() {
        reportAdapter = PaymentFollowUpAdapter(paymentFollowUpList)
        binding.rvPaymentFollowUp.adapter = reportAdapter
        binding.rvPaymentFollowUp.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    inner class PaymentFollowUpAdapter(
        private val paymentFollowList: ArrayList<PaymentFollowUpResponse>
    ) : RecyclerView.Adapter<PaymentFollowUpAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPaymentFollowupBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return paymentFollowList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val paymentFollowUpData = paymentFollowList[position]
            holder.bind(paymentFollowUpData)
        }

        inner class ViewHolder(private val binding: ItemPaymentFollowupBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(paymentFollowUpData: PaymentFollowUpResponse) {
                val amount1 =
                    if (paymentFollowUpData.expectedAmount1 == 0.0) "" else paymentFollowUpData.expectedAmount1.toString() + "\n"
                val amount2 =
                    if (paymentFollowUpData.expectedAmount2 == 0.0) "" else paymentFollowUpData.expectedAmount2.toString() + "\n"
                val amount3 =
                    if (paymentFollowUpData.expectedAmount3 == 0.0) "" else paymentFollowUpData.expectedAmount3.toString() + "\n"
                val amount4 =
                    if (paymentFollowUpData.expectedAmount4 == 0.0) "" else paymentFollowUpData.expectedAmount4.toString() + "\n"
                val amountString = amount1 + amount2 + amount3 + amount4
                val date1 =
                    if (paymentFollowUpData.expectedDate1.isEmpty()) "" else paymentFollowUpData.expectedDate1 + "\n"
                val date2 =
                    if (paymentFollowUpData.expectedDate2.isEmpty()) "" else paymentFollowUpData.expectedDate2 + "\n"
                val date3 =
                    if (paymentFollowUpData.expectedDate3.isEmpty()) "" else paymentFollowUpData.expectedDate3 + "\n"
                val date4 =
                    if (paymentFollowUpData.expectedDate4.isEmpty()) "" else paymentFollowUpData.expectedDate4 + "\n"
                val dateString = date1 + date2 + date3 + date4
                binding.tvAmount.text = amountString
                binding.tvDate.text = paymentFollowUpData.followUpDate
                binding.tvRemarks.text = paymentFollowUpData.remarks
                binding.tvExpectedDate.text = dateString
            }
        }
    }

}