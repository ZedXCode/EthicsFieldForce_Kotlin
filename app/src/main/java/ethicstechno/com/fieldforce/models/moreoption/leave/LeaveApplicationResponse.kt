package ethicstechno.com.fieldforce.models.moreoption.leave

import com.google.gson.annotations.SerializedName

data class LeaveApplicationResponse(
    @SerializedName("LeaveApplicationId") val leaveApplicationId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("UserName") val userName: String,
    @SerializedName("LeaveApplicationDate") val leaveApplicationDate: String,
    @SerializedName("LeaveCategoryMasterId") val leaveCategoryMasterId: Int,
    @SerializedName("LeaveCategoryName") val leaveCategoryName: String,
    @SerializedName("LeaveFromDate") val leaveFromDate: String,
    @SerializedName("LeaveToDate") val leaveToDate: String,
    @SerializedName("TotalLeaveDays") val totalLeaveDays: Double,
    @SerializedName("IsHalfDayLeave") val isHalfDayLeave: Boolean,
    @SerializedName("LeaveReason") val leaveReason: String,
    @SerializedName("ReportingToUserId") val reportingToUserId: Int,
    @SerializedName("ReportingToUserName") val reportingToUserName: String,
    @SerializedName("CreateDateTime") val createDateTime: String?, // Change to the actual type if needed
    @SerializedName("LeaveApprovalStatus") val leaveApprovalStatus: Int,
    @SerializedName("LeaveApprovalStatusName") val leaveApprovalStatusName: String?, // Change to the actual type if needed
    @SerializedName("LeaveApprovalDateTime") val leaveApprovalDateTime: String?, // Change to the actual type if needed
    @SerializedName("LeaveApprovalRemarks") val leaveApprovalRemarks: String?, // Change to the actual type if needed
    @SerializedName("FromDate") val fromDate: String, // Change to the actual type if needed
    @SerializedName("ToDate") val toDate: String,
    @SerializedName("Success") val isSuccess: Boolean,
    @SerializedName("ReturnMessage") val returnMessage: String
)
