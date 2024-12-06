package ethicstechno.com.fieldforce.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ethicstechno.com.fieldforce.listener.ItemClickListener

class GenericAdapter<T>(
    private var items: ArrayList<T>,
    private val layoutId: Int,
    private val bind: (View, T) -> Unit,
    private val itemClickListener: ItemClickListener<T>
) : RecyclerView.Adapter<GenericAdapter.GenericViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return GenericViewHolder(view, bind, itemClickListener)
    }

    override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: ArrayList<T>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    class GenericViewHolder<T>(
        itemView: View,
        private val bind: (View, T) -> Unit,
        private val itemClickListener: ItemClickListener<T>
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: T) {
            bind(itemView, item)
            itemView.setOnClickListener { itemClickListener.onItemSelected(item) }
        }
    }
}
