package ethicstechno.com.fieldforce.models.reports

import com.google.gson.annotations.SerializedName

data class ReportListResponse(
    @SerializedName("ReportSetupId") val reportSetupId: Int = 0,
    @SerializedName("ReportType") val reportType: String = "",
    @SerializedName("ReportName") val reportName: String = "",
    @SerializedName("TitleLine1") val titleLine1: String? = "",
    @SerializedName("TitleLine2") val titleLine2: String? = "",
    @SerializedName("TitleLine3") val titleLine3: String? = "",
    @SerializedName("TitleLine4") val titleLine4: String? = "",
    @SerializedName("APIName") val apiName: String = "",
    @SerializedName("StoreProcedureName") val storeProcedureName: String = "",
    @SerializedName("ReportGroupBy") val reportGroupBy: String = "",
    @SerializedName("Filter") val filter: String = "",
    @SerializedName("UserId") val userId: Int = 0,
    @SerializedName("SerialNo") val serialNo: Int = 0,
    val isDynamicReport: Boolean = true
)