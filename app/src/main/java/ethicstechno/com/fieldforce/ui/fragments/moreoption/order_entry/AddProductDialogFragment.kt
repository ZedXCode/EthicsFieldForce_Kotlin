package ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.groundworksapp.utility.DecimalDigitsInputFilter
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.AddProductDialogLayoutBinding
import ethicstechno.com.fieldforce.listener.ItemClickListener
import ethicstechno.com.fieldforce.models.CommonProductFilterResponse
import ethicstechno.com.fieldforce.models.DropDownItem
import ethicstechno.com.fieldforce.models.moreoption.orderentry.ProductGroupResponse
import ethicstechno.com.fieldforce.ui.adapter.spinneradapter.CommonProductFilterAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseDialogFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.ARG_PARAM2
import ethicstechno.com.fieldforce.utils.ARG_PARAM3
import ethicstechno.com.fieldforce.utils.ARG_PARAM4
import ethicstechno.com.fieldforce.utils.ARG_PARAM5
import ethicstechno.com.fieldforce.utils.ARG_PARAM6
import ethicstechno.com.fieldforce.utils.ARG_PARAM7
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DIALOG_PRODUCT_GROUP_TYPE
import ethicstechno.com.fieldforce.utils.FORM_ID_ORDER_ENTRY
import ethicstechno.com.fieldforce.utils.dialog.SearchDialogUtil
import hideKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.math.BigDecimal


class AddProductDialogFragment : HomeBaseDialogFragment(), View.OnClickListener {

    lateinit var binding: AddProductDialogLayoutBinding
    var partyDealerId = 0
    var orderDate = ""
    private var groupList: ArrayList<ProductGroupResponse> = arrayListOf()
    private var productList: ArrayList<ProductGroupResponse> = arrayListOf()
    private lateinit var groupSearchDialog: SearchDialogUtil<DropDownItem>
    var selectedGroupId = 0
    var selectedGroup: DropDownItem? = null
    private lateinit var productAdapter: ProductAdapter
    private var listener: OnOrderDetailsListener? = null

    //var selectedProductList: ArrayList<ProductGroupResponse> = arrayListOf()
    private var selectedProductAdapterList: java.util.ArrayList<ProductGroupResponse> =
        arrayListOf()
    private var dismissListener: DialogDismissListener? = null
    private var isCartClicked = false
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
    private var partyDealerPageNo = 1
    private var isScrolling = false
    private var isLastPage = false
    private var layoutManager: LinearLayoutManager? = null
    private var companyId = 0
    private var branchId = 0
    private var divisionId = 0
    private var categoryId = 0
    private var isSearchTriggered = false

    interface DialogDismissListener {
        fun onDialogDismissed(isCartClicked: Boolean)
    }

    fun setOrderDetailsListener(listener: OnOrderDetailsListener) {
        this.listener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.e("TAG", "onDismiss: DISMISS DIALOG FRAGMENT ...")
        if (dismissListener != null) {
            dismissListener?.onDialogDismissed(isCartClicked)
        }
    }


