package ethicstechno.com.fieldforce.models.dashboarddrill

import com.google.gson.annotations.SerializedName

data class FilterListResponse(
    @SerializedName("ReportSetupId") val reportSetupId: Int,
    @SerializedName("DropDownFieldName") val dropDownFieldName: String,
    @SerializedName("DropDownFieldValue") val dropDownFieldValue: String
){
    constructor() : this(0,"", "")
}
