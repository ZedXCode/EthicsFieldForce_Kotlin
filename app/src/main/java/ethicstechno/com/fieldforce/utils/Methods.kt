
import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage


private var maxWidth = 0
private var maxHeight = 0


fun AppCompatActivity.hideKeyboard() {

    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    val view = currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun AppCompatActivity.showKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun timeConversionWithMinutes(seconds: Long): String {
    var seconds = seconds / 1000
    var min = ""
    var sec = ""
    val MINUTES_IN_AN_HOUR = 60
    val SECONDS_IN_A_MINUTE = 60
    var minutes = seconds / SECONDS_IN_A_MINUTE
    seconds -= minutes * SECONDS_IN_A_MINUTE
    val hours = minutes / MINUTES_IN_AN_HOUR
    minutes -= hours * MINUTES_IN_AN_HOUR
    min = if (minutes < 10) {
        "0" + minutes.toString()
    } else {
        minutes.toString()
    }
    sec = if (seconds < 10) {
        "0" + seconds.toString()
    } else {
        seconds.toString()
    }
    return "$min:$sec"
}

fun timeConversionWithHours(seconds: Long): String {
    var seconds = seconds / 1000
    var hr = ""
    var min = ""
    var sec = ""
    val MINUTES_IN_AN_HOUR = 60
    val SECONDS_IN_A_MINUTE = 60
    var minutes = seconds / SECONDS_IN_A_MINUTE
    seconds -= minutes * SECONDS_IN_A_MINUTE
    val hours = minutes / MINUTES_IN_AN_HOUR
    minutes -= hours * MINUTES_IN_AN_HOUR
    hr = if (hours < 10) {
        "0" + hours.toString()
    } else {
        hours.toString()
    }
    min = if (minutes < 10) {
        "0" + minutes.toString()
    } else {
        minutes.toString()
    }
    sec = if (seconds < 10) {
        "0" + seconds.toString()
    } else {
        seconds.toString()
    }
    return "$hr:$min:$sec"
}

fun sendMail(context: Context, subject: String, extraText: String, emailId: String) {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:") // only email apps should handle this
    intent.putExtra(Intent.EXTRA_EMAIL, Array<String>(20) { emailId })
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, extraText)

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent);
    }
}

fun shareTo(context: Context) {
    val shareIntent = Intent()
    shareIntent.action = Intent.ACTION_SEND
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");

    if (shareIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(Intent.createChooser(shareIntent, "Share to"))
    }
}

fun getLongFromString(str: String): Long {
    if (!TextUtils.isEmpty(str)) {
        try {
            return java.lang.Long.parseLong(str)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
        }

    }
    return -1
}

fun avoidDoubleClicks(view: View) {
    val DELAY_IN_MS: Long = 900
    if (!view.isClickable) {
        return
    }
    view.isClickable = false
    view.postDelayed({ view.isClickable = true }, DELAY_IN_MS)
}

fun getMaxHeight(context: Context): Int {
    if (maxHeight != 0) {
        return maxHeight
    }

    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val metrics = DisplayMetrics()
    display.getMetrics(metrics)
    val height = metrics.heightPixels
    maxHeight = height
    return maxHeight
}

fun showWebPage(context: Context, webUrl: String) {
    var webUrl = webUrl
    if (!webUrl.startsWith("http://") && !webUrl.startsWith("https://")) {
        webUrl = "http://$webUrl"
    }
    val webpage = Uri.parse(webUrl)
    val intent = Intent(Intent.ACTION_VIEW, webpage)
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

fun openMap(context: Context, address: String) {
    val map = "http://maps.google.com/maps?q=" + address
    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(map))
//    mapIntent.setPackage(GOOGLE_MAP_APP_PACKAGE)
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    }
}

fun appInstalledOrNot(uri: String, context: Context): Boolean {
    val pm = context.packageManager
    return try {
        pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        FirebaseCrashlytics.getInstance().recordException(e)
        false
    } catch (e: java.lang.Exception){
        FirebaseCrashlytics.getInstance().recordException(e)
        false
    }
}

fun animationView(view: View) {
    val animatorSet = AnimatorSet()

    val bounceAnimX = ObjectAnimator.ofFloat(view, "scaleX", 1.5f, 1f)

    val bounceAnimY = ObjectAnimator.ofFloat(view, "scaleY", 1.5f, 1f)

    animatorSet.play(bounceAnimX).with(bounceAnimY)
    animatorSet.duration = 300
    animatorSet.interpolator = AccelerateInterpolator()
    animatorSet.start()
}

fun animateImage(imageView: ImageView, animation: Animation): Boolean {

    if (imageView.isSelected()) {
        imageView.setSelected(false)
        return false
    } else {
        imageView.startAnimation(animation)
        imageView.setSelected(true)
        return true
    }
}

fun isAppIsInBackground(context: Context): Boolean {
    var isInBackground = true
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val runningProcesses = am.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in processInfo.pkgList) {
                    if (activeProcess == context.packageName) {
                        isInBackground = false
                    }
                }
            }
        }
    } else {
        val taskInfo = am.getRunningTasks(1)
        val componentInfo = taskInfo[0].topActivity
        if (componentInfo!!.packageName == context.packageName) {
            isInBackground = false
        }
    }
    return isInBackground
}




@RequiresApi(Build.VERSION_CODES.M)
fun checkLocationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return when {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            else -> {
                true
            }
        }
    } else {
        return when {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED -> {
                false
            }
            else -> {
                true
            }
        }
    }
}

/**
 * common alert dialog for whole application
 */
fun commonDialog(context: Context, message: String, okListener: PositiveButtonListener,
                 positiveButtonText : String = context.resources.getString(R.string.str_ok),
                 cancelButtonText: String = context.resources.getString(R.string.str_cancel),
                 popupCancellation: Boolean = true, showTitle: Boolean = false){

    val dialog = AlertDialog.Builder(context)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, i ->dialog.dismiss()
            okListener.okClickListener()
        }
        .setNegativeButton(cancelButtonText) { dialog, i ->dialog.dismiss() }
    dialog.setCancelable(popupCancellation)
    if (showTitle) {
        dialog.setTitle(context.resources.getString(R.string.app_name))
    }
    dialog.show()
}



fun showSomeThingWrongMessage(context: Context) {
    showToastMessage(context.applicationContext, context.resources.getString(R.string.error_message))
}

fun showNoInternetMessage(context: Context) {
    showToastMessage(context.applicationContext, context.resources.getString(R.string.no_internet))
}