package ethicstechno.com.fieldforce.models.moreoption.expense

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
@Parcelize
data class LeaveDetailListResponse(
    @SerializedName("LeaveApplicationId") val leaveApplicationId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("UserName") val userName: String,
    @SerializedName("LeaveApplicationDate") val leaveApplicationDate: String,
    @SerializedName("LeaveCategoryMasterId") val leaveCategoryMasterId: Int,
    @SerializedName("DDMLeaveTypeId") val dDMLeaveTypeId: Int,
    @SerializedName("LeaveTypeName") val leaveTypeName: String,
    @SerializedName("LeaveCategoryName") val leaveCategoryName: String,
    @SerializedName("LeaveFromDate") val leaveFromDate: String,
    @SerializedName("LeaveToDate") val leaveToDate: String,
    @SerializedName("TotalLeaveDays") val totalLeaveDays: Double,
    @SerializedName("IsHalfDayLeave") val isHalfDayLeave: Boolean,
    @SerializedName("LeaveReason") val leaveReason: String,
    @SerializedName("ReportingToUserId") val reportingToUserId: Int,
    @SerializedName("ReportingToUserName") val reportingToUserName: String,
    @SerializedName("CreateDateTime") val createDateTime: String,
    @SerializedName("LeaveApprovalStatus") val leaveApprovalStatus: Int,
    @SerializedName("LeaveApprovalStatusName") val leaveApprovalStatusName: String,
    @SerializedName("LeaveApprovalDateTime") val leaveApprovalDateTime: String,
    @SerializedName("LeaveApprovalRemarks") val leaveApprovalRemarks: String,
    @SerializedName("FromDate") val fromDate: String,
    @SerializedName("ToDate") val toDate: String,
    @SerializedName("DocumentNo") val documentNo: Int,
    @SerializedName("ParameterString") val parameterString: String,
    @SerializedName("CompanyMasterId") val companyMasterId: Int,
    @SerializedName("BranchMasterId") val branchMasterId: Int,
    @SerializedName("DivisionMasterId") val divisionMasterId: Int,
    @SerializedName("AllowEdit") val allowEdit: Boolean = false,
    @SerializedName("AllowDelete") val allowDelete: Boolean = false,
    @SerializedName("Status") val status: String,
    @SerializedName("CompanyName") val companyName: String,
    @SerializedName("BranchName") val branchName: String,
    @SerializedName("DivisionName") val divisionName: String,
    @SerializedName("Success") val success: Boolean?,
    @SerializedName("ReturnMessage") val returnMessage: String?,
    @SerializedName("CategoryName") val categoryName: String
)
    : Parcelable {constructor() : this(
    0, 0, "", "",0, 0, "", "", "", "", 0.0,
    false, "", 0, "", "", 0,
    "", "", "", "", "",
    0, "", 0, 0, 0,
    false, false, "", "", "",
    "", false, "",""
)
}
