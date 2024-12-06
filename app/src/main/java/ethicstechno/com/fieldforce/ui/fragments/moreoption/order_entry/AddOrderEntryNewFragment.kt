package ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import avoidDoubleClicks
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.groundworksapp.utility.DecimalDigitsInputFilter
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAddOrderEntryBinding
import ethicstechno.com.fieldforce.listener.ItemClickListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.AppRegistrationResponse
import ethicstechno.com.fieldforce.models.CommonDropDownListModel
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.orderentry.OrderDetailsResponse
import ethicstechno.com.fieldforce.models.orderentry.ProductGroupResponse
import ethicstechno.com.fieldforce.ui.adapter.GenericSpinnerAdapter
import ethicstechno.com.fieldforce.ui.adapter.OrderDetailsAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.AlbumUtility
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.dateFormat
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DIALOG_ACCOUNT_MASTER
import ethicstechno.com.fieldforce.utils.DIALOG_PRODUCT_GROUP_TYPE
import ethicstechno.com.fieldforce.utils.DIALOG_PRODUCT_TYPE
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.ImagePreviewCommonDialog
import ethicstechno.com.fieldforce.utils.ImageUtils
import ethicstechno.com.fieldforce.utils.ORDER_CATEGORY
import ethicstechno.com.fieldforce.utils.ORDER_MODE
import ethicstechno.com.fieldforce.utils.PermissionUtil
import ethicstechno.com.fieldforce.utils.dialog.SearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.math.BigDecimal
import java.util.Calendar
import java.util.concurrent.Executors

class AddOrderEntryNewFragment(){}

