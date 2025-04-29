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
    @SerializedName("SerialNo") val serialNo: Int,
    @SerializedName("ProductFilter") val productFilter: Boolean,
    @SerializedName("IsAppReport") val isAppReport: Boolean,
    @SerializedName("CrystalReportPath") val crystalReportPath: String?,
    @SerializedName("Value1") val value1: String,
    @SerializedName("Value2") val value2: String,
    @SerializedName("Value3") val value3: String,
    @SerializedName("Value4") val value4: String,
    @SerializedName("Color1") val color1: String,
    @SerializedName("Color2") val color2: String,
    @SerializedName("Color3") val color3: String,
    @SerializedName("Color4") val color4: String,
    @SerializedName("AllowPrint") val allowPrint: Boolean,
    @SerializedName("ViewType") val viewType: Int,
    @SerializedName("RedirectFormId") val redirectFormId: Int
) : Parcelable {
    constructor() : this(
        0, "", "", "", "", "", "", "", "", "", "", 0, 0,
        false, false, null, "", "", "", "", "", "", "", "", false, 0, 0
    )
}
