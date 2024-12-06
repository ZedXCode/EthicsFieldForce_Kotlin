package ethicstechno.com.fieldforce.models.reports

import com.google.gson.annotations.SerializedName

data class TripListByAttendanceIdResponse(
    @SerializedName("TripId") val tripId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("VehicleTypeId") val vehicleTypeId: Int,
    @SerializedName("VehicleTypeName") val vehicleTypeName: String,
    @SerializedName("UserName") val userName: String,
    @SerializedName("FromPlace") val fromPlace: String,
    @SerializedName("FromPlaceLatitude") val fromPlaceLatitude: Double,
    @SerializedName("FromPlaceLongitude") val fromPlaceLongitude: Double,
    @SerializedName("ToPlace") val toPlace: String,
    @SerializedName("ToPlaceLatitude") val toPlaceLatitude: Double,
    @SerializedName("ToPlaceLongitude") val toPlaceLongitude: Double,
    @SerializedName("StartMeterReading") val startMeterReading: Int,
    @SerializedName("StartMeterReadingPhoto") val startMeterReadingPhoto: String,
    @SerializedName("EndMeterReading") val endMeterReading: Int,
    @SerializedName("EndMeterReadingPhoto") val endMeterReadingPhoto: String,
    @SerializedName("TotalKM") val totalKM: Double,
    @SerializedName("TotalTripKM") val totalTripKM: Double,
    @SerializedName("ActualKM") val actualKM: Double,
    @SerializedName("Remarks") val remarks: String?,
    @SerializedName("IsActive") val isActive: Boolean,
    @SerializedName("CommandId") val commandId: Int,
    @SerializedName("CreateBy") val createBy: Int,
    @SerializedName("CreateDateTime") val createDateTime: String?, // Change the type to a suitable date type
    @SerializedName("UpdateBy") val updateBy: Int,
    @SerializedName("UpdateDateTime") val updateDateTime: String, // Change the type to a suitable date type
    @SerializedName("IsTripCompleted") val isTripCompleted: Boolean,
    @SerializedName("TripCompletionDateTime") val tripCompletionDateTime: String,
    @SerializedName("EndTripLatitude") val endTripLatitude: Double,
    @SerializedName("EndTripLongitude") val endTripLongitude: Double,
    @SerializedName("EndTripLocation") val endTripLocation: String,
    @SerializedName("TripDate") val tripDate: String,
    @SerializedName("TripTime") val tripTime: String,
    @SerializedName("TripStartDate") val tripStartDate: String?, // Change the type to a suitable date type
    @SerializedName("TripStartTime") val tripStartTime: String?, // Change the type to a suitable date type
    @SerializedName("TripEndDate") val tripEndDate: String?, // Change the type to a suitable date type
    @SerializedName("TripEndTime") val tripEndTime: String?, // Change the type to a suitable date type
    @SerializedName("TripRemarks") val tripRemarks: String?,
    @SerializedName("VisitCount") val visitCount: Int
)
