package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.moreoption.visit.BranchMasterResponse
import kotlin.collections.ArrayList

class BranchAdapterForSearchViewDialog(
    var branchList: List<BranchMasterResponse>,
    onClickDetect: BranchItemClick
) :
    RecyclerView.Adapter<BranchAdapterForSearchViewDialog.ViewHolder>() {

    var onBranchItemClick: BranchItemClick = onClickDetect


    fun refreshAdapter(listdata: ArrayList<BranchMasterResponse>) {
        branchList = arrayListOf()
        branchList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: BranchMasterResponse = branchList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return branchList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCompanyName: TextView

        init {
            txtCompanyName =
                itemView.findViewById<TextView>(R.id.tvUserName)
        }

        fun bind(item: BranchMasterResponse) {
            try {
                txtCompanyName.text = item.branchName
                txtCompanyName.setOnClickListener {
                    onBranchItemClick.onBranchItemClick(item)
                }
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    interface BranchItemClick {
        fun onBranchItemClick(countryData: BranchMasterResponse)
    }

}

