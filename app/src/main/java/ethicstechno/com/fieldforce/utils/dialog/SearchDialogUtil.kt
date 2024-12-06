package ethicstechno.com.fieldforce.utils.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.listener.ItemClickListener
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.orderentry.ProductGroupResponse
import ethicstechno.com.fieldforce.ui.adapter.GenericAdapter
import ethicstechno.com.fieldforce.utils.DIALOG_ACCOUNT_MASTER
import ethicstechno.com.fieldforce.utils.DIALOG_PRODUCT_GROUP_TYPE
import ethicstechno.com.fieldforce.utils.DIALOG_PRODUCT_TYPE

class SearchDialogUtil<T>(
    private val activity: Activity,
    private var items: ArrayList<T>,
    private val layoutId: Int,
    private val bind: (View, T) -> Unit,
    private val itemClickListener: ItemClickListener<T>,
    private val title: String,
    private val dialogType:Int
) {
    private lateinit var dialog: AlertDialog
    private lateinit var adapter: GenericAdapter<T>

    fun showSearchDialog() {
        try {
            val builder = AlertDialog.Builder(activity, R.style.MyAlertDialogStyle)
            dialog = builder.create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.dialog_searchable_listing, null)
            val rvItems = layout.findViewById<RecyclerView>(R.id.rvItems)
            val imgClose = layout.findViewById<ImageView>(R.id.imgClose)
            val etSearch = layout.findViewById<EditText>(R.id.edtSearch)
            val tvTitle = layout.findViewById<TextView>(R.id.tvTitle)

            tvTitle.text = title

            // Initialize adapter
            adapter = GenericAdapter(items, layoutId, bind, itemClickListener)
            rvItems.layoutManager = LinearLayoutManager(activity)
            rvItems.adapter = adapter

            imgClose.setOnClickListener { dialog.dismiss() }

            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // No action needed here
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val searchText = s.toString().trim()

                    // Filter the list based on the dialogType and searchText
                    val filteredList = if (searchText.isNotEmpty()) {
                        items.filter { item ->
                            when (dialogType) {
                                DIALOG_ACCOUNT_MASTER -> {
                                    if (item is AccountMasterList) {
                                        item.accountName.contains(searchText, ignoreCase = true)
                                    } else {
                                        false
                                    }
                                }
                                DIALOG_PRODUCT_GROUP_TYPE -> {
                                    if (item is ProductGroupResponse) {
                                        item.productGroupName.contains(searchText, ignoreCase = true)
                                    } else {
                                        false
                                    }
                                }
                                DIALOG_PRODUCT_TYPE -> {
                                    if (item is ProductGroupResponse) {
                                        item.productName!!.contains(searchText, ignoreCase = true)
                                    } else {
                                        false
                                    }
                                }
                                else -> item.toString().contains(searchText, ignoreCase = true)
                            }
                        }
                    } else {
                        items // If the search text is empty, return the full list
                    }

                    // Update the adapter with the filtered list
                    adapter.updateItems(ArrayList(filteredList))
                }

                override fun afterTextChanged(s: Editable?) {
                    // No action needed here
                }
            })

            dialog.setView(layout, 0, 0, 0, 0)
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            //FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun closeDialog() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
