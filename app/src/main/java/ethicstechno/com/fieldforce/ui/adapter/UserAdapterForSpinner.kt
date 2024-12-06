package ethicstechno.com.fieldforce.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.reports.UserListResponse

class UserAdapterForSpinner(
    private val context: Context,
    private val spinnerLayout: Int,
    private var userList: ArrayList<UserListResponse>,
    onUserSelect: UserSelect
) :
    ArrayAdapter<UserListResponse>(context, spinnerLayout, userList) {
    private val mContext: Context = context
    private var userListResponseList: List<UserListResponse> = ArrayList()
    private var userSelect: UserSelect = onUserSelect

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null) listItem =
            LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
        val name: TextView = listItem!!.findViewById(android.R.id.text1)
        val userData = userList[position]
        name.text = userData.userName

        return listItem
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null) listItem =
            LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
        val name: TextView = listItem!!.findViewById(android.R.id.text1)
        val currentSupervisor = userList[position]
        name.text = currentSupervisor.userName
        userSelect.onUserSelect(currentSupervisor)
        return listItem
    }

    init {
        userListResponseList = userList

    }

    interface UserSelect {
        fun onUserSelect(userData: UserListResponse)
    }
}
