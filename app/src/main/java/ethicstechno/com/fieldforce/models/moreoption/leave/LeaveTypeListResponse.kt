package ethicstechno.com.fieldforce.models.moreoption.leave

import com.google.gson.annotations.SerializedName

data class LeaveTypeListResponse(
    @SerializedName("UserId") val userId: Int,
    @SerializedName("CategoryMasterId") val categoryMasterId: Int,
    @SerializedName("CategoryName") val categoryName: String,
    @SerializedName("LeaveBalance") val leaveBalance: Double
) {
    constructor(): this(0,0,"", 0.0)
}
