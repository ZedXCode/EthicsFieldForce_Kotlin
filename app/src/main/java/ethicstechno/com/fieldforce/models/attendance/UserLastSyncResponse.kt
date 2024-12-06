package ethicstechno.com.fieldforce.models.attendance

import com.google.gson.annotations.SerializedName

data class UserLastSyncResponse(
    @SerializedName("AttendanceId") val attendanceId: Int,
    @SerializedName("TripId") val tripId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("LastSyncLatitude") val lastSyncLatitude: Double,
    @SerializedName("LastSyncLongitude") val lastSyncLongitude: Double,
    @SerializedName("LastSyncLocation") val lastSyncLocation: String,
    @SerializedName("LastSyncDateTime") val lastSyncDateTime: String,
    @SerializedName("AttendanceLocationInterval") val attendanceLocationInterval: Int,
    @SerializedName("LastAttendanceLocationDateTime") val lastAttendanceLocationDateTime: String,
    @SerializedName("TripLocationInterval") val tripLocationInterval: Int,
    @SerializedName("LastTripLocationDateTime") val lastTripLocationDateTime: String,
    @SerializedName("SaveIntervalWiseLocation") val saveIntervalWiseLocation: Boolean,
    @SerializedName("ReturnMessage") val returnMessage: String?, // Change the type to the appropriate one
    @SerializedName("Success") val success: Boolean,
    @SerializedName("StopBackgroundService") val isStopBackgroundService: Boolean
)