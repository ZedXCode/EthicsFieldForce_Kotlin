package ethicstechno.com.fieldforce.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class DistanceCalculatorUtils {

    interface DistanceCallback {
        fun onDistanceCalculated(distance: Float)
    }

    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double, apiKey: String, callback: DistanceCallback) {
        val executor = Executors.newSingleThreadExecutor()

        executor.execute {
            var parsedDistance: Float = 0.0f
            var response: String

            try {
                val url = URL("https://maps.googleapis.com/maps/api/directions/json?origin=$lat1,$lon1&destination=$lat2,$lon2&sensor=false&units=metric&mode=driving&key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val inStream: InputStream = BufferedInputStream(conn.inputStream)
                response = inStream.bufferedReader().use { it.readText() }

                val jsonObject = JSONObject(response)
                val routes = jsonObject.getJSONArray("routes")
                val route = routes.getJSONObject(0)
                val legs = route.getJSONArray("legs")
                val leg = legs.getJSONObject(0)
                val distance = leg.getJSONObject("distance")
                val distanceValue = distance.getString("value")
                parsedDistance = distanceValue.toFloat() / 1000.0f
                //parsedDistance = distance.getString("text")
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }

            callback.onDistanceCalculated(parsedDistance ?: 0F)
        }
    }
}
