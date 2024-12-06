package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import kotlin.collections.ArrayList

class PartyDealerAdapterForSearchViewDialog(
    var partyDealerList: List<AccountMasterList>,
    onClickDetect: PartyDealerItemClick
) :
    RecyclerView.Adapter<PartyDealerAdapterForSearchViewDialog.ViewHolder>() {

    var onPartyDealerClick: PartyDealerItemClick = onClickDetect;


    fun refreshAdapter(listdata: ArrayList<AccountMasterList>) {
        partyDealerList = arrayListOf()
        partyDealerList = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: AccountMasterList = partyDealerList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return partyDealerList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtBankName: TextView

        init {
            txtBankName =
                itemView.findViewById<TextView>(R.id.tvUserName)
        }

        fun bind(item: AccountMasterList) {
            try {
                txtBankName.text = item.accountName
                txtBankName.setOnClickListener {
                    onPartyDealerClick.onPartyDealerClick(item)
                }
            } catch (e: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                //writeErrorLog("[DalPaperActivity] *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
            }
        }
    }

    interface PartyDealerItemClick {
        fun onPartyDealerClick(partyDealerData: AccountMasterList)
    }

}

