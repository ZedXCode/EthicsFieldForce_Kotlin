package ethicstechno.com.fieldforce.models.moreoption.leave

import com.google.gson.annotations.SerializedName

data class LeaveTypeDrpdownResponse(
    @SerializedName("DropdownMasterId") val dropdownMasterId: Int,
    @SerializedName("DropdownName") val dropdownName: String,
    @SerializedName("DropdownMasterDetailsId") val dropdownMasterDetailsId: Int,
    @SerializedName("DropdownKeyId") val dropdownKeyId: String,
    @SerializedName("DropdownValue") val dropdownValue: String,
    @SerializedName("ParentDropdownMasterDetailsId") val parentDropdownMasterDetailsId: Int,
    @SerializedName("IsActive") val isActive: Boolean,
) {
    constructor(): this(0,"",0, "","",0,false)
}
