package ethicstechno.com.fieldforce.ui.adapter.spinneradapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse

class VisitTypeAdapter(
    context: Context,
    spinnerLayout: Int,
    visitType: List<CategoryMasterResponse>?
    ) :
        ArrayAdapter<CategoryMasterResponse>(context, spinnerLayout, visitType!!) {
        private val mContext: Context = context
        private var visitTypeList: List<CategoryMasterResponse> = ArrayList()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val visitType = visitTypeList[position]
            name.text = visitType.categoryName



            return listItem
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val visitType = visitTypeList[position]
            name.text = visitType.categoryName
            return listItem
        }

        init {
            visitTypeList = visitType!!

        }
    }