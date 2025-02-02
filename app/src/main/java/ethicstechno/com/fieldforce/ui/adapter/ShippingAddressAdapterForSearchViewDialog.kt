package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.moreoption.orderentry.ShippingAddressResponse

class ShippingAddressAdapterForSearchViewDialog(
    var shippingAddress: List<ShippingAddressResponse>,
    onClickDetect: ShippingAddressItemClick
) :
    RecyclerView.Adapter<ShippingAddressAdapterForSearchViewDialog.ViewHolder>() {

    var onShippinggAddressItemClick: ShippingAddressItemClick = onClickDetect


    fun refreshAdapter(listdata: ArrayList<ShippingAddressResponse>) {
        shippingAddress = arrayListOf()
        shippingAddress = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ShippingAddressResponse = shippingAddress[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return shippingAddress.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val llShippingAddress: LinearLayout
        private val tvShippingAddress: TextView
        private val tvCityState: TextView
        private val tvGSTNo: TextView

        init {
            llShippingAddress = itemView.findViewById(R.id.llShippingAddress)
            tvShippingAddress = itemView.findViewById(R.id.tvShippingAddress)
            tvCityState = itemView.findViewById(R.id.tvCityState)
            tvGSTNo = itemView.findViewById(R.id.tvGstNo)
        }

        fun bind(item: ShippingAddressResponse) {
            try {
                tvShippingAddress.text = item.shippingAddress
                tvCityState.text = item.cityName+", "+item.stateName
                tvGSTNo.text = "GST : "+item.gstNo
                llShippingAddress.setOnClickListener {
                    onShippinggAddressItemClick.onShippingAddressItemClick(item)
                }
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    interface ShippingAddressItemClick {
        fun onShippingAddressItemClick(countryData: ShippingAddressResponse)
    }

}