    fun setDialogDismissListener(listener: DialogDismissListener) {
        dismissListener = listener
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private val groupItemClickListener = object : ItemClickListener<DropDownItem> {
        override fun onItemSelected(item: DropDownItem) {
            // Handle user item selection
            binding.imgGroupClear.visibility = View.VISIBLE
            binding.viewSelectedGroup.visibility = View.VISIBLE
            groupSearchDialog.closeDialog()
            binding.tvSelectGroup.text = item.dropdownValue
            selectedGroupId = (item.dropdownKeyId ?: "0").toInt()
            selectedGroup = item
            isSearchTriggered = true
            partyDealerPageNo = 1
            callProductGroupListApi(false, selectedGroupId)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding =
            DataBindingUtil.inflate(inflater, R.layout.add_product_dialog_layout, container, false)
        return binding.root
    }

    companion object {

        fun newInstance(
            orderDate: String,
            partyDealerId: Int,
            orderDetailsList: ArrayList<ProductGroupResponse>,
            companyId: Int,
            branchId: Int,
            divisionId: Int,
            categoryId: Int
        ): AddProductDialogFragment {
            val args = Bundle()
            args.putString(ARG_PARAM1, orderDate)
            args.putInt(ARG_PARAM2, partyDealerId)
            args.putParcelableArrayList(ARG_PARAM3, orderDetailsList)
            args.putInt(ARG_PARAM4, companyId)
            args.putInt(ARG_PARAM5, branchId)
            args.putInt(ARG_PARAM6, divisionId)
            args.putInt(ARG_PARAM7, categoryId)
            val fragment = AddProductDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        arguments?.let {
            orderDate = it.getString(ARG_PARAM1, "")
            partyDealerId = it.getInt(ARG_PARAM2, 0)
            //selectedProductList = it.serializable(ARG_PARAM3) ?: arrayListOf()
            selectedProductAdapterList = it.serializable(ARG_PARAM3) ?: arrayListOf()
            companyId = it.getInt(ARG_PARAM4, 0)
            branchId = it.getInt(ARG_PARAM5, 0)
            divisionId = it.getInt(ARG_PARAM6, 0)
            categoryId = it.getInt(ARG_PARAM7, 0)
            Log.e("TAG", "initView: " + selectedProductAdapterList.size)
        }
        binding.tvSelectGroup.setOnClickListener(this)
        binding.imgGroupClear.setOnClickListener(this)
        binding.tvFilter2.setOnClickListener(this)
        binding.tvFilter3.setOnClickListener(this)
        binding.tvFilter4.setOnClickListener(this)
        binding.tvFilter5.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)
        binding.tvSearchGO.setOnClickListener(this)
        binding.imgGroupClear.setOnClickListener(this)
        //binding.tvSearchClear.setOnClickListener(this)
        binding.imgCloseSearch.setOnClickListener(this)
        setProductAdapter()
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
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
                    binding.tvSearchGO.visibility = View.VISIBLE
                } else {
                    binding.tvSearchGO.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed here
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                executeAPIsAndSetupData()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }


        binding.fabCart.setOnClickListener {
            if (binding.switchQty.isChecked) {
                var newProductAdapterList: ArrayList<ProductGroupResponse> = arrayListOf()
                for (i in selectedProductAdapterList) {
                    i.salesPrice = (i.salesPrice ?: BigDecimal.ZERO) / (i.conversionFactor
                        ?: 0.0).toBigDecimal()
                    newProductAdapterList.add(i)
                }
            }
            Log.e("TAG", "initView: " + selectedProductAdapterList.toString())
            showCartItems(selectedProductAdapterList)
        }
        binding.imgBack.setOnClickListener(this)
        setupSelectedProducts()

        binding.switchQty.setOnCheckedChangeListener { _, isChecked ->
            //partyDealerPageNo = 1
            //isSearchTriggered = true
            //callProductGroupListApi(false, selectedGroupId)
            productAdapter.refreshAdapter(productList, binding.cbSelectedItems.isChecked)
        }
    }

    private suspend fun executeAPIsAndSetupData() {
        withContext(Dispatchers.IO) {
            try {
                //val callProductGroupApi = async { callProductGroupListApi(true, 0) }
                binding.loader.visibility = View.VISIBLE
                val callProductListApi = async { callProductGroupListApi(false, 0) }
                val callCommonFilterApi = async { callCommonProductFilterApi() }

                //callProductGroupApi.await()
                callProductListApi.await()
                callCommonFilterApi.await()

            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("TAG", "executeAPIsAndSetupData: " + e.message.toString())
            }
        }
    }


    private fun setupSelectedProducts() {
        if (selectedProductAdapterList.size > 0) {
            binding.cbSelectedItems.visibility = View.VISIBLE
            binding.cbSelectedItems.setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    if (selectedProductAdapterList.size > 0 && productAdapter != null) {
                        productAdapter.refreshAdapter(selectedProductAdapterList, true)
                    }
                } else {
                    partyDealerPageNo = 1
                    callProductGroupListApi(false, selectedGroupId)
                }
            }
        }
    }

    private fun setupSearchFilter() {
        binding.svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productAdapter.filter(newText.orEmpty())
                return true
            }
        })
    }

    // Function to display cart items in a dialog
    private fun showCartItems(orderDetailsList: java.util.ArrayList<ProductGroupResponse>) {
        if (orderDetailsList.isNotEmpty()) {
            //val orderDetailsList = productAdapter.getOrderDetailsList()
            listener?.onOrderDetailsSelected(orderDetailsList) // Pass the order details to the listener
            isCartClicked = true
            dismiss()
        } else {
            CommonMethods.showToastMessage(mActivity, "Cart is empty!")
        }
    }

    /*private fun setProductAdapter() {
        productAdapter =
            ProductAdapter(mActivity, productList, binding.txtCartEntry)
        val layoutManager = LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
        binding.rvProduct.layoutManager = layoutManager
        binding.rvProduct.adapter = productAdapter
    }*/

    private fun setProductAdapter() {
        layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        binding.rvProduct.layoutManager = layoutManager

        productAdapter = ProductAdapter(mActivity, productList, binding.txtCartEntry)
        binding.rvProduct.adapter = productAdapter

        binding.rvProduct.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!binding.cbSelectedItems.isChecked) {
                    val visibleItemCount = layoutManager?.childCount ?: 0
                    val totalItemCount = layoutManager?.itemCount ?: 0
                    val firstVisibleItemPosition =
                        layoutManager?.findFirstVisibleItemPosition() ?: 0

                    if (!isScrolling && !isLastPage && !isSearchTriggered &&
                        (visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0
                    ) {
                        isScrolling = true
                        partyDealerPageNo++
                        callProductGroupListApi(false, selectedGroupId)
                    }
                }
            }
        })
    }


    private fun callProductGroupListApi(
        isForProductGroup: Boolean,
        productGroupId: Int
    ) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        //CommonMethods.showLoading(mActivity)
        binding.loader.visibility = View.VISIBLE

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        if (isForProductGroup) {
            jsonReq.addProperty("ProductGroupId", 0)
        } else {
            val orderDateString =
                CommonMethods.convertDateStringForOrderEntry(orderDate)
            val parameterString =
                "CompanyMasterId=$companyId and BranchMasterId=$branchId and DivisionMasterid=$divisionId and CategoryMasterId=$categoryId and AccountMasterId=$partyDealerId and EntryDate=$orderDateString and $FORM_ID_ORDER_ENTRY" +
                        " and ProductGroupId=$productGroupId and " +
                        "$header2Name IN ($filter2KeyIds) AND $header3Name IN ($filter3KeyIds) AND $header4Name IN ($filter4KeyIds) AND $header5Name IN ($filter5KeyIds) and ProductName like '%${binding.edtSearch.text.toString()}%'"
            jsonReq.addProperty("ProductId", 0)
            jsonReq.addProperty("ProductGroupId", productGroupId)
            jsonReq.addProperty("ParameterString", parameterString)
            jsonReq.addProperty("pageNo", partyDealerPageNo)
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
                //CommonMethods.hideLoading()
                isSearchTriggered = false
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (!isForProductGroup) {
                            if (it.isNotEmpty()) {
                                //isSearchTriggered = false
                                if (it.size == 1) {
                                    isSearchTriggered = true
                                }
                                binding.rvProduct.visibility = View.VISIBLE
                                binding.tvNoDataFound.visibility = View.GONE
                                if (partyDealerPageNo == 1) {
                                    productList.clear()
                                    isLastPage = false
                                }
                                //productList.clear()
                                productList.addAll(it)
                                productAdapter.refreshAdapter(productList, false)
                                isScrolling = false
                            } else {
                                isLastPage = true
                                if (partyDealerPageNo <= 1) {
                                    binding.rvProduct.visibility = View.GONE
                                    binding.tvNoDataFound.visibility = View.VISIBLE
                                }
                                //productList.clear()
                            }
                            setupSearchFilter()
                            binding.loader.visibility = View.GONE

                        } else {
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
                                    binding.loader.visibility = View.GONE

                                }
                            }
                        }
                    }
                } else {
                    isLastPage = true
                    if (partyDealerPageNo <= 1) {
                        binding.rvProduct.visibility = View.GONE
                        binding.tvNoDataFound.visibility = View.VISIBLE
                    }
                    binding.loader.visibility = View.GONE
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<ProductGroupResponse>>, t: Throwable) {
                //CommonMethods.hideLoading()
                binding.loader.visibility = View.GONE
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

    private fun callCommonProductFilterApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        //CommonMethods.showLoading(mActivity)
        binding.loader.visibility = View.VISIBLE

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        jsonReq.addProperty("userId", loginData.userId)
        jsonReq.addProperty(
            "parameterString",
            "CompanyMasterId=${companyId} and BranchMasterId=${branchId} and DivisionMasterid=${divisionId} and CategoryMasterId=${categoryId} and $FORM_ID_ORDER_ENTRY"
        )

        val commonProductFilterCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCommonProductFilterList(jsonReq)

        commonProductFilterCall?.enqueue(object : Callback<CommonProductFilterResponse> {
            override fun onResponse(
                call: Call<CommonProductFilterResponse>,
                response: Response<CommonProductFilterResponse>
            ) {
                binding.loader.visibility = View.GONE
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.list1?.items?.size!! > 0) {
                            groupListNew.clear()
                            groupListNew.addAll(it.list1.items)
                        }
                        if (it.list2?.items?.size!! > 0) {
                            itemList2.clear()
                            itemList2.addAll(it.list2.items)
                            binding.tvFilter2.text = it.list2.headerName ?: ""
                            header2Name = it.list2.headerName ?: ""
                        }
                        if (it.list3?.items?.size!! > 0) {
                            itemList3.clear()
                            itemList3.addAll(it.list3.items)
                            binding.tvFilter3.text = it.list3.headerName ?: ""
                            header3Name = it.list3.headerName ?: ""
                        }
                        if (it.list4?.items?.size!! > 0) {
                            itemList4.clear()
                            itemList4.addAll(it.list4.items)
                            binding.tvFilter4.text = it.list4.headerName ?: ""
                            header4Name = it.list4.headerName ?: ""
                        }
                        if (it.list5?.items?.size!! > 0) {
                            itemList5.clear()
                            itemList5.addAll(it.list5.items)
                            binding.tvFilter5.text = it.list5.headerName ?: ""
                            header5Name = it.list5.headerName ?: ""
                        }

                        if (itemList2.isEmpty() && itemList3.isEmpty()) {
                            binding.llFilter1.visibility = View.GONE
                        }
                        if (itemList4.isEmpty() && itemList5.isEmpty()) {
                            binding.llFilter2.visibility = View.GONE
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
                //CommonMethods.hideLoading()
                binding.loader.visibility = View.GONE
            }

            override fun onFailure(call: Call<CommonProductFilterResponse>, t: Throwable) {
                //CommonMethods.hideLoading()
                binding.loader.visibility = View.GONE
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

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                dismiss()
            }

            R.id.imgSearch -> {
                binding.flGroup.visibility = View.GONE
                binding.imgSearch.visibility = View.GONE
                binding.clSearchLayout.visibility = View.VISIBLE
            }

            R.id.imgCloseSearch -> {
                binding.imgSearch.visibility = View.VISIBLE
                binding.clSearchLayout.visibility = View.GONE
                binding.flGroup.visibility = View.VISIBLE
                binding.edtSearch.setText("")
                partyDealerPageNo = 1
                callProductGroupListApi(false, selectedGroupId)
                binding.cbSelectedItems.isChecked = false
                binding.cbSelectedItems.visibility = View.GONE
                mActivity.hideKeyboard()
            }

            R.id.tvSearchGO -> {
                isSearchTriggered = true
                binding.tvSearchGO.visibility = View.GONE
                partyDealerPageNo = 1
                callProductGroupListApi(false, selectedGroupId)
            }

            R.id.tvSelectGroup -> {
                showGroupDialog()
            }

            R.id.imgGroupClear -> {
                binding.tvSelectGroup.text = ""
                binding.tvSelectGroup.hint = mActivity.getString(R.string.select_group)
                selectedGroupId = 0
                selectedGroup = DropDownItem(
                    dropdownKeyId = "0",
                    dropdownValue = mActivity.getString(R.string.select_group)
                )
                binding.viewSelectedGroup.visibility = View.GONE
                binding.imgGroupClear.visibility = View.VISIBLE
                partyDealerPageNo = 1
                callProductGroupListApi(false, 0)
            }

            R.id.tvFilter2 -> {
                showCustomDialog("Size List", itemList2, selectedItemList2) { selectedItems ->
                    selectedItemList2 =
                        if (selectedItems == null || selectedItems.isEmpty()) arrayListOf() else selectedItems as ArrayList<DropDownItem>
                    binding.tvFilter2.text =
                        if (selectedItemList2.isEmpty()) header2Name else selectedItemList2[0].dropdownValue
                    binding.viewSelectedFilter2.visibility =
                        if (selectedItemList2.isEmpty()) View.GONE else View.VISIBLE
                    filter2KeyIds =
                        if (selectedItemList2.isEmpty()) "" else selectedItemList2.joinToString("|") { "${it.dropdownKeyId}" }
                    partyDealerPageNo = 1
                    callProductGroupListApi(false, selectedGroupId)

                }
            }

            R.id.tvFilter3 -> {
                showCustomDialog(
                    "Design List",
                    itemList3,
                    selectedItemList3
                ) { selectedItems ->
                    selectedItemList3 =
                        if (selectedItems == null || selectedItems.isEmpty()) arrayListOf() else selectedItems as ArrayList<DropDownItem>
                    binding.tvFilter3.text =
                        if (selectedItemList3.isEmpty()) header3Name else selectedItemList3[0].dropdownValue
                    binding.viewSelectedFilter3.visibility =
                        if (selectedItemList3.isEmpty()) View.GONE else View.VISIBLE
                    filter3KeyIds =
                        if (selectedItemList3.isEmpty()) "" else selectedItemList3.joinToString("|") { "${it.dropdownKeyId}" }
                    partyDealerPageNo = 1
                    callProductGroupListApi(false, selectedGroupId)
                }
            }

            R.id.tvFilter4 -> {
                showCustomDialog(
                    "Grade List",
                    itemList4,
                    selectedItemList4
                ) { selectedItems ->
                    selectedItemList4 =
                        if (selectedItems == null || selectedItems.isEmpty()) arrayListOf() else selectedItems as ArrayList<DropDownItem>
                    binding.tvFilter4.text =
                        if (selectedItemList4.isEmpty()) header4Name else selectedItemList4[0].dropdownValue
                    binding.viewSelectedFilter4.visibility =
                        if (selectedItemList4.isEmpty()) View.GONE else View.VISIBLE
                    filter4KeyIds =
                        if (selectedItemList4.isEmpty()) "" else selectedItemList4.joinToString("|") { "${it.dropdownKeyId}" }
                    partyDealerPageNo = 1
                    callProductGroupListApi(false, selectedGroupId)
                }
            }

            R.id.tvFilter5 -> {
                showCustomDialog(
                    "Series List",
                    itemList5,
                    selectedItemList5
                ) { selectedItems ->
                    selectedItemList5 =
                        if (selectedItems == null || selectedItems.isEmpty()) arrayListOf() else selectedItems as ArrayList<DropDownItem>
                    binding.tvFilter5.text =
                        if (selectedItemList5.isEmpty()) header5Name else selectedItemList5[0].dropdownValue
                    binding.viewSelectedFilter5.visibility =
                        if (selectedItemList5.isEmpty()) View.GONE else View.VISIBLE
                    filter5KeyIds =
                        if (selectedItemList5.isEmpty()) "" else selectedItemList5.joinToString("|") { "${it.dropdownKeyId}" }
                    partyDealerPageNo = 1
                    callProductGroupListApi(false, selectedGroupId)
                }
            }
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

    private fun showProductFilterDialog() {
        val productFilterDialog = Dialog(mActivity)
        productFilterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        productFilterDialog.setCancelable(true)
        productFilterDialog.setContentView(R.layout.product_filter_dialog)

        val spProductGroup: Spinner = productFilterDialog.findViewById(R.id.spProductGroup)
        val tvFilter2: TextView = productFilterDialog.findViewById(R.id.tvFilter2)
        val tvFilter3: TextView = productFilterDialog.findViewById(R.id.tvFilter3)
        val tvFilter4: TextView = productFilterDialog.findViewById(R.id.tvFilter4)
        val tvFilter5: TextView = productFilterDialog.findViewById(R.id.tvFilter5)
        val btnSubmit: TextView = productFilterDialog.findViewById(R.id.btnSubmit)

        setupProductGroupSpinner(groupListNew, spProductGroup = spProductGroup)

        tvFilter2.setOnClickListener {
            showCustomDialog("Size List", itemList2, selectedItemList2) { selectedItems ->
                selectedItemList2 = selectedItems as ArrayList<DropDownItem>
            }
        }
        tvFilter3.setOnClickListener {
            showCustomDialog(
                "Design List",
                itemList3,
                selectedItemList3
            ) { selectedItems ->
                selectedItemList3 = selectedItems as ArrayList<DropDownItem>
            }
        }
        tvFilter4.setOnClickListener {
            showCustomDialog("Grade List", itemList4, selectedItemList4) { selectedItems ->
                selectedItemList4 = selectedItems as ArrayList<DropDownItem>
            }
        }
        tvFilter5.setOnClickListener {
            showCustomDialog(
                "Series List",
                itemList5,
                selectedItemList5
            ) { selectedItems ->
                selectedItemList5 = selectedItems as ArrayList<DropDownItem>
            }
        }

        btnSubmit.setOnClickListener {
            val selectedGroup = groupListNew[spProductGroup.selectedItemPosition].dropdownKeyId
            val selectedSize = selectedItemList2.joinToString { it.dropdownKeyId ?: "" }
            val selectedDesign = selectedItemList3.joinToString { it.dropdownKeyId ?: "" }
            val selectedGrade = selectedItemList4.joinToString { it.dropdownKeyId ?: "" }
            val selectedSeries = selectedItemList5.joinToString { it.dropdownKeyId ?: "" }

            /*callProductGroupListApi(
                false,
                selectedGroup,
                selectedSize.toIntOrNull() ?: 0,
                selectedDesign.toIntOrNull() ?: 0,
                selectedGrade.toIntOrNull() ?: 0,
                selectedSeries.toIntOrNull() ?: 0
            )*/
            productFilterDialog.dismiss()
        }

        productFilterDialog.window?.apply {
            setLayout(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        productFilterDialog.show()
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

        if (selectedItems.size > 0) {
            cbSelected.visibility = View.VISIBLE
            cbSelected.text = "Selected (${selectedItems.size})"
        }

        tvTitle.text = title
        val adapter = ProductFilterAdapter(mActivity, dropdownItemList) { selectedCount ->
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

    interface OnOrderDetailsListener {
        fun onOrderDetailsSelected(orderDetailsList: java.util.ArrayList<ProductGroupResponse>)
    }

    inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializable(key) as? T
    }


    inner class ProductAdapter(
        var mContext: Context,
        var productList: java.util.ArrayList<ProductGroupResponse>,
        private val txtCartEntry: TextView,
    ) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
        var filteredItems: List<ProductGroupResponse> = productList
        var isUpdating = false

        fun refreshAdapter(
            newProductList: ArrayList<ProductGroupResponse>,
            isSelectedProduct: Boolean
        ) {
            // Update the product list and refresh the view
            productList = arrayListOf()
            productList = newProductList
            filteredItems = productList

            if (!isSelectedProduct && selectedProductAdapterList.isNotEmpty()) {
                setSelectedItems(selectedProductAdapterList)
            }
            notifyDataSetChanged() // Refresh the adapter
        }

        /*fun setSelectedItems(selectedItems: ArrayList<ProductGroupResponse>) {
            //selectedProductAdapterList.clear()
            if (selectedItems.size > 0) {
                //selectedProductAdapterList.clear()
                selectedProductAdapterList = selectedItems
                //this.selectedItemsList = selectedItems // Notify the adapter to refresh the view
                // Compare with selected items and update the product list
                for (product in productList) {
                    val selectedItem =
                        selectedProductAdapterList.find { it.productId == product.productId }
                    if (selectedItem != null) {
                        product.orderDetailsId = selectedItem.orderDetailsId
                        product.price = selectedItem.price
                        product.qty = selectedItem.qty // Set the quantity
                        product.amount = selectedItem.amount // Set the amount
                        //updateCartCount(product, selectedItemsList.size)
                    }
                }
                notifyDataSetChanged()
                updateCartCount()
            }
        }*/

        fun setSelectedItems(selectedItems: ArrayList<ProductGroupResponse>) {
            if (selectedItems.isNotEmpty()) {
                selectedProductAdapterList = selectedItems

                // Update the productList based on selectedProductAdapterList
                productList.forEach { product ->
                    val matchedItem =
                        selectedProductAdapterList.find { it.productId == product.productId }
                    matchedItem?.let { selected ->
                        product.orderDetailsId = selected.orderDetailsId
                        product.price = selected.price
                        product.qty = selected.qty
                        product.amount = selected.amount
                    }
                }

                // Update filteredItems and refresh UI
                filteredItems = productList
                notifyDataSetChanged()
                updateCartCount()
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.product_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item: ProductGroupResponse = filteredItems[position]

            holder.bind(item, holder.bindingAdapterPosition)
        }

        override fun getItemCount(): Int {
            return filteredItems.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private var etQty: EditText = itemView.findViewById(R.id.etQty)
            private var etPrice: EditText = itemView.findViewById(R.id.etPrice)
            private var etAmount: EditText = itemView.findViewById(R.id.etAmount)
            private var tvUnit: TextView = itemView.findViewById(R.id.tvUnit)
            private var tvQtyMode: TextView = itemView.findViewById(R.id.tvQtyMode)
            private var tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
            private var tvSchemeValue: TextView = itemView.findViewById(R.id.tvSchemeValue)
            var lylMain: LinearLayout = itemView.findViewById(R.id.lylMain)

            fun bind(item: ProductGroupResponse, position: Int) {
                try {

                    if ((item.scheme ?: "").isNotEmpty()) {
                        tvSchemeValue.visibility = View.VISIBLE
                        tvSchemeValue.text = "-" + item.scheme.toString()
                    }

                    etQty.clearTextWatcher()
                    etPrice.clearTextWatcher()
                    etAmount.clearTextWatcher()
                    /*if(etQty.text.toString().isNotEmpty() && etQty.text.toString().toBigDecimal() > BigDecimal.ZERO){
                        if (binding.switchQty.isChecked) {
                            tvQtyMode.text = item.unit + (etQty.text.toString()
                                .toBigDecimal() * (item.conversionFactor?.toBigDecimal()
                                ?: BigDecimal.ZERO)).toString()
                        } else {
                            tvQtyMode.text = item.altUnit + (etQty.text.toString()
                                .toBigDecimal() / (item.conversionFactor?.toBigDecimal()
                                ?: BigDecimal.ZERO)).toString()
                        }
                    }*/

                    etQty.setText(item.qty.takeIf { it != BigDecimal.ZERO }?.toString() ?: "")
                    etAmount.setText(item.amount.takeIf { it != BigDecimal.ZERO }?.toString() ?: "")

                    etPrice.isEnabled = item.isPriceEditable == true
                    etAmount.isEnabled = item.isPriceEditable == true

                    // Ensure unit and product name are correctly displayed
                    if (binding.switchQty.isChecked) {
                        tvUnit.text = item.altUnit ?: ""
                        val newQty = (etQty.text.toString()
                            .toBigDecimal() / (item.conversionFactor?.toBigDecimal()
                            ?: BigDecimal.ZERO))
                        tvQtyMode.text = if (newQty != null && newQty > BigDecimal.ZERO) item.unit + newQty else item.unit
                        if (item.salesPrice != null && (item.salesPrice ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                            val newSalePrice = (item.salesPrice ?: BigDecimal.ZERO) / (item.conversionFactor ?: 0.0).toBigDecimal()
                            etPrice.setText(newSalePrice.toString())
                        } else {
                            etPrice.setText(
                                item.salesPrice.takeIf { it != BigDecimal.ZERO }?.toString() ?: ""
                            )
                        }
                        if (item.amount != null && (item.amount ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                            val newSaleAmount =
                                (item.amount ?: BigDecimal.ZERO) / (item.conversionFactor
                                    ?: 0.0).toBigDecimal()
                            etAmount.setText(newSaleAmount.toString())
                        } else {
                            etAmount.setText(
                                item.amount.takeIf { it != BigDecimal.ZERO }?.toString() ?: ""
                            )
                        }
                    } else {
                        tvUnit.text = item.unit ?: ""
                        val newQty = (etQty.text.toString()
                            .toBigDecimal() * (item.conversionFactor?.toBigDecimal()
                            ?: BigDecimal.ZERO))
                        tvQtyMode.text =
                            if (newQty != null && newQty > BigDecimal.ZERO) item.altUnit + newQty else item.altUnit
                        // Only set the values if they're non-null and non-zero
                        if (item.salesPrice != null && (item.salesPrice ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                            etPrice.setText(
                                item.salesPrice.takeIf { it != BigDecimal.ZERO }?.toString() ?: ""
                            )
                        } else {
                            etPrice.setText(
                                item.salesPrice.takeIf { it != BigDecimal.ZERO }?.toString() ?: ""
                            )
                        }
                        if (item.amount != null && (item.amount ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                            etAmount.setText(
                                item.amount.takeIf { it != BigDecimal.ZERO }?.toString() ?: ""
                            )
                        } else {
                            etAmount.setText(
                                item.amount.takeIf { it != BigDecimal.ZERO }?.toString() ?: ""
                            )
                        }
                    }

                    // Set initial values
                    tvProductName.text = item.productName.toString()

                    etPrice.filters = arrayOf(DecimalDigitsInputFilter(10, 2, etPrice))
                    etAmount.filters = arrayOf(DecimalDigitsInputFilter(10, 2, etAmount))
                    etQty.filters = arrayOf(DecimalDigitsInputFilter(10, 2, etQty))

                    // Add new TextWatchers
                    etQty.addTextWatcher { s ->
                        if (etQty.isFocused) {
                            val qtyValue = s?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            if (item.orderDetailsId > 0 && qtyValue <= BigDecimal.ZERO) {
                                etQty.setText("1")  // Restrict qty to be greater than 0 if orderDetailsId > 0
                            } else {
                                calculateAmount(item, etQty, etPrice, etAmount)
                                item.qty = qtyValue
                            }
                        }
                        if (etQty.text.toString().isNotEmpty()) {
                            if (binding.switchQty.isChecked) {
                                item.finalQty = (etQty.text.toString()
                                    .toBigDecimal() / (item.conversionFactor?.toBigDecimal()
                                    ?: BigDecimal.ZERO))
                                tvQtyMode.text = item.unit + item.finalQty
                            } else {
                                item.finalQty = (etQty.text.toString()
                                    .toBigDecimal() * (item.conversionFactor?.toBigDecimal()
                                    ?: BigDecimal.ZERO))
                                tvQtyMode.text = item.altUnit + item.finalQty
                            }
                        } else {
                            if (binding.switchQty.isChecked) {
                                tvQtyMode.text = item.unit
                            } else {
                                tvQtyMode.text = item.altUnit
                            }
                        }
                    }

                    etPrice.addTextWatcher { s ->
                        if (etPrice.isFocused) {
                            val priceValue = s?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            if (item.orderDetailsId > 0 && priceValue <= BigDecimal.ZERO) {
                                etPrice.setText("1")  // Restrict price to be greater than 0 if orderDetailsId > 0
                            } else {
                                calculateAmount(item, etQty, etPrice, etAmount)
                                item.price = priceValue
                            }
                        }
                    }

                    etAmount.addTextWatcher { s ->
                        if (etAmount.isFocused) {
                            val amountValue = s?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            if (item.orderDetailsId > 0 && amountValue <= BigDecimal.ZERO) {
                                etAmount.setText("1")  // Restrict amount to be greater than 0 if orderDetailsId > 0
                            } else {
                                calculatePrice(item, etQty, etPrice, etAmount)
                                item.amount = amountValue
                            }
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun filter(query: String) {
            filteredItems = productList.filter { item ->
                item.productName!!.contains(query, ignoreCase = true)
            }
            notifyDataSetChanged()
        }

        private fun calculateAmount(
            item: ProductGroupResponse,
            etQty: EditText,
            etPrice: EditText,
            etAmount: EditText
        ) {
            if (isUpdating) return
            isUpdating = true

            val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
            val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0

            when {
                qty == 0.0 -> {
                    etAmount.setText("")
                    etAmount.isEnabled = true
                    removeItemFromCart(item)
                }

                qty > 0 -> {
                    val amount = qty * price
                    etAmount.setText(CommonMethods.formatLargeDouble(amount))
                    etAmount.isEnabled = false
                    item.amount = amount.toBigDecimal()
                    item.qty = qty.toBigDecimal()
                    item.price = price.toBigDecimal()
                    addItemToCart(item)
                }

                else -> {
                    etAmount.isEnabled = true
                    etAmount.setText("")
                    removeItemFromCart(item)
                }
            }

            isUpdating = false
        }


        private fun calculatePrice(
            item: ProductGroupResponse,
            etQty: EditText,
            etPrice: EditText,
            etAmount: EditText
        ) {
            if (isUpdating) return
            isUpdating = true

            val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0

            if (qty > 0) {
                val price = amount / qty
                etPrice.setText(CommonMethods.formatLargeDouble(price))
                item.amount = amount.toBigDecimal()
                item.qty = qty.toBigDecimal()
                item.price = price.toBigDecimal()
                addItemToCart(item)
            } else {
                etPrice.setText("")
                etAmount.setText("")
                removeItemFromCart(item)
            }

            isUpdating = false
        }


        private fun addItemToCart(item: ProductGroupResponse) {
            val existingItemIndex =
                selectedProductAdapterList.indexOfFirst { it.productId == item.productId }
            if (existingItemIndex != -1) {
                selectedProductAdapterList[existingItemIndex] = item
            } else {
                selectedProductAdapterList.add(item)
            }
            updateCartCount()
        }

        private fun removeItemFromCart(item: ProductGroupResponse) {
            if (selectedProductAdapterList.contains(item)) {
                selectedProductAdapterList.remove(item)
                //selectedItemsList.remove(item)
                updateCartCount()
            }
        }

        fun updateCartCount() {
            Log.e("TAG", "updateCartCount: " + selectedProductAdapterList.size)
            val count = selectedProductAdapterList.size
            txtCartEntry.text = count.toString()
            txtCartEntry.visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
            if (selectedProductAdapterList.size > 0) {
                if (!binding.cbSelectedItems.isVisible) {
                    setupSelectedProducts()
                }
            } else {
                binding.cbSelectedItems.visibility = View.GONE
            }
        }


        // Extension function to remove all TextWatchers from EditText
        fun EditText.clearTextWatcher() {
            val watchers = this.tag as? MutableList<TextWatcher> ?: mutableListOf()
            for (watcher in watchers) {
                this.removeTextChangedListener(watcher)
            }
            this.tag = null
        }

        // Extension function to add a TextWatcher and store it in the tag for future removal
        fun EditText.addTextWatcher(afterTextChanged: (String?) -> Unit) {
            val watcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    afterTextChanged(s?.toString())
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            // Add the watcher to the EditText
            this.addTextChangedListener(watcher)

            // Store the watcher in the tag for later removal
            var watchers = this.tag as? MutableList<TextWatcher>
            if (watchers == null) {
                watchers = mutableListOf()
                this.tag = watchers
            }
            watchers.add(watcher)
        }

    }

    class ProductFilterAdapter(
        private val context: Context,
        private val items: List<DropDownItem>,
        private val onSelectionChanged: (Int) -> Unit
    ) : RecyclerView.Adapter<ProductFilterAdapter.ItemViewHolder>() {

        private var filteredItems: MutableList<DropDownItem> = items.toMutableList()

        // Filter to show only selected items
        fun filterSelectedItems(selectedItems: List<DropDownItem>) {
            filteredItems = selectedItems.toMutableList()
            notifyDataSetChanged()
        }

        // Reset the filter to show all items
        fun resetFilter() {
            filteredItems = items.toMutableList()
            notifyDataSetChanged()
        }

        // Update selected states for pre-selected items
        fun updateCheckedItems(selectedItems: List<DropDownItem>) {
            items.forEach { item ->
                item.isSelected = selectedItems.any { it.dropdownKeyId == item.dropdownKeyId }
            }
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.item_product_filter_layout, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = filteredItems[position]
            holder.bind(item)

            // Clear previous listeners
            holder.itemCheckBox.setOnCheckedChangeListener(null)

            // Set the checkbox state explicitly
            holder.itemCheckBox.isChecked = item.isSelected

            // Update the item's state on user interaction
            holder.itemCheckBox.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
                onSelectionChanged(getSelectedItems().size) // Notify the selection change
            }
        }

        // Get selected items count
        fun getSelectedCount(): Int = items.count { it.isSelected }

        override fun getItemCount(): Int = filteredItems.size

        // Select or deselect all items
        fun selectAll(isChecked: Boolean) {
            items.forEach { it.isSelected = isChecked }
            notifyDataSetChanged()
        }

        // Clear all selected items
        fun clearAllFilters() {
            items.forEach { it.isSelected = false }
            notifyDataSetChanged()
        }

        // Get selected items
        fun getSelectedItems(): List<DropDownItem> {
            return items.filter { it.isSelected }
        }

        // Filter items based on the search query
        fun filter(query: String) {
            filteredItems = if (query.isEmpty()) {
                items.toMutableList()
            } else {
                items.filter {
                    (it.dropdownValue ?: "").contains(query, ignoreCase = true)
                }.toMutableList()
            }
            notifyDataSetChanged()
        }

        class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemCheckBox: CheckBox = itemView.findViewById(R.id.itemCheckBox)

            fun bind(item: DropDownItem) {
                itemCheckBox.text = item.dropdownValue
            }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }
    }


    private fun setupProductGroupSpinner(groupList: List<DropDownItem>, spProductGroup: Spinner) {
        val adapter = CommonProductFilterAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            groupList
        )
        spProductGroup.adapter = adapter
        selectedGroup = groupList[0]
        spProductGroup.setSelection(0)
    }

}