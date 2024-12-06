package ethicstechno.com.fieldforce.ui.adapter.spinneradapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ethicstechno.com.fieldforce.R

class DateOptionAdapter(
    context: Context,
    spinnerLayout: Int,
    dateOptionList: List<String>?
    ) :
        ArrayAdapter<String>(context, spinnerLayout, dateOptionList!!) {
        private val mContext: Context = context
        private var dateOptionListResponseList: List<String> = ArrayList()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val dateOption = dateOptionListResponseList[position]
            name.text = dateOption



            return listItem
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val currentSupervisor = dateOptionListResponseList[position]
            name.text = currentSupervisor
            return listItem
        }

        init {
            dateOptionListResponseList = dateOptionList!!

        }
    }