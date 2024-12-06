package ethicstechno.com.fieldforce.models.trip

import com.google.gson.annotations.SerializedName

data class TripSubmitResponse(
    @SerializedName("TripId") val tripId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("EndTripLatitude") val endTripLatitude: Double,
    @SerializedName("EndTripLongitude") val endTripLongitude: Double,
    @SerializedName("EndTripLocation") val endTripLocation: String,
    @SerializedName("EndMeterReading") val endMeterReading: Int,
    @SerializedName("EndMeterReadingPhoto") val endMeterReadingPhoto: String,
    @SerializedName("TotalTripKM") val totalTripKM: Double,
    @SerializedName("ActualKM") val actualKM: Double,
    @SerializedName("Remarks") val remarks: String,
    @SerializedName("IsActive") val isActive: Boolean,
    @SerializedName("UpdateBy") val updateBy: Int,
    @SerializedName("IsTripCompleted") val isTripCompleted: Boolean,
    @SerializedName("TripCompletionDateTime") val tripCompletionDateTime: String,
    @SerializedName("ReturnMessage") val returnMessage: String,
    @SerializedName("Success") val success: Boolean
)
