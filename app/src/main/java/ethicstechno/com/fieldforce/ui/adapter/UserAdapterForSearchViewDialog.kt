package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.reports.UserListResponse
import kotlin.collections.ArrayList

class UserAdapterForSearchViewDialog(
    var userList: List<UserListResponse>,
    onItemClick: UserItemClick
) :
    RecyclerView.Adapter<UserAdapterForSearchViewDialog.ViewHolder>() {

    var userItemClick: UserItemClick = onItemClick;


    fun refreshAdapter(listdata: ArrayList<UserListResponse>) {
        userList = arrayListOf()
        userList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: UserListResponse = userList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtBankName: TextView

        init {
            txtBankName =
                itemView.findViewById<TextView>(R.id.tvUserName)
        }

        fun bind(item: UserListResponse) {
            try {
                txtBankName.text = item.userName
                txtBankName.setOnClickListener {
                    userItemClick.onUserOnClick(item)
                }
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    interface UserItemClick {
        fun onUserOnClick(userData: UserListResponse)
    }

}

