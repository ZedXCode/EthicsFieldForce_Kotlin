package ethicstechno.com.fieldforce.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import java.util.concurrent.TimeUnit

object DirectionsApiClient {

    fun getDirections(
        apiKeyString:String,
        origin: String,
        destination: String
    ): DirectionsResult? {
        val context = GeoApiContext.Builder()
            .apiKey(apiKeyString)
            .queryRateLimit(3) // Limit the number of requests per second
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()

        return try {
            DirectionsApi.newRequest(context)
                .origin(origin)
                .destination(destination)
                .mode(TravelMode.DRIVING) // You can change the travel mode as needed
                .await()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            // Handle any exceptions here
            e.printStackTrace()
            null
        }
    }
}
