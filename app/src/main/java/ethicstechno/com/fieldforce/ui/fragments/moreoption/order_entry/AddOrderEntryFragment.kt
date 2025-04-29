package ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
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
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import avoidDoubleClicks
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAddOrderEntryBinding
import ethicstechno.com.fieldforce.databinding.ItemUserBinding
import ethicstechno.com.fieldforce.listener.ItemClickListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.AppRegistrationResponse
import ethicstechno.com.fieldforce.models.ApproveRejectResponse
import ethicstechno.com.fieldforce.models.CommonDropDownListModel
import ethicstechno.com.fieldforce.models.CommonDropDownResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.orderentry.OrderDetailsResponse
import ethicstechno.com.fieldforce.models.moreoption.orderentry.ProductGroupResponse
import ethicstechno.com.fieldforce.models.moreoption.orderentry.ShippingAddressResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.BranchMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CompanyMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.DivisionMasterResponse
import ethicstechno.com.fieldforce.ui.adapter.GenericSpinnerAdapter
import ethicstechno.com.fieldforce.ui.adapter.ImageAdapter
import ethicstechno.com.fieldforce.ui.adapter.LeaveTypeAdapter
import ethicstechno.com.fieldforce.ui.adapter.OrderDetailsAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.ARG_PARAM2
import ethicstechno.com.fieldforce.utils.ARG_PARAM3
import ethicstechno.com.fieldforce.utils.ARG_PARAM4
import ethicstechno.com.fieldforce.utils.ARG_PARAM5
import ethicstechno.com.fieldforce.utils.ARG_PARAM6
import ethicstechno.com.fieldforce.utils.ARG_PARAM7
import ethicstechno.com.fieldforce.utils.AlbumUtility
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.dateFormat
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DIALOG_PRODUCT_GROUP_TYPE
import ethicstechno.com.fieldforce.utils.DIALOG_PRODUCT_TYPE
import ethicstechno.com.fieldforce.utils.DOCUMENT_NAME_ORDER
import ethicstechno.com.fieldforce.utils.DROP_DOWN_CURRENCY
import ethicstechno.com.fieldforce.utils.DROP_DOWN_FREIGHT_TERMS
import ethicstechno.com.fieldforce.utils.DROP_DOWN_TRANSACTION_TYPE
import ethicstechno.com.fieldforce.utils.DecimalDigitsInputFilter
import ethicstechno.com.fieldforce.utils.FORM_ID_ORDER_ENTRY
import ethicstechno.com.fieldforce.utils.FOR_BRANCH
import ethicstechno.com.fieldforce.utils.FOR_COMPANY
import ethicstechno.com.fieldforce.utils.FOR_DIVISION
import ethicstechno.com.fieldforce.utils.FOR_FREIGHT_TERMS
import ethicstechno.com.fieldforce.utils.FOR_SHIPPING_ADDRESS
import ethicstechno.com.fieldforce.utils.FOR_TRANSACTION_TYPE
import ethicstechno.com.fieldforce.utils.ID_ZERO
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.ImagePreviewCommonDialog
import ethicstechno.com.fieldforce.utils.PermissionUtil
import ethicstechno.com.fieldforce.utils.TYPE_CONSIGNEE
import ethicstechno.com.fieldforce.utils.TYPE_DISTRIBUTOR
import ethicstechno.com.fieldforce.utils.TYPE_PARTY_DEALER
import ethicstechno.com.fieldforce.utils.dialog.SearchDialogUtil
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.math.BigDecimal
import java.util.Calendar

