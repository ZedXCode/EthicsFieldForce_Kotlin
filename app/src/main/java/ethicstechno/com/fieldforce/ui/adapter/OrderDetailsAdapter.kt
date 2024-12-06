package ethicstechno.com.fieldforce.ui.adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.orderentry.OrderDetailsResponse
import ethicstechno.com.fieldforce.models.orderentry.ProductGroupResponse
import java.lang.Exception
import kotlin.collections.ArrayList

class OrderDetailsAdapter(
    var mContext: Context,
    var productModelLis: List<ProductGroupResponse>,
    private val listener: ProductItemClickListener,
) :
    RecyclerView.Adapter<OrderDetailsAdapter.ViewHolder>() {

    fun refreshAdapter(listdata: ArrayList<ProductGroupResponse>) {
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
        val item: ProductGroupResponse= productModelLis[position]
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

        fun bind(item: ProductGroupResponse, position: Int) {
            try {

                val srNo = (position + 1).toString()
                tvSRNo.text = srNo
                tvProduct.text = item.productName
                tvQty.text = item.qty.toString()
                tvPrice.text = item.price.toString()
                tvAmount.text = item.amount.toString()
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
        fun onEditClick(item: ProductGroupResponse, position: Int)
        fun onDeleteClick(item: ProductGroupResponse, position: Int)
    }

}