/*class AddOrderEntryNewFragment : HomeBaseFragment(), View.OnClickListener,
    OrderDetailsAdapter.ProductItemClickListener,AddProductDialogFragment.OnOrderDetailsListener {

    lateinit var binding: FragmentAddOrderEntryBinding
    private var orderModeList: ArrayList<CommonDropDownListModel> = arrayListOf()
    private var orderCategoryList: ArrayList<CommonDropDownListModel> = arrayListOf()
    private var accountMasterList: ArrayList<AccountMasterList> = arrayListOf()
    private var distributorList: ArrayList<AccountMasterList> = arrayListOf()
    private var groupList: ArrayList<ProductGroupResponse> = arrayListOf()
    private var productList: ArrayList<ProductGroupResponse> = arrayListOf()
    private var selectedPartyDealerId: Int = -1
    private var selectedCategoryId: Int = -1
    private var selectedOrderModeId: Int = -1
    private var selectedDistributorId: Int = -1
    private var selectedGroupId: Int = 0
    private lateinit var selectedProduct: ProductGroupResponse
    private var selectedGroup: ProductGroupResponse? = null
    private lateinit var searchDialog: SearchDialogUtil<AccountMasterList>
    private lateinit var groupSearchDialog: SearchDialogUtil<ProductGroupResponse>
    private lateinit var productSearchDialog: SearchDialogUtil<ProductGroupResponse>
    private var productDialog: AlertDialog? = null
    private var orderDetailsList: ArrayList<OrderDetailsResponse.OrderDetails> = arrayListOf()
    private lateinit var orderDetailsAdapter: OrderDetailsAdapter
    lateinit var tvSelectProduct: TextView
    lateinit var tvSelectGroup: TextView
    private lateinit var etQty: EditText
    private lateinit var etAmount: EditText
    lateinit var etPrice: EditText
    var isUpdating = false
    lateinit var tvUnit: TextView
    private var imageFile: File? = null
    private var base64Image = ""
    private var orderId: Int = 0
    private var userId = 0
    private var imageUrl = ""
    var isReadOnly = true
    private lateinit var productDialogFragment: AddProductDialogFragment


    private val partyDealerItemClickListener =
        object : ItemClickListener<AccountMasterList> {
            override fun onItemSelected(item: AccountMasterList) {
                // Handle user item selection
                searchDialog.closeDialog()
                binding.tvPartyDealer.text = item.accountName
                selectedPartyDealerId = item.accountMasterId
                binding.etContactPersonName.setText(item.contactPersonName)
            }
        }

    private val distributorItemClickListener =
        object : ItemClickListener<AccountMasterList> {
            override fun onItemSelected(item: AccountMasterList) {
                // Handle user item selection
                searchDialog.closeDialog()
                binding.tvSelectDistributor.text = item.accountName
                selectedDistributorId = item.accountMasterId
            }
        }

    private val groupItemClickListener =
        object : ItemClickListener<ProductGroupResponse> {
            override fun onItemSelected(item: ProductGroupResponse) {
                // Handle user item selection
                groupSearchDialog.closeDialog()
                tvSelectGroup.text = item.productGroupName
                selectedGroupId = item.productGroupId ?: 0
                selectedGroup = item
            }
        }

    private val productItemClickListener =
        object : ItemClickListener<ProductGroupResponse> {
            override fun onItemSelected(item: ProductGroupResponse) {
                // Handle user item selection
                productSearchDialog.closeDialog()
                tvSelectProduct.text = item.productName
                etPrice.setText(item.salesPrice.toString())
                selectedProduct = item
                tvUnit.text = item.unit
            }
        }

    companion object {
        fun newInstance(
            orderId: Int,
        ): AddOrderEntryNewFragment {
            val args = Bundle()
            args.putInt(ARG_PARAM1, orderId)
            val fragment = AddOrderEntryNewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_order_entry, container, false)
        return binding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {
        arguments?.let {
            orderId = it.getInt(ARG_PARAM1, -1)
            userId = loginData.userId
        }
        mActivity.bottomHide()
        binding.toolbar.tvHeader.text =
            if (orderId > 0) getString(R.string.order_details) else getString(R.string.add_order_entry)
        binding.tvSubmit.text =
            if (orderId > 0) getString(R.string.update) else getString(R.string.submit)

        binding.tvDate.text = CommonMethods.getCurrentDate()
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.tvSubmit.setOnClickListener(this)
        binding.cardImage.setOnClickListener(this)
        setOrderDetailsAdapter()
        if (orderId > 0) {
            binding.toolbar.imgEdit.visibility = View.VISIBLE
            binding.toolbar.imgDelete.visibility = View.VISIBLE
            binding.toolbar.imgEdit.setOnClickListener(this)
            binding.toolbar.imgDelete.setOnClickListener(this)
            formViewMode(true)
            callOrderDetailsApi()
        } else {
            formViewMode(false)
        }
    }

    private fun formViewMode(isViewOnly: Boolean) {
        isReadOnly = isViewOnly
        if (isViewOnly) {
            binding.tvOrderMode.visibility = View.VISIBLE
            binding.spOrderMode.visibility = View.GONE
            binding.tvOrderCategory.visibility = View.VISIBLE
            binding.spOrderCategory.visibility = View.GONE
            binding.etContactPersonName.visibility = View.GONE
            binding.tvContactPerson.visibility = View.VISIBLE
            binding.tvRemarks.visibility = View.VISIBLE
            binding.etRemarks.visibility = View.GONE
            binding.llBottom.visibility = View.GONE
        } else {
            binding.tvOrderMode.visibility = View.GONE
            binding.spOrderMode.visibility = View.VISIBLE
            binding.tvOrderCategory.visibility = View.GONE
            binding.spOrderCategory.visibility = View.VISIBLE
            binding.etContactPersonName.visibility = View.VISIBLE
            binding.tvContactPerson.visibility = View.GONE
            binding.tvRemarks.visibility = View.GONE
            binding.etRemarks.visibility = View.VISIBLE
            binding.llBottom.visibility = View.VISIBLE
            binding.tvDate.setOnClickListener(this)
            binding.flPartyDealer.setOnClickListener(this)
            binding.flSelectDistributor.setOnClickListener(this)
            binding.tvDate.setOnClickListener(this)
            binding.btnAddOrderDetails.setOnClickListener(this)
            callCommonDropDownListApi(ORDER_MODE)
        }
    }

    private fun setOrderDetailsAdapter() {
        orderDetailsAdapter = OrderDetailsAdapter(mActivity, orderDetailsList, this)
        val layoutManager = LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
        binding.rvProduct.layoutManager = layoutManager
        binding.rvProduct.adapter = orderDetailsAdapter
    }

    private fun callCommonDropDownListApi(type: String) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        jsonReq.addProperty("DropDownValueId", 0)
        jsonReq.addProperty("DropDownFieldName", type)

        val commonDropDownListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCommonDropDownList(jsonReq)

        commonDropDownListCall?.enqueue(object : Callback<List<CommonDropDownListModel>> {
            override fun onResponse(
                call: Call<List<CommonDropDownListModel>>,
                response: Response<List<CommonDropDownListModel>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty() && it.size > 0) {
                            if (type == ORDER_MODE) {
                                orderModeList.clear()
                                orderModeList.add(
                                    CommonDropDownListModel(
                                        -1,
                                        "Select Order Mode",
                                        ""
                                    )
                                )
                                orderModeList.addAll(it)
                                setupCommonSpinner(
                                    orderModeList,
                                    binding.spOrderMode
                                ) { it -> selectedOrderModeId = it.dropDownValueId }
                                callCommonDropDownListApi(ORDER_CATEGORY)
                            } else if (type == ORDER_CATEGORY) {
                                orderCategoryList.clear()
                                orderCategoryList.add(
                                    CommonDropDownListModel(
                                        -1,
                                        "Select Category",
                                        ""
                                    )
                                )
                                orderCategoryList.addAll(it)
                                setupCommonSpinner(
                                    orderCategoryList,
                                    binding.spOrderCategory
                                ) { it -> selectedCategoryId = it.dropDownValueId }
                                callProductGroupListApi(true, -1)
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

            override fun onFailure(call: Call<List<CommonDropDownListModel>>, t: Throwable) {
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

    private fun callAccountMasterList(isForPartyDealer: Boolean) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val orderDateString =
            CommonMethods.convertDateStringForOrderEntry(binding.tvDate.text.toString())

        val parameterString = if (isForPartyDealer) {
            "FieldName=Order/Party and EntryDate=$orderDateString and OrderMode=$selectedOrderModeId and CategoryMasterId=$selectedCategoryId"
        } else {
            "FieldName=Order/Distributor and EntryDate=$orderDateString and OrderMode=$selectedOrderModeId and CategoryMasterId=$selectedCategoryId"
        }

        val jsonReq = JsonObject()
        jsonReq.addProperty("AccountMasterId", 0)
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty("ParameterString", parameterString)

        val accountMasterCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getOrderAccountMasterList(jsonReq)

        accountMasterCall?.enqueue(object : Callback<List<AccountMasterList>> {
            override fun onResponse(
                call: Call<List<AccountMasterList>>,
                response: Response<List<AccountMasterList>>
            ) {
                CommonMethods.hideLoading()
                when {
                    response.code() == 200 -> {
                        response.body()?.let {
                            if (it.isNotEmpty()) {
                                if (isForPartyDealer) {
                                    accountMasterList.clear()
                                    accountMasterList.addAll(it)
                                    showPartyDealerDialog(true)
                                } else {
                                    distributorList.clear()
                                    distributorList.addAll(it)
                                    showPartyDealerDialog(false)
                                }
                                //setupPartyDealer()
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<AccountMasterList>>, t: Throwable) {
                CommonMethods.hideLoading()
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

    private fun callProductGroupListApi(isForProductGroup: Boolean, productGroupId: Int) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        if (isForProductGroup) {
            jsonReq.addProperty("ProductGroupId", 0)
        } else {
            val orderDateString =
                CommonMethods.convertDateStringForOrderEntry(binding.tvDate.text.toString())
            val parameterString =
                "ProductGroupId=$productGroupId and AccountMasterId=$selectedPartyDealerId EntryDate=$orderDateString"
            jsonReq.addProperty("ProductId", 0)
            jsonReq.addProperty("ProductGroupId", productGroupId)
            jsonReq.addProperty("ParameterString", parameterString)

        }

        val productGroupCall = if (isForProductGroup) WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getProductGroupList(jsonReq) else WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getProductList(jsonReq)

        productGroupCall?.enqueue(object : Callback<List<ProductGroupResponse>> {
            override fun onResponse(
                call: Call<List<ProductGroupResponse>>,
                response: Response<List<ProductGroupResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty() && it.size > 0) {
                            if (isForProductGroup) {
                                groupList.clear()
                                groupList.addAll(it)
                                if (!isReadOnly) {
                                    callOrderDetailsApi()
                                }
                            } else {
                                productList.clear()
                                productList.addAll(it)
                                try {
                                    if (productList.size > 0) {
                                        productSearchDialog = SearchDialogUtil(
                                            activity = mActivity,
                                            items = productList,
                                            layoutId = R.layout.item_user, // The layout resource for each item
                                            bind = { view, item ->
                                                val textView: TextView =
                                                    view.findViewById(R.id.tvUserName)
                                                textView.text =
                                                    item.productName // Bind data to the view
                                            },
                                            itemClickListener = productItemClickListener,
                                            title = "Product List",
                                            dialogType = DIALOG_PRODUCT_TYPE
                                        )
                                        productSearchDialog.showSearchDialog()
                                    }
                                } catch (e: Exception) {
                                    Log.e("TAG", "onClick: " + e.message)
                                }
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

            override fun onFailure(call: Call<List<ProductGroupResponse>>, t: Throwable) {
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

    private fun callOrderDetailsApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        jsonReq.addProperty("OrderId", orderId)

        val orderDetailsCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getOrderDetails(jsonReq)

        orderDetailsCall?.enqueue(object : Callback<List<OrderDetailsResponse>> {
            override fun onResponse(
                call: Call<List<OrderDetailsResponse>>,
                response: Response<List<OrderDetailsResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty() && it.size > 0) {
                            setupOrderDetailsData(it[0], appRegistrationData)
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

            override fun onFailure(call: Call<List<OrderDetailsResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                Log.e("TAG", "onFailure: "+t.message.toString() )
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


    private fun callDeleteOrderDetailsApi(item: OrderDetailsResponse.OrderDetails) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        jsonReq.addProperty("OrderDetailsId", item.orderDetailsId)
        jsonReq.addProperty("UserId", loginData.userId)

        val orderDetailsCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.deleteOrderDetails(jsonReq)

        orderDetailsCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        orderDetailsList.remove(item)
                        if (orderDetailsList.size > 0) {
                            orderDetailsAdapter.refreshAdapter(orderDetailsList)
                        }
                        if (orderDetailsList.isNotEmpty()) {
                            // Calculate the total amount using BigDecimal
                            val totalAmount = orderDetailsList.map { it.amount ?: BigDecimal.ZERO }
                                .reduce { acc, amount -> acc.add(amount) }

                            // Check if totalAmount is greater than BigDecimal.ZERO
                            if (totalAmount > BigDecimal.ZERO) {
                                binding.lylTotalOrderAmount.visibility = View.VISIBLE
                                binding.tvTotalOrderAmount.text = totalAmount.toString()
                            }
                        }
                        handleOrderDetailsRVVisibility(orderDetailsList)
                        showToastMessage(mActivity, it.returnMessage ?: "")
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
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

    private fun callDeleteOrderEntryApi(remarks: String) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        jsonReq.addProperty("UserId", userId)
        jsonReq.addProperty("OrderId", orderId)
        jsonReq.addProperty("Remarks", remarks)

        val orderDetailsCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.deleteOrderEntry(jsonReq)

        orderDetailsCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        AppPreference.saveBooleanPreference(
                            mActivity,
                            IS_DATA_UPDATE,
                            true
                        )
                        showToastMessage(mActivity, it.returnMessage ?: "")
                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                            mActivity.onBackPressedDispatcher.onBackPressed()
                        } else {
                            mActivity.onBackPressed()
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
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


    private fun setupOrderDetailsData(
        orderDetails: OrderDetailsResponse,
        appRegistrationData: AppRegistrationResponse
    ) {
        binding.tvDate.text = orderDetails.orderDate
        selectedOrderModeId = orderDetails.orderMode ?: -1
        val selectedOrderModeIndex =
            orderModeList.indexOfFirst { it.dropDownValueId == orderDetails.orderMode }
        binding.spOrderMode.setSelection(selectedOrderModeIndex)
        binding.tvOrderMode.text = orderDetails.orderModeName
        selectedCategoryId = orderDetails.categoryMasterId ?: -1
        val selectedCategoryIndex =
            orderCategoryList.indexOfFirst { it.dropDownValueId == selectedCategoryId }
        binding.spOrderCategory.setSelection(selectedCategoryIndex)
        binding.tvOrderCategory.text = orderDetails.categoryName
        binding.etContactPersonName.setText(orderDetails.contactPersonName)
        binding.tvContactPerson.text = orderDetails.contactPersonName
        binding.tvPartyDealer.text = orderDetails.accountName
        selectedPartyDealerId = orderDetails.accountMasterId ?: -1
        binding.tvSelectDistributor.text = orderDetails.distributorAccountName
        selectedDistributorId = orderDetails.distributorAccountMasterId ?: -1
        binding.etRemarks.setText(orderDetails.remarks)
        binding.tvRemarks.text = orderDetails.remarks

        if (orderDetails.orderDetails.size > 0) {
            orderDetailsList.clear()
            orderDetailsList.addAll(orderDetails.orderDetails)
            orderDetailsAdapter.refreshAdapter(orderDetailsList)
            handleOrderDetailsRVVisibility(orderDetailsList)
        }

        ImageUtils().loadImageUrl(
            mActivity,
            appRegistrationData.apiHostingServer + orderDetails.filePath,
            binding.imgOrder
        )
        binding.imgOrder.scaleType = ImageView.ScaleType.CENTER_CROP
        imageUrl = orderDetails.filePath ?: ""
        if (orderDetailsList.isNotEmpty()) {
            // Calculate the total amount using BigDecimal
            val totalAmount = orderDetailsList.map { it.amount ?: BigDecimal.ZERO }
                .reduce { acc, amount -> acc.add(amount) }

            // Check if totalAmount is greater than BigDecimal.ZERO
            if (totalAmount > BigDecimal.ZERO) {
                binding.lylTotalOrderAmount.visibility = View.VISIBLE
                binding.tvTotalOrderAmount.text = totalAmount.toString()
            }
        }
    }


    private fun setupCommonSpinner(
        list: List<CommonDropDownListModel>,
        spinner: Spinner,
        onItemSelected: (CommonDropDownListModel) -> Unit
    ) {
        val adapter = GenericSpinnerAdapter(mActivity, list) { it.dropDownValueName }
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onItemSelected(list[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {


        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                mActivity.onBackPressed()
            }

            R.id.flPartyDealer -> {
                avoidDoubleClicks(binding.flPartyDealer)
                if (selectedOrderModeId <= 0) {
                    showToastMessage(mActivity, "Please select order mode")
                    return
                }
                if(selectedCategoryId <=0){
                    showToastMessage(mActivity, "Please select order category")
                    return
                }
                callAccountMasterList(true)
            }

            R.id.flSelectDistributor -> {
                avoidDoubleClicks(binding.flSelectDistributor)
                if (selectedOrderModeId <= 0) {
                    showToastMessage(mActivity, "Please select order mode")
                    return
                }
                if(selectedCategoryId <=0){
                    showToastMessage(mActivity, "Please select order category")
                    return
                }
                callAccountMasterList(false)
            }

            R.id.btnAddOrderDetails -> {
                avoidDoubleClicks(binding.btnAddOrderDetails)
                showProductDialogFragment()
                //showAddProduct(null, -1)
            }

            R.id.tvSubmit -> {
                avoidDoubleClicks(binding.tvSubmit)
                requestValidate()
            }

            R.id.cardImage -> {
                avoidDoubleClicks(binding.cardImage)
                if (isReadOnly) {
                    Log.e("TAG", "onClick: preview iamge")
                    if (imageUrl.isNotEmpty()) {
                        ImagePreviewCommonDialog.showImagePreviewDialog(
                            mActivity,
                            appDatabase.appDao()
                                .getAppRegistration().apiHostingServer + imageUrl
                        )
                    } else {
                        showToastMessage(mActivity, getString(R.string.no_image_found))
                    }
                } else {
                    askCameraGalleryPermission()
                }
            }

            R.id.tvDate -> {
                val selectedCalendar = if (binding.tvDate.text.toString()
                        .isEmpty()
                ) null else CommonMethods.dateStringToCalendar(binding.tvDate.text.toString())
                openDatePicker(
                    selectedCalendar
                ) { selectedDate ->
                    if (binding.tvDate.text.toString().isNotEmpty()) {
                        binding.tvDate.text = selectedDate
                    } else {
                        binding.tvDate.text = selectedDate
                    }
                }
            }

            R.id.imgDelete -> {
                showRemarksDialog()
            }

            R.id.imgEdit -> {
                CommonMethods.showAlertDialog(
                    mActivity,
                    getString(R.string.edit_order_entry),
                    getString(R.string.are_you_sure_you_want_to_edit_order_entry),
                    object : PositiveButtonListener {
                        override fun okClickListener() {
                            formViewMode(false)
                        }
                    },
                    positiveButtonText = getString(R.string.yes),
                    negativeButtonText = getString(R.string.no)
                )

            }
        }
    }

    private fun showProductDialogFragment() {
       productDialogFragment = AddProductDialogFragment.newInstance(binding.tvDate.text.toString(), selectedPartyDealerId, orderDetailsList)
        productDialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyAppTheme)
        productDialogFragment.setOrderDetailsListener(this)
        productDialogFragment.show(mActivity.supportFragmentManager, "AddProductDialogFragment")
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
                showToastMessage(mActivity, getString(R.string.enter_remark))
                return@setOnClickListener
            }
            companyDialog.dismiss()
            callDeleteOrderEntryApi(etRemarks.text.toString().trim())
        }

        val window = companyDialog.window
        window!!.setLayout(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.WRAP_CONTENT
        )
        companyDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        companyDialog.show()
    }



    private fun askCameraGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                openAlbum()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val arrListOfPermission = arrayListOf<String>(
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
                                .into(binding.imgOrder)
                            base64Image =
                                CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                    .toString()
                            binding.imgOrder.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    }
                }
            },
            onError = {
                showToastMessage(mActivity, it)
            }
        )

    }


    private fun requestValidate() {
        if (binding.spOrderMode.selectedItemPosition == 0) {
            showToastMessage(
                mActivity,
                getString(R.string.please_selected_order_mode)
            )
            return
        }

        if (binding.spOrderCategory.selectedItemPosition == 0) {
            showToastMessage(
                mActivity,
                getString(R.string.please_select_order_category)
            )
            return
        }
        if (binding.tvPartyDealer.text.toString().isEmpty()) {
            showToastMessage(mActivity, getString(R.string.party_dealer_validation))
            return
        }
        if (orderDetailsList.size <= 0) {
            showToastMessage(mActivity, getString(R.string.product_validation))
            return
        }

        CommonMethods.showAlertDialog(
            mActivity,
            if (orderId > 0) getString(R.string.update_order_entry) else getString(R.string.more_order_entry),
            if (orderId > 0) getString(R.string.update_order_entry_confirmation) else getString(R.string.order_entry_confirmation),
            object : PositiveButtonListener {
                override fun okClickListener() {
                    createOrderRequest()
                }
            },
            positiveButtonText = getString(R.string.yes),
            negativeButtonText = getString(R.string.no)
        )

    }

    private fun createOrderRequest() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val loginData = loginData
        val objReq = JsonObject()
        objReq.addProperty("OrderId", orderId)
        objReq.addProperty("OrderDate", CommonMethods.convertToAppDateFormat(binding.tvDate.text.toString(), "MM/dd/yyyy"))
        objReq.addProperty("OrderMode", selectedOrderModeId)
        objReq.addProperty("CategoryMasterId", selectedCategoryId)
        objReq.addProperty("AccountMasterId", selectedPartyDealerId)
        objReq.addProperty("ContactPersonName", binding.etContactPersonName.text.toString().trim())
        objReq.addProperty("DistributorAccountMasterId", selectedDistributorId)
        objReq.addProperty("Remarks", binding.etRemarks.text.toString().trim())
        if (orderId > 0) {
            if (base64Image.isEmpty() && imageUrl.isEmpty()) {
                objReq.addProperty("FilePath", "")
            } else if (base64Image.isNotEmpty()) {
                objReq.addProperty("FilePath", base64Image)
            } else {
                objReq.addProperty("FilePath", imageUrl)
            }
        } else {
            objReq.addProperty("FilePath", base64Image.ifEmpty { "" })
        }
        objReq.addProperty("OrderStatusId", "1")
        objReq.addProperty("UserId", loginData.userId)

        val objDetailsArray = JsonArray()
        for (i in orderDetailsList) {
            val objDetails = JsonObject()
            objDetails.addProperty("OrderId", orderId)
            objDetails.addProperty("OrderDetailsId", i.orderDetailsId)
            objDetails.addProperty("ProductId", i.productId)
            objDetails.addProperty("Quantity", i.quantity)
            objDetails.addProperty("Rate", i.rate)
            objDetails.addProperty("Amount", i.amount)
            objDetails.addProperty("UserId", loginData.userId)
            objDetailsArray.add(objDetails)
        }
        objReq.add("OrderDetails", objDetailsArray)

        Log.e("TAG", "createOrderRequest: " + objReq)

        val addOrderCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.addOrderInsertUpdate(objReq)

        addOrderCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        Log.e("TAG", "onResponse: Order entry :: " + it.toString())
                        CommonMethods.showAlertDialog(
                            mActivity,
                            mActivity.getString(R.string.more_order_entry),
                            it.returnMessage,
                            object : PositiveButtonListener {
                                override fun okClickListener() {
                                    if (it.success) {
                                        AppPreference.saveBooleanPreference(
                                            mActivity,
                                            IS_DATA_UPDATE,
                                            true
                                        )
                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                            mActivity.onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            mActivity.onBackPressed()
                                        }
                                    }
                                }
                            }, isCancelVisibility = false
                        )
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

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
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

    private fun showGroupDialog() {
        try {
            if (groupList.size > 0) {
                groupSearchDialog = SearchDialogUtil(
                    activity = mActivity,
                    items = groupList,
                    layoutId = R.layout.item_user, // The layout resource for each item
                    bind = { view, item ->
                        val textView: TextView = view.findViewById(R.id.tvUserName)
                        textView.text = item.productGroupName // Bind data to the view
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

    private fun showPartyDealerDialog(isForPartyDealer: Boolean) {
        try {
            if (accountMasterList.size > 0) {
                searchDialog = SearchDialogUtil(
                    activity = mActivity,
                    items = if (isForPartyDealer) accountMasterList else distributorList,
                    layoutId = R.layout.item_user, // The layout resource for each item
                    bind = { view, item ->
                        val textView: TextView = view.findViewById(R.id.tvUserName)
                        textView.text = item.accountName // Bind data to the view
                    },
                    itemClickListener = if (isForPartyDealer) partyDealerItemClickListener else distributorItemClickListener,
                    title = if (isForPartyDealer) mActivity.getString(R.string.party_dealer_list) else getString(
                        R.string.distributor_list
                    ),
                    dialogType = DIALOG_ACCOUNT_MASTER
                )
                searchDialog.showSearchDialog()
            }
        } catch (e: Exception) {
            Log.e("TAG", "onClick: " + e.message)
        }
    }

    private fun showAddProduct(
        productModel: OrderDetailsResponse.OrderDetails?,
        orderDetailsPosition: Int
    ) {
        if (productDialog != null && productDialog!!.isShowing) {
            productDialog!!.dismiss()
        }

        try {
            val builder = AlertDialog.Builder(mActivity, R.style.MyAlertDialogStyle)
            productDialog = builder.create()
            productDialog!!.setCancelable(false)
            productDialog!!.setCanceledOnTouchOutside(false)
            productDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            productDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val inflater =
                mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.dialog_add_product, null)
            tvSelectGroup = layout.findViewById<TextView>(R.id.tvSelectGroup)
            tvSelectProduct = layout.findViewById<TextView>(R.id.tvSelectProduct)
            etQty = layout.findViewById<TextInputEditText>(R.id.etQty)
            etPrice = layout.findViewById<TextInputEditText>(R.id.etPrice)
            etAmount = layout.findViewById<TextInputEditText>(R.id.etAmount)
            tvUnit = layout.findViewById<TextView>(R.id.tvUnit)
            val btnSubmit = layout.findViewById<MaterialButton>(R.id.btnSubmit)
            val btnCancel = layout.findViewById<MaterialButton>(R.id.btnCancel)

            etPrice.filters = arrayOf(DecimalDigitsInputFilter(10, 2, etPrice))
            etAmount.filters = arrayOf(DecimalDigitsInputFilter(10, 2, etAmount))
            etQty.filters = arrayOf(DecimalDigitsInputFilter(10, 2, etQty))

            if (productModel != null) {
                btnSubmit.text = "Update"
                tvUnit.text = productModel.unit
                tvSelectProduct.text = productModel.productName
                etQty.setText(productModel.quantity.toString())
                etAmount.setText(productModel.amount.toString())
                etPrice.setText(productModel.rate.toString())
            }

            if (orderId == 0 && orderDetailsList.size > 0) {
                tvSelectGroup.text = if(selectedGroup == null) "" else selectedGroup?.productGroupName
                selectedGroupId = if(selectedGroup == null) 0 else selectedGroup?.productGroupId ?: 0
            }

            tvSelectGroup.setOnClickListener {
                if (groupList.size > 0) {
                    showGroupDialog()
                } else {
                    callProductGroupListApi(true, 0)
                }
            }

            tvSelectProduct.setOnClickListener {
                callProductGroupListApi(false, selectedGroupId)
            }

            // Add TextWatcher to Qty and Price EditText
            etQty.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    calculateAmount()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            etPrice.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    calculateAmount()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            etAmount.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    calculatePrice()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })


            btnSubmit.setOnClickListener {
                if (tvSelectProduct.text.isEmpty()) {
                    showToastMessage(
                        mActivity,
                        "Please select product"
                    )
                    return@setOnClickListener
                }
                if (etQty.text.toString().trim().isEmpty()) {
                    showToastMessage(
                        mActivity,
                        getString(R.string.please_enter_quantity)
                    )
                    return@setOnClickListener
                }
                if (etAmount.text.toString().trim().isEmpty()) {
                    showToastMessage(
                        mActivity,
                        getString(R.string.please_enter_amount)
                    )
                    return@setOnClickListener
                }
                val orderDetailsModel = OrderDetailsResponse.OrderDetails(
                    productModel?.orderDetailsId ?: 0,
                    orderId,
                    if (productModel != null) productModel.productId else selectedProduct.productId,
                    if (productModel != null) productModel.productName else selectedProduct.productName,
                    tvUnit.text.toString(),
                    etQty.text.toString().trim().toBigDecimal(),
                    0,
                    etPrice.text.toString().trim().toBigDecimal(),
                    etAmount.text.toString().trim().toBigDecimal(),
                    userId,
                    false,
                    ""
                )

                if (productModel == null) {
                    orderDetailsList.add(orderDetailsModel)
                } else {
                    orderDetailsList[orderDetailsPosition] = orderDetailsModel
                }

                if (orderDetailsList.isNotEmpty()) {
                    // Calculate the total amount using BigDecimal
                    val totalAmount = orderDetailsList.map { it.amount ?: BigDecimal.ZERO }
                        .reduce { acc, amount -> acc.add(amount) }

                    // Check if totalAmount is greater than BigDecimal.ZERO
                    if (totalAmount > BigDecimal.ZERO) {
                        binding.lylTotalOrderAmount.visibility = View.VISIBLE
                        binding.tvTotalOrderAmount.text = totalAmount.toString()
                    }
                }


                if (orderDetailsAdapter != null) {
                    orderDetailsAdapter.refreshAdapter(orderDetailsList)
                }
                // Update loanDetailsEmiDates and set restrictions
                handleOrderDetailsRVVisibility(orderDetailsList)
                productDialog!!.dismiss()
            }

            btnCancel.setOnClickListener { view: View? -> productDialog!!.dismiss() }
            productDialog!!.setView(layout)
            productDialog!!.window!!.setBackgroundDrawableResource(R.drawable.dialog_shape)
            productDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TAG", "showAddProduct: " + e.message)
            //writeErrorLog("[AssetRequestActivity] *ERROR* IN \$assetDetailDialog$ :: error = " + e.message)
        }
    }

    private fun handleOrderDetailsRVVisibility(orderDetailsList: ArrayList<OrderDetailsResponse.OrderDetails>) {
        if (orderDetailsList.size > 0) {
            binding.rvProduct.visibility = View.VISIBLE
            binding.tvNoItemFound.visibility = View.GONE
        } else {
            binding.rvProduct.visibility = View.GONE
            binding.tvNoItemFound.visibility = View.VISIBLE
        }
    }

    override fun onEditClick(item: OrderDetailsResponse.OrderDetails, position: Int) {
        if(!isReadOnly) {
            showAddProduct(item, position)
        }
    }

    override fun onDeleteClick(item: OrderDetailsResponse.OrderDetails, position: Int) {
        if(isReadOnly){
            return
        }
        if (orderDetailsList.size == 1) {
            showToastMessage(
                mActivity,
                getString(R.string.one_item_required_for_order_entry)
            )
            return
        }
        CommonMethods.showAlertDialog(
            mActivity,
            "Delete Order Details",
            "Are you sure you want to delete this product?",
            okListener = object : PositiveButtonListener {
                override fun okClickListener() {
                    if (item.orderDetailsId > 0) {
                        callDeleteOrderDetailsApi(item)
                    } else {
                        orderDetailsList.remove(item)
                        if (orderDetailsList.size > 0) {
                            orderDetailsAdapter.refreshAdapter(orderDetailsList)
                        }
                        handleOrderDetailsRVVisibility(orderDetailsList)
                        if (orderDetailsList.isNotEmpty()) {
                            // Calculate the total amount using BigDecimal
                            val totalAmount = orderDetailsList.map { it.amount ?: BigDecimal.ZERO }
                                .reduce { acc, amount -> acc.add(amount) }

                            // Check if totalAmount is greater than BigDecimal.ZERO
                            if (totalAmount > BigDecimal.ZERO) {
                                binding.lylTotalOrderAmount.visibility = View.VISIBLE
                                binding.tvTotalOrderAmount.text = totalAmount.toString()
                            }
                        }
                    }
                }
            },
            positiveButtonText = getString(R.string.yes),
            negativeButtonText = getString(R.string.no)
        )

    }

    fun calculateAmount() {
        if (isUpdating) return
        isUpdating = true

        val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
        val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0

        if (price > 0) {
            val amount = qty * price
            etAmount.setText(CommonMethods.formatLargeDouble(amount))
            etAmount.isEnabled = false // Make amount non-editable
        } else {
            etAmount.isEnabled = true // Make amount editable
            etAmount.setText("")
        }
        isUpdating = false
    }

    fun calculatePrice() {
        if (isUpdating) return
        isUpdating = true

        val qty = etQty.text.toString().toDoubleOrNull() ?: 0.00
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.00

        if (amount > 0 && qty > 0) {
            val price = amount / qty
            etPrice.setText(CommonMethods.formatLargeDouble(price))
        } else {
            etPrice.setText("")
        }

        isUpdating = false
    }

    /*private fun openDatePicker(): String {
        val newDate = Calendar.getInstance()
        var dateString = ""
        mActivity.hideKeyboard()
        val datePickerDialog = DatePickerDialog(
            mActivity, { view, year, monthOfYear, dayOfMonth ->
                newDate.set(year, monthOfYear, dayOfMonth)
                dateString = CommonMethods.dateFormat.format(newDate.time) ?: ""
                binding.tvDate.text = dateString
            }, newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(
                Calendar.DAY_OF_MONTH
            )
        )
        //datePickerDialog.datePicker.minDate = newDate.timeInMillis
        datePickerDialog.show()
        return dateString
    }*/

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
                val dateString = dateFormat.format(calendar.time)
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

    override fun onOrderDetailsSelected(productList: List<ProductGroupResponse>) {

        for(i in productList){
            orderDetailsList.add(
                OrderDetailsResponse.OrderDetails(i.orderDetailsId, orderId, i.productId, i.productName, i.unit,
                i.qty, 0, i.price, i.amount, 0, false, ""))
        }
        orderDetailsAdapter.refreshAdapter(orderDetailsList)
        handleOrderDetailsRVVisibility(orderDetailsList)
    }

}

 **/