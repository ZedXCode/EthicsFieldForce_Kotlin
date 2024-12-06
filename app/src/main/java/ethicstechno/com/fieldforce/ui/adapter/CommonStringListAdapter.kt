package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import kotlin.collections.ArrayList

class CommonStringListAdapter(
    var stringList: List<String>
    //onItemClick: UserItemClick
) :
    RecyclerView.Adapter<CommonStringListAdapter.ViewHolder>() {

    //var userItemClick: UserItemClick = onItemClick;

    fun refreshAdapter(listdata: ArrayList<String>) {
        stringList = arrayListOf()
        stringList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_header_column, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: String = stringList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return stringList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtBankName: TextView

        init {
            txtBankName =
                itemView.findViewById(R.id.tvHeaderColumn)
        }

        fun bind(item: String) {
            try {
                txtBankName.text = item
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    /*interface UserItemClick {
        fun onUserOnClick(userData: String)
    }*/

}

