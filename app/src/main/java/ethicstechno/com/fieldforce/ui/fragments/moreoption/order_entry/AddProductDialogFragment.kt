package ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry

import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.groundworksapp.utility.DecimalDigitsInputFilter
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.AddProductDialogLayoutBinding
import ethicstechno.com.fieldforce.listener.ItemClickListener
import ethicstechno.com.fieldforce.models.orderentry.ProductGroupResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseDialogFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.ARG_PARAM2
import ethicstechno.com.fieldforce.utils.ARG_PARAM3
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.DIALOG_PRODUCT_GROUP_TYPE
import ethicstechno.com.fieldforce.utils.dialog.SearchDialogUtil
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
    private lateinit var groupSearchDialog: SearchDialogUtil<ProductGroupResponse>
    var selectedGroupId = 0
    var selectedGroup: ProductGroupResponse? = null
    private lateinit var productAdapter: ProductAdapter
    private var listener: OnOrderDetailsListener? = null
    //var selectedProductList: ArrayList<ProductGroupResponse> = arrayListOf()
    private var selectedProductAdapterList: java.util.ArrayList<ProductGroupResponse> = arrayListOf()
    private var dismissListener: DialogDismissListener? = null
    private var isCartClicked = false


    interface DialogDismissListener {
        fun onDialogDismissed(isCartClicked:Boolean)
    }

    fun setOrderDetailsListener(listener: OnOrderDetailsListener) {
        this.listener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.e("TAG", "onDismiss: DISMISS DIALOG FRAGMENT ..." )
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

    private val groupItemClickListener = object : ItemClickListener<ProductGroupResponse> {
        override fun onItemSelected(item: ProductGroupResponse) {
            // Handle user item selection
            groupSearchDialog.closeDialog()
            binding.tvSelectGroup.text = item.productGroupName
            selectedGroupId = item.productGroupId ?: 0
            selectedGroup = item
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
            orderDetailsList: ArrayList<ProductGroupResponse>
        ): AddProductDialogFragment {
            val args = Bundle()
            args.putString(ARG_PARAM1, orderDate)
            args.putInt(ARG_PARAM2, partyDealerId)
            args.putParcelableArrayList(ARG_PARAM3, orderDetailsList)
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
            Log.e("TAG", "initView: " + selectedProductAdapterList.size)
        }
        binding.tvSelectGroup.setOnClickListener(this)
        callProductGroupListApi(false, 0)
        callProductGroupListApi(true, 0)
        setProductAdapter()
        setupSearchFilter()

        binding.fabCart.setOnClickListener {
            showCartItems(selectedProductAdapterList)
        }
        binding.imgBack.setOnClickListener(this)
        setupSelectedProducts()
    }

    private fun setupSelectedProducts() {
        if(selectedProductAdapterList.size > 0){
            binding.cbSelectedItems.visibility = View.VISIBLE
            binding.cbSelectedItems.setOnCheckedChangeListener{view, isChecked ->
                if(isChecked){
                    if(selectedProductAdapterList.size > 0 && productAdapter != null){
                        productAdapter.refreshAdapter(selectedProductAdapterList)
                    }
                }else{
                    callProductGroupListApi(false, 0)
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
                if(::productAdapter.isInitialized){
                    productAdapter.filter(newText.orEmpty())
                }
                //partyDealerAdapter.filter(newText.orEmpty())
                return true
            }
        })
        binding.svView.setOnSearchClickListener {
            binding.flGroup.visibility = View.GONE
        }
        binding.svView.setOnCloseListener {
            binding.flGroup.visibility = View.VISIBLE
            false
        }

        binding.svView.queryHint = HtmlCompat.fromHtml(
            mActivity.getString(R.string.search_here),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
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

    private fun setProductAdapter() {
        productAdapter =
            ProductAdapter(mActivity, productList, binding.txtCartEntry)
        val layoutManager = LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
        binding.rvProduct.layoutManager = layoutManager
        binding.rvProduct.adapter = productAdapter
    }

    private fun callProductGroupListApi(isForProductGroup: Boolean, productGroupId: Int) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        if (isForProductGroup) {
            jsonReq.addProperty("ProductGroupId", 0)
        } else {
            val orderDateString =
                CommonMethods.convertDateStringForOrderEntry(orderDate)
            val parameterString =
                "ProductGroupId=$productGroupId and AccountMasterId=$partyDealerId EntryDate=$orderDateString"
            jsonReq.addProperty("ProductId", 0)
            jsonReq.addProperty("ProductGroupId", productGroupId)
            jsonReq.addProperty("ParameterString", parameterString)
            Log.e("TAG", "callProductGroupListApi: testing : " + jsonReq.toString())

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
                                productAdapter.refreshAdapter(productList)
                                if(selectedProductAdapterList.size > 0) {
                                    productAdapter.setSelectedItems(selectedProductAdapterList)
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


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                dismiss()
            }
            R.id.tvSelectGroup -> {
                showGroupDialog()
            }
        }
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

        fun refreshAdapter(newProductList: ArrayList<ProductGroupResponse>) {
            // Update the product list and refresh the view
            productList = arrayListOf()
            productList = newProductList
            filteredItems = productList
            notifyDataSetChanged() // Refresh the adapter
        }

        fun setSelectedItems(selectedItems: ArrayList<ProductGroupResponse>) {
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
            private var tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
            var lylMain: LinearLayout = itemView.findViewById(R.id.lylMain)

            fun bind(item: ProductGroupResponse, position: Int) {
                try {

                    etQty.clearTextWatcher()
                    etPrice.clearTextWatcher()
                    etAmount.clearTextWatcher()

                    // Only set the values if they're non-null and non-zero
                    if (item.price != null && item.price > BigDecimal.ZERO) {
                        etPrice.setText(item.price.takeIf { it != BigDecimal.ZERO }?.toString() ?: "")
                    } else {
                        etPrice.setText(
                            item.salesPrice.takeIf { it != BigDecimal.ZERO }?.toString() ?: ""
                        )
                    }
                    etQty.setText(item.qty.takeIf { it != BigDecimal.ZERO }?.toString() ?: "")
                    etAmount.setText(item.amount.takeIf { it != BigDecimal.ZERO }?.toString() ?: "")

                    // Ensure unit and product name are correctly displayed
                    tvUnit.text = item.unit ?: ""

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

                price > 0 -> {
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

            if (amount > 0 && qty > 0) {
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
            val count  = selectedProductAdapterList.size
            txtCartEntry.text = count.toString()
            txtCartEntry.visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
            if(selectedProductAdapterList.size > 0){
                if(!binding.cbSelectedItems.isVisible){
                    setupSelectedProducts()
                }
            }else{
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

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
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


}