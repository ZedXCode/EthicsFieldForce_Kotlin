package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.profile.ZoneListResponse

class ZoneAdapterForSearchViewDialog(
    var stateList: List<ZoneListResponse>,
    onClickDetect: ZoneItemClick
) :
    RecyclerView.Adapter<ZoneAdapterForSearchViewDialog.ViewHolder>() {

    var onZoneClick: ZoneItemClick = onClickDetect


    fun refreshAdapter(listdata: ArrayList<ZoneListResponse>) {
        stateList = arrayListOf()
        stateList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ZoneListResponse = stateList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return stateList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtBankName: TextView

        init {
            txtBankName =
                itemView.findViewById<TextView>(R.id.tvUserName)
        }

        fun bind(item: ZoneListResponse) {
            try {
                txtBankName.text = item.zoneName
                txtBankName.setOnClickListener {
                    onZoneClick.onZoneClick(item)
                }
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    interface ZoneItemClick {
        fun onZoneClick(countryData: ZoneListResponse)
    }

}

