package ethicstechno.com.fieldforce.models.dashboarddrill

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DashboardDrillResponse(
    @SerializedName("TableName") val tableName: String,
    @SerializedName("ReferenceId") val referenceId: Int,
    @SerializedName("Column1") val column1: String,
    @SerializedName("Column2") val column2: String,
    @SerializedName("Value1") val value1: String,
    @SerializedName("Value2") val value2: String,
    @SerializedName("ReportGroupBy") val reportGroupBy: String,
    @SerializedName("BackReportGroupBy") val backReportGroupBy: String,
    @SerializedName("ParameterString") val parameterString: String,
    @SerializedName("BackParameterString") val backParameterString: String,
    @SerializedName("DrillDownFlag") val drillDownFlag: Boolean,
    @SerializedName("ReportViewFlag") val reportViewFlag: Boolean = false,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("FromDate") val fromDate: String?, // Nullable
    @SerializedName("ToDate") val toDate: String?, // Nullable
    @SerializedName("Filter") val filter: String,
    @SerializedName("StoreProcedureName") val storeProcedureName: String?,
    @SerializedName("PopUpScreenFlag") val popUpScreenFlag: Int,
    @SerializedName("ReportName") val reportName: String = "",
    @SerializedName("FilterParameterString") val filterParameterString: String = "",
    @SerializedName("Color1") val color1: String = "",
    @SerializedName("Color2") val color2: String = "",
    @SerializedName("RedirectFormId") val redirectFormId: Int = 0,
    @SerializedName("RedirectDocumentId") val redirectDocumentId: Int = 0,
    @SerializedName("ViewType") val viewType: Int = 0,
    @SerializedName("Column3") val column3: String = "",
    @SerializedName("Value3") val value3: String = ""
) : Parcelable {
    constructor() : this(
        "", 0, "", "", "", "", "", "", "", "", false, false, 0,
        null, null, "", null, 0, "", "", "", "", 0, 0, 0, "", ""
    )
}