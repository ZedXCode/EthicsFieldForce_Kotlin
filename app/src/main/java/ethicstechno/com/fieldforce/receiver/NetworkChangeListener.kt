package ethicstechno.com.fieldforce.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS_CHANGE
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS_CONNECTED
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS_DISCONNECTED
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS_SLOW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        /*val isConnected = isInternetAvailable(context)

        val localIntent = Intent("NETWORK_STATUS_CHANGE")
        localIntent.putExtra("networkStatus", if (isConnected) "Connected" else "Disconnected")

        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)*/
        val isConnected = isInternetAvailable(context)

        val localIntent = Intent(NETWORK_STATUS_CHANGE)

        if (isConnected) {
            CoroutineScope(Dispatchers.IO).launch {
                val isInternetUsable = isInternetWorking()
                localIntent.putExtra(NETWORK_STATUS, if (isInternetUsable) NETWORK_STATUS_CONNECTED else NETWORK_STATUS_SLOW)
                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
            }
        } else {
            localIntent.putExtra(NETWORK_STATUS, NETWORK_STATUS_DISCONNECTED)
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        }
    }

    private fun isInternetWorking(): Boolean {
        return try {
            val url = URL("https://www.google.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.connect()
            connection.responseCode == 200
        } catch (e: Exception) {
            false
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(activeNetwork)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
}
