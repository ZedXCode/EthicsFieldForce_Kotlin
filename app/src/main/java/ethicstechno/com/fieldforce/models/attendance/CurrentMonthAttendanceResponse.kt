package ethicstechno.com.fieldforce.models.attendance

import com.google.gson.annotations.SerializedName

data class CurrentMonthAttendanceResponse(
    @SerializedName("AttendanceId") val attendanceId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("PunchInDateTime") val punchInDateTime: String?,
    @SerializedName("PunchInLatitude") val punchInLatitude: Double,
    @SerializedName("PunchInLongitude") val punchInLongitude: Double,
    @SerializedName("PunchInLocation") val punchInLocation: String,
    @SerializedName("PunchOutDateTime") val punchOutDateTime: String?,
    @SerializedName("PunchOutLatitude") val punchOutLatitude: Double,
    @SerializedName("PunchOutLongitude") val punchOutLongitude: Double,
    @SerializedName("PunchOutLocation") val punchOutLocation: String,
    @SerializedName("AttendanceStatus") val attendanceStatus: String,
    @SerializedName("Remarks") val remarks: String?,
    @SerializedName("MapKMFromHQ") val mapKMFromHQ: Double,
    @SerializedName("IsActive") val isActive: Boolean,
    @SerializedName("CommandId") val commandId: Int,
    @SerializedName("CreateBy") val createBy: Int,
    @SerializedName("CreateDateTime") val createDateTime: String,
    @SerializedName("Success") val success: Boolean,
    @SerializedName("PunchInDate") val punchInDate: String,
    @SerializedName("PunchInTime") val punchInTime: String?,
    @SerializedName("PunchOutTime") val punchOutTime: String?,
    @SerializedName("FromDate") val fromDate: String,
    @SerializedName("ToDate") val toDate: String,
    @SerializedName("StartDate") val startDate: String,
    @SerializedName("EndDate") val endDate: String,
    @SerializedName("LastSyncLocation") val lastSyncLocation: String,
    @SerializedName("LastSyncDateTime") val lastSyncDateTime: String
){
    constructor() : this(0,0,"", 0.0, 0.0, "", "",
    0.0, 0.0, "", "", "", 0.0, false, 0, 0, "", false, "", "", "",
    "", "", "", "", "", "")
}