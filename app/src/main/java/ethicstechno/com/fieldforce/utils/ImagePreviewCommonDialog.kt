package ethicstechno.com.fieldforce.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.ui.activities.HomeActivity
import ethicstechno.com.fieldforce.utils.PhotoView.PhotoView

class ImagePreviewCommonDialog {

    companion object{
        fun showImagePreviewDialog(mActivity: HomeActivity, imageUrl: Any) {
            val builder = AlertDialog.Builder(mActivity, R.style.MyAlertDialogStyle)
            val alertDialog = builder.create().apply {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            val inflater = mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout = inflater.inflate(R.layout.dialog_preview_image, null)


            val imageView = layout.findViewById<PhotoView>(R.id.imgPreview)
            val closeButton = layout.findViewById<ImageView>(R.id.imgClose)

            Glide.with(mActivity)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.white_background)
                .error(R.drawable.ic_image_load_error) // Set an error image in case of load failure
                .transition(DrawableTransitionOptions.withCrossFade()) // Add a cross-fade animation
                .into(imageView)
            //ImageUtils().loadImageUrl(mActivity, imageUrl, imageView)
            //imageView.setImageDrawable(ContextCompat.getDrawable(context, drawableResId))
            closeButton.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.setView(layout)
            alertDialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_shape)
            alertDialog.show()
        }
    }
}