package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.moreoption.visit.DivisionMasterResponse
import kotlin.collections.ArrayList

class DivisionAdapterForSearchViewDialog(
    var divisionList: List<DivisionMasterResponse>,
    onClickDetect: DivisionItemClick
) :
    RecyclerView.Adapter<DivisionAdapterForSearchViewDialog.ViewHolder>() {

    var onDivisionItemClick: DivisionItemClick = onClickDetect


    fun refreshAdapter(listdata: ArrayList<DivisionMasterResponse>) {
        divisionList = arrayListOf()
        divisionList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: DivisionMasterResponse = divisionList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return divisionList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCompanyName: TextView

        init {
            txtCompanyName =
                itemView.findViewById<TextView>(R.id.tvUserName)
        }

        fun bind(item: DivisionMasterResponse) {
            try {
                txtCompanyName.text = item.divisionName
                txtCompanyName.setOnClickListener {
                    onDivisionItemClick.onDivisionItemClick(item)
                }
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    interface DivisionItemClick {
        fun onDivisionItemClick(countryData: DivisionMasterResponse)
    }

}

