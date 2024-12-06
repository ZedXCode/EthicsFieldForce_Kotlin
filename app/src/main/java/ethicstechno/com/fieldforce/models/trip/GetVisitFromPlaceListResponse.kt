package ethicstechno.com.fieldforce.models.trip

import com.google.gson.annotations.SerializedName

data class GetVisitFromPlaceListResponse(
    @SerializedName("TripId") val tripId: Int,
    @SerializedName("TripFromPlace") val tripFromPlace: String,
    @SerializedName("TripFromPlaceLatitude") val tripFromPlaceLatitude: Double,
    @SerializedName("TripFromPlaceLongitude") val tripFromPlaceLongitude: Double,
    @SerializedName("TripVehicleTypeId") val tripVehicleTypeId: Int,
    @SerializedName("TripVehicleTypeName") val tripVehicleTypeName: String,
    @SerializedName("TripStartMeterReading") val tripStartMeterReading: Int,
    @SerializedName("TripStartMeterReadingPhoto") val tripStartMeterReadingPhoto: String,
    @SerializedName("PreviousVisitId") val previousVisitId: Int,
    @SerializedName("FromPlace") val fromPlace: String,
    @SerializedName("FromPlaceLatitude") val fromPlaceLatitude: Double,
    @SerializedName("FromPlaceLongitude") val fromPlaceLongitude: Double,
    @SerializedName("UserId") val userId: Int
)