class AddOrderEntryFragment : HomeBaseFragment(), View.OnClickListener,
    OrderDetailsAdapter.ProductItemClickListener, AddProductDialogFragment.OnOrderDetailsListener,
    UserSearchDialogUtil.CompanyDialogDetect, UserSearchDialogUtil.DivisionDialogDetect,
    UserSearchDialogUtil.BranchDialogDetect, LeaveTypeAdapter.TypeSelect,
    UserSearchDialogUtil.CommonDropDownDialogDetect,
    UserSearchDialogUtil.ShippingAddressDialogDetect {

    lateinit var binding: FragmentAddOrderEntryBinding

    //private var orderModeList: ArrayList<CommonDropDownListModel> = arrayListOf()
    private var accountMasterList: ArrayList<AccountMasterList> = arrayListOf()
    private var distributorList: ArrayList<AccountMasterList> = arrayListOf()
    private var consigneeList: ArrayList<AccountMasterList> = arrayListOf()
    private var groupList: ArrayList<ProductGroupResponse> = arrayListOf()
    private var productList: ArrayList<ProductGroupResponse> = arrayListOf()
    private var selectedPartyDealerId: Int = 0

    //private var selectedCategoryId: Int = -1
    //private var selectedOrderModeId: Int = -1
    private var selectedDistributorId: Int = 0
    private var selectedConsigneeId: Int = 0
    private var selectedGroupId: Int = 0
    private var selectedShippingAddressId: Int = 0
    private lateinit var selectedProduct: ProductGroupResponse
    private var selectedGroup: ProductGroupResponse? = null
    private lateinit var searchDialog: SearchDialogUtil<AccountMasterList>
    private lateinit var groupSearchDialog: SearchDialogUtil<ProductGroupResponse>
    private lateinit var productSearchDialog: SearchDialogUtil<ProductGroupResponse>
    private var productDialog: AlertDialog? = null
    private var orderDetailsList: java.util.ArrayList<ProductGroupResponse> = arrayListOf()
    private var holdDetailsDataList: java.util.ArrayList<ProductGroupResponse> = arrayListOf()
    private lateinit var orderDetailsAdapter: OrderDetailsAdapter
    lateinit var tvSelectProduct: TextView
    lateinit var tvSelectGroup: TextView
    private lateinit var etQty: EditText
    private lateinit var etAmount: EditText
    lateinit var etPrice: EditText
    var isUpdating = false
    lateinit var tvUnit: TextView
    private var orderId: Int = 0
    private var userId = 0
    private var imageUrl = ""
    var isReadOnly = true
    private lateinit var productDialogFragment: AddProductDialogFragment
    val isExitFromAddOrder = false
    val companyMasterList: ArrayList<CompanyMasterResponse> = arrayListOf()
    val branchMasterList: ArrayList<BranchMasterResponse> = arrayListOf()
    val divisionMasterList: ArrayList<DivisionMasterResponse> = arrayListOf()
    val categoryList: ArrayList<CategoryMasterResponse> = arrayListOf()
    private var imageAnyList: ArrayList<Any> = arrayListOf()
    private var imageAdapter: ImageAdapter? = null
    var selectedCompany: CompanyMasterResponse? = null
    var selectedBranch: BranchMasterResponse? = null
    var selectedDivision: DivisionMasterResponse? = null
    var selectedCategory: CategoryMasterResponse? = null
    private var selectedModeOfCommunication = 0
    private var isExpanded = false
    private var isShippingExpanded = false
    private var partyDealerPageNo = 1
    private var distributorPageNo = 1
    private var consigneePageNo = 1
    private var isScrolling = false
    private var isLastPage = false
    private var layoutManager: LinearLayoutManager? = null
    private lateinit var partyDealerDialog: AlertDialog
    private lateinit var partyDealerAdapter: PartyDealerListAdapter
    private lateinit var paginationLoader: ProgressBar
    private lateinit var tvSearchGO: TextView
    private lateinit var edtSearchPartyDealer: EditText
    private var isCompanyChange = false
    private var allowEdit: Boolean = false
    private var allowDelete: Boolean = false
    val currencyList: ArrayList<CommonDropDownResponse> = arrayListOf()
    val transactionTypeList: ArrayList<CommonDropDownResponse> = arrayListOf()
    val freightTermsTypeList: ArrayList<CommonDropDownResponse> = arrayListOf()
    private var selectedCurrency: CommonDropDownResponse? = null
    private var selectedTransactionType: CommonDropDownResponse? = null
    private var selectedFreightTerms: CommonDropDownResponse? = null
    private var shippingAddressList: ArrayList<ShippingAddressResponse> = arrayListOf()
    lateinit var rvItems: RecyclerView
    lateinit var tvNoDataFound: TextView
    private var isSearchTriggered = false
    private var accountNameFromVisit: String = ""
    private var accountMasterIdFromVisit: Int = 0
    private var contactPersonNameFromVisit = ""
    private var isPartyChangeFromVisit = false
    private var isForApproval : Boolean = false

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
                etPrice.setText(CommonMethods.formatBigDecimal(item.salesPrice ?: BigDecimal.ZERO))
                selectedProduct = item
                tvUnit.text = item.unit
            }
        }

    companion object {
        fun newInstance(
            orderId: Int,
            allowEdit: Boolean?,
            allowDelete: Boolean?,
            accountName: String?,
            accountMasterId: Int?,
            contactPersonName: String?,
            isForApproval: Boolean
        ): AddOrderEntryFragment {
            val args = Bundle()
            args.putInt(ARG_PARAM1, orderId)
            args.putBoolean(ARG_PARAM2, allowEdit ?: false)
            args.putBoolean(ARG_PARAM3, allowDelete ?: false)
            args.putString(ARG_PARAM4, accountName ?: "")
            args.putInt(ARG_PARAM5, accountMasterId ?: 0)
            args.putString(ARG_PARAM6, contactPersonName ?: "")
            args.putBoolean(ARG_PARAM7, isForApproval)
            val fragment = AddOrderEntryFragment()
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
            allowEdit = it.getBoolean(ARG_PARAM2, false)
            allowDelete = it.getBoolean(ARG_PARAM3, false)
            accountNameFromVisit = it.getString(ARG_PARAM4, "")
            accountMasterIdFromVisit = it.getInt(ARG_PARAM5, 0)
            contactPersonNameFromVisit = it.getString(ARG_PARAM6, "")
            isForApproval = it.getBoolean(ARG_PARAM7, false)
            userId = loginData.userId
        }
        mActivity.bottomHide()
        binding.toolbar.tvHeader.text =
            if (orderId > 0) getString(R.string.order_details) else getString(R.string.add_order_entry)
        binding.tvSubmit.text =
            if (orderId > 0) getString(R.string.update) else getString(R.string.submit)

        if ((accountNameFromVisit ?: "").isNotEmpty()) {
            binding.tvPartyDealer.text = accountNameFromVisit
            binding.etContactPersonName.setText(contactPersonNameFromVisit)
            selectedPartyDealerId = accountMasterIdFromVisit
        }

        selectedCurrency = CommonDropDownResponse(dropdownKeyId = "0", dropdownName = "")
        selectedTransactionType = CommonDropDownResponse(dropdownKeyId = "0", dropdownName = "")
        selectedCompany = CompanyMasterResponse(companyMasterId = 0, companyName = "")
        selectedBranch = BranchMasterResponse(branchMasterId = 0, branchName = "")
        selectedDivision = DivisionMasterResponse(divisionMasterId = 0, divisionName = "")
        selectedCategory = CategoryMasterResponse(categoryMasterId = 0, categoryName = "")
        selectedFreightTerms = CommonDropDownResponse(dropdownKeyId = "0", dropdownName = "")


        binding.tvDate.text = CommonMethods.getCurrentDate()
        binding.tvDispatchDate.text = CommonMethods.getCurrentDate()
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.tvSubmit.setOnClickListener(this)
        binding.cardImageCapture.setOnClickListener(this)
        setupImageUploadRecyclerView()

        if(isForApproval){
            binding.llApprovalRemarks.visibility = View.VISIBLE
            binding.tvSubmit.visibility = View.GONE
            binding.llAcceptReject.visibility = View.VISIBLE
        }else{
            if(orderId > 0){
                binding.tvSubmit.visibility = View.GONE
                binding.llAcceptReject.visibility = View.GONE
            }else{
                binding.tvSubmit.visibility = View.VISIBLE
                binding.llAcceptReject.visibility = View.GONE
            }
        }
        binding.tvAccept.setOnClickListener(this)
        binding.tvReject.setOnClickListener(this)

        setOrderDetailsAdapter()
        binding.llHeader.setOnClickListener {
            toggleSectionVisibility(binding.llOptionalFields, binding.ivToggle, true)
        }
        binding.llShipingAndOtherDetails.setOnClickListener {
            toggleSectionVisibility(binding.llShippingFields, binding.ivShippingToggle, false)
        }
        if (orderId > 0) {
            if (allowEdit) {
                binding.toolbar.imgEdit.visibility = View.VISIBLE
            }
            if (allowDelete) {
                binding.toolbar.imgDelete.visibility = View.VISIBLE
            }

            binding.toolbar.imgEdit.setOnClickListener(this)
            binding.toolbar.imgDelete.setOnClickListener(this)

            formViewMode(true)
            callOrderDetailsApi()
        } else {
            formViewMode(false)
        }
    }

    private suspend fun executeAPIsAndSetupData() {
        withContext(Dispatchers.IO) {
            try {
                val companyListDeferred = async { callCompanyListApi() }
                val freightTermsListDeferred =
                    async { callCommonDropDownListApi(DROP_DOWN_FREIGHT_TERMS) }
                companyListDeferred.await()
                freightTermsListDeferred.await()

            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("TAG", "executeAPIsAndSetupData: " + e.message.toString())
            }
        }
    }

    private fun formViewMode(isViewOnly: Boolean) {
        isReadOnly = isViewOnly
        if (isViewOnly) {
            //binding.tvOrderMode.visibility = View.VISIBLE
            //binding.spOrderMode.visibility = View.GONE
            binding.tvCategory.visibility = View.VISIBLE
            binding.etContactPersonName.visibility = View.GONE
            binding.tvContactPerson.visibility = View.VISIBLE
            binding.tvRemarks.visibility = View.VISIBLE
            binding.etRemarks.visibility = View.GONE
            //binding.llAcceptReject.visibility = View.GONE
            binding.flCategory.visibility = View.GONE
            binding.edtPaymentTerms.isEnabled = false
            binding.edtTermsAndCondition.isEnabled = false
            binding.edtShippingAddress.isEnabled = false
            disableRadioButtons(false)
        } else {
            binding.radioGroupModeOfCom.isEnabled = true
            binding.radioGroupModeOfCom.isClickable = true
            binding.etContactPersonName.visibility = View.VISIBLE
            binding.tvContactPerson.visibility = View.GONE
            binding.tvRemarks.visibility = View.GONE
            binding.etRemarks.visibility = View.VISIBLE
            //binding.llAcceptReject.visibility = View.VISIBLE
            disableRadioButtons(true)
            binding.flConsingneeName.setOnClickListener(this)
            binding.llSelectTransactionType.setOnClickListener(this)
            binding.llSelectFreight.setOnClickListener(this)
            binding.edtPaymentTerms.isEnabled = true
            binding.edtTermsAndCondition.isEnabled = true
            binding.edtShippingAddress.isEnabled = true

            binding.radioGroupModeOfCom.setOnCheckedChangeListener { group, checkedId ->
                val radioButton = view?.findViewById<RadioButton>(checkedId)
                when (radioButton?.text) {
                    mActivity.getString(R.string.personally) -> selectedModeOfCommunication = 1
                    mActivity.getString(R.string.phone) -> selectedModeOfCommunication = 2
                    mActivity.getString(R.string.email) -> selectedModeOfCommunication = 3
                }
            }
            binding.imgSelectShippingAddress.setOnClickListener(this)
            binding.imgAddressClear.setOnClickListener(this)
            binding.tvDispatchDate.setOnClickListener(this)

            if (orderId <= 0) {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        executeAPIsAndSetupData()
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        e.printStackTrace()
                    }
                }
                binding.tvCategory.visibility = View.GONE
                binding.llSelectCompany.setOnClickListener(this)
                binding.llSelectBranch.setOnClickListener(this)
                binding.llSelectDivision.setOnClickListener(this)
                binding.tvDate.setOnClickListener(this)
                binding.flSelectDistributor.setOnClickListener(this)
                binding.btnAddOrderDetails.setOnClickListener(this)
                //binding.llSelectCurrency.setOnClickListener(this)
                binding.flPartyDealer.setOnClickListener(this)
            } else {
                //binding.llSelectCurrency.setOnClickListener(null)
                binding.llSelectCompany.setOnClickListener(null)
                binding.llSelectBranch.setOnClickListener(null)
                binding.llSelectDivision.setOnClickListener(null)
                binding.tvDate.setOnClickListener(null)
                binding.flPartyDealer.setOnClickListener(null)
                callShippingAddressApi()
                callCommonDropDownListApi(DROP_DOWN_TRANSACTION_TYPE)
                callCommonDropDownListApi(DROP_DOWN_FREIGHT_TERMS)
                if (orderId > 0) {
                    binding.tvCategory.visibility = View.VISIBLE
                    binding.flSelectDistributor.setOnClickListener(this)
                    binding.btnAddOrderDetails.setOnClickListener(this)
                }
            }
        }
    }

    private fun disableRadioButtons(isRadioEnable: Boolean) {
        if (!isRadioEnable) {
            for (i in 0 until binding.radioGroupModeOfCom.childCount) {
                val radioButton = binding.radioGroupModeOfCom.getChildAt(i)
                radioButton.isEnabled = false
                radioButton.isClickable = false
            }
        } else {
            for (i in 0 until binding.radioGroupModeOfCom.childCount) {
                val radioButton = binding.radioGroupModeOfCom.getChildAt(i)
                radioButton.isEnabled = true
                radioButton.isClickable = true
            }
        }
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

    private fun askCameraGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                openAlbumForList()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                openAlbumForList()
            }
        } else {
            val arrListOffPermission = arrayListOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOffPermission) {
                openAlbumForList()
            }
        }

    }

    private fun setupImageUploadRecyclerView() {
        val apiUrl = appDao.getAppRegistration().apiHostingServer
        binding.rvImages.layoutManager =
            LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(mActivity, apiUrl)
        binding.rvImages.adapter = imageAdapter
        handleAssetRVView(0)

        imageAdapter?.setOnClick(object : ImageAdapter.OnAssetImageCancelClick {
            override fun onImageCancel(position: Int) {
                if (!isReadOnly && position != RecyclerView.NO_POSITION && imageAnyList.size > 0) {
                    imageAnyList.removeAt(position)
                    imageAdapter?.addImage(imageAnyList, false)
                    handleAssetRVView(imageAnyList.size)
                }
            }

            override fun onImagePreview(position: Int) {
                if (imageAnyList[position] is String) {
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDatabase.appDao()
                            .getAppRegistration().apiHostingServer + imageAnyList[position]
                    )
                } else {
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity, imageAnyList[position]
                    )
                }
            }
        })
    }

    fun handleAssetRVView(imageList: Int) {
        try {
            binding.txtNoFilePathImages.visibility = if (imageList == 0) View.VISIBLE else View.GONE
            binding.rvImages.visibility = if (imageList == 0) View.GONE else View.VISIBLE
        } catch (e: java.lang.Exception) {
            Log.e("TAG", "handleAssetRVView: *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
        }
    }

    private fun openAlbumForList() {
        val maxImageLimit = 4
        val remainingLimit = maxImageLimit - imageAnyList.size

        AlbumUtility(mActivity, true).openAlbumAndHandleImageMultipleSelection(
            onImagesSelected = { selectedFiles ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val modifiedImageFiles = selectedFiles.mapNotNull { file ->
                        CommonMethods.addDateAndTimeToFile(
                            file, CommonMethods.createImageFile(mActivity) ?: return@mapNotNull null
                        )
                    }

                    withContext(Dispatchers.Main) {
                        if (modifiedImageFiles.isNotEmpty()) {
                            imageAnyList.addAll(modifiedImageFiles)
                            imageAdapter?.addImage(imageAnyList, false)
                            handleAssetRVView(imageAnyList.size)
                        }
                    }
                }
            },
            onError = {
                showToastMessage(mActivity, it)
            },
            remainingLimit
        )
    }

    private fun setOrderDetailsAdapter() {
        orderDetailsAdapter = OrderDetailsAdapter(mActivity, orderDetailsList, this@AddOrderEntryFragment)
        val layoutManager = LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
        binding.rvProduct.layoutManager = layoutManager
        binding.rvProduct.adapter = orderDetailsAdapter
    }

    private fun callCommonDropDownListApi(dropDownType: String) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null,
                isCancelVisibility = false
            )
            return
        }

        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()

        var parameterString = ""
        if (dropDownType == DROP_DOWN_TRANSACTION_TYPE) {
            parameterString =
                "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and DivisionMasterid=${selectedDivision?.divisionMasterId} and CategoryMasterId=${selectedCategory?.categoryMasterId} and AccountMasterId=$selectedPartyDealerId and $FORM_ID_ORDER_ENTRY"
        }

        val regionCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDropDownMasterDetails(dropDownType, "")

        regionCall?.enqueue(object : Callback<List<CommonDropDownResponse>> {
            override fun onResponse(
                call: Call<List<CommonDropDownResponse>>,
                response: Response<List<CommonDropDownResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            when (dropDownType) {
                                DROP_DOWN_CURRENCY -> {
                                    currencyList.clear()
                                    currencyList.addAll(it)
                                }

                                DROP_DOWN_TRANSACTION_TYPE -> {
                                    transactionTypeList.clear()
                                    transactionTypeList.addAll(it)
                                }

                                DROP_DOWN_FREIGHT_TERMS -> {
                                    freightTermsTypeList.clear()
                                    freightTermsTypeList.addAll(it)
                                }

                                else -> {

                                }
                            }
                        } else {
                            /*CommonMethods.showToastMessage(
                                mActivity,
                                "$dropDownType List not found"
                            )*/
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

            override fun onFailure(call: Call<List<CommonDropDownResponse>>, t: Throwable) {
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

    private fun callCategoryListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        if (selectedDivision == null || selectedDivision?.divisionMasterId == 0) {
            showToastMessage(
                mActivity,
                "Please select division"
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val jsonReq = JsonObject()
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and DivisionMasterid=${selectedDivision?.divisionMasterId} and $FORM_ID_ORDER_ENTRY"
        )

        val categoryListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCategoryMasterList(jsonReq)

        categoryListCall?.enqueue(object : Callback<List<CategoryMasterResponse>> {
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
            this@AddOrderEntryFragment
        )
        binding.spCategory.adapter = adapter

        // Set the selected item based on the condition
        if (isCompanyChange && categoryList.size == 2) {
            selectedCategory = categoryList[1]
            binding.spCategory.setSelection(1)
        } else if (isCompanyChange) {
            binding.spCategory.setSelection(0)
        } else {
            val selectedCategoryIndex = categoryList.indexOfFirst { it.categoryMasterId == selectedCategory?.categoryMasterId }
            binding.spCategory.setSelection(selectedCategoryIndex)
        }

        // Handle item selection
        binding.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedType = categoryList[position]
                if ((selectedType.categoryMasterId ?: 0) > 0) {
                    selectedCategory = selectedType
                    clearPartyTransactionAddressData()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle if no item is selected (optional)
            }
        }
    }

    fun clearPartyTransactionAddressData() {
        binding.tvPartyDealer.text = ""
        selectedPartyDealerId = 0
        selectedTransactionType = CommonDropDownResponse(dropdownKeyId = "0", dropdownName = "")
        binding.edtShippingAddress.setText("")
        binding.etContactPersonName.setText("")
        if (!isPartyChangeFromVisit && (accountNameFromVisit ?: "").isNotEmpty()) {
            binding.tvPartyDealer.text = accountNameFromVisit
            selectedPartyDealerId = accountMasterIdFromVisit
            binding.etContactPersonName.setText(contactPersonNameFromVisit)
        }
    }


    private fun callAccountMasterList(apiType: Int) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        paginationLoader.visibility = View.VISIBLE

        val appRegistrationData = appDao.getAppRegistration()

        val orderDateString =
            CommonMethods.convertDateStringForOrderEntry(binding.tvDate.text.toString())
        val orderDispatchString =
            CommonMethods.convertDateStringForOrderEntry(binding.tvDispatchDate.text.toString())

        var fieldName = ""
        var pageNo = 0
        when (apiType) {
            TYPE_PARTY_DEALER -> {
                fieldName = "FieldName=Order/Party"
                pageNo = partyDealerPageNo
            }

            TYPE_DISTRIBUTOR -> {
                fieldName = "FieldName=Order/Distributor"
                pageNo = distributorPageNo
            }

            TYPE_CONSIGNEE -> {
                fieldName = "FieldName=Order/Consignee"
                pageNo = consigneePageNo
            }
        }

        val parameterString =
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and DivisionMasterid=${selectedDivision?.divisionMasterId} and" +
                    " CategoryMasterId=${selectedCategory?.categoryMasterId} and EntryDate=$orderDateString and $FORM_ID_ORDER_ENTRY and $fieldName and AccountName like '%${edtSearchPartyDealer.text}%'"

        val jsonReq = JsonObject()
        jsonReq.addProperty("AccountMasterId", 0)
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty("ParameterString", parameterString)
        jsonReq.addProperty("pageNo", pageNo)

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
                            handleApiResponse(response, apiType)
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
        response: Response<List<AccountMasterList>>,
        responseType: Int
    ) {
        isSearchTriggered = false
        paginationLoader.visibility = View.GONE

        if (response.isSuccessful) {
            response.body()?.let { data ->
                if (data.isNotEmpty()) {
                    if (data.size == 1) {
                        isSearchTriggered = true
                    }
                    rvItems.visibility = View.VISIBLE
                    tvNoDataFound.visibility = View.GONE
                    when (responseType) {
                        TYPE_PARTY_DEALER -> {
                            if (partyDealerPageNo == 1) {
                                accountMasterList.clear()
                                isLastPage = false
                            }
                            accountMasterList.addAll(data)
                        }

                        TYPE_DISTRIBUTOR -> {
                            if (distributorPageNo == 1) {
                                distributorList.clear()
                                isLastPage = false
                            }
                            distributorList.addAll(data)
                        }

                        TYPE_CONSIGNEE -> {
                            if (consigneePageNo == 1) {
                                consigneeList.clear()
                                isLastPage = false
                            }
                            consigneeList.addAll(data)
                        }
                    }
                    partyDealerAdapter.notifyDataSetChanged()
                    isScrolling = false
                } else {
                    isLastPage = true
                    when (responseType) {
                        TYPE_PARTY_DEALER -> {
                            if (partyDealerPageNo <= 1) {
                                rvItems.visibility = View.GONE
                                tvNoDataFound.visibility = View.VISIBLE
                            }
                        }

                        TYPE_DISTRIBUTOR -> {
                            if (distributorPageNo <= 1) {
                                rvItems.visibility = View.GONE
                                tvNoDataFound.visibility = View.VISIBLE
                            }
                        }

                        TYPE_CONSIGNEE -> {
                            if (consigneePageNo <= 1) {
                                rvItems.visibility = View.GONE
                                tvNoDataFound.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
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
                "ProductGroupId=$productGroupId and AccountMasterId=$selectedPartyDealerId EntryDate=$orderDateString and $FORM_ID_ORDER_ENTRY"
            jsonReq.addProperty("ProductId", 0)
            jsonReq.addProperty("ProductGroupId", productGroupId)
            jsonReq.addProperty("ParameterString", parameterString)
            //jsonReq.addProperty("pageNo", if(ispar)partyDealerPageNo)

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
                                groupList.add(
                                    ProductGroupResponse(
                                        orderId = 0,
                                        orderDetailsId = 0,
                                        productGroupId = 0,
                                        productGroupName = "All Group"
                                    )
                                )
                                groupList.addAll(it)
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


    private fun callDeleteOrderDetailsApi(item: ProductGroupResponse) {
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
                        calculateTotalOrderAmount()
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
        //accountMasterId = orderDetails.accountMasterId ?: 0
        binding.tvDate.text = orderDetails.orderDate
        binding.tvOrderMode.text = orderDetails.orderModeName
        selectedCategory = CategoryMasterResponse(
            categoryMasterId = orderDetails.categoryMasterId,
            categoryName = orderDetails.categoryName
        )

        binding.etContactPersonName.setText(orderDetails.contactPersonName)
        binding.tvContactPerson.text = orderDetails.contactPersonName
        binding.tvPartyDealer.text = orderDetails.accountName
        selectedPartyDealerId = orderDetails.accountMasterId ?: -1
        binding.tvSelectDistributor.text = orderDetails.distributorAccountName
        selectedDistributorId = orderDetails.distributorAccountMasterId ?: -1
        binding.etRemarks.setText(orderDetails.remarks)
        binding.tvRemarks.text = orderDetails.remarks
        binding.tvSelectCompany.text = orderDetails.companyName
        binding.tvSelectBranch.text = orderDetails.branchName
        binding.tvSelectDivision.text = orderDetails.divisionName
        binding.tvCategory.text = orderDetails.categoryName
        binding.tvSelectTransactionType.text = orderDetails.transactionTypeName
        binding.tvSelectConsigneeName.text = orderDetails.consigneeName
        binding.tvSelectFreight.text = orderDetails.freightTermsName
        binding.edtShippingAddress.setText(orderDetails.shippingAddress.toString())
        selectedShippingAddressId = orderDetails.shippingAddressId ?: 0
        binding.tvDispatchDate.text = orderDetails.expectedDispatchDate
        binding.edtPaymentTerms.setText(orderDetails.creditDays.toString() ?: "")
        binding.edtTermsAndCondition.setText(orderDetails.termsAndCondition ?: "")
        binding.tvSelectFreight.text = orderDetails.freightTermsName

        selectedCompany = CompanyMasterResponse(
            companyMasterId = orderDetails.companyMasterId,
            companyName = orderDetails.companyName
        )
        selectedBranch = BranchMasterResponse(
            branchMasterId = orderDetails.branchMasterId,
            branchName = orderDetails.branchName
        )
        selectedDivision = DivisionMasterResponse(
            divisionMasterId = orderDetails.divisionMasterId,
            divisionName = orderDetails.divisionName
        )
        selectedCategory = CategoryMasterResponse(
            categoryMasterId = orderDetails.categoryMasterId,
            categoryName = orderDetails.categoryName
        )
        selectedTransactionType = CommonDropDownResponse(
            dropdownKeyId = orderDetails.ddmTransactionTypeId.toString(),
            dropdownName = orderDetails.transactionTypeName
        )
        selectedFreightTerms = CommonDropDownResponse(
            dropdownKeyId = orderDetails.ddmFreightTermsId.toString(),
            dropdownName = orderDetails.freightTermsName
        )
        selectedConsigneeId = orderDetails.consigneeAccountMasterId ?: 0

        /*selectedTransactionType = CommonDropDownResponse(
            dropdownName = orderDetails.ddmTransactionTypeId
        )*/

        selectedModeOfCommunication = orderDetails.modeOfCommunication ?: 0
        when (selectedModeOfCommunication) {
            1 -> binding.radioGroupModeOfCom.check(R.id.rbPersonally)
            2 -> binding.radioGroupModeOfCom.check(R.id.rbPhone)
            3 -> binding.radioGroupModeOfCom.check(R.id.rbEmail)
            else -> binding.radioGroupModeOfCom.clearCheck()
        }

        if (orderDetails.orderDetails != null && orderDetails.orderDetails.size > 0) {
            orderDetailsList.clear()
            orderDetails.orderDetails.forEach { it.standardDiscount = it.discount.toDouble() }
            orderDetailsList.addAll(orderDetails.orderDetails)
            orderDetailsAdapter.refreshAdapter(orderDetailsList)
            handleOrderDetailsRVVisibility(orderDetailsList)
        }
        if ((orderDetails.filePath ?: "").isNotEmpty()) {
            imageAnyList.add((orderDetails.filePath) ?: "")
        }
        if ((orderDetails.filePath2 ?: "").isNotEmpty()) {
            imageAnyList.add((orderDetails.filePath2) ?: "")
        }
        if ((orderDetails.filePath3 ?: "").isNotEmpty()) {
            imageAnyList.add((orderDetails.filePath3) ?: "")
        }
        if ((orderDetails.filePath4 ?: "").isNotEmpty()) {
            imageAnyList.add((orderDetails.filePath4) ?: "")
        }

        Log.e("TAG", "setupDataForUpdate:BEFORE " + imageAnyList.toString())

        imageAdapter?.addImage(imageAnyList, true)
        handleAssetRVView(imageAnyList.size)

        imageUrl = orderDetails.filePath ?: ""
        calculateTotalOrderAmount()
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
            Log.e("TAG", "onHiddenChanged: LISTING CALLED")
            // callOrderDetailsApi()
        }
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

            R.id.flPartyDealer -> {
                if (orderDetailsList.size > 0) {
                    return
                }
                avoidDoubleClicks(binding.flPartyDealer)
                /*if (selectedCategory == null || selectedCategory?.categoryMasterId!! <= 0) {
                    showToastMessage(mActivity, "Please select order category")
                    return
                }*/
                showPartyDealerListDialog(TYPE_PARTY_DEALER)
            }

            R.id.flConsingneeName -> {
                avoidDoubleClicks(binding.flConsingneeName)
                /*if (selectedCategory == null || selectedCategory?.categoryMasterId!! <= 0) {
                    showToastMessage(mActivity, "Please select order category")
                    return
                }*/
                showPartyDealerListDialog(TYPE_CONSIGNEE)
            }

            R.id.flSelectDistributor -> {
                avoidDoubleClicks(binding.flPartyDealer)
                /*if (selectedCategory == null || selectedCategory?.categoryMasterId!! <= 0) {
                    showToastMessage(mActivity, "Please select order category")
                    return
                }*/
                showPartyDealerListDialog(TYPE_DISTRIBUTOR)
            }

            R.id.btnAddOrderDetails -> {
                avoidDoubleClicks(binding.btnAddOrderDetails)
                holdDetailsDataList.clear()
                if (orderDetailsList != null && orderDetailsList.size > 0) {
                    holdDetailsDataList.addAll(orderDetailsList)
                }
                showProductDialogFragment()
                //showAddProduct(null, -1)
            }

            R.id.tvSubmit -> {
                avoidDoubleClicks(binding.tvSubmit)
                requestValidate()
            }

            R.id.cardImageCapture -> {
                if (!isReadOnly) {
                    if (imageAnyList.size == 4) {
                        showToastMessage(
                            mActivity,
                            getString(R.string.photo_upload_limit_reached)
                        )
                        return
                    }
                    askCameraGalleryPermission()
                }
            }

            R.id.tvDate -> {
                if (orderDetailsList.size > 0) {
                    return
                }
                val selectedCalendar = if (binding.tvDate.text.toString()
                        .isEmpty()
                ) null else CommonMethods.dateStringToCalendar(binding.tvDate.text.toString())
                openDatePicker(
                    selectedCalendar,
                    isMaxDate = true,
                ) { selectedDate ->
                    if (binding.tvDate.text.toString().isNotEmpty()) {
                        binding.tvDate.text = selectedDate
                    } else {
                        binding.tvDate.text = selectedDate
                    }
                }
            }

            R.id.tvDispatchDate -> {
                val selectedCalendar = if (binding.tvDispatchDate.text.toString()
                        .isEmpty()
                ) null else CommonMethods.dateStringToCalendar(binding.tvDispatchDate.text.toString())
                openDatePicker(
                    selectedCalendar
                ) { selectedDate ->
                    if (binding.tvDispatchDate.text.toString().isNotEmpty()) {
                        binding.tvDispatchDate.text = selectedDate
                    } else {
                        binding.tvDispatchDate.text = selectedDate
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
                            binding.toolbar.imgEdit.visibility = View.GONE
                            binding.llAcceptReject.visibility = View.GONE
                            binding.tvSubmit.visibility = View.VISIBLE
                            binding.tvSubmit.text = getString(R.string.update)
                            formViewMode(false)
                        }
                    },
                    positiveButtonText = getString(R.string.yes),
                    negativeButtonText = getString(R.string.no)
                )

            }

            R.id.llSelectTransactionType -> {
                if (selectedPartyDealerId == 0) {
                    CommonMethods.showToastMessage(mActivity, "Please select party/dealer")
                    return
                }
                if (transactionTypeList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_TRANSACTION_TYPE,
                        commonDropDownList = transactionTypeList,
                        commonDropDownInterfaceDetect = this as UserSearchDialogUtil.CommonDropDownDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.transaction_list_not_found)
                    )
                }
            }

            R.id.llSelectFreight -> {
                if (freightTermsTypeList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_FREIGHT_TERMS,
                        commonDropDownList = freightTermsTypeList,
                        commonDropDownInterfaceDetect = this as UserSearchDialogUtil.CommonDropDownDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.freight_list_not_found)
                    )
                }
            }

            R.id.imgSelectShippingAddress -> {
                if (selectedPartyDealerId == 0) {
                    CommonMethods.showToastMessage(mActivity, "Please select party/dealer")
                    return
                }
                if (shippingAddressList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_SHIPPING_ADDRESS,
                        shippingList = shippingAddressList,
                        shippingAddressInterfaceDetect = this as UserSearchDialogUtil.ShippingAddressDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.shipping_address_list_not_found)
                    )
                }
            }
            /*R.id.llSelectCurrency -> {
                if (currencyList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_CURRENCY_TYPE,
                        commonDropDownList = currencyList,
                        commonDropDownInterfaceDetect = this as UserSearchDialogUtil.CommonDropDownDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.region_list_not_found)
                    )
                }
            }*/

            R.id.llSelectCompany -> {
                if (orderDetailsList.size > 0) {
                    return
                }
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

            R.id.llSelectBranch -> {
                if (orderDetailsList.size > 0) {
                    return
                }
                if (selectedCompany == null || selectedCompany?.companyMasterId == 0) {
                    showToastMessage(
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
                    showToastMessage(
                        mActivity,
                        getString(R.string.branch_list_not_found)
                    )
                }
            }

            R.id.llSelectDivision -> {
                if (orderDetailsList.size > 0) {
                    return
                }
                if (selectedBranch == null || selectedBranch?.branchMasterId == 0) {
                    showToastMessage(
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
                    showToastMessage(
                        mActivity,
                        getString(R.string.branch_list_not_found)
                    )
                }
            }

            R.id.imgAddressClear -> {
                selectedShippingAddressId = 0
                binding.edtShippingAddress.setText("")
                binding.edtShippingAddress.isEnabled = true
                binding.imgAddressClear.visibility = View.GONE
            }
            R.id.tvAccept -> {
                callApproveRejectOrder(true)
            }
            R.id.tvReject -> {
                callApproveRejectOrder(false)
            }
        }
    }

    private fun showProductDialogFragment() {
        productDialogFragment = AddProductDialogFragment.newInstance(
            binding.tvDate.text.toString(),
            selectedPartyDealerId,
            orderDetailsList,
            selectedCompany?.companyMasterId ?: 0,
            selectedBranch?.branchMasterId ?: 0,
            selectedDivision?.divisionMasterId ?: 0,
            selectedCategory?.categoryMasterId ?: 0
        )
        productDialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyAppTheme)
        productDialogFragment.setOrderDetailsListener(this)
        productDialogFragment.show(mActivity.supportFragmentManager, "AddProductDialogFragment")
        productDialogFragment.setDialogDismissListener(object :
            AddProductDialogFragment.DialogDismissListener {
            override fun onDialogDismissed(isCartClicked: Boolean) {
                Log.e("TAG", "onDialogDismissed: " + isCartClicked)
                if (!isCartClicked) {
                    orderDetailsList.clear()
                    orderDetailsList.addAll(holdDetailsDataList)
                    orderDetailsList.removeIf { it.qty <= BigDecimal.ZERO }
                    handleOrderDetailsRVVisibility(orderDetailsList)
                    if (orderDetailsList.size > 0) {
                        binding.spCategory.setEnabled(false);
                        binding.spCategory.setClickable(false);
                        //binding.spCategory.setAdapter(typeAdapter);
                        setOrderDetailsAdapter()
                    }
                    calculateTotalOrderAmount()
                }
            }
        })
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

    private fun requestValidate() {
        /*if (binding.spOrderMode.selectedItemPosition == 0) {
            showToastMessage(
                mActivity,
                getString(R.string.please_selected_order_mode)
            )
            return
        }*/

        if (binding.spCategory.selectedItemPosition == 0) {
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
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("BranchMasterId", ID_ZERO)
        objReq.addProperty("UserId", loginData.userId)
        objReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId ?: 0} and $FORM_ID_ORDER_ENTRY"
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

    private fun callDivisionListApi() {
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
                            if (isCompanyChange && it.size == 1) {
                                selectedDivision = DivisionMasterResponse(
                                    divisionMasterId = divisionMasterList[1].divisionMasterId,
                                    divisionName = divisionMasterList[1].divisionName
                                )
                                binding.tvSelectDivision.text = selectedDivision?.divisionName ?: ""
                                callCategoryListApi()
                            } else {
                                //callCategoryListApi()
                            }
                        } else {
                            showToastMessage(
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

    private fun callShippingAddressApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty("ParameterString", FORM_ID_ORDER_ENTRY)
        objReq.addProperty("accountMasterId", selectedPartyDealerId)
        objReq.addProperty("pageNo", 0)

        val shippingAddressCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getShippingAddress(objReq)

        shippingAddressCall?.enqueue(object : Callback<List<ShippingAddressResponse>> {
            override fun onResponse(
                call: Call<List<ShippingAddressResponse>>,
                response: Response<List<ShippingAddressResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            shippingAddressList.clear()
                            shippingAddressList.addAll(it)
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

            override fun onFailure(call: Call<List<ShippingAddressResponse>>, t: Throwable) {
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
        objReq.addProperty(
            "OrderDate",
            CommonMethods.convertToAppDateFormat(binding.tvDate.text.toString(), "MM/dd/yyyy")
        )

        objReq.addProperty("OrderMode", 0)
        objReq.addProperty("CategoryMasterId", selectedCategory?.categoryMasterId)
        objReq.addProperty("AccountMasterId", selectedPartyDealerId)
        objReq.addProperty("ContactPersonName", binding.etContactPersonName.text.toString().trim())
        objReq.addProperty("DistributorAccountMasterId", selectedDistributorId)
        objReq.addProperty("Remarks", binding.etRemarks.text.toString().trim())
        objReq.addProperty("CompanyMasterId", selectedCompany?.companyMasterId)
        objReq.addProperty("BranchMasterId", selectedBranch?.branchMasterId)
        objReq.addProperty("DivisionMasterId", selectedDivision?.divisionMasterId)
        objReq.addProperty("OrderStatusId", 1)
        objReq.addProperty("UserId", loginData.userId)
        objReq.addProperty("ModeOfCommunication", selectedModeOfCommunication)
        objReq.addProperty("ddmTransactionTypeId", selectedTransactionType?.dropdownKeyId)
        objReq.addProperty("consigneeAccountMasterId", selectedConsigneeId)
        objReq.addProperty("shippingAddress", binding.edtShippingAddress.text.toString().trim())
        objReq.addProperty("shippingAddressId", selectedShippingAddressId)
        objReq.addProperty("ddmFreightTermsId", selectedFreightTerms?.dropdownKeyId)
        objReq.addProperty("expectedDispatchDate", CommonMethods.convertToAppDateFormat(binding.tvDispatchDate.text.toString(), "MM/dd/yyyy"))
        val creditDays = if (binding.edtPaymentTerms.text.toString().isEmpty()) {
            0
        } else {
            binding.edtPaymentTerms.text.toString().toInt()
        }
        objReq.addProperty("creditDays", creditDays)
        objReq.addProperty("termsAndCondition", binding.edtTermsAndCondition.text.toString())
        objReq.addProperty("EntrySystem", 1)


        if (imageAnyList.size > 0) {
            if (imageAnyList[0] is String) {
                objReq.addProperty("FilePath", imageAnyList[0].toString())
            } else {
                val imageBase64 =
                    CommonMethods.convertImageFileToBase64(imageAnyList[0] as File).toString()
                objReq.addProperty("FilePath", imageBase64)
            }
            if (imageAnyList.size > 1) {
                if (imageAnyList[1] is String) {
                    objReq.addProperty("FilePath2", imageAnyList[1].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[1] as File).toString()
                    objReq.addProperty("FilePath2", imageBase64)
                }
            } else {
                objReq.addProperty("FilePath2", "")
            }
            if (imageAnyList.size > 2) {
                if (imageAnyList[2] is String) {
                    objReq.addProperty("FilePath3", imageAnyList[2].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[2] as File).toString()
                    objReq.addProperty("FilePath3", imageBase64)
                }
            } else {
                objReq.addProperty("FilePath3", "")
            }
            if (imageAnyList.size > 3) {
                if (imageAnyList[3] is String) {
                    objReq.addProperty("FilePath4", imageAnyList[3].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[3] as File).toString()
                    objReq.addProperty("FilePath4", imageBase64)
                }
            } else {
                objReq.addProperty("FilePath4", "")
            }
        } else {
            objReq.addProperty("FilePath", "")
            objReq.addProperty("FilePath2", "")
            objReq.addProperty("FilePath3", "")
            objReq.addProperty("FilePath4", "")
        }

        val objDetailsArray = JsonArray()
        for (i in orderDetailsList) {
            val altQty = (i.qty * (i.conversionFactor?.toBigDecimal() ?: BigDecimal.ZERO))
            val objDetails = JsonObject()
            objDetails.addProperty("OrderId", orderId)
            objDetails.addProperty("OrderDetailsId", i.orderDetailsId)
            objDetails.addProperty("ProductId", i.productId)
            objDetails.addProperty("ProductName", i.productName)
            objDetails.addProperty("Unit", i.unit)
            objDetails.addProperty("Quantity", i.qty)
            objDetails.addProperty("Rate", i.salesPrice)
            objDetails.addProperty("Amount", i.amount)
            objDetails.addProperty("MRP", i.mrp)
            objDetails.addProperty("Discount", i.standardDiscount)
            objDetails.addProperty("AdditionalDiscount", i.additionalDiscount)
            objDetails.addProperty("DiscountAmount", i.discountAmount)
            objDetails.addProperty("AltUnit", i.altUnit)
            objDetails.addProperty("ConversionFactor", i.conversionFactor)
            objDetails.addProperty("UserId", loginData.userId)
            objDetails.addProperty("ParameterString", i.parameterString)
            objDetails.addProperty("PriceListDetailsId", i.priceDetailsId)
            objDetails.addProperty("SchemeDetailsId", i.schemeDetailsId)
            objDetails.addProperty("AltUnitQuantity", altQty)
            objDetails.addProperty("PerUnitWeight", i.perUnitWeight)
            objDetails.addProperty("QuantityRoundOffType", i.quantityRoundOffType)
            objDetailsArray.add(objDetails)
        }
        objReq.add("OrderDetails", objDetailsArray)

        Log.e("TAG", "createOrderRequest: " + objReq)

        val addOrderCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.addOrderInsertUpdate(objReq)
        binding.tvSubmit.isEnabled = false
        addOrderCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    binding.tvSubmit.isEnabled = true
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
                    binding.tvSubmit.isEnabled = true
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
                binding.tvSubmit.isEnabled = true
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

    private fun showPartyDealerListDialog(dialogType: Int) {
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
            val imgClose = layout.findViewById<ImageView>(R.id.imgClose)
            edtSearchPartyDealer = layout.findViewById<EditText>(R.id.edtSearch)
            val tvTitle = layout.findViewById<TextView>(R.id.tvTitle)
            paginationLoader = layout.findViewById(R.id.loader)
            tvSearchGO = layout.findViewById(R.id.tvSearchGO)
            tvNoDataFound = layout.findViewById(R.id.tvNoDataFound)
            val imgCloseSearch: ImageView = layout.findViewById(R.id.imgCloseSearch)
            imgCloseSearch.visibility = View.VISIBLE

            when (dialogType) {
                1 -> {
                    tvTitle.text = "Party/Dealer List"
                    if (binding.tvPartyDealer.text.toString().trim().isNotEmpty()) {
                        isSearchTriggered = true
                        edtSearchPartyDealer.setText(binding.tvPartyDealer.text.toString().trim())
                        tvSearchGO.visibility = View.GONE
                        partyDealerPageNo = 1
                    }
                    callAccountMasterList(dialogType)
                }

                2 -> {
                    tvTitle.text = "Distributor List"
                    if (binding.tvSelectDistributor.text.toString().trim().isNotEmpty()) {
                        isSearchTriggered = true
                        edtSearchPartyDealer.setText(
                            binding.tvSelectDistributor.text.toString().trim()
                        )
                        tvSearchGO.visibility = View.GONE
                        distributorPageNo = 1
                    }
                    callAccountMasterList(dialogType)
                }

                3 -> {
                    tvTitle.text = "Consignee List"
                    if (binding.tvSelectConsigneeName.text.toString().trim().isNotEmpty()) {
                        isSearchTriggered = true
                        edtSearchPartyDealer.setText(
                            binding.tvSelectConsigneeName.text.toString().trim()
                        )
                        tvSearchGO.visibility = View.GONE
                        consigneePageNo = 1
                    }
                    callAccountMasterList(dialogType)
                }
            }

            setupRecyclerView(rvItems, dialogType)

            tvSearchGO.setOnClickListener {
                isSearchTriggered = true
                when (dialogType) {
                    TYPE_PARTY_DEALER -> {
                        partyDealerPageNo = 1
                        callAccountMasterList(dialogType)
                    }

                    TYPE_DISTRIBUTOR -> {
                        distributorPageNo = 1
                        callAccountMasterList(dialogType)
                    }

                    TYPE_CONSIGNEE -> {
                        consigneePageNo = 1
                        callAccountMasterList(dialogType)
                    }
                }
                tvSearchGO.visibility = View.GONE
            }
            imgCloseSearch.setOnClickListener {
                edtSearchPartyDealer.setText("")
                when (dialogType) {
                    1 -> {
                        partyDealerPageNo = 1
                        callAccountMasterList(dialogType)
                    }

                    2 -> {
                        distributorPageNo = 1
                        callAccountMasterList(dialogType)
                    }

                    3 -> {
                        consigneePageNo = 1
                        callAccountMasterList(dialogType)
                    }
                }
            }

            imgClose.setOnClickListener { partyDealerDialog.dismiss() }

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

    private fun setupRecyclerView(rvItems: RecyclerView, dialogType: Int) {
        layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        rvItems.layoutManager = layoutManager

        when (dialogType) {
            TYPE_PARTY_DEALER -> {
                partyDealerAdapter = PartyDealerListAdapter(accountMasterList, dialogType)
            }

            TYPE_DISTRIBUTOR -> {
                partyDealerAdapter = PartyDealerListAdapter(distributorList, dialogType)
            }

            TYPE_CONSIGNEE -> {
                partyDealerAdapter = PartyDealerListAdapter(consigneeList, dialogType)
            }
        }

        rvItems.adapter = partyDealerAdapter

        /*rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isScrolling && !isLastPage && (visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                    isScrolling = true
                    when (dialogType) {
                        TYPE_PARTY_DEALER -> {
                            partyDealerPageNo++
                        }

                        TYPE_DISTRIBUTOR -> {
                            distributorPageNo++
                        }
                        TYPE_CONSIGNEE -> {
                            consigneePageNo++
                        }
                    }
                    callAccountMasterList(dialogType)
                }
            }
        })*/
        rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager?.childCount ?: 0
                val totalItemCount = layoutManager?.itemCount ?: 0
                val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                if (!isScrolling && !isLastPage && !isSearchTriggered &&
                    (visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0
                ) {
                    isScrolling = true
                    when (dialogType) {
                        TYPE_PARTY_DEALER -> {
                            partyDealerPageNo++
                        }

                        TYPE_DISTRIBUTOR -> {
                            distributorPageNo++
                        }

                        TYPE_CONSIGNEE -> {
                            consigneePageNo++
                        }
                    }
                    callAccountMasterList(dialogType)
                }
            }
        })
    }

    private fun showAddProduct(
        productModel: ProductGroupResponse?,
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
            val tvSchemeValue = layout.findViewById<TextView>(R.id.tvSchemeValue)
            tvUnit = layout.findViewById<TextView>(R.id.tvUnit)
            val btnSubmit = layout.findViewById<MaterialButton>(R.id.btnSubmit)
            val btnCancel = layout.findViewById<MaterialButton>(R.id.btnCancel)

            etPrice.isEnabled = productModel?.isPriceEditable == true
            etAmount.isEnabled = productModel?.isPriceEditable == true

            etPrice.filters = arrayOf(DecimalDigitsInputFilter(10, 4, etPrice))
            etAmount.filters = arrayOf(DecimalDigitsInputFilter(10, 2, etAmount))

            if (productModel != null) {
                btnSubmit.text = "Update"
                tvSelectGroup.text = productModel.productGroupName
                tvUnit.text = productModel.unit
                tvSelectProduct.text = productModel.productName
                etAmount.setText(CommonMethods.formatBigDecimal(productModel.amount))
                etPrice.setText(CommonMethods.formatBigDecimal(productModel.salesPrice ?: BigDecimal.ZERO))
                if (productModel.quantityRoundOffType == 0) {
                    etQty.setText(CommonMethods.formatThreeDecimal(productModel.qty))//changed
                    etQty.filters = arrayOf(DecimalDigitsInputFilter(10, 3, etQty))
                } else {
                    etQty.setText(CommonMethods.removeDecimal(productModel.qty))//changed
                    etQty.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
                        if (source.toString().contains(".")) "" else null
                    })
                }

                tvSchemeValue.text = buildString {
                    append(productModel.altUnit)
                    if ((productModel.qty)?.takeIf { it > BigDecimal.ZERO } != null) {
                        append(":${productModel.qty * (productModel.conversionFactor?.toBigDecimal() ?: BigDecimal.ZERO)}")
                    }
                    append(", ${productModel.scheme}")
                }
                //tvSchemeValue.text =  (if (productModel.qty != null && newQty > BigDecimal.ZERO) item.unit + ":"+item.finalQty else item.unit) + ", " + item.scheme

            }

            if (orderId == 0 && orderDetailsList.size > 0) {
                tvSelectGroup.text =
                    if (selectedGroup == null) "" else selectedGroup?.productGroupName
                selectedGroupId =
                    if (selectedGroup == null) 0 else selectedGroup?.productGroupId ?: 0
            }

            tvSelectGroup.setOnClickListener {
                if (groupList.size > 0) {
                    showGroupDialog()
                } else {
                    callProductGroupListApi(true, 0)
                }
            }

            /*tvSelectProduct.setOnClickListener {
                callProductGroupListApi(false, selectedGroupId)
            }*/

            // Add TextWatcher to Qty and Price EditText
            etQty.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (etQty.text.toString().isNotEmpty()) {//changed
                        val tempFinalQty = (etQty.text.toString()
                            .toBigDecimal() * (productModel?.conversionFactor?.toBigDecimal()
                            ?: BigDecimal.ZERO))
                        tvSchemeValue.text =
                            productModel?.altUnit + ":" + tempFinalQty + ", " + productModel?.scheme
                    } else {
                        tvSchemeValue.text =
                            productModel?.altUnit + ":" + "0" + ", " + productModel?.scheme
                    }
                    //qtyMode = productModel?.altUnit + productModel?.finalQty
                    calculateAmount(productModel)
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
                    calculateAmount(productModel)
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
                    calculatePrice(productModel)
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
                if (etQty.text.toString().toDouble() <= 0) {
                    showToastMessage(mActivity, getString(R.string.please_enter_valid_quantity))
                    return@setOnClickListener
                }

                /*if ((productModel?.isPriceEditable == true) && etAmount.text.toString().trim().isEmpty()) {
                    showToastMessage(
                        mActivity,
                        getString(R.string.please_enter_amount)
                    )
                    return@setOnClickListener
                }*/
                val qtyText = etQty.text.toString().trim()
                val priceText = etPrice.text.toString().trim()
                val amountText = etAmount.text.toString().trim()

                //changes
                val tempFinalQty = (etQty.text.toString()
                    .toBigDecimal() * (productModel?.conversionFactor?.toBigDecimal()
                    ?: BigDecimal.ZERO))

                val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0

                val discountAmount = ((qty * price) * ((productModel?.standardDiscount
                    ?: 0.0) + (productModel?.additionalDiscount ?: 0.0)) / 100)
                val amount = (qty * price) - discountAmount

                val orderDetailsModel = ProductGroupResponse(
                    orderDetailsId = productModel?.orderDetailsId ?: 0,
                    orderId = productModel?.orderId ?: 0,
                    productId = productModel?.productId ?: 0,
                    productName = productModel?.productName ?: "",
                    productGroupId = productModel?.productGroupId ?: 0,
                    productGroupName = productModel?.productGroupName ?: "",
                    unit = productModel?.unit ?: "",
                    altUnit = productModel?.altUnit ?: "",
                    salesPrice = productModel?.salesPrice ?: BigDecimal.ZERO,
                    qty = if (qtyText.isNotEmpty()) qtyText.toBigDecimal() else BigDecimal.ZERO,
                    price = if (priceText.isNotEmpty()) priceText.toBigDecimal() else BigDecimal.ZERO,
                    amount = amount.toBigDecimal(),//if (amountText.isNotEmpty()) amountText.toBigDecimal() else BigDecimal.ZERO,
                    finalQty = tempFinalQty,
                    scheme = productModel?.scheme,
                    mrp = productModel?.mrp,
                    discountAmount = discountAmount.toBigDecimal() ?: BigDecimal.ZERO,
                    standardDiscount = (productModel?.standardDiscount?.toDouble() ?: 0.0),
                    additionalDiscount = productModel?.additionalDiscount?.toDouble() ?: 0.00,
                    conversionFactor = productModel?.conversionFactor,
                    userId = productModel?.userId,
                    parameterString = productModel?.parameterString,
                    quantityRoundOffType = (productModel?.quantityRoundOffType ?: 0).toInt(),
                    perUnitWeight = productModel?.perUnitWeight ?: BigDecimal.ZERO,
                    schemeDetailsId = productModel?.schemeDetailsId ?: 0,
                    priceDetailsId = productModel?.priceDetailsId ?: 0
                )
                productModel?.amount = amount.toBigDecimal()
                productModel?.qty = qty.toBigDecimal()
                productModel?.price = price.toBigDecimal()
                productModel?.discountAmount = discountAmount.toBigDecimal()
                Log.e("TAG", "showAddProduct:PRICESCHEME ::  "+productModel?.schemeDetailsId+", "+productModel?.priceDetailsId )

                if (productModel == null) {
                    orderDetailsList.add(orderDetailsModel)
                    if (orderDetailsList.size > 0) {
                        binding.spCategory.setEnabled(false);
                        binding.spCategory.setClickable(false);
                    }
                } else {
                    orderDetailsList[orderDetailsPosition] = orderDetailsModel
                }

                calculateTotalOrderAmount()

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
        }
    }

    private fun handleOrderDetailsRVVisibility(orderDetailsList: ArrayList<ProductGroupResponse>) {
        if (orderDetailsList.size > 0) {
            binding.rvProduct.visibility = View.VISIBLE
            binding.tvNoItemFound.visibility = View.GONE
        } else {
            binding.rvProduct.visibility = View.GONE
            binding.tvNoItemFound.visibility = View.VISIBLE
        }
    }

    override fun onEditClick(item: ProductGroupResponse, position: Int) {
        if (!isReadOnly) {
            showAddProduct(item, position)
        }
    }

    override fun onDeleteClick(item: ProductGroupResponse, position: Int) {
        if (isReadOnly) {
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
            getString(R.string.delete_order_details),
            getString(R.string.are_you_sure_you_want_to_delete_this_product),
            okListener = object : PositiveButtonListener {
                override fun okClickListener() {
                    if ((item.orderDetailsId ?: 0) > 0) {
                        callDeleteOrderDetailsApi(item)
                    } else {
                        orderDetailsList.remove(item)
                        if (orderDetailsList.size > 0) {
                            orderDetailsAdapter.refreshAdapter(orderDetailsList)
                        }
                        handleOrderDetailsRVVisibility(orderDetailsList)
                        calculateTotalOrderAmount()
                    }
                }
            },
            positiveButtonText = getString(R.string.yes),
            negativeButtonText = getString(R.string.no)
        )

    }

    private fun calculateTotalOrderAmount() {
        if (orderDetailsList.isNotEmpty()) {
            // Calculate the total amount using BigDecimal
            val totalAmount = orderDetailsList.map { it.amount }
                .reduce { acc, amount -> acc.add(amount) }

            // Calculate the total quantity using BigDecimal
            val totalQty = orderDetailsList.map { it.qty }
                .reduce { acc, qty -> acc.add(qty) }

            // Check if totalAmount is greater than BigDecimal.ZERO
            if (totalQty > BigDecimal.ZERO) {
                binding.lylTotalOrderAmount.visibility = View.VISIBLE
                // Format the text to display amount + qty
                binding.tvTotalOrderAmount.text = "${CommonMethods.formatBigDecimal(totalAmount)}"
                binding.tvTotalOrderQty.text = "${CommonMethods.formatBigDecimal(totalQty)}"
            }
        } else {
            binding.tvTotalOrderAmount.text = ""
            binding.tvTotalOrderQty.text = ""
            binding.lylTotalOrderAmount.visibility = View.GONE
        }
    }


    fun calculateAmount(productModel: ProductGroupResponse?) {
        if (isUpdating) return
        isUpdating = true

        val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
        val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0

        if (qty > 0) {
            //val amount = qty * price
            val discountAmount = ((qty * price) * ((productModel?.standardDiscount
                ?: 0.0) + (productModel?.additionalDiscount ?: 0.0)) / 100)
            val amount = (qty * price) - discountAmount
            etAmount.setText(CommonMethods.formatLargeDouble(amount))
            etAmount.isEnabled = false // Make amount non-editable
        } else {
            etAmount.isEnabled = true // Make amount editable
            etAmount.setText("")
        }
        isUpdating = false
    }

    fun calculatePrice(productModel: ProductGroupResponse?) {
        if (isUpdating) return
        isUpdating = true

        val qty = etQty.text.toString().toDoubleOrNull() ?: 0.00
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.00

        if (qty > 0) {
            /*val price = amount / qty
            etPrice.setText(CommonMethods.formatLargeDouble(price))*/
            val price = amount / qty
            etPrice.setText(CommonMethods.formatLargeDouble(price))
            productModel?.amount = amount.toBigDecimal()
            productModel?.qty = qty.toBigDecimal()
            productModel?.price = price.toBigDecimal()
        } else {
            etPrice.setText("")
        }

        isUpdating = false
    }

    private fun openDatePicker(
        previousSelectedDate: Calendar? = null,
        isMaxDate: Boolean = false,
        onDateSelected: (String) -> Unit
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
        if (isMaxDate) {
            datePickerDialog.datePicker.maxDate =
                CommonMethods.dateStringToCalendar(CommonMethods.getCurrentDate()).timeInMillis
        }
        datePickerDialog.show()
    }

    override fun onOrderDetailsSelected(productList: java.util.ArrayList<ProductGroupResponse>) {

        if (productList.isNotEmpty()) { // Check if productList contains any items
            orderDetailsList = arrayListOf()
            orderDetailsList.addAll(productList) // Add new items to the orderDetailsList
            orderDetailsAdapter.refreshAdapter(orderDetailsList) // Refresh adapter with updated list
            handleOrderDetailsRVVisibility(orderDetailsList)
            if (orderDetailsList.size > 0) {
                binding.spCategory.setEnabled(false);
                binding.spCategory.setClickable(false);
            }
        } else {
            showToastMessage(mActivity, "Cart is empty!")
        }
        calculateTotalOrderAmount()
    }

    override fun companySelect(dropDownData: CompanyMasterResponse) {
        selectedCompany = dropDownData
        clearPartyTransactionAddressData()
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
        Log.e("TAG", "DropDown :: branchSelect: ")
        selectedBranch = dropDownData
        clearPartyTransactionAddressData()
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

    override fun divisionSelect(dropDownData: DivisionMasterResponse) {
        Log.e("TAG", "DropDown :: divisionSelect: ")
        selectedDivision = dropDownData
        clearPartyTransactionAddressData()
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

    override fun shippingAddressSelect(shippingAddress: ShippingAddressResponse) {
        binding.edtShippingAddress.setText(shippingAddress.shippingAddress ?: "")
        selectedShippingAddressId = shippingAddress.shippingAddressId
        binding.edtShippingAddress.isEnabled = false
        binding.imgAddressClear.visibility = View.VISIBLE
    }

    override fun onTypeSelect(typeData: CategoryMasterResponse) {
        /*Log.e("TAG", "DropDown :: onTypeSelect: " )
        selectedCategory = typeData
        binding.tvPartyDealer.text = ""
        selectedPartyDealerId = 0*/
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
            divisionMasterList.clear()
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

    private fun callApproveRejectOrder(isApprove:Boolean) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        if(binding.etApprovalRemarks.text.toString().isEmpty()){
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_approval_remarks))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()


        val orderEntryApproveReq = JsonObject()
        orderEntryApproveReq.addProperty("loginUserId", loginData.userId)

        val objDetailsArray = JsonArray()
        // for (i in orderDetailsList) {
        val objDetails = JsonObject()
        objDetails.addProperty("documentId", orderId)
        objDetails.addProperty("remarks", binding.etApprovalRemarks.text.toString())
        objDetails.addProperty("isApprove",isApprove)
        objDetails.addProperty("documentName", DOCUMENT_NAME_ORDER)
        objDetailsArray.add(objDetails)
        // }
        orderEntryApproveReq.add("authorizeApprove",objDetailsArray)

        /*val orderEntryReq = JsonObject()
        orderEntryReq.addProperty("documentId", orderId)
        orderEntryReq.addProperty("remarks", binding.etApprovalRemarks.text.toString())
        orderEntryReq.addProperty("isApprove",isApprove)
        orderEntryReq.addProperty("documentName","order")*/
        val orderApprovalCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getApprovaReject(orderEntryApproveReq)

        orderApprovalCall?.enqueue(object : Callback<ApproveRejectResponse> {
            override fun onResponse(
                call: Call<ApproveRejectResponse>,
                response: Response<ApproveRejectResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.order_approval),
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

    inner class PartyDealerListAdapter(
        partyDealerList: ArrayList<AccountMasterList>,
        val adapterType: Int
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
                item.accountName.contains(query, ignoreCase = true)
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
                itemBinding.tvUserName.text = partyDealerData.accountName
                itemBinding.tvUserName.setOnClickListener {
                    when (adapterType) {
                        TYPE_PARTY_DEALER -> {
                            isPartyChangeFromVisit = true
                            binding.tvPartyDealer.text = partyDealerData.accountName
                            binding.etContactPersonName.setText(
                                partyDealerData.contactPersonName ?: ""
                            )
                            selectedPartyDealerId = partyDealerData.accountMasterId
                            selectedTransactionType =
                                CommonDropDownResponse(dropdownKeyId = "0", dropdownName = "")
                            selectedShippingAddressId = 0;
                            binding.edtShippingAddress.setText("")
                            callCommonDropDownListApi(DROP_DOWN_TRANSACTION_TYPE)
                            callShippingAddressApi()
                        }

                        TYPE_DISTRIBUTOR -> {
                            binding.tvSelectDistributor.text = partyDealerData.accountName
                            selectedDistributorId = partyDealerData.accountMasterId
                        }

                        TYPE_CONSIGNEE -> {
                            binding.tvSelectConsigneeName.text = partyDealerData.accountName
                            selectedConsigneeId = partyDealerData.accountMasterId
                        }
                    }
                    partyDealerDialog.dismiss()
                }
            }
        }
    }

    override fun dropDownSelect(dropDownData: CommonDropDownResponse, dropDownType: String) {
        when (dropDownType) {
            DROP_DOWN_CURRENCY -> {
                selectedCurrency = dropDownData
            }

            DROP_DOWN_TRANSACTION_TYPE -> {
                selectedTransactionType = dropDownData
                binding.tvSelectTransactionType.text = dropDownData.dropdownValue
            }

            DROP_DOWN_FREIGHT_TERMS -> {
                selectedFreightTerms = dropDownData
                binding.tvSelectFreight.text = dropDownData.dropdownValue
            }
        }
    }


}