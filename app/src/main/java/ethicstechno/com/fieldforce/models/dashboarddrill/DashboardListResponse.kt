package ethicstechno.com.fieldforce.models.dashboarddrill

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DashboardListResponse(
    @SerializedName("ReportSetupId") val reportSetupId: Int,
    @SerializedName("ReportType") val reportType: String,
    @SerializedName("ReportName") val reportName: String,
    @SerializedName("TitleLine1") val titleLine1: String,
    @SerializedName("TitleLine2") val titleLine2: String,
    @SerializedName("TitleLine3") val titleLine3: String,
    @SerializedName("TitleLine4") val titleLine4: String,
    @SerializedName("APIName") val apiName: String,
    @SerializedName("StoreProcedureName") val storeProcedureName: String,
    @SerializedName("ReportGroupBy") val reportGroupBy: String,
    @SerializedName("Filter") val filter: String,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("SerialNo") val serialNo: Int) : Parcelable {
    constructor() : this(
        0,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        0
    )
}