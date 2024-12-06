package ethicstechno.com.fieldforce.models.reports

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TripRoadMapResponse(
    @SerializedName("TripLocationId") val tripLocationId: Int,
    @SerializedName("TripId") val tripId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("Latitude") val latitude: Double,
    @SerializedName("Longitude") val longitude: Double,
    @SerializedName("Location") val location: String,
    @SerializedName("ReturnMessage") val returnMessage: String?,
    @SerializedName("Success") val success: Boolean,
    @SerializedName("FromDate") val fromDate: String,
    @SerializedName("ToDate") val toDate: String,
    @SerializedName("CreateDateTime") val createDateTime: String,
    @SerializedName("UserName") val userName: String,
    @SerializedName("LocationTime") val locationTime: String
): Parcelable{
    constructor() : this(
        0, // Default values for Int and Double
        0,
        0,
        0.0,
        0.0,
        "", // Default value for String
        null,
        false,
        "", // Default value for String
        "",
        "",
        "",
        ""
    )
}