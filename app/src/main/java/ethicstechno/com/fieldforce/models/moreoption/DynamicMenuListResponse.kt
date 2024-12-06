package ethicstechno.com.fieldforce.models.moreoption

import com.google.gson.annotations.SerializedName

data class DynamicMenuListResponse(
    @SerializedName("DynamicScreenSetupId") val dynamicScreenSetupId: Int,
    @SerializedName("DynamicScreenType") val dynamicScreenType: String,
    @SerializedName("DynamicScreenName") val dynamicScreenName: String,
    @SerializedName("ReportSetupId") val reportSetupId: Int,
    @SerializedName("APIName") val apiName: String,
    @SerializedName("StoreProcedureName") val storeProcedureName: String,
    @SerializedName("ControlAPIName") val controlAPIName: String,
    @SerializedName("ControlSPName") val controlSPName: String,
    @SerializedName("InsertUpdateAPIName") val insertUpdateAPIName: String,
    @SerializedName("InsertUpdateSPName") val insertUpdateSPName: String,
    @SerializedName("DeleteAPIName") val deleteAPIName: String,
    @SerializedName("DeleteSPName") val deleteSPName: String,
    @SerializedName("IconFilePath") val iconFilePath: String,
    @SerializedName("ReportName") val reportName: String,
    @SerializedName("ReportGroupBy") val reportGroupBy: String,
    @SerializedName("Filter") val filter: String,
    @SerializedName("DateOption") val dateOption: String,
    @SerializedName("FromDate") val fromDate: String,
    @SerializedName("ToDate") val toDate: String,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("ParameterString") val parameterString: String
)
