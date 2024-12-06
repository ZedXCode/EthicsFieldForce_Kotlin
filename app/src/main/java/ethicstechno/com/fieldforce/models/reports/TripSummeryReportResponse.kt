package ethicstechno.com.fieldforce.models.reports

import com.google.gson.annotations.SerializedName

data class TripSummeryReportResponse(
    @SerializedName("TripId") val tripId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("StartDate") val startDate: String,
    @SerializedName("EndDate") val endDate: String,
    @SerializedName("AttendanceId") val attendanceId: String,
    @SerializedName("TripDate") val tripDate: String,
    @SerializedName("TotalTrips") val totalTrips: String,
    @SerializedName("AttendanceStatus") val attendanceStatus: String,
    @SerializedName("Latitude") val latitude: Double = 0.0,
    @SerializedName("Longitude") val longitude: Double = 0.0,
    @SerializedName("MapKm") val mapKm: Double,
    @SerializedName("TotalTripKM") val totalTripKM: Double,
    @SerializedName("ActualKm") val actualKm: Double,
    @SerializedName("VisitCount") val visitCount: Int
){
    constructor() : this(0, 0, "", "", "", "", "0", "P", 0.0, 0.0, 0.0, 0.0, 0.0, 0)
}