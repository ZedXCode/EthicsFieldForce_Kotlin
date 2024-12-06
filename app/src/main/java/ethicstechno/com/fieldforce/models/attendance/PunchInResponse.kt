package ethicstechno.com.fieldforce.models.attendance

import com.google.gson.annotations.SerializedName

data class PunchInResponse(
    @SerializedName("AttendanceId") val attendanceId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("PunchInDateTime") val punchInDateTime: String,
    @SerializedName("PunchInLatitude") val punchInLatitude: Double,
    @SerializedName("PunchInLongitude") val punchInLongitude: Double,
    @SerializedName("PunchInLocation") val punchInLocation: String,
    @SerializedName("AttendanceStatus") val attendanceStatus: String,
    @SerializedName("Remarks") val remarks: String?, // Note the use of nullable type
    @SerializedName("MapKMFromHQ") val mapKMFromHQ: Double,
    @SerializedName("IsActive") val isActive: Boolean,
    @SerializedName("CommandId") val commandId: Int,
    @SerializedName("CreateBy") val createBy: Int,
    @SerializedName("CreateDateTime") val createDateTime: String,
    @SerializedName("ReturnMessage") val returnMessage: String,
    @SerializedName("Success") val success: Boolean
)