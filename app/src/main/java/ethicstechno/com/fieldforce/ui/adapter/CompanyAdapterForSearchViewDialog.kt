package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.moreoption.visit.CompanyMasterResponse
import kotlin.collections.ArrayList

class CompanyAdapterForSearchViewDialog(
    var companyList: List<CompanyMasterResponse>,
    onClickDetect: CompanyItemClick
) :
    RecyclerView.Adapter<CompanyAdapterForSearchViewDialog.ViewHolder>() {

    var onCompanyItemClick: CompanyItemClick = onClickDetect


    fun refreshAdapter(listdata: ArrayList<CompanyMasterResponse>) {
        companyList = arrayListOf()
        companyList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: CompanyMasterResponse = companyList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return companyList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCompanyName: TextView

        init {
            txtCompanyName =
                itemView.findViewById<TextView>(R.id.tvUserName)
        }

        fun bind(item: CompanyMasterResponse) {
            try {
                txtCompanyName.text = item.companyName
                txtCompanyName.setOnClickListener {
                    onCompanyItemClick.onCompanyItemClick(item)
                }
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    interface CompanyItemClick {
        fun onCompanyItemClick(countryData: CompanyMasterResponse)
    }

}

