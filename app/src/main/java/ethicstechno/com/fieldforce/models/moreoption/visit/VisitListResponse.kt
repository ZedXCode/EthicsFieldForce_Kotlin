package ethicstechno.com.fieldforce.models.moreoption.visit

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VisitListResponse(
    @SerializedName("AccountMasterId") val accountMasterId: Int,
    @SerializedName("CategoryMasterId") val categoryMasterId: Int,
    @SerializedName("CategoryName") val categoryName: String = "",
    @SerializedName("AccountName") val accountName: String,
    @SerializedName("CityId") val cityId: Int,
    @SerializedName("CityName") val cityName: String,
    @SerializedName("PhoneNo") val phoneNo: String,
    @SerializedName("Email") val email: String,
    @SerializedName("ContactPersonName") val contactPersonName: String,
    @SerializedName("Latitude") val latitude: Double,
    @SerializedName("Longitude") val longitude: Double,
    @SerializedName("Location") val location: String,
    @SerializedName("IsActive") val isActive: Boolean,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("ReportSetupId") val reportSetupId: Int,
    @SerializedName("ReportName") val reportName: String,
    @SerializedName("APIName") val apiName: String,
    @SerializedName("StoreProcedureName") val storeProcedureName: String,
    @SerializedName("ReportGroupBy") val reportGroupBy: String,
    @SerializedName("Filter") val filter: String,
    @SerializedName("ParameterString") val parameterString: String
) : Parcelable {
    // Empty constructor
    constructor() : this(
        0, 0, "", "", 0,
        "", "", "", "", 0.0, 0.0,
        "", false, 0, 0, "", "",
        "", "", "", ""
    )
}
