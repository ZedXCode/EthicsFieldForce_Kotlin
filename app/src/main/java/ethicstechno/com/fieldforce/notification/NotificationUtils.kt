package ethicstechno.com.fieldforce.notification

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ethicstechno.com.fieldforce.MainActivity
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.utils.NOTIFICATION_ID_BIG_IMAGE
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationUtils(private val mContext: Context) {

    private val TAG = NotificationUtils::class.java.simpleName

    fun showNotificationMessage(title: String, message: String, timeStamp: String, intent: Intent) {
        showNotificationMessage(title, message, timeStamp, intent, null)
    }

    fun showNotificationMessage(title: String, message: String, timeStamp: String, intent: Intent, imageUrl: String?) {
        if (TextUtils.isEmpty(message)) return

        val icon = R.drawable.ic_notification_logo
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val resultPendingIntent = PendingIntent.getActivity(
            mContext,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val mBuilder = NotificationCompat.Builder(mContext)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (!TextUtils.isEmpty(imageUrl) && imageUrl!!.length > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {
            val bitmap = getBitmapFromURL(imageUrl)
            if (bitmap != null) {
                showBigNotification(bitmap, mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound)
            } else {
                displaySmallNotification(title, message)
            }
        } else {
            displaySmallNotification(title, message)
        }
    }

    private fun displaySmallNotification(title: String, message: String) {
        try {
            val channelId = mContext.getString(R.string.default_notification_channel_id)
            val channelName = mContext.getString(R.string.default_notification_channel_name)
            val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationIntent = Intent(mContext, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            if (title.isNotEmpty()) {
                val pendingIntent = PendingIntent.getActivity(
                    mContext,
                    System.currentTimeMillis().toInt(),
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                val builder = getNotificationBuilder(notificationManager, channelId, channelName, title, message, pendingIntent)
                notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
                Log.d(TAG, "Notification sent successfully.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "displaySmallNotification Error: ${e.message}")
        }
    }

    private fun getNotificationBuilder(
        notifManager: NotificationManager,
        channelId: String,
        channelName: String,
        title: String,
        msg: String,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {

        val color = ContextCompat.getColor(mContext, R.color.colorAccent)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder: NotificationCompat.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var mChannel = notifManager.getNotificationChannel(channelId)
            if (mChannel == null) {
                mChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                }
                notifManager.createNotificationChannel(mChannel)
            }
            builder = NotificationCompat.Builder(mContext, channelId)
        } else {
            builder = NotificationCompat.Builder(mContext, channelId)
            builder.priority = Notification.PRIORITY_HIGH
        }

        return builder.setColor(color)
            .setContentTitle(title)
            .setContentText(msg)
            .setSmallIcon(R.drawable.ic_notification_logo)
            .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_notification_logo))
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSound(defaultSoundUri)
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setContentIntent(pendingIntent)
    }

    private fun showBigNotification(
        bitmap: Bitmap,
        mBuilder: NotificationCompat.Builder,
        icon: Int,
        title: String,
        message: String,
        timeStamp: String,
        resultPendingIntent: PendingIntent,
        alarmSound: Uri
    ) {
        val bigPictureStyle = NotificationCompat.BigPictureStyle()
            .setBigContentTitle(title)
            .setSummaryText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY).toString())
            .bigPicture(bitmap)

        val notification = mBuilder.setSmallIcon(R.drawable.ic_notification_logo)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(message)
            .setSound(alarmSound)
            .setStyle(bigPictureStyle)
            .setWhen(getTimeMilliSec(timeStamp))
            .setLargeIcon(BitmapFactory.decodeResource(mContext.resources, icon))
            .setContentIntent(resultPendingIntent)
            .build()

        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BIG_IMAGE, notification)
    }

    fun getBitmapFromURL(strURL: String): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse("android.resource://${mContext.packageName}/raw/notification")
            val r = RingtoneManager.getRingtone(mContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
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
                if (componentInfo?.packageName == context.packageName) {
                    isInBackground = false
                }
            }
            return isInBackground
        }

        fun clearNotifications(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }

        fun getTimeMilliSec(timeStamp: String): Long {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = format.parse(timeStamp)
                date?.time ?: 0
            } catch (e: ParseException) {
                e.printStackTrace()
                0
            }
        }
    }
}
