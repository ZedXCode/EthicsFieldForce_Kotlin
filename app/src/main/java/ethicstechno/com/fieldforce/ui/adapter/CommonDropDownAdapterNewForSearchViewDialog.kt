package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.CommonDropDownResponse
import kotlin.collections.ArrayList

class CommonDropDownAdapterNewForSearchViewDialog(
    var dropDownList: List<CommonDropDownResponse>,
    onClickDetect: CommonDropDownItemClick,
    val dropDownType:String
) :
    RecyclerView.Adapter<CommonDropDownAdapterNewForSearchViewDialog.ViewHolder>() {

    var onCommonDropDownItemClick: CommonDropDownItemClick = onClickDetect


    fun refreshAdapter(listdata: ArrayList<CommonDropDownResponse>) {
        dropDownList = arrayListOf()
        dropDownList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: CommonDropDownResponse = dropDownList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dropDownList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtBankName: TextView

        init {
            txtBankName =
                itemView.findViewById<TextView>(R.id.tvUserName)
        }

        fun bind(item: CommonDropDownResponse) {
            try {
                txtBankName.text = item.dropdownValue
                txtBankName.setOnClickListener {
                    onCommonDropDownItemClick.onDropDownItemClick(item, dropDownType)
                }
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    interface CommonDropDownItemClick {
        fun onDropDownItemClick(countryData: CommonDropDownResponse, dropDownType:String)
    }

}

