package ethicstechno.com.fieldforce.ui.adapter.spinneradapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.DropDownItem

class CommonProductFilterAdapter(
    context: Context,
    spinnerLayout: Int,
    productFilterList: List<DropDownItem>?
    ) :
        ArrayAdapter<DropDownItem>(context, spinnerLayout, productFilterList!!) {
        private val mContext: Context = context
        private var productFilterResponseList: List<DropDownItem> = ArrayList()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val dateOption = productFilterResponseList[position]
            name.text = dateOption.dropdownValue
            return listItem
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val currentSupervisor = productFilterResponseList[position].dropdownValue
            name.text = currentSupervisor
            return listItem
        }

        init {
            productFilterResponseList = productFilterList!!

        }
    }