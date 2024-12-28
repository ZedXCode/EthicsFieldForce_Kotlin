package ethicstechno.com.fieldforce.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse

class LeaveTypeAdapter(
    private val context: Context,
    private val spinnerLayout: Int,
    private var leaveTypeListResponse: ArrayList<CategoryMasterResponse>,
    onTypeSelect: TypeSelect
) : ArrayAdapter<CategoryMasterResponse>(context, spinnerLayout, leaveTypeListResponse) {
    private val mContext: Context = context
    private val typeSelect = onTypeSelect

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null) listItem =
            LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
        val name: TextView = listItem!!.findViewById(android.R.id.text1)
        val leaveTypeData = leaveTypeListResponse[position]
        name.text = leaveTypeData.categoryName
        typeSelect.onTypeSelect(leaveTypeData)
        return listItem
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null) listItem =
            LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
        val name: TextView = listItem!!.findViewById(android.R.id.text1)
        val leaveTypeData = leaveTypeListResponse[position]
        name.text = leaveTypeData.categoryName
        return listItem
    }

    interface TypeSelect {
        fun onTypeSelect(typeData: CategoryMasterResponse)
    }
}
