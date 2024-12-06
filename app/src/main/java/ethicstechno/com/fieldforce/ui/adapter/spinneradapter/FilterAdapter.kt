package ethicstechno.com.fieldforce.ui.adapter.spinneradapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse

class FilterAdapter(
    context: Context,
    spinnerLayout: Int,
    dateOptionList: List<FilterListResponse>?
    ) :
        ArrayAdapter<FilterListResponse>(context, spinnerLayout, dateOptionList!!) {
        private val mContext: Context = context
        private var filterList: List<FilterListResponse> = ArrayList()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val filterData = filterList[position]
            name.text = filterData.dropDownFieldValue



            return listItem
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val currentSupervisor = filterList[position]
            name.text = currentSupervisor.dropDownFieldValue
            return listItem
        }

        init {
            filterList = dateOptionList!!

        }
    }