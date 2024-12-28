package ethicstechno.com.fieldforce.ui.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.utils.CommonMethods
import java.io.File

class ImageAdapter(
    private val activityMain: Activity,
    private val imageBaseUrl: String
) : RecyclerView.Adapter<ImageAdapter.Holder>() {

    private val list = ArrayList<Any>()
    private var onImageCancelClick: OnAssetImageCancelClick? = null
    private var isImagePreview = false

    fun setOnClick(onClick: OnAssetImageCancelClick) {
        this.onImageCancelClick = onClick
    }

    fun addImage(
        arrAssetImages: ArrayList<Any>,
        imagePreview: Boolean
    ) {
        list.clear()
        list.addAll(arrAssetImages)
        isImagePreview = imagePreview
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(activityMain)
            .inflate(R.layout.item_image_layout, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val pos = holder.bindingAdapterPosition
        if (pos != RecyclerView.NO_POSITION) {
            if (!isImagePreview) {
                holder.bindImage(list[pos], isPreview = false)
            } else {
                holder.bindImage(list[pos], isPreview = true)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAssetImage: ImageView = itemView.findViewById(R.id.ivAssetImage)
        val ivClose: ImageView = itemView.findViewById(R.id.ivCancel)
        val viewShadow: View = itemView.findViewById(R.id.viewShadow)

        fun bindImage(imageFile: Any, isPreview: Boolean) {
            //ivClose.visibility = if (!isPreview) View.VISIBLE else View.GONE
            viewShadow.visibility = if (isPreview) View.VISIBLE else View.GONE

            if(imageFile is String){
                val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                Glide.with(activityMain.applicationContext)
                    .load(imageBaseUrl+imageFile)
                    .apply(requestOptions)
                    .into(ivAssetImage)
            }else{
                val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                Glide.with(activityMain.applicationContext)
                    .load(imageFile)
                    .apply(requestOptions)
                    .into(ivAssetImage)
            }

            ivClose.setOnClickListener {
                onImageCancelClick?.onImageCancel(bindingAdapterPosition)
            }

            ivAssetImage.setOnClickListener{
                onImageCancelClick?.onImagePreview(bindingAdapterPosition)
            }
        }
    }

    interface OnAssetImageCancelClick {
        fun onImageCancel(position: Int)
        fun onImagePreview(position: Int)
    }
}
