package ethicstechno.com.fieldforce.notification

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ethicstechno.com.fieldforce.MainActivity
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.FCM_TOKEN
import org.json.JSONException
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = MyFirebaseMessagingService::class.java.simpleName
    private var notificationUtils: NotificationUtils? = null

    override fun onMessageReceived(message: RemoteMessage) {
        message ?: return

        Log.e(TAG, "From: ${message.from}")

        // Notification payload
        message.notification?.let {
            Log.e(TAG, "Notification Body: ${it.body}")
            handleNotification(message)

            // Added by Jyoti Chauhan
            //AppPreferences.setUnreadNotification(remoteMessage.toString())
            //AppPreferences.setUnreadNotificationCount(AppPreferences.getUnreadNotificationCount() + 1)
        }

        // Data payload
        if (message.data.isNotEmpty()) {
            Log.e(TAG, "Data Payload: ${message.data}")

            try {
                val json = JSONObject(message.data as Map<*, *>)
                handleDataMessage(json)
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AppPreference.saveStringPreference(applicationContext, FCM_TOKEN, token)
    }

    private fun handleNotification(message: RemoteMessage) {
        try {
            val resultIntent = Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("message", message.notification?.body)
            }

            val title = message.notification?.title
            val messageBody = message.notification?.body

            showNotificationMessage(applicationContext, title, messageBody, "", resultIntent)

        } catch (e: Exception) {
            Log.e(TAG, "handleNotification: "+e.message.toString() )
            //PubFun.writeLog("[MyFirebaseMessagingService] *ERROR* IN \$handleNotification\$ :: error = ${e}")
        }
    }

    private fun handleDataMessage(json: JSONObject) {
        Log.e(TAG, "push json: $json")

        try {
            val data = json.getJSONObject("data")
            val title = data.getString("title")
            val message = data.getString("message")
            val isBackground = data.getBoolean("is_background")
            val imageUrl = data.getString("image")
            val timestamp = data.getString("timestamp")
            val payload = data.getJSONObject("payload")

            Log.e(TAG, "title: $title")
            Log.e(TAG, "message: $message")
            Log.e(TAG, "isBackground: $isBackground")
            Log.e(TAG, "payload: $payload")
            Log.e(TAG, "imageUrl: $imageUrl")
            Log.e(TAG, "timestamp: $timestamp")

            val resultIntent = Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("message", message)
            }

            if (TextUtils.isEmpty(imageUrl)) {
                showNotificationMessage(applicationContext, title, message, timestamp, resultIntent)
            } else {
                showNotificationMessageWithBigImage(applicationContext, title, message, timestamp, resultIntent, imageUrl)
            }

        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    private fun showNotificationMessage(context: Context, title: String?, message: String?, timeStamp: String, intent: Intent) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils?.showNotificationMessage(title ?: "", message ?: "", timeStamp, intent)
    }

    private fun showNotificationMessageWithBigImage(context: Context, title: String?, message: String?, timeStamp: String, intent: Intent, imageUrl: String) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils?.showNotificationMessage(title ?: "", message ?: "", timeStamp, intent, imageUrl)
    }
}
