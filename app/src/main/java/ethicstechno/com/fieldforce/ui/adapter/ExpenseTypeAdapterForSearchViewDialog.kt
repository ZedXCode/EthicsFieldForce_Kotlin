package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseTypeListResponse
import kotlin.collections.ArrayList

class ExpenseTypeAdapterForSearchViewDialog(
    var expenseTypeList: List<ExpenseTypeListResponse>,
    onClickDetect: ExpenseTypeItemClick
) :
    RecyclerView.Adapter<ExpenseTypeAdapterForSearchViewDialog.ViewHolder>() {

    var onZoneClick: ExpenseTypeItemClick = onClickDetect


    fun refreshAdapter(listdata: ArrayList<ExpenseTypeListResponse>) {
        expenseTypeList = arrayListOf()
        expenseTypeList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ExpenseTypeListResponse = expenseTypeList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return expenseTypeList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtBankName: TextView

        init {
            txtBankName =
                itemView.findViewById<TextView>(R.id.tvUserName)
        }

        fun bind(item: ExpenseTypeListResponse) {
            try {
                txtBankName.text = item.expenseTypeName
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

    interface ExpenseTypeItemClick {
        fun onZoneClick(countryData: ExpenseTypeListResponse)
    }

}

