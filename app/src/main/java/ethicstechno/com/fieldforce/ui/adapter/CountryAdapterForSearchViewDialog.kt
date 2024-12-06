package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.profile.CountryListResponse
import kotlin.collections.ArrayList

class CountryAdapterForSearchViewDialog(
    var countryList: List<CountryListResponse>,
    onClickDetect: CountryItemClick
) :
    RecyclerView.Adapter<CountryAdapterForSearchViewDialog.ViewHolder>() {

    var onCountryClick: CountryItemClick = onClickDetect;


    fun refreshAdapter(listdata: ArrayList<CountryListResponse>) {
        countryList = arrayListOf()
        countryList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: CountryListResponse = countryList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return countryList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtBankName: TextView

        init {
            txtBankName =
                itemView.findViewById<TextView>(R.id.tvUserName)
        }

        fun bind(item: CountryListResponse) {
            try {
                txtBankName.text = item.countryName
                txtBankName.setOnClickListener {
                    onCountryClick.onCountryClick(item)
                }
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    interface CountryItemClick {
        fun onCountryClick(countryData: CountryListResponse)
    }

}

