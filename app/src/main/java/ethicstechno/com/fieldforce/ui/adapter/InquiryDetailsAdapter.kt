package ethicstechno.com.fieldforce.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.moreoption.inquiry.ProductInquiryGroupResponse
import ethicstechno.com.fieldforce.utils.CommonMethods

class InquiryDetailsAdapter(
    var mContext: Context,
    var productModelLis: List<ProductInquiryGroupResponse>,
    private val listener: ProductItemClickListener,
) :
    RecyclerView.Adapter<InquiryDetailsAdapter.ViewHolder>() {

    fun refreshAdapter(listdata: ArrayList<ProductInquiryGroupResponse>) {
        productModelLis = arrayListOf()
        productModelLis = listdata
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_row_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ProductInquiryGroupResponse = productModelLis[position]
        holder.bind(item, holder.adapterPosition)
    }

    override fun getItemCount(): Int {
        return productModelLis.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvSRNo:TextView
        private val tvProduct: TextView
        private val tvQty:TextView
        private val tvAmount:TextView
        private val tvUnit:TextView
        private val tvPrice:TextView
        private val ivEdit:ImageView
        private val ivDelete:ImageView
        init {
            tvSRNo = itemView.findViewById(R.id.tvSRNo)
            tvProduct = itemView.findViewById(R.id.tvProduct)
            tvQty = itemView.findViewById(R.id.tvQty)
            tvAmount = itemView.findViewById(R.id.tvAmount)
            tvPrice = itemView.findViewById(R.id.tvPrice)
            tvUnit = itemView.findViewById(R.id.tvUnit)
            ivEdit = itemView.findViewById(R.id.ivEdit)
            ivDelete = itemView.findViewById(R.id.ivDelete)
        }

        fun bind(item: ProductInquiryGroupResponse, position: Int) {
            try {

                val srNo = (position + 1).toString()
                tvSRNo.text = srNo
                tvProduct.text = item.productName
                tvQty.text = CommonMethods.formatBigDecimal(item.qty)
                tvPrice.text = CommonMethods.formatBigDecimal(item.price)
                tvAmount.text = CommonMethods.formatBigDecimal(item.amount)
                tvUnit.text = item.unit.toString()

                ivEdit.setOnClickListener {
                    listener.onEditClick(item, position)
                }
                ivDelete.setOnClickListener {
                    listener.onDeleteClick(item, position)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface ProductItemClickListener {
        fun onEditClick(item: ProductInquiryGroupResponse, position: Int)
        fun onDeleteClick(item: ProductInquiryGroupResponse, position: Int)
    }

}
