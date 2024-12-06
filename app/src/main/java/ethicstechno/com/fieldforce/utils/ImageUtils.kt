package ethicstechno.com.fieldforce.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import de.hdodenhof.circleimageview.CircleImageView
import ethicstechno.com.fieldforce.R
import java.io.File

class ImageUtils {

    fun loadImageFile(context: Context, imageFile: File, imageView: ImageView) {
        try {
            Glide.with(context)
                .load(imageFile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_image_load_error) // Set an error image in case of load failure
                .transition(DrawableTransitionOptions.withCrossFade()) // Add a cross-fade animation
                .into(imageView)
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }

    }

    fun loadImageUrl(context: Context, imageUrl: String, imageView: ImageView) {
        try {
            Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_image_load_error) // Set an error image in case of load failure
                .transition(DrawableTransitionOptions.withCrossFade()) // Add a cross-fade animation
                .into(imageView)
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun loadImageUrlWithoutPlaceHolder(context: Context, imageUrl: String, imageView: ImageView) {
        try {
            Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_image_load_error) // Set an error image in case of load failure
                .transition(DrawableTransitionOptions.withCrossFade()) // Add a cross-fade animation
                .into(imageView)
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun loadCircleIMageUrl(context: Context, imageUrl: String, imageView: CircleImageView) {
        try {
            Glide.with(context)
                .load(imageUrl)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .placeholder(R.drawable.ic_profile) // Placeholder image while loading
                .error(R.drawable.ic_profile) // Image to display in case of an error
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(imageView)
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}
