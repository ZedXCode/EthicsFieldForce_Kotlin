package ethicstechno.com.fieldforce.models.attendance

import com.google.gson.annotations.SerializedName

data class UserLocationListResponse(
    @SerializedName("AttendanceLocationId")
    val attendanceLocationId: Int,

    @SerializedName("AttendanceId")
    val attendanceId: Int,

    @SerializedName("UserId")
    val userId: Int,

    @SerializedName("Latitude")
    val latitude: Double,

    @SerializedName("Longitude")
    val longitude: Double,

    @SerializedName("Location")
    val location: String,

    @SerializedName("ReturnMessage")
    val returnMessage: String?,

    @SerializedName("Success")
    val success: Boolean,

    @SerializedName("FromDate")
    val fromDate: String,

    @SerializedName("ToDate")
    val toDate: String,

    @SerializedName("CreateDateTime")
    val createDateTime: String,

    @SerializedName("UserName")
    val userName: String,

    @SerializedName("LocationTime")
    val locationTime: String
)
