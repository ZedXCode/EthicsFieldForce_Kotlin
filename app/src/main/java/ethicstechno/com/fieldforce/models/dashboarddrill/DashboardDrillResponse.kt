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
    @SerializedName("FromDate") val fromDate: String, // Change to the actual type if needed
    @SerializedName("ToDate") val toDate: String, // Change to the actual type if needed
    @SerializedName("Filter") val filter: String,
    @SerializedName("StoreProcedureName") val storeProcedureName: String?, // Change to the actual type if needed
    @SerializedName("PopUpScreenFlag") val popUpScreenFlag: Int,
    @SerializedName("ReportFileName") val reportFileName: String = ""
): Parcelable{
    constructor() : this(
        "",
        0,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        false,
        false,
        0,
        "",
        "",
        "",
        null,
        0,
        ""
    )
}
